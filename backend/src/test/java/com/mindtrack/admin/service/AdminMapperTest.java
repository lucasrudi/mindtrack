package com.mindtrack.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mindtrack.admin.dto.PermissionResponse;
import com.mindtrack.admin.dto.RolePermissionResponse;
import com.mindtrack.admin.dto.UserResponse;
import com.mindtrack.common.model.Permission;
import com.mindtrack.common.model.Role;
import com.mindtrack.common.model.User;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminMapperTest {

    private final AdminMapper mapper = new AdminMapper();

    @Test
    void mapsUserToUserResponse() {
        Role role = new Role("ADMIN");
        role.setId(1L);

        User user = new User();
        user.setId(10L);
        user.setEmail("admin@mindtrack.app");
        user.setName("Admin");
        user.setRole(role);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        user.setUpdatedAt(LocalDateTime.of(2025, 1, 2, 10, 0));

        UserResponse response = mapper.toUserResponse(user);

        assertEquals(10L, response.getId());
        assertEquals("admin@mindtrack.app", response.getEmail());
        assertEquals("Admin", response.getName());
        assertEquals("ADMIN", response.getRole());
        assertTrue(response.isEnabled());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), response.getCreatedAt());
    }

    @Test
    void mapsPermissionToPermissionResponse() {
        Permission permission = new Permission("users", "read");
        permission.setId(1L);

        PermissionResponse response = mapper.toPermissionResponse(permission);

        assertEquals(1L, response.getId());
        assertEquals("users", response.getResource());
        assertEquals("read", response.getAction());
    }

    @Test
    void mapsRoleWithPermissionsToResponse() {
        Role role = new Role("ADMIN");
        role.setId(1L);

        Permission p1 = new Permission("users", "read");
        p1.setId(1L);
        Permission p2 = new Permission("users", "write");
        p2.setId(2L);

        RolePermissionResponse response = mapper.toRolePermissionResponse(role, List.of(p1, p2));

        assertEquals(1L, response.getRoleId());
        assertEquals("ADMIN", response.getRoleName());
        assertEquals(2, response.getPermissions().size());
        assertEquals("users", response.getPermissions().get(0).getResource());
        assertEquals("write", response.getPermissions().get(1).getAction());
    }

    @Test
    void mapsDisabledUser() {
        Role role = new Role("USER");
        role.setId(2L);

        User user = new User();
        user.setId(20L);
        user.setEmail("disabled@test.com");
        user.setName("Disabled User");
        user.setRole(role);
        user.setEnabled(false);
        user.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

        UserResponse response = mapper.toUserResponse(user);

        assertEquals("USER", response.getRole());
        assertEquals(false, response.isEnabled());
    }
}
