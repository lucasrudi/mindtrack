package com.mindtrack.analytics.controller;

import com.mindtrack.analytics.dto.ActivityStatsResponse;
import com.mindtrack.analytics.dto.DashboardSummaryResponse;
import com.mindtrack.analytics.dto.GoalProgressResponse;
import com.mindtrack.analytics.dto.MoodTrendResponse;
import com.mindtrack.analytics.service.AnalyticsService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    private static UsernamePasswordAuthenticationToken mockAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldReturnSummary() throws Exception {
        DashboardSummaryResponse summary = new DashboardSummaryResponse();
        summary.setTotalJournalEntries(5);
        summary.setAverageMood(7.5);
        summary.setTotalActivitiesLogged(10);
        summary.setActivityCompletionRate(80.0);
        summary.setTotalGoals(3);
        summary.setCompletedGoals(1);
        summary.setActiveGoals(2);

        when(analyticsService.getDashboardSummary(eq(1L), any(), any()))
                .thenReturn(summary);

        mockMvc.perform(get("/api/analytics/summary")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalJournalEntries").value(5))
                .andExpect(jsonPath("$.averageMood").value(7.5))
                .andExpect(jsonPath("$.totalActivitiesLogged").value(10))
                .andExpect(jsonPath("$.activityCompletionRate").value(80.0))
                .andExpect(jsonPath("$.totalGoals").value(3))
                .andExpect(jsonPath("$.completedGoals").value(1))
                .andExpect(jsonPath("$.activeGoals").value(2));
    }

    @Test
    void shouldReturnSummaryWithDateRange() throws Exception {
        DashboardSummaryResponse summary = new DashboardSummaryResponse();
        summary.setTotalJournalEntries(2);
        summary.setAverageMood(6.0);

        when(analyticsService.getDashboardSummary(eq(1L),
                eq(LocalDate.of(2025, 1, 1)), eq(LocalDate.of(2025, 1, 31))))
                .thenReturn(summary);

        mockMvc.perform(get("/api/analytics/summary")
                        .param("from", "2025-01-01")
                        .param("to", "2025-01-31")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalJournalEntries").value(2));
    }

    @Test
    void shouldReturnMoodTrends() throws Exception {
        List<MoodTrendResponse> trends = List.of(
                new MoodTrendResponse(LocalDate.of(2025, 1, 5), 7.0, 2),
                new MoodTrendResponse(LocalDate.of(2025, 1, 10), 8.5, 1));

        when(analyticsService.getMoodTrends(eq(1L), any(), any()))
                .thenReturn(trends);

        mockMvc.perform(get("/api/analytics/mood-trends")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].date").value("2025-01-05"))
                .andExpect(jsonPath("$[0].averageMood").value(7.0))
                .andExpect(jsonPath("$[0].entryCount").value(2));
    }

    @Test
    void shouldReturnActivityStats() throws Exception {
        List<ActivityStatsResponse> stats = List.of(
                new ActivityStatsResponse("EXERCISE", 5, 4, 80.0),
                new ActivityStatsResponse("MEDITATION", 3, 3, 100.0));

        when(analyticsService.getActivityStats(eq(1L), any(), any()))
                .thenReturn(stats);

        mockMvc.perform(get("/api/analytics/activity-stats")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].activityType").value("EXERCISE"))
                .andExpect(jsonPath("$[0].completionRate").value(80.0));
    }

    @Test
    void shouldReturnGoalProgress() throws Exception {
        List<GoalProgressResponse> progress = List.of(
                new GoalProgressResponse("IN_PROGRESS", 2),
                new GoalProgressResponse("COMPLETED", 1));

        when(analyticsService.getGoalProgress(eq(1L)))
                .thenReturn(progress);

        mockMvc.perform(get("/api/analytics/goal-progress")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].count").value(2));
    }

    @Test
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/analytics/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForMoodTrendsWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/analytics/mood-trends"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForGoalProgressWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/analytics/goal-progress"))
                .andExpect(status().isUnauthorized());
    }
}
