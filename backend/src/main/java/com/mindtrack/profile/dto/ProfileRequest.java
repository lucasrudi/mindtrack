package com.mindtrack.profile.dto;

import java.util.Map;

/**
 * Request DTO for updating user profile.
 */
public class ProfileRequest {

    private String displayName;
    private String avatarUrl;
    private String timezone;
    private Map<String, Object> notificationPrefs;
    private String telegramChatId;
    private String whatsappNumber;

    public ProfileRequest() {
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
}
