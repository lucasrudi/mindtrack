package com.mindtrack.auth.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating the current user's patient/therapist role flags.
 */
public class SelfRolesRequest {

    @NotNull
    private Boolean isPatient;

    @NotNull
    private Boolean isTherapist;

    public SelfRolesRequest() {
    }

    public Boolean getIsPatient() {
        return isPatient;
    }

    public void setIsPatient(Boolean isPatient) {
        this.isPatient = isPatient;
    }

    public Boolean getIsTherapist() {
        return isTherapist;
    }

    public void setIsTherapist(Boolean isTherapist) {
        this.isTherapist = isTherapist;
    }
}
