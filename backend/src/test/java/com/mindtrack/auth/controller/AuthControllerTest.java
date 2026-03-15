package com.mindtrack.auth.controller;

import com.mindtrack.auth.service.JwtService;
import com.mindtrack.auth.service.RefreshTokenService;
import com.mindtrack.auth.service.UserService;
import com.mindtrack.common.model.Role;
import com.mindtrack.common.model.User;
import com.mindtrack.profile.model.UserProfile;
import com.mindtrack.profile.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

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
    void shouldLogoutAndClearCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .with(authentication(mockAuth(1L))))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn401OnLogoutWhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUpdateBothRolesAndReturnNewToken() throws Exception {
        User user = createUser(1L, "test@example.com", "Test User", "USER");
        UserProfile profile = createProfile(true, true);
        when(profileService.updateRoles(1L, true, true)).thenReturn(profile);
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(anyLong(), anyString(), anyString(), anyBoolean(), anyBoolean(), anyInt()))
                .thenReturn("new-token-both-roles");

        mockMvc.perform(patch("/api/auth/me/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isPatient\":true,\"isTherapist\":true}")
                        .with(authentication(mockAuth(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-token-both-roles"))
                .andExpect(jsonPath("$.isPatient").value(true))
                .andExpect(jsonPath("$.isTherapist").value(true));
    }

    @Test
    void shouldUpdateOnlyTherapistRoleAndReturnNewToken() throws Exception {
        User user = createUser(1L, "test@example.com", "Test User", "USER");
        UserProfile profile = createProfile(false, true);
        when(profileService.updateRoles(1L, false, true)).thenReturn(profile);
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(anyLong(), anyString(), anyString(), anyBoolean(), anyBoolean(), anyInt()))
                .thenReturn("new-token-therapist-only");

        mockMvc.perform(patch("/api/auth/me/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isPatient\":false,\"isTherapist\":true}")
                        .with(authentication(mockAuth(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-token-therapist-only"))
                .andExpect(jsonPath("$.isPatient").value(false))
                .andExpect(jsonPath("$.isTherapist").value(true));
    }

    @Test
    void shouldReturn401OnRolesUpdateWhenNotAuthenticated() throws Exception {
        mockMvc.perform(patch("/api/auth/me/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isPatient\":true,\"isTherapist\":false}"))
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

    private UserProfile createProfile(boolean isPatient, boolean isTherapist) {
        UserProfile profile = new UserProfile();
        profile.setUserId(1L);
        profile.setPatient(isPatient);
        profile.setTherapist(isTherapist);
        return profile;
    }
}
