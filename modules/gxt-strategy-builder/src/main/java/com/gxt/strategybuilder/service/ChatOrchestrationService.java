package com.gxt.strategybuilder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gxt.common.dto.StrategySpecDto;
import com.gxt.strategybuilder.ai.LlmChatClient;
import com.gxt.strategybuilder.ai.LlmChatResult;
import com.gxt.strategybuilder.ai.LlmProviderRouter;
import com.gxt.strategybuilder.ai.StrategyChatPromptBuilder;
import com.gxt.strategybuilder.domain.ChatMessageEntity;
import com.gxt.strategybuilder.domain.StrategyEntity;
import com.gxt.strategybuilder.domain.StrategySpecSnapshotEntity;
import com.gxt.strategybuilder.dto.ChatHistoryResponse;
import com.gxt.strategybuilder.dto.ChatMessageResponse;
import com.gxt.strategybuilder.dto.ChatResponse;
import com.gxt.strategybuilder.dto.StrategyResponse;
import com.gxt.strategybuilder.repository.ChatMessageRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatOrchestrationService {
    private final ChatMessageRepository chatMessageRepository;
    private final StrategyCrudService strategyCrudService;
    private final StrategyPlanNormalizer normalizer;
    private final LlmProviderRouter llmProviderRouter;
    private final StrategyChatPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    public ChatOrchestrationService(
            ChatMessageRepository chatMessageRepository,
            StrategyCrudService strategyCrudService,
            StrategyPlanNormalizer normalizer,
            LlmProviderRouter llmProviderRouter,
            StrategyChatPromptBuilder promptBuilder,
            ObjectMapper objectMapper) {
        this.chatMessageRepository = chatMessageRepository;
        this.strategyCrudService = strategyCrudService;
        this.normalizer = normalizer;
        this.llmProviderRouter = llmProviderRouter;
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ChatResponse processMessage(String strategyId, String message) {
        StrategyEntity strategy = strategyCrudService.getEntity(strategyId);
        saveMessage(strategyId, "user", message);

        String context = chatMessageRepository.findByStrategyIdOrderByCreatedAtAsc(strategyId).stream()
                .map(msg -> msg.getRole() + ": " + msg.getContent())
                .collect(Collectors.joining("\n"));

        LlmChatClient client = llmProviderRouter.resolve();
        LlmChatResult llmResult = client.chat(promptBuilder.systemPrompt(), message, context);

        saveMessage(strategyId, "assistant", llmResult.assistantText());

        try {
            JsonNode rawPlan = objectMapper.readTree(llmResult.rawJsonPlan());
            StrategyPlanNormalizer.NormalizationResult normalized = normalizer.normalize(rawPlan);
            if (normalized.valid()) {
                StrategySpecSnapshotEntity snapshot = strategyCrudService.saveSpecSnapshot(strategy, normalized.spec());
                StrategyResponse card = strategyCrudService.get(strategyId);
                return new ChatResponse(llmResult.assistantText(), card, List.of());
            }
            StrategyResponse card = strategyCrudService.get(strategyId);
            return new ChatResponse(llmResult.assistantText(), card, normalized.errors());
        } catch (Exception ex) {
            StrategyResponse card = strategyCrudService.get(strategyId);
            return new ChatResponse(
                    llmResult.assistantText(),
                    card,
                    List.of("Failed to parse LLM strategy plan: " + ex.getMessage()));
        }
    }

    public ChatHistoryResponse history(String strategyId) {
        strategyCrudService.getEntity(strategyId);
        List<ChatMessageResponse> messages = chatMessageRepository.findByStrategyIdOrderByCreatedAtAsc(strategyId).stream()
                .map(this::toDto)
                .toList();
        return new ChatHistoryResponse(messages);
    }

    private void saveMessage(String strategyId, String role, String content) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setId("msg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        entity.setStrategyId(strategyId);
        entity.setRole(role);
        entity.setContent(content);
        entity.setCreatedAt(Instant.now());
        chatMessageRepository.save(entity);
    }

    private ChatMessageResponse toDto(ChatMessageEntity entity) {
        return new ChatMessageResponse(entity.getId(), entity.getRole(), entity.getContent(), entity.getCreatedAt());
    }
}
