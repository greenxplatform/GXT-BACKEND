package com.gxt.strategybuilder.ai;

import com.gxt.common.config.GxtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class LlmProviderRouter {
    private final GxtProperties properties;
    private final LlmChatClient springAiClient;

    @Autowired(required = false)
    @Qualifier("heuristicLlmChatClient")
    private LlmChatClient heuristicClient;

    public LlmProviderRouter(GxtProperties properties, SpringAiLlmChatClient springAiClient) {
        this.properties = properties;
        this.springAiClient = springAiClient;
    }

    public LlmChatClient resolve() {
        return springAiClient;
    }

    public String activeProvider() {
        return properties.getAiProvider();
    }
}
