package com.mindtrack.ai.dto;

import com.mindtrack.ai.model.Channel;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for a conversation with its messages.
 */
public record ConversationDto(
        Long id,
        Channel channel,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        List<MessageDto> messages
) {
}
