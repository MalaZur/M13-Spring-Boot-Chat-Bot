
package com.accenture.springai_bootcamp_demo.client;

import com.accenture.springai_bootcamp_demo.config.OpenRouterProperties;
import com.accenture.springai_bootcamp_demo.entity.ChatMessage;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@EnableConfigurationProperties(OpenRouterProperties.class)
public class OpenRouterClient {

    private final ChatClient chatClient;
    private final OpenRouterProperties properties;

    public OpenRouterClient(
            ChatClient.Builder chatClientBuilder,
            OpenRouterProperties properties) {

        this.chatClient = chatClientBuilder.build();
        this.properties = properties;
    }

    public String complete(List<ChatMessage> history) {
        String reply = call(history);
        return extractContent(reply);
    }

    private String call(List<ChatMessage> history) {
        try {
            List<Message> messages = buildMessages(history);

            log.info("Sending {} messages to Ollama", messages.size());

            String response = chatClient.prompt()
                    .messages(messages)
                    .call()
                    .content();

            log.info("Received response from Ollama");

            return response;

        } catch (OpenRouterException ex) {
            throw ex;

        } catch (RuntimeException ex) {
            log.error("Ollama request failed", ex);

            throw new OpenRouterException(
                    "Failed to reach Ollama: " + ex.getMessage(),
                    ex
            );
        }
    }

    private List<Message> buildMessages(List<ChatMessage> history) {
        List<Message> messages = new ArrayList<>();

        // System prompt
        messages.add(new SystemMessage(properties.systemPrompt()));

        // Conversation history
        for (ChatMessage chatMessage : history) {

            String content = chatMessage.getContent();

            switch (chatMessage.getRole()) {

                case SYSTEM ->
                        messages.add(new SystemMessage(content));

                case USER ->
                        messages.add(new UserMessage(content));

                case ASSISTANT ->
                        messages.add(new AssistantMessage(content));
            }
        }

        return messages;
    }

    private String extractContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new OpenRouterException(
                    "Ollama returned an empty response"
            );
        }

        return content.trim();
    }
}
