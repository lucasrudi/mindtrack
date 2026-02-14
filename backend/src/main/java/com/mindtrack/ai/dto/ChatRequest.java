package com.mindtrack.ai.dto;

import com.mindtrack.ai.model.ConversationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for sending a chat message to the AI.
 */
public class ChatRequest {

    private Long conversationId;

    @NotBlank(message = "Message content is required")
    @Size(max = 10000, message = "Message cannot exceed 10000 characters")
    private String message;

    private ConversationType type;

    /** Default constructor. */
    public ChatRequest() {
        this.type = ConversationType.COACHING;
    }

    /**
     * Creates a chat request.
     *
     * @param conversationId optional existing conversation ID (null for new)
     * @param message the user's message
     * @param type the conversation type (determines token budget)
     */
    public ChatRequest(Long conversationId, String message, ConversationType type) {
        this.conversationId = conversationId;
        this.message = message;
        this.type = type != null ? type : ConversationType.COACHING;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ConversationType getType() {
        return type;
    }

    public void setType(ConversationType type) {
        this.type = type != null ? type : ConversationType.COACHING;
    }
}
