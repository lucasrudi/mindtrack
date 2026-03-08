package com.mindtrack.messaging.controller;

import com.mindtrack.messaging.config.MessagingProperties;
import com.mindtrack.messaging.dto.TelegramUpdate;
import com.mindtrack.messaging.service.MessagingService;
import jakarta.annotation.PostConstruct;
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
 * A non-blank {@code mindtrack.messaging.telegram.webhook-secret} must be configured;
 * every incoming request is validated against that secret.
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
     * Fails startup when the Telegram webhook secret is blank.
     * A blank secret would leave the endpoint open to unauthenticated webhook delivery.
     */
    @PostConstruct
    public void validateConfig() {
        String secret = properties.getTelegram().getWebhookSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "mindtrack.messaging.telegram.webhook-secret must not be blank. "
                    + "Configure a strong secret to secure the Telegram webhook endpoint.");
        }
        LOG.info("Telegram webhook secret is configured — endpoint will enforce token validation.");
    }

    /**
     * Receives a Telegram webhook update.
     * Always validates the X-Telegram-Bot-Api-Secret-Token header; returns 403 on mismatch.
     *
     * @param update the Telegram update payload
     * @param secretToken the X-Telegram-Bot-Api-Secret-Token header (required by Telegram)
     * @return 200 OK to acknowledge receipt, or 403 if the secret token is invalid
     */
    @PostMapping
    public ResponseEntity<Void> handleUpdate(
            @RequestBody TelegramUpdate update,
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false)
            String secretToken) {

        String configuredSecret = properties.getTelegram().getWebhookSecret();
        if (!configuredSecret.equals(secretToken)) {
            LOG.warn("Telegram webhook: invalid or missing secret token — rejecting request");
            return ResponseEntity.status(403).build();
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
