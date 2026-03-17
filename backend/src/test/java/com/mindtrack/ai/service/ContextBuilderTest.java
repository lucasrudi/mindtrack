package com.mindtrack.ai.service;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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

    /**
     * Unit-level tests using a mocked JdbcTemplate to exercise populated and empty query paths
     * without a running database.
     */
    @Nested
    @ExtendWith(MockitoExtension.class)
    class WithMockedJdbc {

        @Mock
        private JdbcTemplate jdbcTemplate;

        @InjectMocks
        private ContextBuilder contextBuilder;

        // -----------------------------------------------------------------------
        // buildSystemPrompt — populated paths
        // -----------------------------------------------------------------------

        @Test
        void shouldIncludeMoodContextWhenJournalEntriesExist() {
            // appendMoodContext calls: queryForList(sql, userId, MOOD_HISTORY_DAYS)
            // Use lenient to avoid UnnecessaryStubbing for other queryForList calls
            org.mockito.Mockito.lenient()
                    .when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                    .thenReturn(List.of());
            when(jdbcTemplate.queryForList(
                    org.mockito.ArgumentMatchers.contains("journal_entries"),
                    any(Object[].class)))
                    .thenReturn(List.of(
                            Map.of("entry_date", "2025-01-10", "mood", 7),
                            Map.of("entry_date", "2025-01-09", "mood", 5)
                    ));

            String prompt = contextBuilder.buildSystemPrompt(1L);

            assertNotNull(prompt);
            assertTrue(prompt.contains("RECENT MOOD DATA"));
        }

        @Test
        void shouldIncludeGoalContextWhenActiveGoalsExist() {
            // appendGoalContext calls: queryForList(sql, userId) where sql contains "goals g LEFT JOIN"
            org.mockito.Mockito.lenient()
                    .when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                    .thenReturn(List.of());
            when(jdbcTemplate.queryForList(
                    org.mockito.ArgumentMatchers.contains("goals g LEFT JOIN milestones"),
                    any(Object[].class)))
                    .thenReturn(List.of(
                            Map.of("title", "Run a 5K",
                                    "status", "IN_PROGRESS",
                                    "total_milestones", 3L,
                                    "completed_milestones", 1L)
                    ));

            String prompt = contextBuilder.buildSystemPrompt(1L);

            assertNotNull(prompt);
            assertTrue(prompt.contains("ACTIVE GOALS"));
            assertTrue(prompt.contains("Run a 5K"));
        }

        @Test
        void shouldIncludeInterviewContextWhenRecentInterviewExists() {
            // appendInterviewContext calls: queryForList(sql, userId, INTERVIEW_RECENCY_DAYS)
            org.mockito.Mockito.lenient()
                    .when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                    .thenReturn(List.of());
            when(jdbcTemplate.queryForList(
                    org.mockito.ArgumentMatchers.contains("FROM interviews"),
                    any(Object[].class)))
                    .thenReturn(List.of(
                            Map.of("interview_date", "2025-01-05",
                                    "recommendations", "Practice deep breathing",
                                    "mood_after", 8)
                    ));

            String prompt = contextBuilder.buildSystemPrompt(1L);

            assertNotNull(prompt);
            assertTrue(prompt.contains("LATEST INTERVIEW"));
            assertTrue(prompt.contains("Practice deep breathing"));
        }

        @Test
        void shouldIncludeActivityContextWhenActivitiesExist() {
            // appendActivityContext calls: queryForList(sql, MOOD_HISTORY_DAYS, userId)
            org.mockito.Mockito.lenient()
                    .when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                    .thenReturn(List.of());
            when(jdbcTemplate.queryForList(
                    org.mockito.ArgumentMatchers.contains("activities a LEFT JOIN activity_logs"),
                    any(Object[].class)))
                    .thenReturn(List.of(
                            Map.of("name", "Morning run", "completed_count", 5L)
                    ));

            String prompt = contextBuilder.buildSystemPrompt(1L);

            assertNotNull(prompt);
            assertTrue(prompt.contains("ACTIVE ACTIVITIES"));
            assertTrue(prompt.contains("Morning run"));
        }

        // -----------------------------------------------------------------------
        // buildSystemPrompt — empty/fallback paths
        // -----------------------------------------------------------------------

        @Test
        void shouldReturnValidPromptWhenAllRepositoriesReturnEmpty() {
            // All list queries return empty — no context sections should be appended
            when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                    .thenReturn(List.of());

            String prompt = contextBuilder.buildSystemPrompt(1L);

            assertNotNull(prompt);
            assertFalse(prompt.isBlank());
            assertTrue(prompt.contains("supportive AI mental health coach"));
            assertFalse(prompt.contains("RECENT MOOD DATA"));
            assertFalse(prompt.contains("ACTIVE GOALS"));
            assertFalse(prompt.contains("LATEST INTERVIEW"));
        }

        // -----------------------------------------------------------------------
        // generateFingerprint
        // -----------------------------------------------------------------------

        @Test
        void shouldReturnValidFingerprintWhenAllQueriesThrowOrReturnEmpty() {
            // queryLatestMood throws (no journal entries)
            when(jdbcTemplate.queryForObject(
                    org.mockito.ArgumentMatchers.contains("journal_entries"),
                    eq(Integer.class), any(Object[].class)))
                    .thenThrow(new EmptyResultDataAccessException(1));
            // queryActiveGoalsCount returns 0
            when(jdbcTemplate.queryForObject(
                    org.mockito.ArgumentMatchers.contains("FROM goals"),
                    eq(Integer.class), any(Object[].class)))
                    .thenReturn(0);
            // queryRecentCompletions returns 0
            when(jdbcTemplate.queryForObject(
                    org.mockito.ArgumentMatchers.contains("activity_logs"),
                    eq(Integer.class), any(Object[].class)))
                    .thenReturn(0);
            // queryLatestInterviewDate returns null
            when(jdbcTemplate.queryForObject(
                    org.mockito.ArgumentMatchers.contains("MAX(interview_date)"),
                    eq(String.class), any(Object[].class)))
                    .thenReturn(null);

            String fingerprint = contextBuilder.generateFingerprint(1L);

            assertNotNull(fingerprint);
            assertFalse(fingerprint.isBlank());
            // SHA-256 hex = 64 chars
            assertEquals(64, fingerprint.length());
        }

        @Test
        void shouldProduceDifferentFingerprintsWhenMoodChanges() {
            // First call: mood = 7
            when(jdbcTemplate.queryForObject(
                    org.mockito.ArgumentMatchers.contains("journal_entries"),
                    eq(Integer.class), any(Object[].class)))
                    .thenReturn(7);
            when(jdbcTemplate.queryForObject(
                    org.mockito.ArgumentMatchers.contains("FROM goals"),
                    eq(Integer.class), any(Object[].class)))
                    .thenReturn(2);
            when(jdbcTemplate.queryForObject(
                    org.mockito.ArgumentMatchers.contains("activity_logs"),
                    eq(Integer.class), any(Object[].class)))
                    .thenReturn(5);
            when(jdbcTemplate.queryForObject(
                    org.mockito.ArgumentMatchers.contains("MAX(interview_date)"),
                    eq(String.class), any(Object[].class)))
                    .thenReturn("2025-01-10");

            String fp1 = contextBuilder.generateFingerprint(1L);

            // Second call: mood changed to 3
            when(jdbcTemplate.queryForObject(
                    org.mockito.ArgumentMatchers.contains("journal_entries"),
                    eq(Integer.class), any(Object[].class)))
                    .thenReturn(3);

            String fp2 = contextBuilder.generateFingerprint(1L);

            assertNotNull(fp1);
            assertNotNull(fp2);
            assertFalse(fp1.equals(fp2), "Fingerprints should differ when mood changes");
        }

        @Test
        void shouldProduceSameFingerprintWhenDataUnchanged() {
            when(jdbcTemplate.queryForObject(
                    org.mockito.ArgumentMatchers.contains("journal_entries"),
                    eq(Integer.class), any(Object[].class)))
                    .thenReturn(6);
            when(jdbcTemplate.queryForObject(
                    org.mockito.ArgumentMatchers.contains("FROM goals"),
                    eq(Integer.class), any(Object[].class)))
                    .thenReturn(1);
            when(jdbcTemplate.queryForObject(
                    org.mockito.ArgumentMatchers.contains("activity_logs"),
                    eq(Integer.class), any(Object[].class)))
                    .thenReturn(3);
            when(jdbcTemplate.queryForObject(
                    org.mockito.ArgumentMatchers.contains("MAX(interview_date)"),
                    eq(String.class), any(Object[].class)))
                    .thenReturn("2025-02-01");

            String fp1 = contextBuilder.generateFingerprint(1L);
            String fp2 = contextBuilder.generateFingerprint(1L);

            assertEquals(fp1, fp2, "Fingerprints should be identical when data is unchanged");
        }
    }
}
