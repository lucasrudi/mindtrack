package com.mindtrack.profile.dto;

import java.util.Map;

/**
 * Response DTO for user profile data.
 */
public class ProfileResponse {

    private Long id;
    private Long userId;
    private String displayName;
    private String avatarUrl;
    private String timezone;
    private Map<String, Object> notificationPrefs;
    private String telegramChatId;
    private String whatsappNumber;
    private boolean tutorialCompleted;
    private boolean onboardingCompleted;
    private boolean surveyCompleted;
    private boolean patient;
    private boolean therapist;
    private boolean aiConsentGiven;

    public ProfileResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Map<String, Object> getNotificationPrefs() {
        return notificationPrefs;
    }

    public void setNotificationPrefs(Map<String, Object> notificationPrefs) {
        this.notificationPrefs = notificationPrefs;
    }

    public String getTelegramChatId() {
        return telegramChatId;
    }

    public void setTelegramChatId(String telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    public String getWhatsappNumber() {
        return whatsappNumber;
    }

    public void setWhatsappNumber(String whatsappNumber) {
        this.whatsappNumber = whatsappNumber;
    }

    public boolean isTutorialCompleted() {
        return tutorialCompleted;
    }

    public void setTutorialCompleted(boolean tutorialCompleted) {
        this.tutorialCompleted = tutorialCompleted;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public void setOnboardingCompleted(boolean onboardingCompleted) {
        this.onboardingCompleted = onboardingCompleted;
    }

    public boolean isSurveyCompleted() {
        return surveyCompleted;
    }

    public void setSurveyCompleted(boolean surveyCompleted) {
        this.surveyCompleted = surveyCompleted;
    }

    public boolean isPatient() {
        return patient;
    }

    public void setPatient(boolean patient) {
        this.patient = patient;
    }

    public boolean isTherapist() {
        return therapist;
    }

    public void setTherapist(boolean therapist) {
        this.therapist = therapist;
    }

    public boolean isAiConsentGiven() {
        return aiConsentGiven;
    }

    public void setAiConsentGiven(boolean aiConsentGiven) {
        this.aiConsentGiven = aiConsentGiven;
    }
}
