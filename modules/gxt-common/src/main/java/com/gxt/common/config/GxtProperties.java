package com.gxt.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GxtProperties {
    @Value("${gxt.auth.stub-user-id:demo-user}")
    private String stubUserId;

    @Value("${gxt.ai.provider:anthropic}")
    private String aiProvider;

    @Value("${gxt.backtest.max-concurrent-jobs:8}")
    private int maxConcurrentJobs;

    public String getStubUserId() {
        return stubUserId;
    }

    public String getAiProvider() {
        return aiProvider;
    }

    public int getMaxConcurrentJobs() {
        return maxConcurrentJobs;
    }
}
