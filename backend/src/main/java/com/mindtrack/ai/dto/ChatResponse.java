package com.mindtrack.ai.dto;

/**
 * Response DTO for AI chat messages.
 */
public record ChatResponse(
        Long conversationId,
        Long messageId,
        String content,
        boolean cached,
        int tokensUsed
) {
}
