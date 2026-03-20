package com.mindtrack.therapist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.therapist.dto.InviteGenerateResponse;
import com.mindtrack.therapist.dto.InvitePreviewResponse;
import com.mindtrack.therapist.dto.PatientRequestCreateRequest;
import com.mindtrack.therapist.service.InviteService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class InviteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InviteService inviteService;

    private static UsernamePasswordAuthenticationToken therapistAuth() {
        return new UsernamePasswordAuthenticationToken(
                3L, null, List.of(new SimpleGrantedAuthority("ROLE_THERAPIST")));
    }

    private static UsernamePasswordAuthenticationToken patientAuth() {
        return new UsernamePasswordAuthenticationToken(
                4L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void generatesTherapistRequestForPatientEmail() throws Exception {
        when(inviteService.requestPatient(eq(3L), eq("patient@test.com")))
                .thenReturn(new InviteGenerateResponse("tok", "http://localhost:3000/invite/tok"));

        PatientRequestCreateRequest request = new PatientRequestCreateRequest();
        request.setPatientEmail("patient@test.com");

        mockMvc.perform(post("/api/invites/request")
                        .with(authentication(therapistAuth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("tok"));
    }

    @Test
    void rejectsNonTherapistRequestCreation() throws Exception {
        PatientRequestCreateRequest request = new PatientRequestCreateRequest();
        request.setPatientEmail("patient@test.com");

        mockMvc.perform(post("/api/invites/request")
                        .with(authentication(patientAuth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsInviteForAuthenticatedUser() throws Exception {
        mockMvc.perform(post("/api/invites/token/reject")
                        .with(authentication(patientAuth()))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void previewsInviteWithStatus() throws Exception {
        when(inviteService.previewInvite("abc"))
                .thenReturn(new InvitePreviewResponse("Dr Smith", "THERAPIST", "PENDING"));

        mockMvc.perform(get("/api/invites/abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
