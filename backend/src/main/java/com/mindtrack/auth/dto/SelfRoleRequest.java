package com.mindtrack.auth.dto;

import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for self-service role change. Only USER and THERAPIST are allowed.
 */
public class SelfRoleRequest {

    @Pattern(regexp = "USER|THERAPIST", message = "Role must be USER or THERAPIST")
    private String role;

    public SelfRoleRequest() {
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
