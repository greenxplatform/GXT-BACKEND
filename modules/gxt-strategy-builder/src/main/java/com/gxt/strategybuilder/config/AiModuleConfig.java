package com.gxt.strategybuilder.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "gxt.ai.provider", havingValue = "anthropic", matchIfMissing = true)
class AnthropicAiConfig {
    // Spring AI Anthropic starter auto-configures ChatModel when API key is present.
}

// OpenAI starter is not on the classpath. Wire it later when we add that provider.
