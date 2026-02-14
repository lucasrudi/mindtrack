package com.mindtrack.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Request body for Claude Messages API.
 */
public record ClaudeRequest(
        String model,
        @JsonProperty("max_tokens") int maxTokens,
        String system,
        List<Map<String, String>> messages
) {
}
