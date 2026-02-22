package com.mindtrack.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for messaging integrations (Telegram, WhatsApp).
 */
@Configuration
@ConfigurationProperties(prefix = "mindtrack.messaging")
public class MessagingProperties {

    private Telegram telegram = new Telegram();
    private Whatsapp whatsapp = new Whatsapp();

    public Telegram getTelegram() {
        return telegram;
    }

    public void setTelegram(Telegram telegram) {
        this.telegram = telegram;
    }

    public Whatsapp getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(Whatsapp whatsapp) {
        this.whatsapp = whatsapp;
    }

    /**
     * Telegram Bot API configuration.
     */
    public static class Telegram {
        private String botToken = "";
        private String webhookSecret = "";

        public String getBotToken() {
            return botToken;
        }

        public void setBotToken(String botToken) {
            this.botToken = botToken;
        }

        public String getWebhookSecret() {
            return webhookSecret;
        }

        public void setWebhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
        }
    }

    /**
     * WhatsApp Cloud API configuration.
     */
    public static class Whatsapp {
        private String apiToken = "";
        private String verifyToken = "";
        private String phoneNumberId = "";

        public String getApiToken() {
            return apiToken;
        }

        public void setApiToken(String apiToken) {
            this.apiToken = apiToken;
        }

        public String getVerifyToken() {
            return verifyToken;
        }

        public void setVerifyToken(String verifyToken) {
            this.verifyToken = verifyToken;
        }

        public String getPhoneNumberId() {
            return phoneNumberId;
        }

        public void setPhoneNumberId(String phoneNumberId) {
            this.phoneNumberId = phoneNumberId;
        }
    }
}
