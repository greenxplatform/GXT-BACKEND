package com.gxt.strategybuilder.ai;

public interface LlmChatClient {
    LlmChatResult chat(String systemPrompt, String userPrompt, String conversationContext);
}
