package com.mindtrack.profile.service;

import com.mindtrack.profile.dto.ProfileRequest;
import com.mindtrack.profile.dto.ProfileResponse;
import com.mindtrack.profile.model.UserProfile;
import com.mindtrack.profile.repository.UserProfileRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserProfileRepository profileRepository;

    @Mock
    private ProfileMapper profileMapper;

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(profileRepository, profileMapper);
    }

    @Test
    void shouldReturnExistingProfile() {
        UserProfile profile = createProfile();
        ProfileResponse expectedResponse = createResponse();

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileMapper.toResponse(profile)).thenReturn(expectedResponse);

        ProfileResponse result = profileService.getProfile(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getDisplayName());
    }

    @Test
    void shouldCreateDefaultProfileWhenNotFound() {
        UserProfile defaultProfile = new UserProfile();
        defaultProfile.setId(1L);
        defaultProfile.setUserId(1L);
        ProfileResponse expectedResponse = new ProfileResponse();
        expectedResponse.setId(1L);
        expectedResponse.setUserId(1L);

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(profileRepository.save(any(UserProfile.class))).thenReturn(defaultProfile);
        when(profileMapper.toResponse(defaultProfile)).thenReturn(expectedResponse);

        ProfileResponse result = profileService.getProfile(1L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(profileRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getUserId());
    }

    @Test
    void shouldUpdateExistingProfile() {
        UserProfile profile = createProfile();
        ProfileRequest request = createRequest();
        ProfileResponse expectedResponse = createResponse();
        expectedResponse.setDisplayName("Jane Doe");

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(profile)).thenReturn(profile);
        when(profileMapper.toResponse(profile)).thenReturn(expectedResponse);

        ProfileResponse result = profileService.updateProfile(1L, request);

        assertNotNull(result);
        verify(profileMapper).applyRequest(request, profile);
        verify(profileRepository).save(profile);
    }

    @Test
    void shouldCreateAndUpdateProfileWhenNotFound() {
        UserProfile defaultProfile = new UserProfile();
        defaultProfile.setId(1L);
        defaultProfile.setUserId(1L);
        ProfileRequest request = createRequest();
        ProfileResponse expectedResponse = createResponse();

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(profileRepository.save(any(UserProfile.class))).thenReturn(defaultProfile);
        when(profileMapper.toResponse(defaultProfile)).thenReturn(expectedResponse);

        ProfileResponse result = profileService.updateProfile(1L, request);

        assertNotNull(result);
        verify(profileRepository, times(2)).save(any(UserProfile.class));
        verify(profileMapper).applyRequest(eq(request), any(UserProfile.class));
    }

    @Test
    void shouldUpdateAllFields() {
        UserProfile profile = createProfile();
        ProfileRequest request = createRequest();
        ProfileResponse expectedResponse = createResponse();

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(profile)).thenReturn(profile);
        when(profileMapper.toResponse(profile)).thenReturn(expectedResponse);

        profileService.updateProfile(1L, request);

        verify(profileMapper).applyRequest(request, profile);
    }

    private UserProfile createProfile() {
        UserProfile profile = new UserProfile();
        profile.setId(1L);
        profile.setUserId(1L);
        profile.setDisplayName("John Doe");
        profile.setAvatarUrl("https://example.com/avatar.jpg");
        profile.setTimezone("America/New_York");
        return profile;
    }

    private ProfileRequest createRequest() {
        ProfileRequest request = new ProfileRequest();
        request.setDisplayName("Jane Doe");
        request.setAvatarUrl("https://example.com/new.jpg");
        request.setTimezone("Europe/London");
        request.setNotificationPrefs(Map.of("emailNotifications", true));
        request.setTelegramChatId("123456");
        request.setWhatsappNumber("+1234567890");
        return request;
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
