package com.mindtrack.appointment.controller;

import com.mindtrack.appointment.dto.AppointmentRequest;
import com.mindtrack.appointment.dto.AppointmentResponse;
import com.mindtrack.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Therapist appointment calendar and booking API.
 */
@RestController
@RequestMapping("/api/therapist")
@PreAuthorize("hasRole('THERAPIST')")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Lists all appointments for the authenticated therapist.
     */
    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponse>> listAppointments(Authentication authentication) {
        Long therapistId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(appointmentService.listAppointments(therapistId));
    }

    /**
     * Books a new appointment for a therapist-patient pair.
     */
    @PostMapping("/patients/{patientId}/appointments")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @PathVariable Long patientId,
            @RequestBody @Valid AppointmentRequest request,
            Authentication authentication) {
        Long therapistId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.bookAppointment(therapistId, patientId, request));
    }
}
