package com.mindtrack.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO returned after successful authentication.
 */
public class AuthResponse {

    private String token;
    private String email;
    private String name;
    private String role;
    private boolean isPatient;
    private boolean isTherapist;

    public AuthResponse() {
    }

    public AuthResponse(String token, String email, String name, String role) {
        this.token = token;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public AuthResponse(String token, String email, String name, String role,
                        boolean isPatient, boolean isTherapist) {
        this.token = token;
        this.email = email;
        this.name = name;
        this.role = role;
        this.isPatient = isPatient;
        this.isTherapist = isTherapist;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @JsonProperty("isPatient")
    public boolean isPatient() {
        return isPatient;
    }

    public void setPatient(boolean isPatient) {
        this.isPatient = isPatient;
    }

    @JsonProperty("isTherapist")
    public boolean isTherapist() {
        return isTherapist;
    }

    public void setTherapist(boolean isTherapist) {
        this.isTherapist = isTherapist;
    }
}
