package com.mindtrack.therapist.controller;

import com.mindtrack.therapist.dto.TherapistRequestResponse;
import com.mindtrack.therapist.service.PatientService;
import java.time.LocalDateTime;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    private static UsernamePasswordAuthenticationToken userAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private static TherapistRequestResponse makeRequest(Long id, String status) {
        TherapistRequestResponse r = new TherapistRequestResponse();
        r.setRelationshipId(id);
        r.setTherapistId(10L);
        r.setTherapistName("Dr. Smith");
        r.setTherapistEmail("drsmith@example.com");
        r.setStatus(status);
        r.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        return r;
    }

    @Test
    void shouldListRequests() throws Exception {
        when(patientService.getRequests(1L)).thenReturn(List.of(
                makeRequest(1L, "PENDING"),
                makeRequest(2L, "ACTIVE")
        ));

        mockMvc.perform(get("/api/patient/requests")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].therapistName").value("Dr. Smith"))
                .andExpect(jsonPath("$[1].status").value("ACTIVE"));
    }

    @Test
    void shouldAcceptRequest() throws Exception {
        doNothing().when(patientService).acceptRequest(5L, 1L);

        mockMvc.perform(post("/api/patient/requests/5/accept")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectRequest() throws Exception {
        doNothing().when(patientService).rejectRequest(5L, 1L);

        mockMvc.perform(post("/api/patient/requests/5/reject")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn401ForUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/patient/requests"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400WhenAcceptingNonPendingRequest() throws Exception {
        doThrow(new IllegalArgumentException("Therapist request is not in PENDING status: ACTIVE"))
                .when(patientService).acceptRequest(5L, 1L);

        mockMvc.perform(post("/api/patient/requests/5/accept")
                        .with(authentication(userAuth())))
                .andExpect(status().isBadRequest());
    }
}
