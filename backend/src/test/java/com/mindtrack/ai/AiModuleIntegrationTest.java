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
