package com.mindtrack.therapist.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for therapist-initiated patient requests.
 */
public class PatientRequestCreateRequest {

    @NotBlank
    @Email
    private String patientEmail;

    public PatientRequestCreateRequest() {
        // Required by Jackson for request-body deserialization.
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }
}
