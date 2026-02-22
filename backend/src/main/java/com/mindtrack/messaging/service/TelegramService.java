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
 * HTTP client for the Telegram Bot API. Sends text messages to Telegram chats.
 */
@Service
public class TelegramService {

    private static final Logger LOG = LoggerFactory.getLogger(TelegramService.class);
    private static final String TELEGRAM_API_BASE = "https://api.telegram.org/bot";

    private final MessagingProperties properties;
    private final HttpClient httpClient;

    /**
     * Creates the Telegram service.
     *
     * @param properties messaging configuration
     */
    public TelegramService(MessagingProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Sends a text message to a Telegram chat.
     *
     * @param chatId the Telegram chat ID
     * @param text the message text to send
     */
    public void sendMessage(String chatId, String text) {
        String token = properties.getTelegram().getBotToken();
        if (token == null || token.isBlank()) {
            LOG.warn("Telegram bot token not configured, skipping message send");
            return;
        }

        try {
            String url = TELEGRAM_API_BASE + token + "/sendMessage";
            String jsonBody = String.format(
                    "{\"chat_id\":\"%s\",\"text\":\"%s\",\"parse_mode\":\"Markdown\"}",
                    escapeJson(chatId),
                    escapeJson(text)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOG.error("Telegram API error: status={} body={}", response.statusCode(), response.body());
            } else {
                LOG.debug("Telegram message sent to chat={}", chatId);
            }
        } catch (Exception e) {
            LOG.error("Failed to send Telegram message to chat={}", chatId, e);
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
