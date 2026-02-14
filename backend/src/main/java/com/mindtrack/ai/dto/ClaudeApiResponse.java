package com.mindtrack.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response body from Claude Messages API.
 */
public record ClaudeApiResponse(
        String id,
        List<ContentBlock> content,
        Usage usage
) {

    /** A content block in the API response. */
    public record ContentBlock(
            String type,
            String text
    ) {
    }

    /** Token usage from the API response. */
    public record Usage(
            @JsonProperty("input_tokens") int inputTokens,
            @JsonProperty("output_tokens") int outputTokens
    ) {
    }
}
