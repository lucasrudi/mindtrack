package com.mindtrack.admin.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.admin.dto.PermissionResponse;
import com.mindtrack.admin.dto.RoleChangeRequest;
import com.mindtrack.admin.dto.RolePermissionResponse;
import com.mindtrack.admin.dto.UpdatePermissionsRequest;
import com.mindtrack.admin.dto.UserResponse;
import com.mindtrack.admin.service.AdminService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

    private static UsernamePasswordAuthenticationToken adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private static UsernamePasswordAuthenticationToken userAuth() {
        return new UsernamePasswordAuthenticationToken(
                2L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private UserResponse sampleUser() {
        return new UserResponse(1L, "admin@test.com", "Admin", "ADMIN", true,
                LocalDateTime.of(2025, 1, 1, 10, 0), LocalDateTime.of(2025, 1, 1, 10, 0));
    }

    // --- Authorization tests ---

    @Test
    void rejectsNonAdminUserForListUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(authentication(userAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsNonAdminUserForGetUser() throws Exception {
        mockMvc.perform(get("/api/admin/users/1")
                        .with(authentication(userAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsNonAdminForRoleChange() throws Exception {
        mockMvc.perform(patch("/api/admin/users/1/role")
                        .with(authentication(userAuth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoleChangeRequest("ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    // --- User management tests ---

    @Test
    void listsUsersForAdmin() throws Exception {
        UserResponse user1 = sampleUser();
        UserResponse user2 = new UserResponse(2L, "user@test.com", "User", "USER", true,
                LocalDateTime.of(2025, 1, 2, 10, 0), null);
        Page<UserResponse> page = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 20), 2);
        when(adminService.listUsers(any())).thenReturn(page);

        mockMvc.perform(get("/api/admin/users")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].email").value("admin@test.com"))
                .andExpect(jsonPath("$.content[1].email").value("user@test.com"));
    }

    @Test
    void getsUserById() throws Exception {
        when(adminService.getUser(1L)).thenReturn(sampleUser());

        mockMvc.perform(get("/api/admin/users/1")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void changesUserRole() throws Exception {
        UserResponse updated = new UserResponse(2L, "user@test.com", "User", "ADMIN", true,
                LocalDateTime.of(2025, 1, 1, 10, 0), LocalDateTime.of(2025, 1, 2, 10, 0));
        when(adminService.changeUserRole(eq(2L), eq("ADMIN"))).thenReturn(updated);

        mockMvc.perform(patch("/api/admin/users/2/role")
                        .with(authentication(adminAuth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoleChangeRequest("ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void disablesUser() throws Exception {
        UserResponse disabled = new UserResponse(2L, "user@test.com", "User", "USER", false,
                LocalDateTime.of(2025, 1, 1, 10, 0), LocalDateTime.of(2025, 1, 2, 10, 0));
        when(adminService.setUserEnabled(eq(2L), eq(false))).thenReturn(disabled);

        mockMvc.perform(patch("/api/admin/users/2/enabled")
                        .with(authentication(adminAuth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("enabled", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void enablesUser() throws Exception {
        UserResponse enabled = new UserResponse(2L, "user@test.com", "User", "USER", true,
                LocalDateTime.of(2025, 1, 1, 10, 0), LocalDateTime.of(2025, 1, 2, 10, 0));
        when(adminService.setUserEnabled(eq(2L), eq(true))).thenReturn(enabled);

        mockMvc.perform(patch("/api/admin/users/2/enabled")
                        .with(authentication(adminAuth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("enabled", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    // --- RBAC tests ---

    @Test
    void listsRolesWithPermissions() throws Exception {
        PermissionResponse p1 = new PermissionResponse(1L, "users", "read");
        RolePermissionResponse role1 = new RolePermissionResponse(1L, "ADMIN", List.of(p1));
        RolePermissionResponse role2 = new RolePermissionResponse(2L, "USER", List.of());
        when(adminService.listRolesWithPermissions()).thenReturn(List.of(role1, role2));

        mockMvc.perform(get("/api/admin/roles")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].roleName").value("ADMIN"))
                .andExpect(jsonPath("$[0].permissions", hasSize(1)))
                .andExpect(jsonPath("$[1].roleName").value("USER"));
    }

    @Test
    void listsAllPermissions() throws Exception {
        PermissionResponse p1 = new PermissionResponse(1L, "users", "read");
        PermissionResponse p2 = new PermissionResponse(2L, "users", "write");
        when(adminService.listPermissions()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/admin/permissions")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].resource").value("users"));
    }

    @Test
    void updatesRolePermissions() throws Exception {
        PermissionResponse p1 = new PermissionResponse(1L, "users", "read");
        PermissionResponse p2 = new PermissionResponse(2L, "users", "write");
        RolePermissionResponse updated = new RolePermissionResponse(2L, "USER", List.of(p1, p2));
        when(adminService.updateRolePermissions(eq(2L), eq(List.of(1L, 2L)))).thenReturn(updated);

        mockMvc.perform(put("/api/admin/roles/2/permissions")
                        .with(authentication(adminAuth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdatePermissionsRequest(List.of(1L, 2L)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("USER"))
                .andExpect(jsonPath("$.permissions", hasSize(2)));
    }

    @Test
    void rejectsNonAdminForRolePermissions() throws Exception {
        mockMvc.perform(get("/api/admin/roles")
                        .with(authentication(userAuth())))
                .andExpect(status().isForbidden());
    }
}
