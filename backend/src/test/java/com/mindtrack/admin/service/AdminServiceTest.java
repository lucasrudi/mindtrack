package com.mindtrack.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mindtrack.admin.dto.RolePermissionResponse;
import com.mindtrack.admin.dto.UserResponse;
import com.mindtrack.admin.repository.PermissionRepository;
import com.mindtrack.admin.repository.RolePermissionRepository;
import com.mindtrack.auth.repository.RoleRepository;
import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.Permission;
import com.mindtrack.common.model.Role;
import com.mindtrack.common.model.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Spy
    private AdminMapper mapper;

    @InjectMocks
    private AdminService service;

    private User createUser(Long id, String email, String name, String roleName, boolean enabled) {
        Role role = new Role(roleName);
        role.setId(1L);

        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        user.setRole(role);
        user.setEnabled(enabled);
        user.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        user.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        return user;
    }

    @Test
    void listsUsersWithPagination() {
        User user1 = createUser(1L, "admin@test.com", "Admin", "ADMIN", true);
        User user2 = createUser(2L, "user@test.com", "User", "USER", true);
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> page = new PageImpl<>(List.of(user1, user2), pageable, 2);
        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<UserResponse> result = service.listUsers(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("admin@test.com", result.getContent().get(0).getEmail());
    }

    @Test
    void getsUserById() {
        User user = createUser(1L, "admin@test.com", "Admin", "ADMIN", true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = service.getUser(1L);

        assertEquals("admin@test.com", result.getEmail());
        assertEquals("ADMIN", result.getRole());
    }

    @Test
    void throwsWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.getUser(999L));
    }

    @Test
    void changesUserRole() {
        User user = createUser(1L, "user@test.com", "User", "USER", true);
        Role adminRole = new Role("ADMIN");
        adminRole.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = service.changeUserRole(1L, "ADMIN");

        assertEquals("ADMIN", result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void throwsWhenRoleNotFound() {
        User user = createUser(1L, "user@test.com", "User", "USER", true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.changeUserRole(1L, "NONEXISTENT"));
    }

    @Test
    void disablesUserAccount() {
        User user = createUser(1L, "user@test.com", "User", "USER", true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = service.setUserEnabled(1L, false);

        assertFalse(result.isEnabled());
    }

    @Test
    void enablesUserAccount() {
        User user = createUser(1L, "user@test.com", "User", "USER", false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = service.setUserEnabled(1L, true);

        assertTrue(result.isEnabled());
    }

    @Test
    void listsRolesWithPermissions() {
        Role admin = new Role("ADMIN");
        admin.setId(1L);
        Role userRole = new Role("USER");
        userRole.setId(2L);
        when(roleRepository.findAll()).thenReturn(List.of(admin, userRole));

        Permission p1 = new Permission("users", "read");
        p1.setId(1L);
        when(permissionRepository.findByRoleId(1L)).thenReturn(List.of(p1));
        when(permissionRepository.findByRoleId(2L)).thenReturn(List.of());

        List<RolePermissionResponse> result = service.listRolesWithPermissions();

        assertEquals(2, result.size());
        assertEquals("ADMIN", result.get(0).getRoleName());
        assertEquals(1, result.get(0).getPermissions().size());
        assertEquals("USER", result.get(1).getRoleName());
        assertEquals(0, result.get(1).getPermissions().size());
    }

    @Test
    void listsAllPermissions() {
        Permission p1 = new Permission("users", "read");
        p1.setId(1L);
        Permission p2 = new Permission("users", "write");
        p2.setId(2L);
        when(permissionRepository.findAll()).thenReturn(List.of(p1, p2));

        var result = service.listPermissions();

        assertEquals(2, result.size());
        assertEquals("users", result.get(0).getResource());
    }

    @Test
    void updatesRolePermissions() {
        Role role = new Role("USER");
        role.setId(2L);
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));

        Permission p1 = new Permission("users", "read");
        p1.setId(1L);
        Permission p2 = new Permission("users", "write");
        p2.setId(2L);
        when(permissionRepository.findByRoleId(2L)).thenReturn(List.of(p1, p2));

        RolePermissionResponse result = service.updateRolePermissions(2L, List.of(1L, 2L));

        verify(rolePermissionRepository).deleteByRoleId(2L);
        verify(rolePermissionRepository).saveAll(anyList());
        assertEquals("USER", result.getRoleName());
        assertEquals(2, result.getPermissions().size());
    }

    @Test
    void throwsWhenUpdatingPermissionsForNonexistentRole() {
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.updateRolePermissions(999L, List.of(1L)));
    }
}
