package com.mindtrack.admin.dto;

import java.util.List;

/**
 * DTO for a role with its assigned permissions.
 */
public class RolePermissionResponse {

    private Long roleId;
    private String roleName;
    private List<PermissionResponse> permissions;

    public RolePermissionResponse() {
    }

    public RolePermissionResponse(Long roleId, String roleName, List<PermissionResponse> permissions) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.permissions = permissions;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<PermissionResponse> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionResponse> permissions) {
        this.permissions = permissions;
    }
}
