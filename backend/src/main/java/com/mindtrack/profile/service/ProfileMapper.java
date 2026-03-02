package com.mindtrack.profile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.profile.dto.ProfileRequest;
import com.mindtrack.profile.dto.ProfileResponse;
import com.mindtrack.profile.model.UserProfile;
import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Maps between UserProfile entities and DTOs.
 */
@Component
public class ProfileMapper {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileMapper.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };

    private final ObjectMapper objectMapper;

    public ProfileMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converts a UserProfile entity to a response DTO.
     */
    public ProfileResponse toResponse(UserProfile profile) {
        ProfileResponse response = new ProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUserId());
        response.setDisplayName(profile.getDisplayName());
        response.setAvatarUrl(profile.getAvatarUrl());
        response.setTimezone(profile.getTimezone());
        response.setNotificationPrefs(parseJson(profile.getNotificationPrefs()));
        response.setTelegramChatId(profile.getTelegramChatId());
        response.setWhatsappNumber(profile.getWhatsappNumber());
        response.setTutorialCompleted(profile.isTutorialCompleted());
        response.setOnboardingCompleted(profile.isOnboardingCompleted());
        response.setSurveyCompleted(profile.isSurveyCompleted());
        response.setPatient(profile.isPatient());
        response.setTherapist(profile.isTherapist());
        return response;
    }

    /**
     * Applies request DTO fields to a UserProfile entity.
     */
    public void applyRequest(ProfileRequest request, UserProfile profile) {
        profile.setDisplayName(request.getDisplayName());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setTimezone(request.getTimezone());
        profile.setNotificationPrefs(serializeJson(request.getNotificationPrefs()));
        profile.setTelegramChatId(request.getTelegramChatId());
        profile.setWhatsappNumber(request.getWhatsappNumber());
        if (request.getTutorialCompleted() != null) {
            profile.setTutorialCompleted(request.getTutorialCompleted());
        }
    }

    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (JsonProcessingException ex) {
            LOG.warn("Failed to parse notification prefs JSON: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }

    private String serializeJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException ex) {
            LOG.warn("Failed to serialize notification prefs: {}", ex.getMessage());
            return null;
        }
    }
}
