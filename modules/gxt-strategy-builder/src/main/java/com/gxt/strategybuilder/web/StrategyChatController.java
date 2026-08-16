package com.gxt.strategybuilder.web;

import com.gxt.strategybuilder.dto.ChatHistoryResponse;
import com.gxt.strategybuilder.dto.ChatRequest;
import com.gxt.strategybuilder.dto.ChatResponse;
import com.gxt.strategybuilder.service.ChatOrchestrationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/strategies/{strategyId}/chat")
public class StrategyChatController {
    private final ChatOrchestrationService chatOrchestrationService;

    public StrategyChatController(ChatOrchestrationService chatOrchestrationService) {
        this.chatOrchestrationService = chatOrchestrationService;
    }

    @PostMapping
    public ChatResponse chat(@PathVariable String strategyId, @Valid @RequestBody ChatRequest request) {
        return chatOrchestrationService.processMessage(strategyId, request.message());
    }

    @GetMapping
    public ChatHistoryResponse history(@PathVariable String strategyId) {
        return chatOrchestrationService.history(strategyId);
    }
}
