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
import org.springframework.stereotype.Service;

/**
 * HTTP client for the Telegram Bot API. Sends text messages to Telegram chats.
 */
@Service
public class TelegramService {

    private static final Logger LOG = LoggerFactory.getLogger(TelegramService.class);
    private static final String TELEGRAM_API_BASE = "https://api.telegram.org/bot";
    private static final Pattern TELEGRAM_CHAT_ID = Pattern.compile("^-?\\d{5,20}$");
    private static final Pattern CONTROL_CHARACTERS = Pattern.compile("[\\p{Cntrl}&&[^\\n\\r\\t]]");
    private static final Pattern URL_PATTERN = Pattern.compile("(?i)https?://\\S+");

    private final MessagingProperties properties;
    private final HttpClient httpClient;

    /**
     * Creates the Telegram service.
     *
     * @param properties messaging configuration
     */
    @Autowired
    public TelegramService(MessagingProperties properties) {
        this(properties, HttpClient.newHttpClient());
    }

    TelegramService(MessagingProperties properties, HttpClient httpClient) {
        this.properties = properties;
        this.httpClient = httpClient;
    }

    /**
     * Sends a text message to a Telegram chat.
     *
     * @param chatId the Telegram chat ID
     * @param text the message text to send
     */
    public void sendMessage(String chatId, String text) {
        String token = properties.getTelegram().getBotToken();
        String safeChatId = sanitizeChatId(chatId);
        String safeText = sanitizeText(text);
        if (token == null || token.isBlank()) {
            LOG.warn("Telegram bot token not configured, skipping message send");
            return;
        }
        if (safeChatId == null) {
            LOG.warn("Invalid Telegram chat id, skipping message send");
            return;
        }

        try {
            String url = TELEGRAM_API_BASE + token + "/sendMessage";
            String jsonBody = String.format(
                    "{\"chat_id\":\"%s\",\"text\":\"%s\",\"parse_mode\":\"Markdown\"}",
                    escapeJson(safeChatId),
                    escapeJson(safeText)
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
                LOG.debug("Telegram message sent to chat={}", safeChatId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Telegram send interrupted for chat={}", safeChatId, e);
        } catch (IOException e) {
            LOG.error("Failed to send Telegram message to chat={}", safeChatId, e);
        }
    }

    private String sanitizeChatId(String chatId) {
        if (chatId == null) {
            return null;
        }
        String trimmed = chatId.trim();
        return TELEGRAM_CHAT_ID.matcher(trimmed).matches() ? trimmed : null;
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
