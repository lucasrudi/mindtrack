package com.mindtrack.messaging.service;

import com.mindtrack.messaging.config.MessagingProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * HTTP client for the WhatsApp Cloud API. Sends text messages to WhatsApp numbers.
 */
@Service
@ConditionalOnProperty(prefix = "mindtrack.messaging.whatsapp", name = "enabled", havingValue = "true")
public class WhatsAppService {

    private static final Logger LOG = LoggerFactory.getLogger(WhatsAppService.class);
    private static final String WHATSAPP_API_BASE = "https://graph.facebook.com/v18.0/";
    private static final Pattern WHATSAPP_PHONE_NUMBER = Pattern.compile("^\\+?\\d{8,15}$");
    private static final Pattern CONTROL_CHARACTERS = Pattern.compile("[\\p{Cntrl}&&[^\\n\\r\\t]]");
    private static final Pattern URL_PATTERN = Pattern.compile("(?i)https?://\\S+");

    private final MessagingProperties properties;
    private final HttpClient httpClient;

    /**
     * Creates the WhatsApp service.
     *
     * @param properties messaging configuration
     */
    @Autowired
    public WhatsAppService(MessagingProperties properties) {
        this(properties, HttpClient.newHttpClient());
    }

    WhatsAppService(MessagingProperties properties, HttpClient httpClient) {
        this.properties = properties;
        this.httpClient = httpClient;
    }

    /**
     * Sends a text message to a WhatsApp number.
     *
     * @param phoneNumber the recipient phone number (with country code, no +)
     * @param text the message text to send
     */
    public void sendMessage(String phoneNumber, String text) {
        String token = properties.getWhatsapp().getApiToken();
        String phoneNumberId = properties.getWhatsapp().getPhoneNumberId();
        String safePhoneNumber = sanitizePhoneNumber(phoneNumber);
        String safeText = sanitizeText(text);

        if (token == null || token.isBlank()) {
            LOG.warn("WhatsApp API token not configured, skipping message send");
            return;
        }
        if (safePhoneNumber == null) {
            LOG.warn("Invalid WhatsApp phone number, skipping message send");
            return;
        }

        try {
            String url = WHATSAPP_API_BASE + phoneNumberId + "/messages";
            String jsonBody = String.format(
                    "{\"messaging_product\":\"whatsapp\",\"to\":\"%s\","
                            + "\"type\":\"text\",\"text\":{\"body\":\"%s\"}}",
                    escapeJson(safePhoneNumber),
                    escapeJson(safeText)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOG.error("WhatsApp API error: status={} body={}", response.statusCode(), response.body());
            } else {
                LOG.debug("WhatsApp message sent to phone={}", safePhoneNumber);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("WhatsApp send interrupted for phone={}", safePhoneNumber, e);
        } catch (IOException e) {
            LOG.error("Failed to send WhatsApp message to phone={}", safePhoneNumber, e);
        }
    }

    private String sanitizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        String trimmed = phoneNumber.trim();
        String digitsOnly = trimmed.replaceAll("\\D", "");
        String canonical = trimmed.startsWith("+") ? "+" + digitsOnly : digitsOnly;
        return WHATSAPP_PHONE_NUMBER.matcher(canonical).matches() ? canonical : null;
    }

    private String sanitizeText(String text) {
        if (text == null) {
            return "";
        }
        String withoutUrls = URL_PATTERN.matcher(text).replaceAll("[link removed]");
        return CONTROL_CHARACTERS.matcher(withoutUrls).replaceAll("");
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
