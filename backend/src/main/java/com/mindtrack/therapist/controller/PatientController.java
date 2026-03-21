package com.mindtrack.therapist.controller;

import com.mindtrack.therapist.dto.TherapistRequestResponse;
import com.mindtrack.therapist.service.PatientService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for patient-side therapist connection management.
 *
 * <p>All endpoints require authentication. Ownership is validated in the service layer.
 */
@RestController
@RequestMapping("/api/patient/requests")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * Lists all pending and active therapist connection requests for the authenticated patient.
     */
    @GetMapping
    public ResponseEntity<List<TherapistRequestResponse>> getRequests(
            Authentication authentication) {
        Long patientId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(patientService.getRequests(patientId));
    }

    /**
     * Accepts a pending therapist connection request.
     */
    @PostMapping("/{id}/accept")
    public ResponseEntity<Void> acceptRequest(
            @PathVariable Long id, Authentication authentication) {
        Long patientId = (Long) authentication.getPrincipal();
        patientService.acceptRequest(id, patientId);
        return ResponseEntity.ok().build();
    }

    /**
     * Rejects a pending therapist connection request.
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectRequest(
            @PathVariable Long id, Authentication authentication) {
        Long patientId = (Long) authentication.getPrincipal();
        patientService.rejectRequest(id, patientId);
        return ResponseEntity.ok().build();
    }
}
