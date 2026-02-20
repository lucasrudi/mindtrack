package com.mindtrack.admin.controller;

import com.mindtrack.admin.dto.PermissionResponse;
import com.mindtrack.admin.dto.RoleChangeRequest;
import com.mindtrack.admin.dto.RolePermissionResponse;
import com.mindtrack.admin.dto.UpdatePermissionsRequest;
import com.mindtrack.admin.dto.UserResponse;
import com.mindtrack.admin.service.AdminService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for admin operations.
 *
 * <p>All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Lists all users with pagination.
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> listUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.listUsers(pageable));
    }

    /**
     * Gets a single user by ID.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUser(id));
    }

    /**
     * Changes a user's role.
     */
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> changeRole(@PathVariable Long id,
                                                   @Valid @RequestBody RoleChangeRequest request) {
        return ResponseEntity.ok(adminService.changeUserRole(id, request.getRole()));
    }

    /**
     * Enables or disables a user account.
     */
    @PatchMapping("/users/{id}/enabled")
    public ResponseEntity<UserResponse> setEnabled(@PathVariable Long id,
                                                   @RequestBody Map<String, Boolean> body) {
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(adminService.setUserEnabled(id, enabled));
    }

    /**
     * Lists all roles with their assigned permissions.
     */
    @GetMapping("/roles")
    public ResponseEntity<List<RolePermissionResponse>> listRoles() {
        return ResponseEntity.ok(adminService.listRolesWithPermissions());
    }

    /**
     * Lists all available permissions.
     */
    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionResponse>> listPermissions() {
        return ResponseEntity.ok(adminService.listPermissions());
    }

    /**
     * Updates permissions for a role.
     */
    @PutMapping("/roles/{roleId}/permissions")
    public ResponseEntity<RolePermissionResponse> updateRolePermissions(
            @PathVariable Long roleId, @Valid @RequestBody UpdatePermissionsRequest request) {
        return ResponseEntity.ok(adminService.updateRolePermissions(roleId, request.getPermissionIds()));
    }
}
