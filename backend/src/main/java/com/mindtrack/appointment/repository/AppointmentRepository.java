package com.mindtrack.appointment.repository;

import com.mindtrack.appointment.model.Appointment;
import com.mindtrack.appointment.model.AppointmentStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for therapist appointment persistence.
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByTherapistIdOrderByStartAtAsc(Long therapistId);

    List<Appointment> findByPatientIdOrderByStartAtAsc(Long patientId);

    List<Appointment> findByTherapistIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            Long therapistId,
            Collection<AppointmentStatus> status,
            LocalDateTime endAt,
            LocalDateTime startAt);

    List<Appointment> findByPatientIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            Long patientId,
            Collection<AppointmentStatus> status,
            LocalDateTime endAt,
            LocalDateTime startAt);
}
