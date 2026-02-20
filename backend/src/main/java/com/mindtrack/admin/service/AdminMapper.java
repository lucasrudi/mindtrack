package com.mindtrack.admin.service;

import com.mindtrack.admin.dto.PermissionResponse;
import com.mindtrack.admin.dto.RolePermissionResponse;
import com.mindtrack.admin.dto.UserResponse;
import com.mindtrack.common.model.Permission;
import com.mindtrack.common.model.Role;
import com.mindtrack.common.model.User;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Maps admin domain entities to DTOs.
 */
@Component
public class AdminMapper {

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().getName(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public PermissionResponse toPermissionResponse(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getResource(),
                permission.getAction()
        );
    }

    public RolePermissionResponse toRolePermissionResponse(Role role, List<Permission> permissions) {
        List<PermissionResponse> permissionResponses = permissions.stream()
                .map(this::toPermissionResponse)
                .toList();
        return new RolePermissionResponse(role.getId(), role.getName(), permissionResponses);
    }
}
