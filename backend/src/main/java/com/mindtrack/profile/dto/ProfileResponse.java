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

    public ProfileResponse() {
    }

    public ProfileResponse(Long id, Long userId, String displayName, String avatarUrl,
                           String timezone, Map<String, Object> notificationPrefs,
                           String telegramChatId, String whatsappNumber) {
        this.id = id;
        this.userId = userId;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.timezone = timezone;
        this.notificationPrefs = notificationPrefs;
        this.telegramChatId = telegramChatId;
        this.whatsappNumber = whatsappNumber;
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
}
