package com.mindtrack.therapist.repository;

import com.mindtrack.therapist.model.TherapistPatient;
import com.mindtrack.therapist.model.TherapistPatientStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for therapist-patient relationship operations.
 */
public interface TherapistPatientRepository extends JpaRepository<TherapistPatient, Long> {

    List<TherapistPatient> findByTherapistIdAndStatus(Long therapistId, TherapistPatientStatus status);

    Optional<TherapistPatient> findByTherapistIdAndPatientId(Long therapistId, Long patientId);

    boolean existsByTherapistIdAndPatientIdAndStatus(Long therapistId, Long patientId,
                                                      TherapistPatientStatus status);
}
