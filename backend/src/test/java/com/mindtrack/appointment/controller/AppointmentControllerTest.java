package com.mindtrack.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.appointment.dto.AppointmentRequest;
import com.mindtrack.appointment.dto.AppointmentResponse;
import com.mindtrack.appointment.model.AppointmentStatus;
import com.mindtrack.appointment.service.AppointmentService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentService appointmentService;

    private static UsernamePasswordAuthenticationToken therapistAuth() {
        return new UsernamePasswordAuthenticationToken(
                3L, null, List.of(new SimpleGrantedAuthority("ROLE_THERAPIST")));
    }

    @Test
    void shouldListAppointments() throws Exception {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(1L);
        response.setPatientName("Patient One");
        response.setStatus(AppointmentStatus.SCHEDULED);
        when(appointmentService.listAppointments(3L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/therapist/appointments")
                        .with(authentication(therapistAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].patientName").value("Patient One"));
    }

    @Test
    void shouldBookAppointment() throws Exception {
        AppointmentRequest request = new AppointmentRequest();
        request.setStartAt(LocalDateTime.of(2026, 4, 20, 10, 0));
        request.setEndAt(LocalDateTime.of(2026, 4, 20, 10, 50));
        request.setReason("Weekly check-in");

        AppointmentResponse response = new AppointmentResponse();
        response.setId(5L);
        response.setStatus(AppointmentStatus.SCHEDULED);
        when(appointmentService.bookAppointment(eq(3L), eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/therapist/patients/1/appointments")
                        .with(authentication(therapistAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }
}
