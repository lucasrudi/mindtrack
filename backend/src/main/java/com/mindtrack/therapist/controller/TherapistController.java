package com.mindtrack.therapist.controller;

import com.mindtrack.activity.dto.ActivityResponse;
import com.mindtrack.goals.dto.GoalRequest;
import com.mindtrack.goals.dto.GoalResponse;
import com.mindtrack.interview.dto.InterviewResponse;
import com.mindtrack.journal.dto.JournalEntryResponse;
import com.mindtrack.therapist.dto.PatientDetailResponse;
import com.mindtrack.therapist.dto.PatientSummaryResponse;
import com.mindtrack.therapist.model.TherapistPatientStatus;
import com.mindtrack.therapist.service.TherapistService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
     * Approves a pending patient request (PENDING → ACTIVE).
     */
    @PostMapping("/patients/{patientId}/accept")
    public ResponseEntity<Void> acceptPatient(@PathVariable Long patientId,
                                              Authentication authentication) {
        Long therapistId = (Long) authentication.getPrincipal();
        therapistService.setPatientStatus(therapistId, patientId, TherapistPatientStatus.ACTIVE);
        return ResponseEntity.ok().build();
    }

    /**
     * Rejects a pending patient request (PENDING → INACTIVE).
     */
    @PostMapping("/patients/{patientId}/reject")
    public ResponseEntity<Void> rejectPatient(@PathVariable Long patientId,
                                              Authentication authentication) {
        Long therapistId = (Long) authentication.getPrincipal();
        therapistService.setPatientStatus(therapistId, patientId, TherapistPatientStatus.INACTIVE);
        return ResponseEntity.ok().build();
    }

    /**
     * Creates a goal for a patient (therapist-authored, pre-validated).
     */
    @PostMapping("/patients/{patientId}/goals")
    public ResponseEntity<GoalResponse> createGoalForPatient(
            @PathVariable Long patientId,
            @RequestBody @Valid GoalRequest request,
            Authentication authentication) {
        Long therapistId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(201)
                .body(therapistService.createGoalForPatient(therapistId, patientId, request));
    }

    /**
     * Edits a patient's goal and marks it as OVERRIDDEN.
     */
    @PutMapping("/patients/{patientId}/goals/{goalId}")
    public ResponseEntity<GoalResponse> editGoalForPatient(
            @PathVariable Long patientId,
            @PathVariable Long goalId,
            @RequestBody @Valid GoalRequest request,
            Authentication authentication) {
        Long therapistId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(
                therapistService.editGoalForPatient(therapistId, patientId, goalId, request));
    }

    /**
     * Validates a patient's goal.
     */
    @PostMapping("/patients/{patientId}/goals/{goalId}/validate")
    public ResponseEntity<GoalResponse> validateGoal(
            @PathVariable Long patientId,
            @PathVariable Long goalId,
            Authentication authentication) {
        Long therapistId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(therapistService.validateGoal(therapistId, patientId, goalId));
    }

    /**
     * Rejects a patient's goal.
     */
    @PostMapping("/patients/{patientId}/goals/{goalId}/reject")
    public ResponseEntity<GoalResponse> rejectGoal(
            @PathVariable Long patientId,
            @PathVariable Long goalId,
            Authentication authentication) {
        Long therapistId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(therapistService.rejectGoal(therapistId, patientId, goalId));
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
