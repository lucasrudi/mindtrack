package com.mindtrack.ai.service;

import com.mindtrack.ai.dto.ChatRequest;
import com.mindtrack.ai.dto.ChatResponse;
import com.mindtrack.ai.dto.ConversationDto;
import com.mindtrack.ai.dto.MessageDto;
import com.mindtrack.ai.model.Channel;
import com.mindtrack.ai.model.Conversation;
import com.mindtrack.ai.model.ConversationType;
import com.mindtrack.ai.model.Message;
import com.mindtrack.ai.model.MessageRole;
import com.mindtrack.ai.repository.ConversationRepository;
import com.mindtrack.ai.repository.MessageRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates AI conversations: manages persistence, caching, and Claude API calls.
 */
@Service
public class ConversationService {

    private static final Logger LOG = LoggerFactory.getLogger(ConversationService.class);

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ClaudeApiClient claudeApiClient;
    private final ContextBuilder contextBuilder;
    private final AiResponseCache responseCache;

    /**
     * Creates the conversation service.
     *
     * @param conversationRepository conversation repo
     * @param messageRepository message repo
     * @param claudeApiClient the Claude API client
     * @param contextBuilder the context builder
     * @param responseCache the response cache
     */
    public ConversationService(ConversationRepository conversationRepository,
                               MessageRepository messageRepository,
                               ClaudeApiClient claudeApiClient,
                               ContextBuilder contextBuilder,
                               AiResponseCache responseCache) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.claudeApiClient = claudeApiClient;
        this.contextBuilder = contextBuilder;
        this.responseCache = responseCache;
    }

    /**
     * Processes a chat message: checks cache, calls Claude if needed, persists messages.
     *
     * @param userId the current user ID
     * @param request the chat request
     * @return the AI response
     */
    @Transactional
    public ChatResponse chat(Long userId, ChatRequest request) {
        ConversationType type = request.getType();

        // Check cache for cacheable types
        String fingerprint = contextBuilder.generateFingerprint(userId);
        ChatResponse cached = responseCache.get(userId, type, fingerprint);
        if (cached != null) {
            LOG.info("Returning cached response for user={} type={}", userId, type);
            return new ChatResponse(
                    cached.conversationId(),
                    cached.messageId(),
                    cached.content(),
                    true,
                    cached.tokensUsed()
            );
        }

        // Get or create conversation
        Conversation conversation = getOrCreateConversation(userId, request.getConversationId());

        // Save user message
        Message userMessage = new Message(conversation, MessageRole.USER, request.getMessage());
        messageRepository.save(userMessage);

        // Build context and conversation history
        String systemPrompt = contextBuilder.buildSystemPrompt(userId);
        List<MessageDto> history = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversation.getId())
                .stream()
                .map(m -> new MessageDto(m.getId(), m.getRole(), m.getContent(), m.getCreatedAt()))
                .toList();

        // Call Claude API
        ChatResponse aiResponse = claudeApiClient.sendMessage(systemPrompt, history, type);

        // Save AI response
        Message assistantMessage = new Message(conversation, MessageRole.ASSISTANT, aiResponse.content());
        messageRepository.save(assistantMessage);

        ChatResponse response = new ChatResponse(
                conversation.getId(),
                assistantMessage.getId(),
                aiResponse.content(),
                false,
                aiResponse.tokensUsed()
        );

        // Cache if applicable
        responseCache.put(userId, type, fingerprint, response);

        return response;
    }

    /**
     * Lists all conversations for a user.
     *
     * @param userId the user ID
     * @return list of conversation DTOs
     */
    public List<ConversationDto> listConversations(Long userId) {
        return conversationRepository.findByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Gets a single conversation with messages.
     *
     * @param conversationId the conversation ID
     * @return the conversation DTO, or null if not found
     */
    public ConversationDto getConversation(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .map(this::toDto)
                .orElse(null);
    }

    private Conversation getOrCreateConversation(Long userId, Long conversationId) {
        if (conversationId != null) {
            return conversationRepository.findById(conversationId)
                    .orElseGet(() -> createNewConversation(userId));
        }
        return createNewConversation(userId);
    }

    private Conversation createNewConversation(Long userId) {
        Conversation conversation = new Conversation(userId, Channel.WEB);
        return conversationRepository.save(conversation);
    }

    private ConversationDto toDto(Conversation conversation) {
        List<MessageDto> messageDtos = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversation.getId())
                .stream()
                .map(m -> new MessageDto(m.getId(), m.getRole(), m.getContent(), m.getCreatedAt()))
                .toList();

        return new ConversationDto(
                conversation.getId(),
                conversation.getChannel(),
                conversation.getStartedAt(),
                conversation.getEndedAt(),
                messageDtos
        );
    }
}
