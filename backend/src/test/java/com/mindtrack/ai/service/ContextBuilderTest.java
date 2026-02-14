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
