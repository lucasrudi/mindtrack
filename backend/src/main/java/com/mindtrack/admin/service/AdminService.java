package com.mindtrack.admin.service;

import com.mindtrack.admin.dto.PermissionResponse;
import com.mindtrack.admin.dto.RolePermissionResponse;
import com.mindtrack.admin.dto.UserResponse;
import com.mindtrack.admin.model.RolePermission;
import com.mindtrack.admin.repository.PermissionRepository;
import com.mindtrack.admin.repository.RolePermissionRepository;
import com.mindtrack.auth.repository.RoleRepository;
import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.Permission;
import com.mindtrack.common.model.Role;
import com.mindtrack.common.model.User;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for admin operations: user management and RBAC configuration.
 */
@Service
public class AdminService {

    private static final Logger LOG = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final AdminMapper mapper;

    public AdminService(UserRepository userRepository, RoleRepository roleRepository,
                        PermissionRepository permissionRepository,
                        RolePermissionRepository rolePermissionRepository,
                        AdminMapper mapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.mapper = mapper;
    }

    /**
     * Lists all users with pagination.
     */
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(mapper::toUserResponse);
    }

    /**
     * Gets a user by ID.
     */
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return mapper.toUserResponse(user);
    }

    /**
     * Changes a user's role.
     */
    @Transactional
    public UserResponse changeUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);

        LOG.info("Changed role for user {} to {}", userId, roleName);
        return mapper.toUserResponse(saved);
    }

    /**
     * Enables or disables a user account.
     */
    @Transactional
    public UserResponse setUserEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setEnabled(enabled);
        if (!enabled) {
            user.setTokenVersion(user.getTokenVersion() + 1);
        }
        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);

        LOG.info("{} user {}", enabled ? "Enabled" : "Disabled", userId);
        return mapper.toUserResponse(saved);
    }

    /**
     * Lists all available roles.
     */
    public List<RolePermissionResponse> listRolesWithPermissions() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(role -> {
                    List<Permission> permissions = permissionRepository.findByRoleId(role.getId());
                    return mapper.toRolePermissionResponse(role, permissions);
                })
                .toList();
    }

    /**
     * Lists all available permissions.
     */
    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAll().stream()
                .map(mapper::toPermissionResponse)
                .toList();
    }

    /**
     * Updates the permission set for a role.
     */
    @Transactional
    public RolePermissionResponse updateRolePermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

        // Delete existing role permissions
        rolePermissionRepository.deleteByRoleId(roleId);

        // Add new permissions
        List<RolePermission> newMappings = permissionIds.stream()
                .map(permissionId -> new RolePermission(roleId, permissionId))
                .toList();
        rolePermissionRepository.saveAll(newMappings);

        List<Permission> permissions = permissionRepository.findByRoleId(roleId);

        LOG.info("Updated permissions for role {} with {} permissions", role.getName(), permissionIds.size());
        return mapper.toRolePermissionResponse(role, permissions);
    }
}
