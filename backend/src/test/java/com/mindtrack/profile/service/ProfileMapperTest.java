package com.mindtrack.profile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.profile.dto.ProfileRequest;
import com.mindtrack.profile.dto.ProfileResponse;
import com.mindtrack.profile.model.UserProfile;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileMapperTest {

    private ProfileMapper profileMapper;

    @BeforeEach
    void setUp() {
        profileMapper = new ProfileMapper(new ObjectMapper());
    }

    @Test
    void shouldMapAllFieldsToResponse() {
        UserProfile profile = createProfile();
        profile.setNotificationPrefs("{\"emailNotifications\":true,\"pushNotifications\":false}");

        ProfileResponse response = profileMapper.toResponse(profile);

        assertEquals(1L, response.getId());
        assertEquals(10L, response.getUserId());
        assertEquals("John Doe", response.getDisplayName());
        assertEquals("https://example.com/avatar.jpg", response.getAvatarUrl());
        assertEquals("America/New_York", response.getTimezone());
        assertNotNull(response.getNotificationPrefs());
        assertEquals(true, response.getNotificationPrefs().get("emailNotifications"));
        assertEquals(false, response.getNotificationPrefs().get("pushNotifications"));
        assertEquals("123456789", response.getTelegramChatId());
        assertEquals("+1234567890", response.getWhatsappNumber());
    }

    @Test
    void shouldHandleNullNotificationPrefs() {
        UserProfile profile = createProfile();
        profile.setNotificationPrefs(null);

        ProfileResponse response = profileMapper.toResponse(profile);

        assertNull(response.getNotificationPrefs());
    }

    @Test
    void shouldHandleEmptyNotificationPrefs() {
        UserProfile profile = createProfile();
        profile.setNotificationPrefs("");

        ProfileResponse response = profileMapper.toResponse(profile);

        assertNull(response.getNotificationPrefs());
    }

    @Test
    void shouldApplyRequestToProfile() {
        ProfileRequest request = new ProfileRequest();
        request.setDisplayName("Jane Doe");
        request.setAvatarUrl("https://example.com/new-avatar.jpg");
        request.setTimezone("Europe/London");
        request.setNotificationPrefs(Map.of("emailNotifications", true));
        request.setTelegramChatId("987654321");
        request.setWhatsappNumber("+9876543210");

        UserProfile profile = new UserProfile();
        profileMapper.applyRequest(request, profile);

        assertEquals("Jane Doe", profile.getDisplayName());
        assertEquals("https://example.com/new-avatar.jpg", profile.getAvatarUrl());
        assertEquals("Europe/London", profile.getTimezone());
        assertNotNull(profile.getNotificationPrefs());
        assertEquals("987654321", profile.getTelegramChatId());
        assertEquals("+9876543210", profile.getWhatsappNumber());
    }

    @Test
    void shouldSerializeNullNotificationPrefsAsNull() {
        ProfileRequest request = new ProfileRequest();
        request.setNotificationPrefs(null);

        UserProfile profile = new UserProfile();
        profileMapper.applyRequest(request, profile);

        assertNull(profile.getNotificationPrefs());
    }

    @Test
    void shouldMapSurveyCompletedToResponse() {
        UserProfile profile = new UserProfile();
        profile.setUserId(1L);
        profile.setSurveyCompleted(true);
        profile.setOnboardingCompleted(true);

        ProfileResponse response = profileMapper.toResponse(profile);

        assertTrue(response.isSurveyCompleted());
        assertTrue(response.isOnboardingCompleted());
    }

    @Test
    void shouldDefaultSurveyCompletedToFalse() {
        UserProfile profile = new UserProfile();
        profile.setUserId(1L);

        ProfileResponse response = profileMapper.toResponse(profile);

        assertFalse(response.isSurveyCompleted());
        assertFalse(response.isOnboardingCompleted());
    }

    private UserProfile createProfile() {
        UserProfile profile = new UserProfile();
        profile.setId(1L);
        profile.setUserId(10L);
        profile.setDisplayName("John Doe");
        profile.setAvatarUrl("https://example.com/avatar.jpg");
        profile.setTimezone("America/New_York");
        profile.setTelegramChatId("123456789");
        profile.setWhatsappNumber("+1234567890");
        return profile;
    }
}
