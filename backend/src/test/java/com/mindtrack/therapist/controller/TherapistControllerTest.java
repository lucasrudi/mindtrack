package com.mindtrack.therapist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.goals.dto.GoalRequest;
import com.mindtrack.goals.dto.GoalResponse;
import com.mindtrack.goals.model.GoalValidationStatus;
import com.mindtrack.therapist.dto.PatientDetailResponse;
import com.mindtrack.therapist.dto.PatientSummaryResponse;
import com.mindtrack.therapist.service.TherapistService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class TherapistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TherapistService therapistService;

    private static UsernamePasswordAuthenticationToken therapistAuth() {
        return new UsernamePasswordAuthenticationToken(
                3L, null, List.of(new SimpleGrantedAuthority("ROLE_THERAPIST")));
    }

    private static UsernamePasswordAuthenticationToken userAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private static UsernamePasswordAuthenticationToken adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                2L, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void shouldListPatients() throws Exception {
        PatientSummaryResponse patient = new PatientSummaryResponse(
                1L, "John Patient", "john@example.com", 5, 3, 8,
                LocalDateTime.of(2025, 1, 15, 10, 0));
        when(therapistService.listPatients(3L)).thenReturn(List.of(patient));

        mockMvc.perform(get("/api/therapist/patients")
                        .with(authentication(therapistAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Patient"))
                .andExpect(jsonPath("$[0].interviewCount").value(5));
    }

    @Test
    void shouldGetPatientDetail() throws Exception {
        PatientDetailResponse detail = new PatientDetailResponse(
                1L, "John Patient", "john@example.com",
                List.of(), List.of(), List.of(), List.of());
        when(therapistService.getPatientDetail(3L, 1L)).thenReturn(detail);

        mockMvc.perform(get("/api/therapist/patients/1")
                        .with(authentication(therapistAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.patientName").value("John Patient"));
    }

    @Test
    void shouldGetPatientInterviews() throws Exception {
        when(therapistService.getPatientInterviews(3L, 1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/therapist/patients/1/interviews")
                        .with(authentication(therapistAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldGetPatientActivities() throws Exception {
        when(therapistService.getPatientActivities(3L, 1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/therapist/patients/1/activities")
                        .with(authentication(therapistAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldGetPatientGoals() throws Exception {
        when(therapistService.getPatientGoals(3L, 1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/therapist/patients/1/goals")
                        .with(authentication(therapistAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldGetPatientSharedJournal() throws Exception {
        when(therapistService.getPatientSharedJournal(3L, 1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/therapist/patients/1/journal")
                        .with(authentication(therapistAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldRejectNonTherapistUser() throws Exception {
        mockMvc.perform(get("/api/therapist/patients")
                        .with(authentication(userAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectAdminUser() throws Exception {
        mockMvc.perform(get("/api/therapist/patients")
                        .with(authentication(adminAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreateGoalForPatient() throws Exception {
        GoalRequest request = new GoalRequest();
        request.setTitle("New therapy goal");
        GoalResponse response = new GoalResponse();
        response.setId(5L);
        response.setTitle("New therapy goal");
        response.setValidationStatus(GoalValidationStatus.VALIDATED);

        when(therapistService.createGoalForPatient(eq(3L), eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/therapist/patients/1/goals")
                        .with(authentication(therapistAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.validationStatus").value("VALIDATED"));
    }

    @Test
    void shouldValidateGoalForPatient() throws Exception {
        GoalResponse response = new GoalResponse();
        response.setId(10L);
        response.setValidationStatus(GoalValidationStatus.VALIDATED);
        when(therapistService.validateGoal(3L, 1L, 10L)).thenReturn(response);

        mockMvc.perform(post("/api/therapist/patients/1/goals/10/validate")
                        .with(authentication(therapistAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validationStatus").value("VALIDATED"));
    }

    @Test
    void shouldRejectGoalForPatient() throws Exception {
        GoalResponse response = new GoalResponse();
        response.setId(10L);
        response.setValidationStatus(GoalValidationStatus.REJECTED);
        when(therapistService.rejectGoal(3L, 1L, 10L)).thenReturn(response);

        mockMvc.perform(post("/api/therapist/patients/1/goals/10/reject")
                        .with(authentication(therapistAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validationStatus").value("REJECTED"));
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/therapist/patients"))
                .andExpect(status().isUnauthorized());
    }
}
