package com.mindtrack.auth.controller;

import com.mindtrack.auth.service.JwtService;
import com.mindtrack.auth.service.UserService;
import com.mindtrack.common.model.Role;
import com.mindtrack.common.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    private static UsernamePasswordAuthenticationToken mockAuth(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldReturnCurrentUser() throws Exception {
        User user = createUser(1L, "test@example.com", "Test User", "USER");
        when(userService.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/auth/me")
                        .with(authentication(mockAuth(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/me")
                        .with(authentication(mockAuth(999L))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldChangeRoleToTherapistAndReturnNewToken() throws Exception {
        User user = createUser(1L, "test@example.com", "Test User", "THERAPIST");
        when(userService.changeRole(1L, "THERAPIST")).thenReturn(user);
        when(jwtService.generateToken(anyLong(), anyString(), anyString())).thenReturn("new-token");

        mockMvc.perform(patch("/api/auth/me/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"THERAPIST\"}")
                        .with(authentication(mockAuth(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-token"))
                .andExpect(jsonPath("$.role").value("THERAPIST"));
    }

    @Test
    void shouldRejectSelfAssignAdminRole() throws Exception {
        mockMvc.perform(patch("/api/auth/me/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}")
                        .with(authentication(mockAuth(1L))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn401OnRoleChangeWhenNotAuthenticated() throws Exception {
        mockMvc.perform(patch("/api/auth/me/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"THERAPIST\"}"))
                .andExpect(status().isUnauthorized());
    }

    private User createUser(Long id, String email, String name, String roleName) {
        Role role = new Role(roleName);
        role.setId(1L);

        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        user.setRole(role);
        user.setEnabled(true);
        return user;
    }
}
