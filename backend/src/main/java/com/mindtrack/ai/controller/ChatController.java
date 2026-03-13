package com.mindtrack.ai.controller;

import com.mindtrack.ai.dto.ChatRequest;
import com.mindtrack.ai.dto.ChatResponse;
import com.mindtrack.ai.dto.ConversationDto;
import com.mindtrack.ai.service.ConversationService;
import com.mindtrack.audit.model.AuditAction;
import com.mindtrack.audit.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for AI chat operations.
 * Endpoints: POST /api/ai/chat, GET /api/ai/conversations, GET /api/ai/conversations/{id}
 */
@RestController
@RequestMapping("/api/ai")
public class ChatController {

    private final ConversationService conversationService;
    private final AuditService auditService;

    /**
     * Creates the chat controller.
     *
     * @param conversationService the conversation service
     * @param auditService        the audit service
     */
    public ChatController(ConversationService conversationService, AuditService auditService) {
        this.conversationService = conversationService;
        this.auditService = auditService;
    }

    /**
     * Send a message to the AI and receive a response.
     * Uses cached response when available for SESSION_SUMMARY and QUICK_CHECKIN types.
     *
     * @param request the chat request with message and optional conversation ID
     * @return the AI response
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody @Valid ChatRequest request,
                                             Authentication authentication,
                                             HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        ChatResponse response = conversationService.chat(userId, request);
        auditService.log(userId, AuditAction.WRITE, "CONVERSATION", response.conversationId(),
                userId, getClientIp(httpRequest), "WEB");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * List all conversations for the current user.
     *
     * @return list of conversations
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> listConversations(Authentication authentication,
                                                                   HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        List<ConversationDto> conversations = conversationService.listConversations(userId);
        conversations.forEach(c -> auditService.log(userId, AuditAction.READ, "CONVERSATION",
                c.id(), userId, getClientIp(httpRequest), "WEB"));
        return ResponseEntity.ok(conversations);
    }

    /**
     * Get a single conversation with all messages, scoped to the current user (prevents IDOR).
     *
     * @param id the conversation ID
     * @return the conversation with messages
     */
    @GetMapping("/conversations/{id}")
    public ResponseEntity<ConversationDto> getConversation(@PathVariable Long id,
                                                           Authentication authentication,
                                                           HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        ConversationDto conversation = conversationService.getConversation(id, userId);
        auditService.log(userId, AuditAction.READ, "CONVERSATION", id,
                userId, getClientIp(httpRequest), "WEB");
        return ResponseEntity.ok(conversation);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
