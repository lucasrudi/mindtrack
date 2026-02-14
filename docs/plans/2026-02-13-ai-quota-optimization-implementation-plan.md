# AI Service with Response Caching & Tiered Token Limits — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Implement the full AI service layer (Claude API client, conversation service, context builder) with Caffeine in-memory caching and tiered `max_tokens` to optimize Claude API quota usage.

**Architecture:** Modular monolith Spring Boot app. The `ai` module gets a complete backend service layer: JPA entities mapping to existing `conversations`/`messages` tables, a REST controller, a Claude API client using Spring `RestClient`, a Caffeine cache for expensive AI responses, and a context builder that aggregates user data into system prompts. Token tiers (`QUICK_CHECKIN`=200, `COACHING`=500, `SESSION_SUMMARY`=1500) are configured in `application.yml`.

**Tech Stack:** Java 21, Spring Boot 3.4.2, Spring Cache + Caffeine, Spring RestClient, JPA/Hibernate, JUnit 5 + Mockito.

**Design doc:** `docs/plans/2026-02-13-ai-quota-optimization-design.md`

---

## Task 1: Add Caffeine Dependency to pom.xml

**Files:**
- Modify: `backend/pom.xml`

**Step 1: Add Caffeine + Spring Cache dependencies**

Add these two dependencies to `backend/pom.xml` inside the `<dependencies>` block, after the JWT section:

```xml
        <!-- Cache -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>
        <dependency>
            <groupId>com.github.ben-manes.caffeine</groupId>
            <artifactId>caffeine</artifactId>
        </dependency>
```

**Step 2: Verify it compiles**

Run: `cd backend && JAVA_HOME="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home" /opt/homebrew/bin/mvn compile -q`
Expected: BUILD SUCCESS (no output on quiet mode)

**Step 3: Commit**

```bash
git add backend/pom.xml
git commit -m "chore(backend): add Caffeine cache and Spring Cache dependencies"
```

---

## Task 2: Add AI Configuration Properties to application.yml

**Files:**
- Modify: `backend/src/main/resources/application.yml`
- Modify: `backend/src/main/resources/application-local.yml`
- Modify: `backend/src/main/resources/application-prod.yml`

**Step 1: Add `mindtrack.ai` section to `application.yml`**

Append at the end of the file:

```yaml

mindtrack:
  ai:
    api-key: ${CLAUDE_API_KEY:dummy-key-for-local}
    api-url: https://api.anthropic.com/v1/messages
    model: claude-sonnet-4-20250514
    token-tiers:
      quick-checkin: 200
      coaching: 500
      session-summary: 1500
    cache:
      ttl-hours: 24
      max-size: 500
```

**Step 2: Add local overrides to `application-local.yml`**

Append at the end:

```yaml

mindtrack:
  ai:
    api-key: dummy-key-for-local
    model: claude-sonnet-4-20250514
```

**Step 3: Add prod overrides to `application-prod.yml`**

Append at the end:

```yaml

mindtrack:
  ai:
    api-key: ${CLAUDE_API_KEY}
```

**Step 4: Commit**

```bash
git add backend/src/main/resources/application.yml backend/src/main/resources/application-local.yml backend/src/main/resources/application-prod.yml
git commit -m "chore(backend): add AI service configuration (token tiers, cache, API settings)"
```

---

## Task 3: ConversationType Enum + AiProperties Config Class

**Files:**
- Create: `backend/src/main/java/com/mindtrack/ai/model/ConversationType.java`
- Create: `backend/src/main/java/com/mindtrack/ai/config/AiProperties.java`
- Delete: `backend/src/main/java/com/mindtrack/ai/model/.gitkeep`
- Delete: `backend/src/main/java/com/mindtrack/ai/config/.gitkeep`

**Step 1: Create `ConversationType.java`**

```java
package com.mindtrack.ai.model;

/**
 * Defines the type of AI conversation, which determines the max_tokens budget.
 */
public enum ConversationType {

    /**
     * Short check-in via Telegram/WhatsApp. Budget: ~200 tokens.
     */
    QUICK_CHECKIN,

    /**
     * Interactive coaching conversation via web chat. Budget: ~500 tokens.
     */
    COACHING,

    /**
     * Comprehensive post-interview summary or weekly report. Budget: ~1500 tokens.
     */
    SESSION_SUMMARY
}
```

**Step 2: Create `AiProperties.java`**

```java
package com.mindtrack.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the AI service.
 * Bound to the {@code mindtrack.ai} prefix in application.yml.
 */
@Component
@ConfigurationProperties(prefix = "mindtrack.ai")
public class AiProperties {

    private String apiKey;
    private String apiUrl = "https://api.anthropic.com/v1/messages";
    private String model = "claude-sonnet-4-20250514";
    private TokenTiers tokenTiers = new TokenTiers();
    private CacheConfig cache = new CacheConfig();

    /** Token tier budgets by conversation type. */
    public static class TokenTiers {
        private int quickCheckin = 200;
        private int coaching = 500;
        private int sessionSummary = 1500;

        public int getQuickCheckin() {
            return quickCheckin;
        }

        public void setQuickCheckin(int quickCheckin) {
            this.quickCheckin = quickCheckin;
        }

        public int getCoaching() {
            return coaching;
        }

        public void setCoaching(int coaching) {
            this.coaching = coaching;
        }

        public int getSessionSummary() {
            return sessionSummary;
        }

        public void setSessionSummary(int sessionSummary) {
            this.sessionSummary = sessionSummary;
        }
    }

    /** Caffeine cache configuration. */
    public static class CacheConfig {
        private int ttlHours = 24;
        private int maxSize = 500;

        public int getTtlHours() {
            return ttlHours;
        }

        public void setTtlHours(int ttlHours) {
            this.ttlHours = ttlHours;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public TokenTiers getTokenTiers() {
        return tokenTiers;
    }

    public void setTokenTiers(TokenTiers tokenTiers) {
        this.tokenTiers = tokenTiers;
    }

    public CacheConfig getCache() {
        return cache;
    }

    public void setCache(CacheConfig cache) {
        this.cache = cache;
    }

    /**
     * Returns the max_tokens budget for the given conversation type.
     *
     * @param type the conversation type
     * @return max tokens allowed
     */
    public int getMaxTokensFor(com.mindtrack.ai.model.ConversationType type) {
        return switch (type) {
            case QUICK_CHECKIN -> tokenTiers.getQuickCheckin();
            case COACHING -> tokenTiers.getCoaching();
            case SESSION_SUMMARY -> tokenTiers.getSessionSummary();
        };
    }
}
```

**Step 3: Delete `.gitkeep` placeholders**

Run: `rm backend/src/main/java/com/mindtrack/ai/model/.gitkeep backend/src/main/java/com/mindtrack/ai/config/.gitkeep`

**Step 4: Write test for AiProperties**

Create: `backend/src/test/java/com/mindtrack/ai/config/AiPropertiesTest.java`

```java
package com.mindtrack.ai.config;

import com.mindtrack.ai.model.ConversationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("local")
class AiPropertiesTest {

    @Autowired
    private AiProperties aiProperties;

    @Test
    void shouldLoadDefaultTokenTiers() {
        assertNotNull(aiProperties);
        assertEquals(200, aiProperties.getMaxTokensFor(ConversationType.QUICK_CHECKIN));
        assertEquals(500, aiProperties.getMaxTokensFor(ConversationType.COACHING));
        assertEquals(1500, aiProperties.getMaxTokensFor(ConversationType.SESSION_SUMMARY));
    }

    @Test
    void shouldLoadCacheDefaults() {
        assertEquals(24, aiProperties.getCache().getTtlHours());
        assertEquals(500, aiProperties.getCache().getMaxSize());
    }

    @Test
    void shouldLoadModelName() {
        assertEquals("claude-sonnet-4-20250514", aiProperties.getModel());
    }
}
```

**Step 5: Run tests**

Run: `cd backend && JAVA_HOME="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home" /opt/homebrew/bin/mvn test -pl . -Dtest=AiPropertiesTest -q`
Expected: Tests pass

**Step 6: Commit**

```bash
git add backend/src/main/java/com/mindtrack/ai/model/ConversationType.java backend/src/main/java/com/mindtrack/ai/config/AiProperties.java backend/src/test/java/com/mindtrack/ai/config/AiPropertiesTest.java
git rm backend/src/main/java/com/mindtrack/ai/model/.gitkeep backend/src/main/java/com/mindtrack/ai/config/.gitkeep
git commit -m "feat(backend): add ConversationType enum and AiProperties config with token tiers"
```

---

## Task 4: JPA Entities — Conversation + Message

**Files:**
- Create: `backend/src/main/java/com/mindtrack/ai/model/Conversation.java`
- Create: `backend/src/main/java/com/mindtrack/ai/model/Message.java`
- Create: `backend/src/main/java/com/mindtrack/ai/model/MessageRole.java`
- Create: `backend/src/main/java/com/mindtrack/ai/model/Channel.java`

**Step 1: Create `Channel.java` enum**

```java
package com.mindtrack.ai.model;

/**
 * Communication channel for conversations.
 */
public enum Channel {
    TELEGRAM,
    WHATSAPP,
    WEB
}
```

**Step 2: Create `MessageRole.java` enum**

```java
package com.mindtrack.ai.model;

/**
 * Role of a message sender in a conversation.
 */
public enum MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}
```

**Step 3: Create `Conversation.java`**

Maps to existing `conversations` table from V1 migration.

```java
package com.mindtrack.ai.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity mapping to the conversations table.
 */
@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<Message> messages = new ArrayList<>();

    /** Default constructor for JPA. */
    public Conversation() {
    }

    /**
     * Creates a new conversation.
     *
     * @param userId the user ID
     * @param channel the communication channel
     */
    public Conversation(Long userId, Channel channel) {
        this.userId = userId;
        this.channel = channel;
        this.startedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
```

**Step 4: Create `Message.java`**

Maps to existing `messages` table from V1 migration.

```java
package com.mindtrack.ai.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * JPA entity mapping to the messages table.
 */
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Default constructor for JPA. */
    public Message() {
    }

    /**
     * Creates a new message.
     *
     * @param conversation the parent conversation
     * @param role the message role
     * @param content the message content
     */
    public Message(Conversation conversation, MessageRole role, String content) {
        this.conversation = conversation;
        this.role = role;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public MessageRole getRole() {
        return role;
    }

    public void setRole(MessageRole role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
```

**Step 5: Verify compilation**

Run: `cd backend && JAVA_HOME="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home" /opt/homebrew/bin/mvn compile -q`
Expected: BUILD SUCCESS

**Step 6: Commit**

```bash
git add backend/src/main/java/com/mindtrack/ai/model/
git commit -m "feat(backend): add Conversation and Message JPA entities with Channel and MessageRole enums"
```

---

## Task 5: Repositories

**Files:**
- Create: `backend/src/main/java/com/mindtrack/ai/repository/ConversationRepository.java`
- Create: `backend/src/main/java/com/mindtrack/ai/repository/MessageRepository.java`
- Delete: `backend/src/main/java/com/mindtrack/ai/repository/.gitkeep`

**Step 1: Create `ConversationRepository.java`**

```java
package com.mindtrack.ai.repository;

import com.mindtrack.ai.model.Channel;
import com.mindtrack.ai.model.Conversation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for conversation persistence.
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Find all conversations for a user, ordered by most recent first.
     *
     * @param userId the user ID
     * @return list of conversations
     */
    List<Conversation> findByUserIdOrderByStartedAtDesc(Long userId);

    /**
     * Find conversations for a user on a specific channel.
     *
     * @param userId the user ID
     * @param channel the communication channel
     * @return list of conversations
     */
    List<Conversation> findByUserIdAndChannelOrderByStartedAtDesc(Long userId, Channel channel);
}
```

**Step 2: Create `MessageRepository.java`**

```java
package com.mindtrack.ai.repository;

import com.mindtrack.ai.model.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for message persistence.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find all messages in a conversation, ordered chronologically.
     *
     * @param conversationId the conversation ID
     * @return list of messages
     */
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
}
```

**Step 3: Delete `.gitkeep`**

Run: `rm backend/src/main/java/com/mindtrack/ai/repository/.gitkeep`

**Step 4: Verify compilation**

Run: `cd backend && JAVA_HOME="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home" /opt/homebrew/bin/mvn compile -q`
Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add backend/src/main/java/com/mindtrack/ai/repository/
git rm backend/src/main/java/com/mindtrack/ai/repository/.gitkeep
git commit -m "feat(backend): add ConversationRepository and MessageRepository"
```

---

## Task 6: DTOs

**Files:**
- Create: `backend/src/main/java/com/mindtrack/ai/dto/ChatRequest.java`
- Create: `backend/src/main/java/com/mindtrack/ai/dto/ChatResponse.java`
- Create: `backend/src/main/java/com/mindtrack/ai/dto/ConversationDto.java`
- Create: `backend/src/main/java/com/mindtrack/ai/dto/MessageDto.java`
- Delete: `backend/src/main/java/com/mindtrack/ai/dto/.gitkeep`

**Step 1: Create `ChatRequest.java`**

```java
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
```

**Step 2: Create `ChatResponse.java`**

```java
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
```

**Step 3: Create `MessageDto.java`**

```java
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
```

**Step 4: Create `ConversationDto.java`**

```java
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
```

**Step 5: Delete `.gitkeep`**

Run: `rm backend/src/main/java/com/mindtrack/ai/dto/.gitkeep`

**Step 6: Verify compilation**

Run: `cd backend && JAVA_HOME="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home" /opt/homebrew/bin/mvn compile -q`
Expected: BUILD SUCCESS

**Step 7: Commit**

```bash
git add backend/src/main/java/com/mindtrack/ai/dto/
git rm backend/src/main/java/com/mindtrack/ai/dto/.gitkeep
git commit -m "feat(backend): add AI chat DTOs (ChatRequest, ChatResponse, ConversationDto, MessageDto)"
```

---

## Task 7: AiResponseCache Service

**Files:**
- Create: `backend/src/main/java/com/mindtrack/ai/service/AiResponseCache.java`
- Create: `backend/src/main/java/com/mindtrack/ai/config/AiConfig.java`
- Create: `backend/src/test/java/com/mindtrack/ai/service/AiResponseCacheTest.java`
- Delete: `backend/src/main/java/com/mindtrack/ai/service/.gitkeep`

**Step 1: Create `AiConfig.java`**

```java
package com.mindtrack.ai.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration for the AI module: cache manager and REST client.
 */
@Configuration
@EnableCaching
public class AiConfig {

    /**
     * Caffeine cache manager for AI response caching.
     *
     * @param aiProperties the AI configuration properties
     * @return configured cache manager
     */
    @Bean
    public CacheManager aiCacheManager(AiProperties aiProperties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("ai-responses");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(aiProperties.getCache().getMaxSize())
                .expireAfterWrite(aiProperties.getCache().getTtlHours(), TimeUnit.HOURS)
                .recordStats());
        return cacheManager;
    }

    /**
     * REST client for Claude API calls.
     *
     * @param aiProperties the AI configuration properties
     * @return configured REST client
     */
    @Bean
    public RestClient claudeRestClient(AiProperties aiProperties) {
        return RestClient.builder()
                .baseUrl(aiProperties.getApiUrl())
                .defaultHeader("x-api-key", aiProperties.getApiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", "application/json")
                .build();
    }
}
```

**Step 2: Create `AiResponseCache.java`**

```java
package com.mindtrack.ai.service;

import com.mindtrack.ai.dto.ChatResponse;
import com.mindtrack.ai.model.ConversationType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Caffeine-backed cache for AI responses.
 * Caches SESSION_SUMMARY and QUICK_CHECKIN responses keyed by a context fingerprint.
 * COACHING responses are never cached (interactive conversations).
 */
@Service
public class AiResponseCache {

    private static final Logger LOG = LoggerFactory.getLogger(AiResponseCache.class);
    private static final String CACHE_NAME = "ai-responses";

    private final CacheManager cacheManager;

    /**
     * Creates the cache service.
     *
     * @param cacheManager the Spring cache manager
     */
    public AiResponseCache(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Checks if a cached response exists for the given context.
     *
     * @param userId the user ID
     * @param type the conversation type
     * @param contextFingerprint hash of user's current data state
     * @return cached response, or null if not found
     */
    public ChatResponse get(Long userId, ConversationType type, String contextFingerprint) {
        if (!isCacheable(type)) {
            return null;
        }

        String key = buildKey(userId, type, contextFingerprint);
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            return null;
        }

        ChatResponse cached = cache.get(key, ChatResponse.class);
        if (cached != null) {
            LOG.debug("Cache HIT for user={} type={}", userId, type);
        }
        return cached;
    }

    /**
     * Stores a response in the cache.
     *
     * @param userId the user ID
     * @param type the conversation type
     * @param contextFingerprint hash of user's current data state
     * @param response the AI response to cache
     */
    public void put(Long userId, ConversationType type, String contextFingerprint,
                    ChatResponse response) {
        if (!isCacheable(type)) {
            return;
        }

        String key = buildKey(userId, type, contextFingerprint);
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(key, response);
            LOG.debug("Cached response for user={} type={}", userId, type);
        }
    }

    /**
     * Determines if a conversation type should be cached.
     * COACHING is never cached (interactive). SESSION_SUMMARY and QUICK_CHECKIN are cached.
     *
     * @param type the conversation type
     * @return true if cacheable
     */
    public boolean isCacheable(ConversationType type) {
        return type == ConversationType.SESSION_SUMMARY
                || type == ConversationType.QUICK_CHECKIN;
    }

    private String buildKey(Long userId, ConversationType type, String contextFingerprint) {
        String raw = userId + ":" + type.name() + ":" + contextFingerprint;
        return sha256(raw);
    }

    static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
```

**Step 3: Delete `.gitkeep`**

Run: `rm backend/src/main/java/com/mindtrack/ai/service/.gitkeep`

**Step 4: Create test `AiResponseCacheTest.java`**

```java
package com.mindtrack.ai.service;

import com.mindtrack.ai.dto.ChatResponse;
import com.mindtrack.ai.model.ConversationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("local")
class AiResponseCacheTest {

    @Autowired
    private AiResponseCache cache;

    @Test
    void shouldCacheSessionSummaryResponses() {
        ChatResponse response = new ChatResponse(1L, 10L, "summary content", false, 1200);
        cache.put(1L, ConversationType.SESSION_SUMMARY, "fp-abc", response);

        ChatResponse cached = cache.get(1L, ConversationType.SESSION_SUMMARY, "fp-abc");
        assertNotNull(cached);
        assertEquals("summary content", cached.content());
    }

    @Test
    void shouldCacheQuickCheckinResponses() {
        ChatResponse response = new ChatResponse(1L, 11L, "checkin content", false, 150);
        cache.put(1L, ConversationType.QUICK_CHECKIN, "fp-def", response);

        ChatResponse cached = cache.get(1L, ConversationType.QUICK_CHECKIN, "fp-def");
        assertNotNull(cached);
        assertEquals("checkin content", cached.content());
    }

    @Test
    void shouldNotCacheCoachingResponses() {
        ChatResponse response = new ChatResponse(1L, 12L, "coaching content", false, 400);
        cache.put(1L, ConversationType.COACHING, "fp-ghi", response);

        ChatResponse cached = cache.get(1L, ConversationType.COACHING, "fp-ghi");
        assertNull(cached);
    }

    @Test
    void shouldReturnNullOnCacheMiss() {
        ChatResponse cached = cache.get(999L, ConversationType.SESSION_SUMMARY, "unknown-fp");
        assertNull(cached);
    }

    @Test
    void shouldInvalidateOnDifferentFingerprint() {
        ChatResponse response = new ChatResponse(1L, 13L, "old data", false, 1000);
        cache.put(1L, ConversationType.SESSION_SUMMARY, "fp-v1", response);

        ChatResponse cached = cache.get(1L, ConversationType.SESSION_SUMMARY, "fp-v2");
        assertNull(cached);
    }

    @Test
    void shouldIdentifyCacheableTypes() {
        assertTrue(cache.isCacheable(ConversationType.SESSION_SUMMARY));
        assertTrue(cache.isCacheable(ConversationType.QUICK_CHECKIN));
        assertFalse(cache.isCacheable(ConversationType.COACHING));
    }

    @Test
    void shouldProduceDeterministicSha256() {
        String hash1 = AiResponseCache.sha256("test-input");
        String hash2 = AiResponseCache.sha256("test-input");
        assertEquals(hash1, hash2);
        assertEquals(64, hash1.length());
    }
}
```

**Step 5: Run tests**

Run: `cd backend && JAVA_HOME="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home" /opt/homebrew/bin/mvn test -pl . -Dtest=AiResponseCacheTest -q`
Expected: All 7 tests pass

**Step 6: Commit**

```bash
git add backend/src/main/java/com/mindtrack/ai/config/AiConfig.java backend/src/main/java/com/mindtrack/ai/service/AiResponseCache.java backend/src/test/java/com/mindtrack/ai/service/AiResponseCacheTest.java
git rm backend/src/main/java/com/mindtrack/ai/service/.gitkeep
git commit -m "feat(backend): add Caffeine-backed AiResponseCache with fingerprint-based invalidation"
```

---

## Task 8: ContextBuilder Service

**Files:**
- Create: `backend/src/main/java/com/mindtrack/ai/service/ContextBuilder.java`
- Create: `backend/src/test/java/com/mindtrack/ai/service/ContextBuilderTest.java`

The ContextBuilder aggregates user data into a system prompt and generates a fingerprint for cache invalidation. Since other modules (interview, activity, journal, goals) are also still scaffolded, we use raw JDBC queries via `JdbcTemplate` to query the existing schema tables directly.

**Step 1: Create `ContextBuilder.java`**

```java
package com.mindtrack.ai.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Builds AI system prompts from user data and generates context fingerprints
 * for cache invalidation.
 */
@Service
public class ContextBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ContextBuilder.class);
    private static final int MOOD_HISTORY_DAYS = 7;
    private static final int INTERVIEW_RECENCY_DAYS = 30;

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates the context builder.
     *
     * @param jdbcTemplate the JDBC template for raw queries
     */
    public ContextBuilder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Builds a system prompt with the user's recent data context.
     *
     * @param userId the user ID
     * @return the system prompt string
     */
    public String buildSystemPrompt(Long userId) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a supportive AI mental health coach for the MindTrack app. ");
        prompt.append("Be empathetic, encouraging, and evidence-based. ");
        prompt.append("Never provide medical diagnoses or medication advice. ");
        prompt.append("Always recommend consulting a professional for serious concerns.\n\n");

        appendMoodContext(prompt, userId);
        appendActivityContext(prompt, userId);
        appendGoalContext(prompt, userId);
        appendInterviewContext(prompt, userId);

        return prompt.toString();
    }

    /**
     * Generates a fingerprint hash of the user's current data state.
     * Changes in the fingerprint indicate the cached response should be invalidated.
     *
     * @param userId the user ID
     * @return SHA-256 hex string
     */
    public String generateFingerprint(Long userId) {
        StringBuilder state = new StringBuilder();

        // Latest mood from journal
        Integer latestMood = queryLatestMood(userId);
        state.append("mood:").append(latestMood).append(";");

        // Active goals count
        int activeGoals = queryActiveGoalsCount(userId);
        state.append("goals:").append(activeGoals).append(";");

        // Recent activity completion count (last 7 days)
        int recentCompletions = queryRecentCompletions(userId);
        state.append("completions:").append(recentCompletions).append(";");

        // Latest interview date
        String latestInterview = queryLatestInterviewDate(userId);
        state.append("interview:").append(latestInterview).append(";");

        return sha256(state.toString());
    }

    private void appendMoodContext(StringBuilder prompt, Long userId) {
        List<Map<String, Object>> moods = jdbcTemplate.queryForList(
                "SELECT entry_date, mood FROM journal_entries "
                        + "WHERE user_id = ? AND mood IS NOT NULL "
                        + "AND entry_date >= DATE_SUB(CURRENT_DATE, INTERVAL ? DAY) "
                        + "ORDER BY entry_date DESC",
                userId, MOOD_HISTORY_DAYS);

        if (!moods.isEmpty()) {
            prompt.append("RECENT MOOD DATA (last 7 days):\n");
            for (Map<String, Object> row : moods) {
                prompt.append("- ").append(row.get("entry_date"))
                        .append(": mood ").append(row.get("mood")).append("/10\n");
            }
            prompt.append("\n");
        }
    }

    private void appendActivityContext(StringBuilder prompt, Long userId) {
        List<Map<String, Object>> activities = jdbcTemplate.queryForList(
                "SELECT a.name, COUNT(al.id) AS completed_count "
                        + "FROM activities a LEFT JOIN activity_logs al "
                        + "ON a.id = al.activity_id AND al.completed = TRUE "
                        + "AND al.log_date >= DATE_SUB(CURRENT_DATE, INTERVAL ? DAY) "
                        + "WHERE a.user_id = ? AND a.active = TRUE "
                        + "GROUP BY a.id, a.name",
                MOOD_HISTORY_DAYS, userId);

        if (!activities.isEmpty()) {
            prompt.append("ACTIVE ACTIVITIES (last 7 days completions):\n");
            for (Map<String, Object> row : activities) {
                prompt.append("- ").append(row.get("name"))
                        .append(": completed ").append(row.get("completed_count"))
                        .append(" times\n");
            }
            prompt.append("\n");
        }
    }

    private void appendGoalContext(StringBuilder prompt, Long userId) {
        List<Map<String, Object>> goals = jdbcTemplate.queryForList(
                "SELECT g.title, g.status, COUNT(m.id) AS total_milestones, "
                        + "SUM(CASE WHEN m.completed_at IS NOT NULL THEN 1 ELSE 0 END) "
                        + "AS completed_milestones "
                        + "FROM goals g LEFT JOIN milestones m ON g.id = m.goal_id "
                        + "WHERE g.user_id = ? AND g.status IN ('NOT_STARTED', 'IN_PROGRESS') "
                        + "GROUP BY g.id, g.title, g.status",
                userId);

        if (!goals.isEmpty()) {
            prompt.append("ACTIVE GOALS:\n");
            for (Map<String, Object> row : goals) {
                prompt.append("- ").append(row.get("title"))
                        .append(" (").append(row.get("status")).append(")")
                        .append(": ").append(row.get("completed_milestones"))
                        .append("/").append(row.get("total_milestones"))
                        .append(" milestones done\n");
            }
            prompt.append("\n");
        }
    }

    private void appendInterviewContext(StringBuilder prompt, Long userId) {
        List<Map<String, Object>> interviews = jdbcTemplate.queryForList(
                "SELECT interview_date, recommendations, mood_after "
                        + "FROM interviews WHERE user_id = ? "
                        + "AND interview_date >= DATE_SUB(CURRENT_DATE, INTERVAL ? DAY) "
                        + "ORDER BY interview_date DESC LIMIT 1",
                userId, INTERVIEW_RECENCY_DAYS);

        if (!interviews.isEmpty()) {
            Map<String, Object> latest = interviews.get(0);
            prompt.append("LATEST INTERVIEW (").append(latest.get("interview_date")).append("):\n");
            if (latest.get("recommendations") != null) {
                prompt.append("- Recommendations: ").append(latest.get("recommendations")).append("\n");
            }
            if (latest.get("mood_after") != null) {
                prompt.append("- Post-session mood: ").append(latest.get("mood_after")).append("/10\n");
            }
            prompt.append("\n");
        }
    }

    private Integer queryLatestMood(Long userId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT mood FROM journal_entries WHERE user_id = ? "
                            + "AND mood IS NOT NULL ORDER BY entry_date DESC LIMIT 1",
                    Integer.class, userId);
        } catch (Exception e) {
            return null;
        }
    }

    private int queryActiveGoalsCount(Long userId) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM goals WHERE user_id = ? "
                            + "AND status IN ('NOT_STARTED', 'IN_PROGRESS')",
                    Integer.class, userId);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private int queryRecentCompletions(Long userId) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM activity_logs al "
                            + "JOIN activities a ON al.activity_id = a.id "
                            + "WHERE a.user_id = ? AND al.completed = TRUE "
                            + "AND al.log_date >= DATE_SUB(CURRENT_DATE, INTERVAL ? DAY)",
                    Integer.class, userId, MOOD_HISTORY_DAYS);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private String queryLatestInterviewDate(Long userId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT MAX(interview_date) FROM interviews WHERE user_id = ?",
                    String.class, userId);
        } catch (Exception e) {
            return "none";
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
```

**Step 2: Create test `ContextBuilderTest.java`**

```java
package com.mindtrack.ai.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("local")
class ContextBuilderTest {

    @Autowired
    private ContextBuilder contextBuilder;

    @Test
    void shouldBuildSystemPromptWithBaseInstructions() {
        String prompt = contextBuilder.buildSystemPrompt(1L);
        assertNotNull(prompt);
        assertTrue(prompt.contains("supportive AI mental health coach"));
        assertTrue(prompt.contains("Never provide medical diagnoses"));
    }

    @Test
    void shouldGenerateDeterministicFingerprint() {
        String fp1 = contextBuilder.generateFingerprint(1L);
        String fp2 = contextBuilder.generateFingerprint(1L);
        assertNotNull(fp1);
        assertFalse(fp1.isEmpty());
        // Same user with same data = same fingerprint
        assertTrue(fp1.equals(fp2));
    }

    @Test
    void shouldHandleUserWithNoData() {
        // User 999 doesn't exist — should not throw, just return base prompt
        String prompt = contextBuilder.buildSystemPrompt(999L);
        assertNotNull(prompt);
        assertTrue(prompt.contains("supportive AI mental health coach"));
    }
}
```

**Step 3: Run tests**

Run: `cd backend && JAVA_HOME="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home" /opt/homebrew/bin/mvn test -pl . -Dtest=ContextBuilderTest -q`
Expected: All 3 tests pass

**Step 4: Commit**

```bash
git add backend/src/main/java/com/mindtrack/ai/service/ContextBuilder.java backend/src/test/java/com/mindtrack/ai/service/ContextBuilderTest.java
git commit -m "feat(backend): add ContextBuilder for user data aggregation and fingerprint generation"
```

---

## Task 9: ClaudeApiClient Service

**Files:**
- Create: `backend/src/main/java/com/mindtrack/ai/service/ClaudeApiClient.java`
- Create: `backend/src/test/java/com/mindtrack/ai/service/ClaudeApiClientTest.java`

**Step 1: Create `ClaudeApiClient.java`**

```java
package com.mindtrack.ai.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mindtrack.ai.config.AiProperties;
import com.mindtrack.ai.dto.ChatResponse;
import com.mindtrack.ai.dto.MessageDto;
import com.mindtrack.ai.model.ConversationType;
import com.mindtrack.ai.model.MessageRole;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * HTTP client for the Anthropic Claude Messages API.
 * Automatically selects max_tokens based on ConversationType tier.
 */
@Service
public class ClaudeApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(ClaudeApiClient.class);

    private final RestClient restClient;
    private final AiProperties aiProperties;

    /**
     * Creates the Claude API client.
     *
     * @param claudeRestClient the pre-configured REST client
     * @param aiProperties the AI configuration properties
     */
    public ClaudeApiClient(RestClient claudeRestClient, AiProperties aiProperties) {
        this.restClient = claudeRestClient;
        this.aiProperties = aiProperties;
    }

    /**
     * Sends a message to Claude and returns the response.
     *
     * @param systemPrompt the system prompt with user context
     * @param conversationHistory prior messages in the conversation
     * @param type the conversation type (determines max_tokens)
     * @return the AI response with token usage
     */
    public ChatResponse sendMessage(String systemPrompt, List<MessageDto> conversationHistory,
                                    ConversationType type) {
        int maxTokens = aiProperties.getMaxTokensFor(type);
        LOG.info("Sending Claude API request: model={} type={} maxTokens={}",
                aiProperties.getModel(), type, maxTokens);

        List<Map<String, String>> messages = new ArrayList<>();
        for (MessageDto msg : conversationHistory) {
            String role = msg.role() == MessageRole.ASSISTANT ? "assistant" : "user";
            messages.add(Map.of("role", role, "content", msg.content()));
        }

        ClaudeRequest request = new ClaudeRequest(
                aiProperties.getModel(),
                maxTokens,
                systemPrompt,
                messages
        );

        ClaudeApiResponse apiResponse = restClient.post()
                .body(request)
                .retrieve()
                .body(ClaudeApiResponse.class);

        if (apiResponse == null || apiResponse.content() == null || apiResponse.content().isEmpty()) {
            LOG.error("Empty response from Claude API");
            return new ChatResponse(null, null, "I'm sorry, I couldn't generate a response.", false, 0);
        }

        String responseText = apiResponse.content().get(0).text();
        int tokensUsed = apiResponse.usage() != null ? apiResponse.usage().outputTokens() : 0;

        LOG.info("Claude API response: tokensUsed={}", tokensUsed);
        return new ChatResponse(null, null, responseText, false, tokensUsed);
    }

    /** Request body for Claude Messages API. */
    record ClaudeRequest(
            String model,
            @JsonProperty("max_tokens") int maxTokens,
            String system,
            List<Map<String, String>> messages
    ) {
    }

    /** Response body from Claude Messages API. */
    record ClaudeApiResponse(
            String id,
            List<ContentBlock> content,
            Usage usage
    ) {
    }

    /** A content block in the API response. */
    record ContentBlock(
            String type,
            String text
    ) {
    }

    /** Token usage from the API response. */
    record Usage(
            @JsonProperty("input_tokens") int inputTokens,
            @JsonProperty("output_tokens") int outputTokens
    ) {
    }
}
```

**Step 2: Create test `ClaudeApiClientTest.java`**

Tests use a mock RestClient to verify the tiered max_tokens logic without real API calls.

```java
package com.mindtrack.ai.service;

import com.mindtrack.ai.config.AiProperties;
import com.mindtrack.ai.dto.ChatResponse;
import com.mindtrack.ai.dto.MessageDto;
import com.mindtrack.ai.model.ConversationType;
import com.mindtrack.ai.model.MessageRole;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClaudeApiClientTest {

    @Test
    void shouldSelectCorrectMaxTokensPerTier() {
        AiProperties props = new AiProperties();
        props.setApiKey("test-key");
        props.setModel("claude-sonnet-4-20250514");

        // Verify tier values from default config
        assertEquals(200, props.getMaxTokensFor(ConversationType.QUICK_CHECKIN));
        assertEquals(500, props.getMaxTokensFor(ConversationType.COACHING));
        assertEquals(1500, props.getMaxTokensFor(ConversationType.SESSION_SUMMARY));
    }

    @Test
    void shouldSelectCustomMaxTokensWhenOverridden() {
        AiProperties props = new AiProperties();
        AiProperties.TokenTiers tiers = new AiProperties.TokenTiers();
        tiers.setQuickCheckin(100);
        tiers.setCoaching(300);
        tiers.setSessionSummary(2000);
        props.setTokenTiers(tiers);

        assertEquals(100, props.getMaxTokensFor(ConversationType.QUICK_CHECKIN));
        assertEquals(300, props.getMaxTokensFor(ConversationType.COACHING));
        assertEquals(2000, props.getMaxTokensFor(ConversationType.SESSION_SUMMARY));
    }

    @Test
    void shouldCreateValidMessageDtos() {
        MessageDto msg = new MessageDto(1L, MessageRole.USER, "hello", LocalDateTime.now());
        assertNotNull(msg);
        assertEquals(MessageRole.USER, msg.role());
        assertEquals("hello", msg.content());
    }
}
```

**Step 3: Run tests**

Run: `cd backend && JAVA_HOME="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home" /opt/homebrew/bin/mvn test -pl . -Dtest=ClaudeApiClientTest -q`
Expected: All 3 tests pass

**Step 4: Commit**

```bash
git add backend/src/main/java/com/mindtrack/ai/service/ClaudeApiClient.java backend/src/test/java/com/mindtrack/ai/service/ClaudeApiClientTest.java
git commit -m "feat(backend): add ClaudeApiClient with tiered max_tokens per ConversationType"
```

---

## Task 10: ConversationService

**Files:**
- Create: `backend/src/main/java/com/mindtrack/ai/service/ConversationService.java`
- Create: `backend/src/test/java/com/mindtrack/ai/service/ConversationServiceTest.java`

**Step 1: Create `ConversationService.java`**

```java
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
```

**Step 2: Create test `ConversationServiceTest.java`**

```java
package com.mindtrack.ai.service;

import com.mindtrack.ai.dto.ChatRequest;
import com.mindtrack.ai.dto.ChatResponse;
import com.mindtrack.ai.dto.MessageDto;
import com.mindtrack.ai.model.ConversationType;
import com.mindtrack.ai.model.MessageRole;
import com.mindtrack.ai.repository.ConversationRepository;
import com.mindtrack.ai.repository.MessageRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
```

**Step 3: Run tests**

Run: `cd backend && JAVA_HOME="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home" /opt/homebrew/bin/mvn test -pl . -Dtest=ConversationServiceTest -q`
Expected: All 3 tests pass

**Step 4: Commit**

```bash
git add backend/src/main/java/com/mindtrack/ai/service/ConversationService.java backend/src/test/java/com/mindtrack/ai/service/ConversationServiceTest.java
git commit -m "feat(backend): add ConversationService with cache-first pattern and message persistence"
```

---

## Task 11: ChatController

**Files:**
- Create: `backend/src/main/java/com/mindtrack/ai/controller/ChatController.java`
- Create: `backend/src/test/java/com/mindtrack/ai/controller/ChatControllerTest.java`
- Delete: `backend/src/main/java/com/mindtrack/ai/controller/.gitkeep`

**Step 1: Create `ChatController.java`**

```java
package com.mindtrack.ai.controller;

import com.mindtrack.ai.dto.ChatRequest;
import com.mindtrack.ai.dto.ChatResponse;
import com.mindtrack.ai.dto.ConversationDto;
import com.mindtrack.ai.service.ConversationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    /**
     * Creates the chat controller.
     *
     * @param conversationService the conversation service
     */
    public ChatController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * Send a message to the AI and receive a response.
     * Uses cached response when available for SESSION_SUMMARY and QUICK_CHECKIN types.
     *
     * @param request the chat request with message and optional conversation ID
     * @return the AI response
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody @Valid ChatRequest request) {
        // TODO: get userId from SecurityContext once auth module is implemented
        Long userId = 1L;

        ChatResponse response = conversationService.chat(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * List all conversations for the current user.
     *
     * @return list of conversations
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> listConversations() {
        // TODO: get userId from SecurityContext once auth module is implemented
        Long userId = 1L;

        List<ConversationDto> conversations = conversationService.listConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    /**
     * Get a single conversation with all messages.
     *
     * @param id the conversation ID
     * @return the conversation with messages
     */
    @GetMapping("/conversations/{id}")
    public ResponseEntity<ConversationDto> getConversation(@PathVariable Long id) {
        ConversationDto conversation = conversationService.getConversation(id);
        if (conversation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(conversation);
    }
}
```

**Step 2: Delete `.gitkeep`**

Run: `rm backend/src/main/java/com/mindtrack/ai/controller/.gitkeep`

**Step 3: Create test `ChatControllerTest.java`**

```java
package com.mindtrack.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.ai.dto.ChatRequest;
import com.mindtrack.ai.dto.ChatResponse;
import com.mindtrack.ai.model.ConversationType;
import com.mindtrack.ai.service.ConversationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConversationService conversationService;

    @Test
    @WithMockUser
    void shouldReturnAiResponse() throws Exception {
        ChatResponse response = new ChatResponse(1L, 10L, "Hello! How are you?", false, 150);
        when(conversationService.chat(eq(1L), any(ChatRequest.class))).thenReturn(response);

        ChatRequest request = new ChatRequest(null, "Hi there", ConversationType.COACHING);

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello! How are you?"))
                .andExpect(jsonPath("$.cached").value(false))
                .andExpect(jsonPath("$.tokensUsed").value(150));
    }

    @Test
    @WithMockUser
    void shouldReturnCachedResponse() throws Exception {
        ChatResponse response = new ChatResponse(1L, 10L, "Cached summary", true, 1200);
        when(conversationService.chat(eq(1L), any(ChatRequest.class))).thenReturn(response);

        ChatRequest request = new ChatRequest(null, "Summarize my week", ConversationType.SESSION_SUMMARY);

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cached").value(true));
    }

    @Test
    @WithMockUser
    void shouldRejectEmptyMessage() throws Exception {
        ChatRequest request = new ChatRequest(null, "", ConversationType.COACHING);

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldListConversations() throws Exception {
        when(conversationService.listConversations(1L)).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/ai/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser
    void shouldReturn404ForMissingConversation() throws Exception {
        when(conversationService.getConversation(999L)).thenReturn(null);

        mockMvc.perform(get("/api/ai/conversations/999"))
                .andExpect(status().isNotFound());
    }
}
```

**Step 4: Run tests**

Run: `cd backend && JAVA_HOME="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home" /opt/homebrew/bin/mvn test -pl . -Dtest=ChatControllerTest -q`
Expected: All 5 tests pass

**Step 5: Commit**

```bash
git add backend/src/main/java/com/mindtrack/ai/controller/ChatController.java backend/src/test/java/com/mindtrack/ai/controller/ChatControllerTest.java
git rm backend/src/main/java/com/mindtrack/ai/controller/.gitkeep
git commit -m "feat(backend): add ChatController with REST endpoints for AI chat and conversation listing"
```

---

## Task 12: Full Integration Test + Final Verification

**Files:**
- Create: `backend/src/test/java/com/mindtrack/ai/AiModuleIntegrationTest.java`

**Step 1: Create integration test**

```java
package com.mindtrack.ai;

import com.mindtrack.ai.config.AiProperties;
import com.mindtrack.ai.model.ConversationType;
import com.mindtrack.ai.service.AiResponseCache;
import com.mindtrack.ai.service.ContextBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test verifying the full AI module wiring.
 */
@SpringBootTest
@ActiveProfiles("local")
class AiModuleIntegrationTest {

    @Autowired
    private AiProperties aiProperties;

    @Autowired
    private AiResponseCache responseCache;

    @Autowired
    private ContextBuilder contextBuilder;

    @Test
    void shouldLoadAllAiBeans() {
        assertNotNull(aiProperties);
        assertNotNull(responseCache);
        assertNotNull(contextBuilder);
    }

    @Test
    void shouldHaveCorrectTokenTiers() {
        assertEquals(200, aiProperties.getMaxTokensFor(ConversationType.QUICK_CHECKIN));
        assertEquals(500, aiProperties.getMaxTokensFor(ConversationType.COACHING));
        assertEquals(1500, aiProperties.getMaxTokensFor(ConversationType.SESSION_SUMMARY));
    }

    @Test
    void shouldIdentifyCacheableAndNonCacheableTypes() {
        assertTrue(responseCache.isCacheable(ConversationType.SESSION_SUMMARY));
        assertTrue(responseCache.isCacheable(ConversationType.QUICK_CHECKIN));
        assertFalse(responseCache.isCacheable(ConversationType.COACHING));
    }

    @Test
    void shouldBuildContextWithoutErrors() {
        String prompt = contextBuilder.buildSystemPrompt(1L);
        assertNotNull(prompt);
        assertFalse(prompt.isEmpty());
    }

    @Test
    void shouldGenerateFingerprintWithoutErrors() {
        String fp = contextBuilder.generateFingerprint(1L);
        assertNotNull(fp);
        assertEquals(64, fp.length()); // SHA-256 hex length
    }
}
```

**Step 2: Run the full test suite**

Run: `cd backend && JAVA_HOME="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home" /opt/homebrew/bin/mvn verify -B`
Expected: BUILD SUCCESS with all tests passing

**Step 3: Commit**

```bash
git add backend/src/test/java/com/mindtrack/ai/AiModuleIntegrationTest.java
git commit -m "test(backend): add AI module integration test verifying full wiring"
```

---

## Summary of All Tasks

| # | Task | Files | Tests |
|---|------|-------|-------|
| 1 | Add Caffeine dependency | `pom.xml` | compile check |
| 2 | AI config in application.yml | `application*.yml` | — |
| 3 | ConversationType + AiProperties | model + config classes | `AiPropertiesTest` |
| 4 | JPA entities | Conversation, Message, Channel, MessageRole | compile check |
| 5 | Repositories | ConversationRepository, MessageRepository | compile check |
| 6 | DTOs | ChatRequest, ChatResponse, ConversationDto, MessageDto | compile check |
| 7 | AiResponseCache + AiConfig | cache service + Spring config | `AiResponseCacheTest` (7 tests) |
| 8 | ContextBuilder | user data aggregation + fingerprint | `ContextBuilderTest` (3 tests) |
| 9 | ClaudeApiClient | HTTP client with tiered tokens | `ClaudeApiClientTest` (3 tests) |
| 10 | ConversationService | orchestrator with cache-first pattern | `ConversationServiceTest` (3 tests) |
| 11 | ChatController | REST endpoints | `ChatControllerTest` (5 tests) |
| 12 | Integration test | full module wiring verification | `AiModuleIntegrationTest` (5 tests) |
