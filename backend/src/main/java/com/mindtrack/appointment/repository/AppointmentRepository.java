package com.mindtrack.appointment.repository;

import com.mindtrack.appointment.model.Appointment;
import com.mindtrack.appointment.model.AppointmentStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for therapist appointment persistence.
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByTherapistIdOrderByStartAtAsc(Long therapistId);

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

    List<Appointment> findBySeriesIdOrderBySeriesIndexAsc(String seriesId);

    @Modifying
    @Query("UPDATE Appointment a SET a.status = :status WHERE a.seriesId = :seriesId")
    void updateStatusBySeriesId(@Param("seriesId") String seriesId,
                                @Param("status") AppointmentStatus status);

    @Modifying
    @Query("UPDATE Appointment a SET a.status = :status "
            + "WHERE a.seriesId = :seriesId AND a.seriesIndex >= :fromIndex")
    void updateStatusBySeriesIdAndSeriesIndexGreaterThanEqual(
            @Param("seriesId") String seriesId,
            @Param("fromIndex") int fromIndex,
            @Param("status") AppointmentStatus status);
}
