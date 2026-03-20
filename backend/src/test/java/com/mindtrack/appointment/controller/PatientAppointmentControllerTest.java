package com.mindtrack.appointment.controller;

import com.mindtrack.appointment.dto.AppointmentResponse;
import com.mindtrack.appointment.model.AppointmentStatus;
import com.mindtrack.appointment.service.AppointmentService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class PatientAppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;

    private static UsernamePasswordAuthenticationToken patientAuth() {
        return new UsernamePasswordAuthenticationToken(
                10L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldListPatientAppointments() throws Exception {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(1L);
        response.setTherapistName("Dr. Lane");
        response.setStatus(AppointmentStatus.SCHEDULED);
        when(appointmentService.listPatientAppointments(10L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/patient/appointments")
                        .with(authentication(patientAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].therapistName").value("Dr. Lane"));
    }

    @Test
    void shouldCancelPatientAppointment() throws Exception {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(1L);
        response.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentService.cancelAppointmentAsPatient(10L, 1L)).thenReturn(response);

        mockMvc.perform(patch("/api/patient/appointments/1/cancel")
                        .with(authentication(patientAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
