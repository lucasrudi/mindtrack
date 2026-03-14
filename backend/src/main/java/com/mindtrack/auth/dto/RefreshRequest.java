package com.mindtrack.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for the refresh token endpoint.
 */
public class RefreshRequest {

    @NotBlank
    private String refreshToken;

    public RefreshRequest() {
    }

    /**
     * Returns the raw refresh token value supplied by the client.
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Sets the raw refresh token value.
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
