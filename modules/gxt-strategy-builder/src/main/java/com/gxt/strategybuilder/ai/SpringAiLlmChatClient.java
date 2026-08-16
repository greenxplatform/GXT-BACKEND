package com.gxt.strategybuilder.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpringAiLlmChatClient implements LlmChatClient {
    private static final Pattern JSON_BLOCK = Pattern.compile("```json\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL);

    private final ObjectMapper objectMapper;
    private final HeuristicLlmChatClient heuristicLlmChatClient;

    @Autowired(required = false)
    private ChatModel chatModel;

    public SpringAiLlmChatClient(ObjectMapper objectMapper, HeuristicLlmChatClient heuristicLlmChatClient) {
        this.objectMapper = objectMapper;
        this.heuristicLlmChatClient = heuristicLlmChatClient;
    }

    @Override
    public LlmChatResult chat(String systemPrompt, String userPrompt, String conversationContext) {
        if (chatModel == null) {
            return heuristicLlmChatClient.chat(systemPrompt, userPrompt, conversationContext);
        }

        String fullPrompt = systemPrompt + "\n\nConversation:\n" + conversationContext + "\n\nUser: " + userPrompt;
        String response = chatModel.call(new Prompt(fullPrompt)).getResult().getOutput().getText();
        String json = extractJson(response);
        if (json == null) {
            return heuristicLlmChatClient.chat(systemPrompt, userPrompt, conversationContext);
        }
        return new LlmChatResult(response, json);
    }

    private String extractJson(String response) {
        Matcher matcher = JSON_BLOCK.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return null;
    }
}
