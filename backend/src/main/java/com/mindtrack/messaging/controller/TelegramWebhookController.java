package com.mindtrack.messaging.controller;

import com.mindtrack.messaging.config.MessagingProperties;
import com.mindtrack.messaging.dto.TelegramUpdate;
import com.mindtrack.messaging.service.MessagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhook endpoint for Telegram Bot API updates.
 * This endpoint is publicly accessible (no JWT auth) and receives updates from Telegram.
 *
 * <p>Endpoint: POST /api/webhooks/telegram
 */
@RestController
@RequestMapping("/api/webhooks/telegram")
public class TelegramWebhookController {

    private static final Logger LOG = LoggerFactory.getLogger(TelegramWebhookController.class);

    private final MessagingService messagingService;
    private final MessagingProperties properties;

    /**
     * Creates the Telegram webhook controller.
     *
     * @param messagingService the messaging orchestration service
     * @param properties messaging configuration
     */
    public TelegramWebhookController(MessagingService messagingService,
                                      MessagingProperties properties) {
        this.messagingService = messagingService;
        this.properties = properties;
    }

    /**
     * Receives a Telegram webhook update.
     * Validates the secret token header if configured, then processes the update asynchronously.
     *
     * @param update the Telegram update payload
     * @param secretToken the optional X-Telegram-Bot-Api-Secret-Token header
     * @return 200 OK to acknowledge receipt
     */
    @PostMapping
    public ResponseEntity<Void> handleUpdate(
            @RequestBody TelegramUpdate update,
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false)
            String secretToken) {

        // Validate webhook secret if configured
        String configuredSecret = properties.getTelegram().getWebhookSecret();
        if (configuredSecret != null && !configuredSecret.isBlank()) {
            if (!configuredSecret.equals(secretToken)) {
                LOG.warn("Telegram webhook: invalid secret token");
                return ResponseEntity.status(403).build();
            }
        }

        LOG.debug("Telegram webhook received update_id={}", update.getUpdateId());

        try {
            messagingService.handleTelegramMessage(update);
        } catch (Exception e) {
            LOG.error("Error handling Telegram webhook", e);
        }

        // Always return 200 to prevent Telegram from retrying
        return ResponseEntity.ok().build();
    }
}
