package com.mindtrack.interview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.interview.dto.InterviewRequest;
import com.mindtrack.interview.dto.InterviewResponse;
import com.mindtrack.interview.service.InterviewService;
import java.time.LocalDate;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class InterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InterviewService interviewService;

    private static UsernamePasswordAuthenticationToken mockAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldCreateInterview() throws Exception {
        InterviewRequest request = createRequest();
        InterviewResponse response = createResponse(1L);
        when(interviewService.create(eq(1L), any(InterviewRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/interviews")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.interviewDate").value("2025-01-15"))
                .andExpect(jsonPath("$.moodBefore").value(5))
                .andExpect(jsonPath("$.moodAfter").value(7))
                .andExpect(jsonPath("$.topics", hasSize(2)))
                .andExpect(jsonPath("$.medicationChanges").value("Increased dosage"))
                .andExpect(jsonPath("$.recommendations").value("Try meditation"))
                .andExpect(jsonPath("$.notes").value("Good session"));
    }

    @Test
    void shouldRejectCreateWithoutInterviewDate() throws Exception {
        InterviewRequest request = new InterviewRequest();
        request.setMoodBefore(5);

        mockMvc.perform(post("/api/interviews")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateWithInvalidMood() throws Exception {
        InterviewRequest request = new InterviewRequest();
        request.setInterviewDate(LocalDate.of(2025, 1, 15));
        request.setMoodBefore(11);

        mockMvc.perform(post("/api/interviews")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListInterviews() throws Exception {
        InterviewResponse response1 = createResponse(1L);
        InterviewResponse response2 = createResponse(2L);
        when(interviewService.listByUser(1L)).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/interviews")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void shouldGetInterviewById() throws Exception {
        InterviewResponse response = createResponse(1L);
        when(interviewService.getByIdAndUser(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/interviews/1")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.interviewDate").value("2025-01-15"));
    }

    @Test
    void shouldReturn404WhenInterviewNotFound() throws Exception {
        when(interviewService.getByIdAndUser(999L, 1L)).thenReturn(null);

        mockMvc.perform(get("/api/interviews/999")
                        .with(authentication(mockAuth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateInterview() throws Exception {
        InterviewRequest request = createRequest();
        InterviewResponse response = createResponse(1L);
        response.setMoodAfter(9);
        when(interviewService.update(eq(1L), eq(1L), any(InterviewRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/interviews/1")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.moodAfter").value(9));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentInterview() throws Exception {
        InterviewRequest request = createRequest();
        when(interviewService.update(eq(999L), eq(1L), any(InterviewRequest.class)))
                .thenReturn(null);

        mockMvc.perform(put("/api/interviews/999")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteInterview() throws Exception {
        when(interviewService.delete(1L, 1L)).thenReturn(true);

        mockMvc.perform(delete("/api/interviews/1")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentInterview() throws Exception {
        when(interviewService.delete(999L, 1L)).thenReturn(false);

        mockMvc.perform(delete("/api/interviews/999")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/interviews"))
                .andExpect(status().isUnauthorized());
    }

    private InterviewRequest createRequest() {
        InterviewRequest request = new InterviewRequest();
        request.setInterviewDate(LocalDate.of(2025, 1, 15));
        request.setMoodBefore(5);
        request.setMoodAfter(7);
        request.setTopics(List.of("anxiety", "sleep"));
        request.setMedicationChanges("Increased dosage");
        request.setRecommendations("Try meditation");
        request.setNotes("Good session");
        return request;
    }

    private InterviewResponse createResponse(Long id) {
        InterviewResponse response = new InterviewResponse();
        response.setId(id);
        response.setInterviewDate(LocalDate.of(2025, 1, 15));
        response.setMoodBefore(5);
        response.setMoodAfter(7);
        response.setTopics(List.of("anxiety", "sleep"));
        response.setMedicationChanges("Increased dosage");
        response.setRecommendations("Try meditation");
        response.setNotes("Good session");
        response.setHasAudio(false);
        return response;
    }
}
