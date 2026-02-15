package com.mindtrack.activity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.activity.dto.ActivityLogRequest;
import com.mindtrack.activity.dto.ActivityLogResponse;
import com.mindtrack.activity.dto.ActivityRequest;
import com.mindtrack.activity.dto.ActivityResponse;
import com.mindtrack.activity.dto.DailyChecklistResponse;
import com.mindtrack.activity.model.ActivityType;
import com.mindtrack.activity.service.ActivityService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ActivityService activityService;

    private static UsernamePasswordAuthenticationToken mockAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldCreateActivity() throws Exception {
        ActivityRequest request = createRequest();
        ActivityResponse response = createResponse(1L);
        when(activityService.create(eq(1L), any(ActivityRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/activities")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("EXERCISE"))
                .andExpect(jsonPath("$.name").value("Morning jog"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldRejectCreateWithoutName() throws Exception {
        ActivityRequest request = new ActivityRequest();
        request.setType(ActivityType.EXERCISE);

        mockMvc.perform(post("/api/activities")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateWithoutType() throws Exception {
        ActivityRequest request = new ActivityRequest();
        request.setName("Morning jog");

        mockMvc.perform(post("/api/activities")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListActivities() throws Exception {
        ActivityResponse r1 = createResponse(1L);
        ActivityResponse r2 = createResponse(2L);
        r2.setName("Meditation");
        when(activityService.listByUser(1L, null)).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/activities")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldListActiveActivitiesOnly() throws Exception {
        ActivityResponse r1 = createResponse(1L);
        when(activityService.listByUser(1L, true)).thenReturn(List.of(r1));

        mockMvc.perform(get("/api/activities").param("active", "true")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldGetActivityById() throws Exception {
        ActivityResponse response = createResponse(1L);
        when(activityService.getByIdAndUser(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/activities/1")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Morning jog"));
    }

    @Test
    void shouldReturn404WhenActivityNotFound() throws Exception {
        when(activityService.getByIdAndUser(999L, 1L)).thenReturn(null);

        mockMvc.perform(get("/api/activities/999")
                        .with(authentication(mockAuth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateActivity() throws Exception {
        ActivityRequest request = createRequest();
        ActivityResponse response = createResponse(1L);
        response.setName("Evening jog");
        when(activityService.update(eq(1L), eq(1L), any(ActivityRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/activities/1")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Evening jog"));
    }

    @Test
    void shouldToggleActive() throws Exception {
        ActivityResponse response = createResponse(1L);
        response.setActive(false);
        when(activityService.toggleActive(1L, 1L)).thenReturn(response);

        mockMvc.perform(patch("/api/activities/1/toggle")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void shouldDeleteActivity() throws Exception {
        when(activityService.delete(1L, 1L)).thenReturn(true);

        mockMvc.perform(delete("/api/activities/1")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistent() throws Exception {
        when(activityService.delete(999L, 1L)).thenReturn(false);

        mockMvc.perform(delete("/api/activities/999")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldLogActivity() throws Exception {
        ActivityLogRequest logRequest = new ActivityLogRequest();
        logRequest.setLogDate(LocalDate.of(2025, 1, 15));
        logRequest.setCompleted(true);
        logRequest.setMoodRating(8);

        ActivityLogResponse logResponse = createLogResponse(10L);
        when(activityService.logActivity(eq(1L), eq(1L), any(ActivityLogRequest.class)))
                .thenReturn(logResponse);

        mockMvc.perform(post("/api/activities/1/logs")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.moodRating").value(8));
    }

    @Test
    void shouldListLogs() throws Exception {
        ActivityLogResponse log = createLogResponse(10L);
        when(activityService.listLogs(1L, 1L)).thenReturn(List.of(log));

        mockMvc.perform(get("/api/activities/1/logs")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldGetDailyChecklist() throws Exception {
        DailyChecklistResponse item = new DailyChecklistResponse();
        item.setActivityId(1L);
        item.setActivityName("Morning jog");
        item.setActivityType("EXERCISE");
        item.setDate(LocalDate.of(2025, 1, 15));
        item.setCompleted(true);

        when(activityService.getDailyChecklist(1L, LocalDate.of(2025, 1, 15)))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/activities/checklist")
                        .param("date", "2025-01-15")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].activityName").value("Morning jog"))
                .andExpect(jsonPath("$[0].completed").value(true));
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isUnauthorized());
    }

    private ActivityRequest createRequest() {
        ActivityRequest request = new ActivityRequest();
        request.setType(ActivityType.EXERCISE);
        request.setName("Morning jog");
        request.setDescription("30 minutes");
        request.setFrequency("Daily");
        return request;
    }

    private ActivityResponse createResponse(Long id) {
        ActivityResponse response = new ActivityResponse();
        response.setId(id);
        response.setType(ActivityType.EXERCISE);
        response.setName("Morning jog");
        response.setDescription("30 minutes");
        response.setFrequency("Daily");
        response.setActive(true);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    private ActivityLogResponse createLogResponse(Long id) {
        ActivityLogResponse response = new ActivityLogResponse();
        response.setId(id);
        response.setActivityId(1L);
        response.setActivityName("Morning jog");
        response.setLogDate(LocalDate.of(2025, 1, 15));
        response.setCompleted(true);
        response.setMoodRating(8);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }
}
