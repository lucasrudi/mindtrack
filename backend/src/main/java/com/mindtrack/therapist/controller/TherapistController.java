package com.mindtrack.therapist.controller;

import com.mindtrack.activity.dto.ActivityResponse;
import com.mindtrack.goals.dto.GoalResponse;
import com.mindtrack.interview.dto.InterviewResponse;
import com.mindtrack.journal.dto.JournalEntryResponse;
import com.mindtrack.therapist.dto.PatientDetailResponse;
import com.mindtrack.therapist.dto.PatientSummaryResponse;
import com.mindtrack.therapist.service.TherapistService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for therapist read-only patient data access.
 *
 * <p>All endpoints require THERAPIST role.
 */
@RestController
@RequestMapping("/api/therapist")
@PreAuthorize("hasRole('THERAPIST')")
public class TherapistController {

    private final TherapistService therapistService;

    public TherapistController(TherapistService therapistService) {
        this.therapistService = therapistService;
    }

    /**
     * Lists all active patients assigned to the therapist.
     */
    @GetMapping("/patients")
    public ResponseEntity<List<PatientSummaryResponse>> listPatients(
            Authentication authentication) {
        Long therapistId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(therapistService.listPatients(therapistId));
    }

    /**
     * Gets detailed data for a specific patient.
     */
    @GetMapping("/patients/{patientId}")
    public ResponseEntity<PatientDetailResponse> getPatientDetail(
            @PathVariable Long patientId, Authentication authentication) {
        Long therapistId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(therapistService.getPatientDetail(therapistId, patientId));
    }

    /**
     * Gets patient's interviews (read-only).
     */
    @GetMapping("/patients/{patientId}/interviews")
    public ResponseEntity<List<InterviewResponse>> getPatientInterviews(
            @PathVariable Long patientId, Authentication authentication) {
        Long therapistId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(therapistService.getPatientInterviews(therapistId, patientId));
    }

    /**
     * Gets patient's activities (read-only).
     */
    @GetMapping("/patients/{patientId}/activities")
    public ResponseEntity<List<ActivityResponse>> getPatientActivities(
            @PathVariable Long patientId, Authentication authentication) {
        Long therapistId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(therapistService.getPatientActivities(therapistId, patientId));
    }

    /**
     * Gets patient's goals (read-only).
     */
    @GetMapping("/patients/{patientId}/goals")
    public ResponseEntity<List<GoalResponse>> getPatientGoals(
            @PathVariable Long patientId, Authentication authentication) {
        Long therapistId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(therapistService.getPatientGoals(therapistId, patientId));
    }

    /**
     * Gets patient's shared journal entries.
     */
    @GetMapping("/patients/{patientId}/journal")
    public ResponseEntity<List<JournalEntryResponse>> getPatientSharedJournal(
            @PathVariable Long patientId, Authentication authentication) {
        Long therapistId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(therapistService.getPatientSharedJournal(therapistId, patientId));
    }
}
