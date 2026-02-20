package com.mindtrack.analytics.service;

import com.mindtrack.activity.model.ActivityLog;
import com.mindtrack.activity.repository.ActivityLogRepository;
import com.mindtrack.analytics.dto.ActivityStatsResponse;
import com.mindtrack.analytics.dto.DashboardSummaryResponse;
import com.mindtrack.analytics.dto.GoalProgressResponse;
import com.mindtrack.analytics.dto.MoodTrendResponse;
import com.mindtrack.goals.model.Goal;
import com.mindtrack.goals.model.GoalStatus;
import com.mindtrack.goals.repository.GoalRepository;
import com.mindtrack.journal.model.JournalEntry;
import com.mindtrack.journal.repository.JournalEntryRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for computing analytics and dashboard metrics on-the-fly
 * from journal entries, activity logs, and goals.
 */
@Service
public class AnalyticsService {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsService.class);

    private final JournalEntryRepository journalEntryRepository;
    private final ActivityLogRepository activityLogRepository;
    private final GoalRepository goalRepository;

    public AnalyticsService(JournalEntryRepository journalEntryRepository,
                            ActivityLogRepository activityLogRepository,
                            GoalRepository goalRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.activityLogRepository = activityLogRepository;
        this.goalRepository = goalRepository;
    }

    /**
     * Computes a dashboard summary with key metrics for the date range.
     */
    public DashboardSummaryResponse getDashboardSummary(Long userId, LocalDate from,
                                                        LocalDate to) {
        LOG.info("Computing dashboard summary for user {} from {} to {}", userId, from, to);

        List<JournalEntry> entries =
                journalEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(
                        userId, from, to);

        List<ActivityLog> logs =
                activityLogRepository.findByActivity_UserIdAndLogDateBetween(userId, from, to);

        List<Goal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(userId);

        DashboardSummaryResponse summary = new DashboardSummaryResponse();

        // Journal metrics
        summary.setTotalJournalEntries(entries.size());
        summary.setAverageMood(entries.stream()
                .filter(e -> e.getMood() != null)
                .mapToInt(JournalEntry::getMood)
                .average()
                .orElse(0.0));

        // Activity metrics
        summary.setTotalActivitiesLogged(logs.size());
        long completedLogs = logs.stream().filter(ActivityLog::isCompleted).count();
        summary.setActivityCompletionRate(
                logs.isEmpty() ? 0.0 : (double) completedLogs / logs.size() * 100.0);

        // Goal metrics
        summary.setTotalGoals(goals.size());
        summary.setCompletedGoals(
                goals.stream().filter(g -> g.getStatus() == GoalStatus.COMPLETED).count());
        summary.setActiveGoals(goals.stream()
                .filter(g -> g.getStatus() == GoalStatus.IN_PROGRESS
                        || g.getStatus() == GoalStatus.NOT_STARTED)
                .count());

        return summary;
    }

    /**
     * Computes mood trends grouped by date within the range.
     */
    public List<MoodTrendResponse> getMoodTrends(Long userId, LocalDate from, LocalDate to) {
        LOG.info("Computing mood trends for user {} from {} to {}", userId, from, to);

        List<JournalEntry> entries =
                journalEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(
                        userId, from, to);

        Map<LocalDate, List<JournalEntry>> byDate = entries.stream()
                .filter(e -> e.getMood() != null)
                .collect(Collectors.groupingBy(JournalEntry::getEntryDate));

        return byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    double avgMood = entry.getValue().stream()
                            .mapToInt(JournalEntry::getMood)
                            .average()
                            .orElse(0.0);
                    return new MoodTrendResponse(
                            entry.getKey(),
                            Math.round(avgMood * 10.0) / 10.0,
                            entry.getValue().size());
                })
                .toList();
    }

    /**
     * Computes activity completion statistics grouped by activity type.
     */
    public List<ActivityStatsResponse> getActivityStats(Long userId, LocalDate from,
                                                        LocalDate to) {
        LOG.info("Computing activity stats for user {} from {} to {}", userId, from, to);

        List<ActivityLog> logs =
                activityLogRepository.findByActivity_UserIdAndLogDateBetween(userId, from, to);

        Map<String, List<ActivityLog>> byType = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getActivity().getType().name()));

        return byType.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    long total = entry.getValue().size();
                    long completed = entry.getValue().stream()
                            .filter(ActivityLog::isCompleted)
                            .count();
                    double rate = total == 0 ? 0.0 : (double) completed / total * 100.0;
                    return new ActivityStatsResponse(
                            entry.getKey(), total, completed,
                            Math.round(rate * 10.0) / 10.0);
                })
                .toList();
    }

    /**
     * Computes goal progress counts by status.
     */
    public List<GoalProgressResponse> getGoalProgress(Long userId) {
        LOG.info("Computing goal progress for user {}", userId);

        List<Goal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(userId);

        Map<GoalStatus, Long> byStatus = goals.stream()
                .collect(Collectors.groupingBy(Goal::getStatus, Collectors.counting()));

        return Arrays.stream(GoalStatus.values())
                .map(status -> new GoalProgressResponse(
                        status.name(),
                        byStatus.getOrDefault(status, 0L)))
                .filter(gp -> gp.getCount() > 0)
                .toList();
    }
}
