package com.mindtrack.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for redeeming a therapist registration token.
 */
public class TherapistRegistrationRedeemRequest {

    @NotBlank
    @Size(min = 64, max = 64)
    private String token;

    public TherapistRegistrationRedeemRequest() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
