package com.mindtrack.appointment.service;

import com.mindtrack.appointment.dto.AppointmentResponse;
import com.mindtrack.appointment.model.Appointment;
import com.mindtrack.common.model.User;
import java.time.Duration;
import org.springframework.stereotype.Component;

/**
 * Maps appointment entities to API responses.
 */
@Component
public class AppointmentMapper {

    public AppointmentResponse toResponse(Appointment appointment, User patient) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setTherapistId(appointment.getTherapistId());
        response.setPatientId(appointment.getPatientId());
        response.setPatientName(patient.getName());
        response.setPatientEmail(patient.getEmail());
        response.setStartAt(appointment.getStartAt());
        response.setEndAt(appointment.getEndAt());
        response.setStatus(appointment.getStatus());
        response.setReason(appointment.getReason());
        response.setNotes(appointment.getNotes());
        response.setDurationMinutes(Duration.between(
                appointment.getStartAt(), appointment.getEndAt()).toMinutes());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());
        return response;
    }
}
