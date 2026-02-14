package com.mindtrack.ai.dto;

import com.mindtrack.ai.model.MessageRole;
import java.time.LocalDateTime;

/**
 * DTO for a single message in a conversation.
 */
public record MessageDto(
        Long id,
        MessageRole role,
        String content,
        LocalDateTime createdAt
) {
}
