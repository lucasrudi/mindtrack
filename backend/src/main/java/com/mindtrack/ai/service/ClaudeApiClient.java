package com.mindtrack.ai.service;

import com.mindtrack.ai.config.AiProperties;
import com.mindtrack.ai.dto.ChatResponse;
import com.mindtrack.ai.dto.ClaudeApiResponse;
import com.mindtrack.ai.dto.ClaudeRequest;
import com.mindtrack.ai.dto.MessageDto;
import com.mindtrack.ai.model.ConversationType;
import com.mindtrack.ai.model.MessageRole;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * HTTP client for the Anthropic Claude Messages API.
 * Automatically selects max_tokens based on ConversationType tier.
 */
@Service
public class ClaudeApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(ClaudeApiClient.class);

    private final RestClient restClient;
    private final AiProperties aiProperties;

    /**
     * Creates the Claude API client.
     *
     * @param claudeRestClient the pre-configured REST client
     * @param aiProperties the AI configuration properties
     */
    public ClaudeApiClient(RestClient claudeRestClient, AiProperties aiProperties) {
        this.restClient = claudeRestClient;
        this.aiProperties = aiProperties;
    }

    /**
     * Sends a message to Claude and returns the response.
     *
     * @param systemPrompt the system prompt with user context
     * @param conversationHistory prior messages in the conversation
     * @param type the conversation type (determines max_tokens)
     * @return the AI response with token usage
     */
    public ChatResponse sendMessage(String systemPrompt, List<MessageDto> conversationHistory,
                                    ConversationType type) {
        int maxTokens = aiProperties.getMaxTokensFor(type);
        LOG.info("Sending Claude API request: model={} type={} maxTokens={}",
                aiProperties.getModel(), type, maxTokens);

        List<Map<String, String>> messages = new ArrayList<>();
        for (MessageDto msg : conversationHistory) {
            String role = msg.role() == MessageRole.ASSISTANT ? "assistant" : "user";
            messages.add(Map.of("role", role, "content", msg.content()));
        }

        ClaudeRequest request = new ClaudeRequest(
                aiProperties.getModel(),
                maxTokens,
                systemPrompt,
                messages
        );

        ClaudeApiResponse apiResponse = restClient.post()
                .body(request)
                .retrieve()
                .body(ClaudeApiResponse.class);

        if (apiResponse == null || apiResponse.content() == null || apiResponse.content().isEmpty()) {
            LOG.error("Empty response from Claude API");
            return new ChatResponse(null, null, "I'm sorry, I couldn't generate a response.", false, 0);
        }

        String responseText = apiResponse.content().get(0).text();
        int tokensUsed = apiResponse.usage() != null ? apiResponse.usage().outputTokens() : 0;

        LOG.info("Claude API response: tokensUsed={}", tokensUsed);
        return new ChatResponse(null, null, responseText, false, tokensUsed);
    }
}
