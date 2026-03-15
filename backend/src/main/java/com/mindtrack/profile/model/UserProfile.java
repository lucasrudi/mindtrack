package com.mindtrack.profile.model;

import com.mindtrack.common.service.KmsEncryptionConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * JPA entity representing a user's profile settings.
 */
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(length = 50)
    private String timezone;

    @Column(name = "notification_prefs", columnDefinition = "JSON")
    private String notificationPrefs;

    @Convert(converter = KmsEncryptionConverter.class)
    @Column(name = "telegram_chat_id", length = 512)
    private String telegramChatId;

    @Convert(converter = KmsEncryptionConverter.class)
    @Column(name = "whatsapp_number", length = 512)
    private String whatsappNumber;

    @Column(name = "tutorial_completed", nullable = false)
    private boolean tutorialCompleted;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted;

    @Column(name = "survey_completed", nullable = false)
    private boolean surveyCompleted;

    @Column(name = "is_patient", nullable = false)
    private boolean patient = true;

    @Column(name = "is_therapist", nullable = false)
    private boolean therapist = false;

    @Column(name = "ai_consent_given", nullable = false)
    private boolean aiConsentGiven = false;

    @Column(name = "anonymized_at")
    private LocalDateTime anonymizedAt;

    public UserProfile() {
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

    public String getNotificationPrefs() {
        return notificationPrefs;
    }

    public void setNotificationPrefs(String notificationPrefs) {
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

    public LocalDateTime getAnonymizedAt() {
        return anonymizedAt;
    }

    public void setAnonymizedAt(LocalDateTime anonymizedAt) {
        this.anonymizedAt = anonymizedAt;
    }
}
