# AI Service with Response Caching & Tiered Token Limits

**Goal:** Implement the full AI service layer (Claude API client, conversation service, context builder) with Caffeine in-memory caching and tiered `max_tokens` to optimize Claude API quota usage.

**Scope:** Backend only. No frontend, no streaming, no rate limiting.

---

## Architecture

```
ai/
├── config/
│   └── AiConfig.java              # Claude API client bean + Caffeine cache config
│   └── AiProperties.java          # Token tiers + cache TTL from application.yml
├── controller/
│   └── ChatController.java        # REST endpoints (POST /api/ai/chat, GET /api/ai/conversations)
├── service/
│   └── ClaudeApiClient.java       # HTTP client wrapping Anthropic Messages API
│   └── ConversationService.java   # Conversation CRUD + message persistence
│   └── ContextBuilder.java        # Aggregates user data into system prompt
│   └── AiResponseCache.java       # Caffeine cache for AI summaries (keyed by content hash)
├── model/
│   └── Conversation.java          # JPA entity (maps to existing conversations table)
│   └── Message.java               # JPA entity (maps to existing messages table)
│   └── ConversationType.java      # Enum: QUICK_CHECKIN, COACHING, SESSION_SUMMARY
├── dto/
│   └── ChatRequest.java           # { conversationId, message, type }
│   └── ChatResponse.java          # { messageId, content, cached, tokensUsed }
│   └── ConversationDto.java       # Conversation list/detail DTO
├── repository/
│   └── ConversationRepository.java
│   └── MessageRepository.java
```

Reuses existing `conversations` and `messages` tables. No new migration needed for core entities.

---

## Tiered Response Lengths

Three conversation tiers with different `max_tokens`:

| Tier | ConversationType | max_tokens | Use Case |
|------|-----------------|------------|----------|
| Quick | QUICK_CHECKIN | 200 | Telegram/WhatsApp check-ins, mood responses |
| Standard | COACHING | 500 | Interactive coaching conversations via web chat |
| Deep | SESSION_SUMMARY | 1500 | Post-interview comprehensive summaries, weekly reports |

Configuration in `application.yml`:

```yaml
mindtrack:
  ai:
    api-key: ${CLAUDE_API_KEY:dummy-key-for-local}
    model: claude-sonnet-4-20250514
    token-tiers:
      quick-checkin: 200
      coaching: 500
      session-summary: 1500
    cache:
      ttl-hours: 24
      max-size: 500
```

The `ChatRequest` DTO includes a `type` field. The `ClaudeApiClient` reads the tier config and sets `max_tokens` accordingly. Defaults to `COACHING` if no type provided.

---

## Response Caching

**Backend:** Caffeine in-memory cache (zero infrastructure).

**Key:** SHA-256 hash of `(userId, conversationType, contextFingerprint)` — where `contextFingerprint` is a hash of the most recent user data snapshot (last mood, last activity date, active goals count).

**Value:** AI response text + metadata (tokens used, timestamp).

**TTL:** 24 hours (configurable). **Max size:** 500 entries per Lambda instance.

**Caching rules:**

| Type | Cached? | Reason |
|------|---------|--------|
| SESSION_SUMMARY | Yes | Expensive, rarely changes within 24h |
| QUICK_CHECKIN | Yes | Only when user data hasn't changed since last check-in |
| COACHING | No | Interactive, each message builds on previous |

```java
@Service
public class AiResponseCache {
    private final Cache<String, CachedResponse> cache;
    public CachedResponse get(Long userId, ConversationType type, String contextFingerprint);
    public void put(Long userId, ConversationType type, String contextFingerprint, ChatResponse response);
}
```

The `ContextBuilder` generates the fingerprint. If fingerprint matches a cached entry, return cached response without calling Claude.

---

## Claude API Client

Uses Spring `RestClient` (no external SDK). Calls Anthropic Messages API at `https://api.anthropic.com/v1/messages`.

```java
@Service
public class ClaudeApiClient {
    public ChatResponse sendMessage(
        String systemPrompt,
        List<MessageDto> conversationHistory,
        ConversationType type
    );
}
```

No streaming — deferred to task-08 frontend work.

---

## Context Builder

```java
@Service
public class ContextBuilder {
    public SystemContext buildContext(Long userId);
    public String generateFingerprint(Long userId);
}
```

System prompt includes:
- Last 7 days of mood ratings (from activity logs + journal)
- Active goals and their progress
- Recent activity completion rates
- Last interview recommendations (if within 30 days)

---

## Not Included (YAGNI)

- Streaming (SSE/WebSocket) — deferred to task-08 frontend
- Anthropic prompt caching API feature — later optimization
- Redis — Caffeine sufficient for Lambda
- Rate limiting — separate concern
- Frontend — backend only
