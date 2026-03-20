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
    private final AppointmentNotificationService appointmentNotificationService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              TherapistPatientRepository therapistPatientRepository,
                              UserRepository userRepository,
                              AppointmentMapper appointmentMapper,
                              AppointmentNotificationService appointmentNotificationService) {
        this.appointmentRepository = appointmentRepository;
        this.therapistPatientRepository = therapistPatientRepository;
        this.userRepository = userRepository;
        this.appointmentMapper = appointmentMapper;
        this.appointmentNotificationService = appointmentNotificationService;
    }

    /**
     * Lists all appointments for the therapist, ordered chronologically.
     */
    public List<AppointmentResponse> listAppointments(Long therapistId) {
        List<Appointment> appointments = appointmentRepository
                .findByTherapistIdOrderByStartAtAsc(therapistId);
        User therapist = loadUser(therapistId, "Therapist not found: ");
        return appointments.stream()
                .map(appointment -> appointmentMapper.toResponse(
                        appointment, therapist, loadPatient(appointment.getPatientId())))
                .toList();
    }

    /**
     * Lists all appointments for the patient, ordered chronologically.
     */
    public List<AppointmentResponse> listPatientAppointments(Long patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatientIdOrderByStartAtAsc(patientId);
        User patient = loadPatient(patientId);
        return appointments.stream()
                .map(appointment -> appointmentMapper.toResponse(
                        appointment,
                        loadUser(appointment.getTherapistId(), "Therapist not found: "),
                        patient))
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

        User patient = loadUser(patientId, PATIENT_NOT_FOUND_PREFIX);
        Appointment saved = appointmentRepository.save(appointment);
        appointmentNotificationService.notifyPatientAboutBooking(
                saved,
                loadUser(therapistId, "Therapist not found: "),
                patient);
        return appointmentMapper.toResponse(
                saved,
                loadUser(therapistId, "Therapist not found: "),
                patient);
    }

    /**
     * Cancels an appointment on behalf of the therapist.
     */
    @Transactional
    public AppointmentResponse cancelAppointmentAsTherapist(Long therapistId, Long appointmentId) {
        Appointment appointment = loadAppointment(appointmentId);
        if (!therapistId.equals(appointment.getTherapistId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Appointment does not belong to therapist");
        }
        return cancelAppointment(appointment, loadUser(therapistId, "Therapist not found: "));
    }

    /**
     * Cancels an appointment on behalf of the patient.
     */
    @Transactional
    public AppointmentResponse cancelAppointmentAsPatient(Long patientId, Long appointmentId) {
        Appointment appointment = loadAppointment(appointmentId);
        if (!patientId.equals(appointment.getPatientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Appointment does not belong to patient");
        }
        return cancelAppointment(appointment, loadPatient(patientId));
    }

    private AppointmentResponse cancelAppointment(Appointment appointment, User cancelledBy) {
        validateCancellable(appointment);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment saved = appointmentRepository.save(appointment);

        User therapist = loadUser(saved.getTherapistId(), "Therapist not found: ");
        User patient = loadPatient(saved.getPatientId());
        appointmentNotificationService.notifyParticipantsAboutCancellation(
                saved, therapist, patient, cancelledBy);
        return appointmentMapper.toResponse(saved, therapist, patient);
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
        return loadUser(patientId, PATIENT_NOT_FOUND_PREFIX);
    }

    private Appointment loadAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Appointment not found: " + appointmentId));
    }

    private void validateCancellable(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Appointment is already cancelled");
        }
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only scheduled appointments can be cancelled");
        }
    }

    private User loadUser(Long userId, String notFoundPrefix) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(notFoundPrefix + userId));
    }
}
