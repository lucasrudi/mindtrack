package com.mindtrack.ai.service;

import com.mindtrack.ai.dto.ChatResponse;
import com.mindtrack.ai.model.ConversationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
