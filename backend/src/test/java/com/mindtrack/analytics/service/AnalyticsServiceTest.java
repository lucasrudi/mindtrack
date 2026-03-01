package com.mindtrack.analytics.service;

import com.mindtrack.activity.model.Activity;
import com.mindtrack.activity.model.ActivityLog;
import com.mindtrack.activity.model.ActivityType;
import com.mindtrack.activity.repository.ActivityLogRepository;
import com.mindtrack.analytics.dto.ActivityStatsResponse;
import com.mindtrack.analytics.dto.DashboardSummaryResponse;
import com.mindtrack.analytics.dto.GoalProgressResponse;
import com.mindtrack.analytics.dto.MoodTrendResponse;
import com.mindtrack.goals.model.Goal;
import com.mindtrack.goals.model.GoalStatus;
import com.mindtrack.goals.model.GoalValidationStatus;
import com.mindtrack.goals.repository.GoalRepository;
import com.mindtrack.journal.model.JournalEntry;
import com.mindtrack.journal.repository.JournalEntryRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private GoalRepository goalRepository;

    private AnalyticsService analyticsService;

    private static final Long USER_ID = 1L;
    private static final LocalDate FROM = LocalDate.of(2025, 1, 1);
    private static final LocalDate TO = LocalDate.of(2025, 1, 31);

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(
                journalEntryRepository, activityLogRepository, goalRepository);
    }

    // --- Dashboard Summary ---

    @Test
    void shouldReturnSummaryWithData() {
        when(journalEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(
                eq(USER_ID), any(), any()))
                .thenReturn(List.of(
                        createJournalEntry(1L, LocalDate.of(2025, 1, 5), 7),
                        createJournalEntry(2L, LocalDate.of(2025, 1, 10), 8)));

        when(activityLogRepository.findByActivity_UserIdAndLogDateBetween(
                eq(USER_ID), any(), any()))
                .thenReturn(List.of(
                        createActivityLog(1L, true, ActivityType.EXERCISE),
                        createActivityLog(2L, true, ActivityType.MEDITATION),
                        createActivityLog(3L, false, ActivityType.EXERCISE)));

        when(goalRepository.findByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(List.of(
                        createGoal(1L, GoalStatus.IN_PROGRESS),
                        createGoal(2L, GoalStatus.COMPLETED),
                        createGoal(3L, GoalStatus.NOT_STARTED)));

        DashboardSummaryResponse result =
                analyticsService.getDashboardSummary(USER_ID, FROM, TO);

        assertNotNull(result);
        assertEquals(2, result.getTotalJournalEntries());
        assertEquals(7.5, result.getAverageMood());
        assertEquals(3, result.getTotalActivitiesLogged());
        assertEquals(66.7, Math.round(result.getActivityCompletionRate() * 10.0) / 10.0);
        assertEquals(3, result.getTotalGoals());
        assertEquals(1, result.getCompletedGoals());
        assertEquals(2, result.getActiveGoals());
    }

    @Test
    void shouldReturnEmptySummaryForNewUser() {
        when(journalEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(
                eq(USER_ID), any(), any()))
                .thenReturn(List.of());
        when(activityLogRepository.findByActivity_UserIdAndLogDateBetween(
                eq(USER_ID), any(), any()))
                .thenReturn(List.of());
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(List.of());

        DashboardSummaryResponse result =
                analyticsService.getDashboardSummary(USER_ID, FROM, TO);

        assertNotNull(result);
        assertEquals(0, result.getTotalJournalEntries());
        assertEquals(0.0, result.getAverageMood());
        assertEquals(0, result.getTotalActivitiesLogged());
        assertEquals(0.0, result.getActivityCompletionRate());
        assertEquals(0, result.getTotalGoals());
    }

    @Test
    void shouldHandleEntriesWithNullMood() {
        when(journalEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(
                eq(USER_ID), any(), any()))
                .thenReturn(List.of(
                        createJournalEntry(1L, LocalDate.of(2025, 1, 5), null),
                        createJournalEntry(2L, LocalDate.of(2025, 1, 10), 8)));
        when(activityLogRepository.findByActivity_UserIdAndLogDateBetween(
                eq(USER_ID), any(), any()))
                .thenReturn(List.of());
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(List.of());

        DashboardSummaryResponse result =
                analyticsService.getDashboardSummary(USER_ID, FROM, TO);

        assertEquals(2, result.getTotalJournalEntries());
        assertEquals(8.0, result.getAverageMood());
    }

    // --- Mood Trends ---

    @Test
    void shouldReturnMoodTrendsGroupedByDate() {
        when(journalEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(
                eq(USER_ID), any(), any()))
                .thenReturn(List.of(
                        createJournalEntry(1L, LocalDate.of(2025, 1, 5), 6),
                        createJournalEntry(2L, LocalDate.of(2025, 1, 5), 8),
                        createJournalEntry(3L, LocalDate.of(2025, 1, 10), 5)));

        List<MoodTrendResponse> result = analyticsService.getMoodTrends(USER_ID, FROM, TO);

        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2025, 1, 5), result.get(0).getDate());
        assertEquals(7.0, result.get(0).getAverageMood());
        assertEquals(2, result.get(0).getEntryCount());
        assertEquals(LocalDate.of(2025, 1, 10), result.get(1).getDate());
        assertEquals(5.0, result.get(1).getAverageMood());
        assertEquals(1, result.get(1).getEntryCount());
    }

    @Test
    void shouldReturnEmptyMoodTrendsWhenNoEntries() {
        when(journalEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(
                eq(USER_ID), any(), any()))
                .thenReturn(List.of());

        List<MoodTrendResponse> result = analyticsService.getMoodTrends(USER_ID, FROM, TO);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSkipEntriesWithNullMoodInTrends() {
        when(journalEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(
                eq(USER_ID), any(), any()))
                .thenReturn(List.of(
                        createJournalEntry(1L, LocalDate.of(2025, 1, 5), null),
                        createJournalEntry(2L, LocalDate.of(2025, 1, 10), 7)));

        List<MoodTrendResponse> result = analyticsService.getMoodTrends(USER_ID, FROM, TO);

        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2025, 1, 10), result.get(0).getDate());
    }

    // --- Activity Stats ---

    @Test
    void shouldReturnActivityStatsByType() {
        when(activityLogRepository.findByActivity_UserIdAndLogDateBetween(
                eq(USER_ID), any(), any()))
                .thenReturn(List.of(
                        createActivityLog(1L, true, ActivityType.EXERCISE),
                        createActivityLog(2L, false, ActivityType.EXERCISE),
                        createActivityLog(3L, true, ActivityType.MEDITATION)));

        List<ActivityStatsResponse> result =
                analyticsService.getActivityStats(USER_ID, FROM, TO);

        assertEquals(2, result.size());
        ActivityStatsResponse exercise = result.stream()
                .filter(s -> s.getActivityType().equals("EXERCISE")).findFirst().orElse(null);
        assertNotNull(exercise);
        assertEquals(2, exercise.getTotalLogs());
        assertEquals(1, exercise.getCompletedLogs());
        assertEquals(50.0, exercise.getCompletionRate());

        ActivityStatsResponse meditation = result.stream()
                .filter(s -> s.getActivityType().equals("MEDITATION")).findFirst().orElse(null);
        assertNotNull(meditation);
        assertEquals(1, meditation.getTotalLogs());
        assertEquals(1, meditation.getCompletedLogs());
        assertEquals(100.0, meditation.getCompletionRate());
    }

    @Test
    void shouldReturnEmptyActivityStatsWhenNoLogs() {
        when(activityLogRepository.findByActivity_UserIdAndLogDateBetween(
                eq(USER_ID), any(), any()))
                .thenReturn(List.of());

        List<ActivityStatsResponse> result =
                analyticsService.getActivityStats(USER_ID, FROM, TO);

        assertTrue(result.isEmpty());
    }

    // --- Goal Progress ---

    @Test
    void shouldReturnGoalProgressByStatus() {
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(List.of(
                        createGoal(1L, GoalStatus.IN_PROGRESS),
                        createGoal(2L, GoalStatus.COMPLETED),
                        createGoal(3L, GoalStatus.COMPLETED),
                        createGoal(4L, GoalStatus.NOT_STARTED)));

        List<GoalProgressResponse> result = analyticsService.getGoalProgress(USER_ID);

        assertEquals(3, result.size());
        GoalProgressResponse inProgress = result.stream()
                .filter(g -> g.getStatus().equals("IN_PROGRESS")).findFirst().orElse(null);
        assertNotNull(inProgress);
        assertEquals(1, inProgress.getCount());

        GoalProgressResponse completed = result.stream()
                .filter(g -> g.getStatus().equals("COMPLETED")).findFirst().orElse(null);
        assertNotNull(completed);
        assertEquals(2, completed.getCount());
    }

    @Test
    void shouldReturnEmptyGoalProgressWhenNoGoals() {
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(List.of());

        List<GoalProgressResponse> result = analyticsService.getGoalProgress(USER_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIncludeValidationCountsInDashboardSummary() {
        when(journalEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(
                eq(USER_ID), any(), any()))
                .thenReturn(List.of());
        when(activityLogRepository.findByActivity_UserIdAndLogDateBetween(
                eq(USER_ID), any(), any()))
                .thenReturn(List.of());
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(List.of());
        when(goalRepository.countByUserIdAndValidationStatus(USER_ID, GoalValidationStatus.VALIDATED))
                .thenReturn(3L);
        when(goalRepository.countByUserIdAndValidationStatus(USER_ID, GoalValidationStatus.PENDING_VALIDATION))
                .thenReturn(2L);

        DashboardSummaryResponse result = analyticsService.getDashboardSummary(USER_ID, FROM, TO);

        assertEquals(3, result.getValidatedGoals());
        assertEquals(2, result.getPendingValidationGoals());
    }

    // --- Helper methods ---

    private JournalEntry createJournalEntry(Long id, LocalDate date, Integer mood) {
        JournalEntry entry = new JournalEntry();
        entry.setId(id);
        entry.setUserId(USER_ID);
        entry.setEntryDate(date);
        entry.setMood(mood);
        entry.setContent("Test entry");
        entry.setCreatedAt(LocalDateTime.now());
        return entry;
    }

    private ActivityLog createActivityLog(Long id, boolean completed, ActivityType type) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setUserId(USER_ID);
        activity.setType(type);
        activity.setName(type.name());

        ActivityLog log = new ActivityLog();
        log.setId(id);
        log.setActivity(activity);
        log.setLogDate(LocalDate.of(2025, 1, 15));
        log.setCompleted(completed);
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }

    private Goal createGoal(Long id, GoalStatus status) {
        Goal goal = new Goal();
        goal.setId(id);
        goal.setUserId(USER_ID);
        goal.setTitle("Goal " + id);
        goal.setStatus(status);
        goal.setCreatedAt(LocalDateTime.now());
        return goal;
    }
}
