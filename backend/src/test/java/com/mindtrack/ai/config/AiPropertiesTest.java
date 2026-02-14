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
