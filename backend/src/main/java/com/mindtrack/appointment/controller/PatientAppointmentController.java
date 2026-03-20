package com.mindtrack.appointment.controller;

import com.mindtrack.appointment.dto.AppointmentResponse;
import com.mindtrack.appointment.service.AppointmentService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Patient appointment access and cancellation API.
 */
@RestController
@RequestMapping("/api/patient")
public class PatientAppointmentController {

    private final AppointmentService appointmentService;

    public PatientAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Lists all appointments for the authenticated patient.
     */
    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponse>> listAppointments(Authentication authentication) {
        Long patientId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(appointmentService.listPatientAppointments(patientId));
    }

    /**
     * Cancels one of the authenticated patient's appointments.
     */
    @PatchMapping("/appointments/{appointmentId}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(@PathVariable Long appointmentId,
                                                                 Authentication authentication) {
        Long patientId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(appointmentService.cancelAppointmentAsPatient(patientId, appointmentId));
    }
}
