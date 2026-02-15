package com.mindtrack.goals.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.goals.dto.GoalRequest;
import com.mindtrack.goals.dto.GoalResponse;
import com.mindtrack.goals.dto.MilestoneRequest;
import com.mindtrack.goals.dto.MilestoneResponse;
import com.mindtrack.goals.model.GoalStatus;
import com.mindtrack.goals.service.GoalService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GoalService goalService;

    private static UsernamePasswordAuthenticationToken mockAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldCreateGoal() throws Exception {
        GoalRequest request = createGoalRequest();
        GoalResponse response = createGoalResponse(1L);
        when(goalService.create(eq(1L), any(GoalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/goals")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Learn guitar"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void shouldRejectCreateWithoutTitle() throws Exception {
        GoalRequest request = new GoalRequest();

        mockMvc.perform(post("/api/goals")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListGoals() throws Exception {
        GoalResponse r1 = createGoalResponse(1L);
        GoalResponse r2 = createGoalResponse(2L);
        when(goalService.listByUser(1L, null)).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/goals")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldListGoalsByStatus() throws Exception {
        GoalResponse r1 = createGoalResponse(1L);
        when(goalService.listByUser(1L, GoalStatus.IN_PROGRESS))
                .thenReturn(List.of(r1));

        mockMvc.perform(get("/api/goals")
                        .param("status", "IN_PROGRESS")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldGetGoalById() throws Exception {
        GoalResponse response = createGoalResponse(1L);
        when(goalService.getByIdAndUser(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/goals/1")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Learn guitar"));
    }

    @Test
    void shouldReturn404WhenGoalNotFound() throws Exception {
        when(goalService.getByIdAndUser(999L, 1L)).thenReturn(null);

        mockMvc.perform(get("/api/goals/999")
                        .with(authentication(mockAuth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateGoal() throws Exception {
        GoalRequest request = createGoalRequest();
        request.setTitle("Updated");
        GoalResponse response = createGoalResponse(1L);
        response.setTitle("Updated");
        when(goalService.update(eq(1L), eq(1L), any(GoalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/goals/1")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void shouldUpdateGoalStatus() throws Exception {
        GoalResponse response = createGoalResponse(1L);
        response.setStatus(GoalStatus.COMPLETED);
        when(goalService.updateStatus(1L, 1L, GoalStatus.COMPLETED))
                .thenReturn(response);

        mockMvc.perform(patch("/api/goals/1/status")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("status", "COMPLETED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldRejectInvalidStatus() throws Exception {
        mockMvc.perform(patch("/api/goals/1/status")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("status", "INVALID"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteGoal() throws Exception {
        when(goalService.delete(1L, 1L)).thenReturn(true);

        mockMvc.perform(delete("/api/goals/1")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistent() throws Exception {
        when(goalService.delete(999L, 1L)).thenReturn(false);

        mockMvc.perform(delete("/api/goals/999")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAddMilestone() throws Exception {
        MilestoneRequest request = new MilestoneRequest();
        request.setTitle("Learn chords");
        MilestoneResponse response = createMilestoneResponse(10L);
        when(goalService.addMilestone(eq(1L), eq(1L), any(MilestoneRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/goals/1/milestones")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Learn chords"));
    }

    @Test
    void shouldToggleMilestone() throws Exception {
        MilestoneResponse response = createMilestoneResponse(10L);
        response.setCompleted(true);
        when(goalService.toggleMilestoneCompletion(1L, 1L, 10L))
                .thenReturn(response);

        mockMvc.perform(patch("/api/goals/1/milestones/10/toggle")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void shouldDeleteMilestone() throws Exception {
        when(goalService.deleteMilestone(1L, 1L, 10L)).thenReturn(true);

        mockMvc.perform(delete("/api/goals/1/milestones/10")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn401WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isUnauthorized());
    }

    private GoalRequest createGoalRequest() {
        GoalRequest request = new GoalRequest();
        request.setTitle("Learn guitar");
        request.setDescription("Practice daily");
        request.setCategory("Personal");
        request.setTargetDate(LocalDate.of(2025, 12, 31));
        return request;
    }

    private GoalResponse createGoalResponse(Long id) {
        GoalResponse response = new GoalResponse();
        response.setId(id);
        response.setTitle("Learn guitar");
        response.setDescription("Practice daily");
        response.setCategory("Personal");
        response.setTargetDate(LocalDate.of(2025, 12, 31));
        response.setStatus(GoalStatus.IN_PROGRESS);
        response.setTotalMilestones(0);
        response.setCompletedMilestones(0);
        response.setMilestones(Collections.emptyList());
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }

    private MilestoneResponse createMilestoneResponse(Long id) {
        MilestoneResponse response = new MilestoneResponse();
        response.setId(id);
        response.setGoalId(1L);
        response.setTitle("Learn chords");
        response.setTargetDate(LocalDate.of(2025, 3, 31));
        response.setCompleted(false);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }
}
