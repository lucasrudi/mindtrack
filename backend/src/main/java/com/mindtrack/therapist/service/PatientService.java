package com.mindtrack.therapist.service;

import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.User;
import com.mindtrack.therapist.dto.TherapistRequestResponse;
import com.mindtrack.therapist.model.TherapistPatient;
import com.mindtrack.therapist.model.TherapistPatientStatus;
import com.mindtrack.therapist.repository.TherapistPatientRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for patient-side therapist connection operations.
 */
@Service
public class PatientService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientService.class);
    private static final List<TherapistPatientStatus> VISIBLE_STATUSES =
            List.of(TherapistPatientStatus.PENDING, TherapistPatientStatus.ACTIVE);

    private final TherapistPatientRepository therapistPatientRepository;
    private final UserRepository userRepository;

    public PatientService(TherapistPatientRepository therapistPatientRepository,
                          UserRepository userRepository) {
        this.therapistPatientRepository = therapistPatientRepository;
        this.userRepository = userRepository;
    }

    /**
     * Returns all PENDING and ACTIVE therapist connection requests for the patient.
     * EXPIRED and INACTIVE records are excluded.
     */
    public List<TherapistRequestResponse> getRequests(Long patientId) {
        return therapistPatientRepository
                .findByPatientIdAndStatusIn(patientId, VISIBLE_STATUSES)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Accepts a PENDING therapist connection request, transitioning it to ACTIVE.
     *
     * @throws IllegalArgumentException if the relationship is not found, not owned by this
     *     patient, or not in PENDING status
     */
    @Transactional
    public void acceptRequest(Long relationshipId, Long patientId) {
        TherapistPatient relationship = findOwnedPendingRelationship(relationshipId, patientId);
        relationship.setStatus(TherapistPatientStatus.ACTIVE);
        therapistPatientRepository.save(relationship);
        LOG.info("Patient {} accepted therapist request {}", patientId, relationshipId);
    }

    /**
     * Rejects a PENDING therapist connection request, transitioning it to INACTIVE.
     *
     * @throws IllegalArgumentException if the relationship is not found, not owned by this
     *     patient, or not in PENDING status
     */
    @Transactional
    public void rejectRequest(Long relationshipId, Long patientId) {
        TherapistPatient relationship = findOwnedPendingRelationship(relationshipId, patientId);
        relationship.setStatus(TherapistPatientStatus.INACTIVE);
        therapistPatientRepository.save(relationship);
        LOG.info("Patient {} rejected therapist request {}", patientId, relationshipId);
    }

    private TherapistPatient findOwnedPendingRelationship(Long relationshipId, Long patientId) {
        TherapistPatient relationship = therapistPatientRepository.findById(relationshipId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Therapist request not found: " + relationshipId));
        if (!relationship.getPatientId().equals(patientId)) {
            throw new IllegalArgumentException("Therapist request does not belong to this patient");
        }
        if (relationship.getStatus() != TherapistPatientStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Therapist request is not in PENDING status: " + relationship.getStatus());
        }
        return relationship;
    }

    private TherapistRequestResponse toResponse(TherapistPatient relationship) {
        User therapist = userRepository.findById(relationship.getTherapistId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Therapist not found: " + relationship.getTherapistId()));
        TherapistRequestResponse response = new TherapistRequestResponse();
        response.setRelationshipId(relationship.getId());
        response.setTherapistId(therapist.getId());
        response.setTherapistName(therapist.getName());
        response.setTherapistEmail(therapist.getEmail());
        response.setStatus(relationship.getStatus().name());
        response.setCreatedAt(relationship.getCreatedAt());
        return response;
    }
}
