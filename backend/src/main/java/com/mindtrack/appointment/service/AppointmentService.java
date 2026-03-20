package com.mindtrack.appointment.service;

import com.mindtrack.appointment.dto.AppointmentRequest;
import com.mindtrack.appointment.dto.AppointmentResponse;
import com.mindtrack.appointment.model.Appointment;
import com.mindtrack.appointment.model.AppointmentStatus;
import com.mindtrack.appointment.repository.AppointmentRepository;
import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.User;
import com.mindtrack.therapist.model.TherapistPatientStatus;
import com.mindtrack.therapist.repository.TherapistPatientRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service for therapist appointment booking and calendar access.
 */
@Service
public class AppointmentService {

    private static final String PATIENT_NOT_FOUND_PREFIX = "Patient not found: ";

    private final AppointmentRepository appointmentRepository;
    private final TherapistPatientRepository therapistPatientRepository;
    private final UserRepository userRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              TherapistPatientRepository therapistPatientRepository,
                              UserRepository userRepository,
                              AppointmentMapper appointmentMapper) {
        this.appointmentRepository = appointmentRepository;
        this.therapistPatientRepository = therapistPatientRepository;
        this.userRepository = userRepository;
        this.appointmentMapper = appointmentMapper;
    }

    /**
     * Lists all appointments for the therapist, ordered chronologically.
     */
    public List<AppointmentResponse> listAppointments(Long therapistId) {
        List<Appointment> appointments = appointmentRepository
                .findByTherapistIdOrderByStartAtAsc(therapistId);
        return appointments.stream()
                .map(appointment -> appointmentMapper.toResponse(
                        appointment, loadPatient(appointment.getPatientId())))
                .toList();
    }

    /**
     * Books a new appointment with a patient after validating relationship and conflicts.
     */
    @Transactional
    public AppointmentResponse bookAppointment(Long therapistId, Long patientId,
                                               AppointmentRequest request) {
        validateRelationship(therapistId, patientId);
        validateTimeRange(request.getStartAt(), request.getEndAt());
        validateConflicts(therapistId, patientId, request.getStartAt(), request.getEndAt());

        Appointment appointment = new Appointment();
        appointment.setTherapistId(therapistId);
        appointment.setPatientId(patientId);
        appointment.setStartAt(request.getStartAt());
        appointment.setEndAt(request.getEndAt());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setReason(request.getReason());
        appointment.setNotes(request.getNotes());

        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(saved, loadPatient(patientId));
    }

    private void validateRelationship(Long therapistId, Long patientId) {
        boolean active = therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                therapistId, patientId, TherapistPatientStatus.ACTIVE);
        if (!active) {
            throw new IllegalArgumentException("Active therapist-patient relationship not found");
        }
    }

    private void validateTimeRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (endAt.isBefore(startAt) || endAt.isEqual(startAt)) {
            throw new IllegalArgumentException("Appointment end time must be after start time");
        }
    }

    private void validateConflicts(Long therapistId, Long patientId,
                                   LocalDateTime startAt, LocalDateTime endAt) {
        List<AppointmentStatus> blockingStatuses = List.of(AppointmentStatus.SCHEDULED);

        boolean therapistConflict = !appointmentRepository
                .findByTherapistIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        therapistId, blockingStatuses, endAt, startAt)
                .isEmpty();
        if (therapistConflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Therapist already has an appointment during this time");
        }

        boolean patientConflict = !appointmentRepository
                .findByPatientIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        patientId, blockingStatuses, endAt, startAt)
                .isEmpty();
        if (patientConflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Patient already has an appointment during this time");
        }
    }

    private User loadPatient(Long patientId) {
        return userRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException(PATIENT_NOT_FOUND_PREFIX + patientId));
    }
}
