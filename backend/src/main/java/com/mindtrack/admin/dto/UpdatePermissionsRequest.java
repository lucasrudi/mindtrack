package com.mindtrack.admin.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO for updating a role's permission set.
 */
public class UpdatePermissionsRequest {

    @NotNull
    private List<Long> permissionIds;

    public UpdatePermissionsRequest() {
    }

    public UpdatePermissionsRequest(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}
