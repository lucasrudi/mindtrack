package com.mindtrack.analytics.controller;

import com.mindtrack.analytics.dto.ActivityStatsResponse;
import com.mindtrack.analytics.dto.DashboardSummaryResponse;
import com.mindtrack.analytics.dto.GoalProgressResponse;
import com.mindtrack.analytics.dto.MoodTrendResponse;
import com.mindtrack.analytics.service.AnalyticsService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for analytics and dashboard data.
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private static final int DEFAULT_DAYS = 30;

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Returns a dashboard summary with key metrics.
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        LocalDate[] range = resolveRange(from, to);
        return ResponseEntity.ok(
                analyticsService.getDashboardSummary(userId, range[0], range[1]));
    }

    /**
     * Returns mood trend data points for chart visualization.
     */
    @GetMapping("/mood-trends")
    public ResponseEntity<List<MoodTrendResponse>> getMoodTrends(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        LocalDate[] range = resolveRange(from, to);
        return ResponseEntity.ok(
                analyticsService.getMoodTrends(userId, range[0], range[1]));
    }

    /**
     * Returns activity completion statistics by type.
     */
    @GetMapping("/activity-stats")
    public ResponseEntity<List<ActivityStatsResponse>> getActivityStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        LocalDate[] range = resolveRange(from, to);
        return ResponseEntity.ok(
                analyticsService.getActivityStats(userId, range[0], range[1]));
    }

    /**
     * Returns goal progress counts by status.
     */
    @GetMapping("/goal-progress")
    public ResponseEntity<List<GoalProgressResponse>> getGoalProgress(
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(analyticsService.getGoalProgress(userId));
    }

    /**
     * Resolves date range with defaults (last 30 days).
     */
    private LocalDate[] resolveRange(LocalDate from, LocalDate to) {
        LocalDate resolvedTo = (to != null) ? to : LocalDate.now();
        LocalDate resolvedFrom = (from != null) ? from : resolvedTo.minusDays(DEFAULT_DAYS);
        return new LocalDate[]{resolvedFrom, resolvedTo};
    }
}
