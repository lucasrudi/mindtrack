package com.mindtrack.auth;

import com.mindtrack.activity.service.ActivityService;
import com.mindtrack.admin.service.AdminService;
import com.mindtrack.ai.service.ConversationService;
import com.mindtrack.analytics.dto.DashboardSummaryResponse;
import com.mindtrack.analytics.service.AnalyticsService;
import com.mindtrack.goals.service.GoalService;
import com.mindtrack.interview.service.AudioService;
import com.mindtrack.interview.service.InterviewService;
import com.mindtrack.journal.service.JournalService;
import com.mindtrack.profile.service.ProfileService;
import com.mindtrack.therapist.service.TherapistService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cross-module security integration test.
 *
 * <p>Verifies the global security rules enforced by {@code SecurityConfig}:
 * <ul>
 *   <li>All {@code /api/**} endpoints require authentication (401 without JWT).</li>
 *   <li>Admin-only endpoints reject regular users with 403.</li>
 *   <li>Therapist-only endpoints reject regular users and admins with 403.</li>
 *   <li>Public endpoints (health, webhooks) remain accessible without authentication.</li>
 *   <li>Authenticated {@code ROLE_USER} can reach every user-facing module.</li>
 *   <li>{@code ROLE_ADMIN} can reach admin endpoints.</li>
 *   <li>{@code ROLE_THERAPIST} can reach therapist endpoints.</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@SuppressWarnings("null")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InterviewService interviewService;

    @MockitoBean
    private AudioService audioService;

    @MockitoBean
    private ActivityService activityService;

    @MockitoBean
    private GoalService goalService;

    @MockitoBean
    private JournalService journalService;

    @MockitoBean
    private AnalyticsService analyticsService;

    @MockitoBean
    private ConversationService conversationService;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private TherapistService therapistService;

    @MockitoBean
    private AdminService adminService;

    // -----------------------------------------------------------------------
    // Helper factories
    // -----------------------------------------------------------------------

    private static UsernamePasswordAuthenticationToken userAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private static UsernamePasswordAuthenticationToken adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                99L, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private static UsernamePasswordAuthenticationToken therapistAuth() {
        return new UsernamePasswordAuthenticationToken(
                50L, null, List.of(new SimpleGrantedAuthority("ROLE_THERAPIST")));
    }

    // -----------------------------------------------------------------------
    // Unauthenticated access — all /api/** must return 401
    // -----------------------------------------------------------------------

    @Test
    void unauthenticatedInterviewsReturns401() throws Exception {
        mockMvc.perform(get("/api/interviews"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedActivitiesReturns401() throws Exception {
        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedGoalsReturns401() throws Exception {
        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedJournalReturns401() throws Exception {
        mockMvc.perform(get("/api/journal"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedAnalyticsSummaryReturns401() throws Exception {
        mockMvc.perform(get("/api/analytics/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedAnalyticsMoodTrendsReturns401() throws Exception {
        mockMvc.perform(get("/api/analytics/mood-trends"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedAnalyticsActivityStatsReturns401() throws Exception {
        mockMvc.perform(get("/api/analytics/activity-stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedAnalyticsGoalProgressReturns401() throws Exception {
        mockMvc.perform(get("/api/analytics/goal-progress"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedAiConversationsReturns401() throws Exception {
        mockMvc.perform(get("/api/ai/conversations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedProfileReturns401() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedAdminUsersReturns401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedAuthMeReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedTherapistPatientsReturns401() throws Exception {
        mockMvc.perform(get("/api/therapist/patients"))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // Public endpoints — must remain accessible without authentication
    // -----------------------------------------------------------------------

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    // -----------------------------------------------------------------------
    // Admin endpoints — ROLE_USER must receive 403
    // -----------------------------------------------------------------------

    @Test
    void regularUserCannotAccessAdminUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(authentication(userAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    void regularUserCannotAccessAdminRoles() throws Exception {
        mockMvc.perform(get("/api/admin/roles")
                        .with(authentication(userAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    void regularUserCannotAccessAdminPermissions() throws Exception {
        mockMvc.perform(get("/api/admin/permissions")
                        .with(authentication(userAuth())))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------------
    // Therapist endpoints — ROLE_USER and ROLE_ADMIN must receive 403
    // -----------------------------------------------------------------------

    @Test
    void regularUserCannotAccessTherapistPatients() throws Exception {
        mockMvc.perform(get("/api/therapist/patients")
                        .with(authentication(userAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCannotAccessTherapistPatients() throws Exception {
        mockMvc.perform(get("/api/therapist/patients")
                        .with(authentication(adminAuth())))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------------
    // Authenticated ROLE_USER — must reach all user-facing modules with 200
    // -----------------------------------------------------------------------

    @Test
    void authenticatedUserCanListInterviews() throws Exception {
        when(interviewService.listByUser(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/interviews")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedUserCanListActivities() throws Exception {
        when(activityService.listByUser(1L, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/activities")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedUserCanListGoals() throws Exception {
        when(goalService.listByUser(1L, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/goals")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedUserCanListJournalEntries() throws Exception {
        when(journalService.listByUser(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/journal")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedUserCanListAiConversations() throws Exception {
        when(conversationService.listConversations(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/ai/conversations")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedUserCanGetAnalyticsSummary() throws Exception {
        when(analyticsService.getDashboardSummary(eq(1L), any(), any()))
                .thenReturn(new DashboardSummaryResponse());

        mockMvc.perform(get("/api/analytics/summary")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedUserCanGetMoodTrends() throws Exception {
        when(analyticsService.getMoodTrends(eq(1L), any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/analytics/mood-trends")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedUserCanGetActivityStats() throws Exception {
        when(analyticsService.getActivityStats(eq(1L), any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/analytics/activity-stats")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedUserCanGetGoalProgress() throws Exception {
        when(analyticsService.getGoalProgress(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/analytics/goal-progress")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk());
    }

    // -----------------------------------------------------------------------
    // ROLE_ADMIN — can reach admin-only endpoints
    // -----------------------------------------------------------------------

    @Test
    void adminUserCanAccessAdminUsers() throws Exception {
        when(adminService.listUsers(any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/admin/users")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    void adminUserCanAccessAdminRoles() throws Exception {
        when(adminService.listRolesWithPermissions()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/roles")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk());
    }

    // -----------------------------------------------------------------------
    // ROLE_THERAPIST — can reach therapist-only endpoints
    // -----------------------------------------------------------------------

    @Test
    void therapistUserCanListPatients() throws Exception {
        when(therapistService.listPatients(50L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/therapist/patients")
                        .with(authentication(therapistAuth())))
                .andExpect(status().isOk());
    }
}
