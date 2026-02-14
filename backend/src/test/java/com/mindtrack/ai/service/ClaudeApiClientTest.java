package com.mindtrack.ai.service;

import com.mindtrack.ai.config.AiProperties;
import com.mindtrack.ai.dto.MessageDto;
import com.mindtrack.ai.model.ConversationType;
import com.mindtrack.ai.model.MessageRole;
import java.time.LocalDateTime;
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
