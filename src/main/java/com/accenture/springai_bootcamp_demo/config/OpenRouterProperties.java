package com.accenture.springai_bootcamp_demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openrouter")
public record OpenRouterProperties(String systemPrompt) {
    public OpenRouterProperties {
        if (systemPrompt == null || systemPrompt.isBlank()) {
            systemPrompt = "You are a helpful, concise assistant.";
        }
    }
}
