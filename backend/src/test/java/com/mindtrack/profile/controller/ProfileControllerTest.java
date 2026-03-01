package com.mindtrack.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.profile.dto.ProfileRequest;
import com.mindtrack.profile.dto.ProfileResponse;
import com.mindtrack.profile.service.ProfileService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProfileService profileService;

    private static UsernamePasswordAuthenticationToken userAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldGetProfileForAuthenticatedUser() throws Exception {
        ProfileResponse response = createResponse();
        when(profileService.getProfile(1L)).thenReturn(response);

        mockMvc.perform(get("/api/profile")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.displayName").value("John Doe"))
                .andExpect(jsonPath("$.timezone").value("America/New_York"));
    }

    @Test
    void shouldUpdateProfileForAuthenticatedUser() throws Exception {
        ProfileRequest request = new ProfileRequest();
        request.setDisplayName("Jane Doe");
        request.setTimezone("Europe/London");

        ProfileResponse response = createResponse();
        response.setDisplayName("Jane Doe");
        response.setTimezone("Europe/London");
        when(profileService.updateProfile(eq(1L), any(ProfileRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/profile")
                        .with(csrf())
                        .with(authentication(userAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Jane Doe"))
                .andExpect(jsonPath("$.timezone").value("Europe/London"));
    }

    @Test
    void shouldRejectUnauthenticatedGetRequest() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnauthenticatedPutRequest() throws Exception {
        ProfileRequest request = new ProfileRequest();
        request.setDisplayName("Test");

        mockMvc.perform(put("/api/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUpdateProfileWithAllFields() throws Exception {
        ProfileRequest request = new ProfileRequest();
        request.setDisplayName("Full Name");
        request.setAvatarUrl("https://example.com/avatar.jpg");
        request.setTimezone("Asia/Tokyo");
        request.setNotificationPrefs(Map.of("emailNotifications", true, "pushNotifications", false));
        request.setTelegramChatId("telegram123");
        request.setWhatsappNumber("+81901234567");

        ProfileResponse response = new ProfileResponse();
        response.setId(1L);
        response.setUserId(1L);
        response.setDisplayName("Full Name");
        response.setAvatarUrl("https://example.com/avatar.jpg");
        response.setTimezone("Asia/Tokyo");
        response.setNotificationPrefs(Map.of("emailNotifications", true, "pushNotifications", false));
        response.setTelegramChatId("telegram123");
        response.setWhatsappNumber("+81901234567");
        when(profileService.updateProfile(eq(1L), any(ProfileRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/profile")
                        .with(csrf())
                        .with(authentication(userAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Full Name"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.jpg"))
                .andExpect(jsonPath("$.timezone").value("Asia/Tokyo"))
                .andExpect(jsonPath("$.notificationPrefs.emailNotifications").value(true))
                .andExpect(jsonPath("$.telegramChatId").value("telegram123"))
                .andExpect(jsonPath("$.whatsappNumber").value("+81901234567"));
    }

    private ProfileResponse createResponse() {
        ProfileResponse response = new ProfileResponse();
        response.setId(1L);
        response.setUserId(1L);
        response.setDisplayName("John Doe");
        response.setAvatarUrl("https://example.com/avatar.jpg");
        response.setTimezone("America/New_York");
        return response;
    }
}
