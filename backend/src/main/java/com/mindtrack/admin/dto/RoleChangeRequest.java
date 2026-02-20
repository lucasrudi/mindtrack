package com.mindtrack.admin.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for changing a user's role.
 */
public class RoleChangeRequest {

    @NotBlank
    private String role;

    public RoleChangeRequest() {
    }

    public RoleChangeRequest(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
