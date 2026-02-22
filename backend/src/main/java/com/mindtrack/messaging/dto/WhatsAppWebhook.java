package com.mindtrack.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO representing a WhatsApp Cloud API webhook notification.
 * Only maps the fields needed for text message handling.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppWebhook {

    private String object;

    private List<Entry> entry;

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public List<Entry> getEntry() {
        return entry;
    }

    public void setEntry(List<Entry> entry) {
        this.entry = entry;
    }

    /**
     * Represents a webhook entry.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {

        private String id;

        private List<Change> changes;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<Change> getChanges() {
            return changes;
        }

        public void setChanges(List<Change> changes) {
            this.changes = changes;
        }
    }

    /**
     * Represents a change within an entry.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Change {

        private Value value;

        private String field;

        public Value getValue() {
            return value;
        }

        public void setValue(Value value) {
            this.value = value;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }
    }

    /**
     * Represents the value payload containing messages.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {

        @JsonProperty("messaging_product")
        private String messagingProduct;

        private List<Contact> contacts;

        private List<Message> messages;

        public String getMessagingProduct() {
            return messagingProduct;
        }

        public void setMessagingProduct(String messagingProduct) {
            this.messagingProduct = messagingProduct;
        }

        public List<Contact> getContacts() {
            return contacts;
        }

        public void setContacts(List<Contact> contacts) {
            this.contacts = contacts;
        }

        public List<Message> getMessages() {
            return messages;
        }

        public void setMessages(List<Message> messages) {
            this.messages = messages;
        }
    }

    /**
     * Represents a WhatsApp contact.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Contact {

        @JsonProperty("wa_id")
        private String waId;

        private Profile profile;

        public String getWaId() {
            return waId;
        }

        public void setWaId(String waId) {
            this.waId = waId;
        }

        public Profile getProfile() {
            return profile;
        }

        public void setProfile(Profile profile) {
            this.profile = profile;
        }
    }

    /**
     * Represents a WhatsApp user profile.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * Represents a WhatsApp message.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {

        private String from;

        private String id;

        private String timestamp;

        private String type;

        private Text text;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Text getText() {
            return text;
        }

        public void setText(Text text) {
            this.text = text;
        }
    }

    /**
     * Represents text content in a message.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Text {

        private String body;

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }
}
