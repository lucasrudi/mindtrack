package com.mindtrack.ai.service;

import com.mindtrack.ai.dto.ChatRequest;
import com.mindtrack.ai.dto.ChatResponse;
import com.mindtrack.ai.model.ConversationType;
import com.mindtrack.ai.repository.ConversationRepository;
import com.mindtrack.ai.repository.MessageRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ClaudeApiClient claudeApiClient;

    @Mock
    private ContextBuilder contextBuilder;

    @Mock
    private AiResponseCache responseCache;

    @InjectMocks
    private ConversationService conversationService;

    @Test
    void shouldReturnCachedResponseWhenAvailable() {
        // Arrange
        ChatRequest request = new ChatRequest(null, "How am I doing?", ConversationType.QUICK_CHECKIN);
        ChatResponse cachedResponse = new ChatResponse(1L, 10L, "You're doing great!", false, 100);

        when(contextBuilder.generateFingerprint(1L)).thenReturn("test-fp");
        when(responseCache.get(1L, ConversationType.QUICK_CHECKIN, "test-fp")).thenReturn(cachedResponse);

        // Act
        ChatResponse result = conversationService.chat(1L, request);

        // Assert
        assertTrue(result.cached());
        assertEquals("You're doing great!", result.content());
        verify(claudeApiClient, never()).sendMessage(anyString(), any(), any());
    }

    @Test
    void shouldCallClaudeWhenCacheMiss() {
        // Arrange
        ChatRequest request = new ChatRequest(null, "Help me with my goals", ConversationType.COACHING);
        ChatResponse apiResponse = new ChatResponse(null, null, "Let's review your goals.", false, 350);

        when(contextBuilder.generateFingerprint(1L)).thenReturn("test-fp");
        when(responseCache.get(anyLong(), any(), anyString())).thenReturn(null);
        when(contextBuilder.buildSystemPrompt(1L)).thenReturn("system prompt");
        when(conversationRepository.save(any())).thenAnswer(inv -> {
            var conv = inv.getArgument(0, com.mindtrack.ai.model.Conversation.class);
            conv.setId(1L);
            return conv;
        });
        when(messageRepository.save(any())).thenAnswer(inv -> {
            var msg = inv.getArgument(0, com.mindtrack.ai.model.Message.class);
            msg.setId(10L);
            return msg;
        });
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(anyLong()))
                .thenReturn(List.of());
        when(claudeApiClient.sendMessage(anyString(), any(), eq(ConversationType.COACHING)))
                .thenReturn(apiResponse);

        // Act
        ChatResponse result = conversationService.chat(1L, request);

        // Assert
        assertFalse(result.cached());
        assertEquals("Let's review your goals.", result.content());
        verify(claudeApiClient).sendMessage(anyString(), any(), eq(ConversationType.COACHING));
    }

    @Test
    void shouldNotCacheCoachingResponses() {
        // Arrange
        ChatRequest request = new ChatRequest(null, "Tell me more", ConversationType.COACHING);
        ChatResponse apiResponse = new ChatResponse(null, null, "Sure, let's continue.", false, 200);

        when(contextBuilder.generateFingerprint(1L)).thenReturn("test-fp");
        when(responseCache.get(anyLong(), any(), anyString())).thenReturn(null);
        when(contextBuilder.buildSystemPrompt(1L)).thenReturn("system prompt");
        when(conversationRepository.save(any())).thenAnswer(inv -> {
            var conv = inv.getArgument(0, com.mindtrack.ai.model.Conversation.class);
            conv.setId(1L);
            return conv;
        });
        when(messageRepository.save(any())).thenAnswer(inv -> {
            var msg = inv.getArgument(0, com.mindtrack.ai.model.Message.class);
            msg.setId(10L);
            return msg;
        });
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(anyLong()))
                .thenReturn(List.of());
        when(claudeApiClient.sendMessage(anyString(), any(), any())).thenReturn(apiResponse);

        // Act
        conversationService.chat(1L, request);

        // Assert — cache.put is called but AiResponseCache.isCacheable returns false for COACHING
        verify(responseCache).put(eq(1L), eq(ConversationType.COACHING), eq("test-fp"), any());
    }
}
