package com.mindtrack.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing a Telegram webhook update.
 * Only maps the fields needed for text message handling.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramUpdate {

    @JsonProperty("update_id")
    private Long updateId;

    private TelegramMessage message;

    public Long getUpdateId() {
        return updateId;
    }

    public void setUpdateId(Long updateId) {
        this.updateId = updateId;
    }

    public TelegramMessage getMessage() {
        return message;
    }

    public void setMessage(TelegramMessage message) {
        this.message = message;
    }

    /**
     * Represents a Telegram message within an update.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelegramMessage {

        @JsonProperty("message_id")
        private Long messageId;

        private TelegramChat chat;

        private TelegramUser from;

        private String text;

        public Long getMessageId() {
            return messageId;
        }

        public void setMessageId(Long messageId) {
            this.messageId = messageId;
        }

        public TelegramChat getChat() {
            return chat;
        }

        public void setChat(TelegramChat chat) {
            this.chat = chat;
        }

        public TelegramUser getFrom() {
            return from;
        }

        public void setFrom(TelegramUser from) {
            this.from = from;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    /**
     * Represents a Telegram chat.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelegramChat {

        private Long id;

        private String type;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    /**
     * Represents a Telegram user.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelegramUser {

        private Long id;

        @JsonProperty("first_name")
        private String firstName;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
    }
}
