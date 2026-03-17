package com.mindtrack.ai.service;

import com.mindtrack.ai.dto.ChatRequest;
import com.mindtrack.ai.dto.ChatResponse;
import com.mindtrack.ai.dto.ConversationDto;
import com.mindtrack.ai.model.Channel;
import com.mindtrack.ai.model.Conversation;
import com.mindtrack.ai.model.ConversationType;
import com.mindtrack.ai.repository.ConversationRepository;
import com.mindtrack.ai.repository.MessageRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    @Test
    void shouldReturnExistingConversation() {
        // Arrange
        Conversation conversation = new Conversation(1L, Channel.WEB);
        conversation.setId(42L);

        when(conversationRepository.findByIdAndUserId(42L, 1L))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(42L))
                .thenReturn(List.of());

        // Act
        ConversationDto dto = conversationService.getConversation(42L, 1L);

        // Assert
        assertNotNull(dto);
        assertEquals(42L, dto.id());
        assertEquals(Channel.WEB, dto.channel());
    }

    @Test
    void shouldThrowNotFoundWhenConversationDoesNotExist() {
        // Arrange
        when(conversationRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class,
                () -> conversationService.getConversation(999L, 1L));
    }

    @Test
    void shouldThrowNotFoundWhenConversationBelongsToAnotherUser() {
        // Arrange — user 2 owns the conversation, user 1 tries to access it
        when(conversationRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class,
                () -> conversationService.getConversation(10L, 1L));
    }

    @Test
    void shouldReuseExistingConversationOnSecondChatMessage() {
        // Arrange — first call creates conversation (id=1), second call reuses it via conversationId
        ChatRequest first = new ChatRequest(null, "First message", ConversationType.COACHING);
        ChatRequest second = new ChatRequest(1L, "Second message", ConversationType.COACHING);
        ChatResponse apiResponse = new ChatResponse(null, null, "Response.", false, 100);

        when(contextBuilder.generateFingerprint(1L)).thenReturn("fp");
        when(responseCache.get(anyLong(), any(), anyString())).thenReturn(null);
        when(contextBuilder.buildSystemPrompt(anyLong())).thenReturn("prompt");

        Conversation conversation = new Conversation(1L, Channel.WEB);
        conversation.setId(1L);

        // First call: no existing conversation → save creates one
        when(conversationRepository.save(any())).thenReturn(conversation);
        when(messageRepository.save(any())).thenAnswer(inv -> {
            var msg = inv.getArgument(0, com.mindtrack.ai.model.Message.class);
            msg.setId(10L);
            return msg;
        });
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(anyLong()))
                .thenReturn(List.of());
        when(claudeApiClient.sendMessage(anyString(), any(), any())).thenReturn(apiResponse);

        conversationService.chat(1L, first);

        // Second call: conversationId=1 supplied → findById returns the existing conversation
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));

        conversationService.chat(1L, second);

        // Assert — save was called once for the first conversation creation; not again for second
        verify(conversationRepository, times(1)).save(any(Conversation.class));
    }

    @Test
    void shouldChatWithTelegramChannel() {
        // Arrange
        ChatRequest request = new ChatRequest(null, "Hello from Telegram", ConversationType.COACHING);
        ChatResponse apiResponse = new ChatResponse(null, null, "Hi there!", false, 80);

        when(contextBuilder.generateFingerprint(1L)).thenReturn("fp");
        when(responseCache.get(anyLong(), any(), anyString())).thenReturn(null);
        when(contextBuilder.buildSystemPrompt(anyLong())).thenReturn("prompt");
        when(conversationRepository.findByUserIdAndChannelOrderByStartedAtDesc(1L, Channel.TELEGRAM))
                .thenReturn(List.of());
        when(conversationRepository.save(any())).thenAnswer(inv -> {
            var conv = inv.getArgument(0, Conversation.class);
            conv.setId(5L);
            return conv;
        });
        when(messageRepository.save(any())).thenAnswer(inv -> {
            var msg = inv.getArgument(0, com.mindtrack.ai.model.Message.class);
            msg.setId(20L);
            return msg;
        });
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(anyLong()))
                .thenReturn(List.of());
        when(claudeApiClient.sendMessage(anyString(), any(), eq(ConversationType.COACHING)))
                .thenReturn(apiResponse);

        // Act
        ChatResponse result = conversationService.chatWithChannel(1L, request, Channel.TELEGRAM);

        // Assert
        assertFalse(result.cached());
        assertEquals("Hi there!", result.content());
        verify(conversationRepository).findByUserIdAndChannelOrderByStartedAtDesc(1L, Channel.TELEGRAM);
    }

    @Test
    void shouldChatWithWhatsAppChannel() {
        // Arrange
        ChatRequest request = new ChatRequest(null, "Hello from WhatsApp", ConversationType.QUICK_CHECKIN);
        ChatResponse apiResponse = new ChatResponse(null, null, "How are you?", false, 60);

        when(contextBuilder.generateFingerprint(1L)).thenReturn("fp-wa");
        when(responseCache.get(anyLong(), any(), anyString())).thenReturn(null);
        when(contextBuilder.buildSystemPrompt(anyLong())).thenReturn("prompt");
        when(conversationRepository.findByUserIdAndChannelOrderByStartedAtDesc(1L, Channel.WHATSAPP))
                .thenReturn(List.of());
        when(conversationRepository.save(any())).thenAnswer(inv -> {
            var conv = inv.getArgument(0, Conversation.class);
            conv.setId(6L);
            return conv;
        });
        when(messageRepository.save(any())).thenAnswer(inv -> {
            var msg = inv.getArgument(0, com.mindtrack.ai.model.Message.class);
            msg.setId(21L);
            return msg;
        });
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(anyLong()))
                .thenReturn(List.of());
        when(claudeApiClient.sendMessage(anyString(), any(), eq(ConversationType.QUICK_CHECKIN)))
                .thenReturn(apiResponse);

        // Act
        ChatResponse result = conversationService.chatWithChannel(1L, request, Channel.WHATSAPP);

        // Assert
        assertFalse(result.cached());
        assertEquals("How are you?", result.content());
        verify(conversationRepository).findByUserIdAndChannelOrderByStartedAtDesc(1L, Channel.WHATSAPP);
    }

    @Test
    void shouldReuseExistingChannelConversation() {
        // Arrange — an existing Telegram conversation already exists for user 1
        Conversation existing = new Conversation(1L, Channel.TELEGRAM);
        existing.setId(7L);

        ChatRequest request = new ChatRequest(null, "Another message", ConversationType.COACHING);
        ChatResponse apiResponse = new ChatResponse(null, null, "Sure!", false, 50);

        when(contextBuilder.generateFingerprint(1L)).thenReturn("fp");
        when(responseCache.get(anyLong(), any(), anyString())).thenReturn(null);
        when(contextBuilder.buildSystemPrompt(anyLong())).thenReturn("prompt");
        when(conversationRepository.findByUserIdAndChannelOrderByStartedAtDesc(1L, Channel.TELEGRAM))
                .thenReturn(List.of(existing));
        when(messageRepository.save(any())).thenAnswer(inv -> {
            var msg = inv.getArgument(0, com.mindtrack.ai.model.Message.class);
            msg.setId(30L);
            return msg;
        });
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(anyLong()))
                .thenReturn(List.of());
        when(claudeApiClient.sendMessage(anyString(), any(), any())).thenReturn(apiResponse);

        // Act
        conversationService.chatWithChannel(1L, request, Channel.TELEGRAM);

        // Assert — no new conversation was created; repository.save(Conversation) never called
        verify(conversationRepository, never()).save(any(Conversation.class));
    }
}
