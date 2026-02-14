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
