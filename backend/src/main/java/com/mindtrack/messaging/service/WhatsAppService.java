package com.mindtrack.messaging.service;

import com.mindtrack.messaging.config.MessagingProperties;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * HTTP client for the WhatsApp Cloud API. Sends text messages to WhatsApp numbers.
 */
@Service
public class WhatsAppService {

    private static final Logger LOG = LoggerFactory.getLogger(WhatsAppService.class);
    private static final String WHATSAPP_API_BASE = "https://graph.facebook.com/v18.0/";

    private final MessagingProperties properties;
    private final HttpClient httpClient;

    /**
     * Creates the WhatsApp service.
     *
     * @param properties messaging configuration
     */
    public WhatsAppService(MessagingProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newHttpClient();
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

        if (token == null || token.isBlank()) {
            LOG.warn("WhatsApp API token not configured, skipping message send");
            return;
        }

        try {
            String url = WHATSAPP_API_BASE + phoneNumberId + "/messages";
            String jsonBody = String.format(
                    "{\"messaging_product\":\"whatsapp\",\"to\":\"%s\","
                            + "\"type\":\"text\",\"text\":{\"body\":\"%s\"}}",
                    escapeJson(phoneNumber),
                    escapeJson(text)
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
                LOG.debug("WhatsApp message sent to phone={}", phoneNumber);
            }
        } catch (Exception e) {
            LOG.error("Failed to send WhatsApp message to phone={}", phoneNumber, e);
        }
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
