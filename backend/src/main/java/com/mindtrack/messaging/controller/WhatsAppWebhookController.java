package com.mindtrack.messaging.controller;

import com.mindtrack.messaging.config.MessagingProperties;
import com.mindtrack.messaging.dto.WhatsAppWebhook;
import com.mindtrack.messaging.service.MessagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhook endpoint for the WhatsApp Cloud API.
 * This endpoint is publicly accessible (no JWT auth) and handles both verification and messages.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /api/webhooks/whatsapp — Webhook verification (handshake)</li>
 *   <li>POST /api/webhooks/whatsapp — Incoming message notifications</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/webhooks/whatsapp")
public class WhatsAppWebhookController {

    private static final Logger LOG = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    private final MessagingService messagingService;
    private final MessagingProperties properties;

    /**
     * Creates the WhatsApp webhook controller.
     *
     * @param messagingService the messaging orchestration service
     * @param properties messaging configuration
     */
    public WhatsAppWebhookController(MessagingService messagingService,
                                      MessagingProperties properties) {
        this.messagingService = messagingService;
        this.properties = properties;
    }

    /**
     * WhatsApp webhook verification endpoint.
     * Meta sends a GET request with a challenge during webhook setup.
     *
     * @param mode the hub.mode parameter (should be "subscribe")
     * @param token the hub.verify_token parameter (must match configured token)
     * @param challenge the hub.challenge parameter to echo back
     * @return the challenge string if verification succeeds, 403 otherwise
     */
    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String token,
            @RequestParam(value = "hub.challenge", required = false) String challenge) {

        String configuredToken = properties.getWhatsapp().getVerifyToken();

        if ("subscribe".equals(mode) && configuredToken.equals(token)) {
            LOG.info("WhatsApp webhook verified successfully");
            return ResponseEntity.ok(challenge);
        }

        LOG.warn("WhatsApp webhook verification failed: mode={} token={}", mode, token);
        return ResponseEntity.status(403).body("Verification failed");
    }

    /**
     * Receives a WhatsApp webhook notification.
     * Processes incoming messages and routes them through the AI pipeline.
     *
     * @param webhook the WhatsApp webhook payload
     * @return 200 OK to acknowledge receipt
     */
    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody WhatsAppWebhook webhook) {
        LOG.debug("WhatsApp webhook received");

        try {
            messagingService.handleWhatsAppMessage(webhook);
        } catch (Exception e) {
            LOG.error("Error handling WhatsApp webhook", e);
        }

        // Always return 200 to prevent retries
        return ResponseEntity.ok().build();
    }
}
