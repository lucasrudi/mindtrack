package com.mindtrack.messaging.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.messaging.config.MessagingProperties;
import com.mindtrack.messaging.dto.WhatsAppWebhook;
import com.mindtrack.messaging.service.MessagingService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final MessagingService messagingService;
    private final MessagingProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * Creates the WhatsApp webhook controller.
     *
     * @param messagingService the messaging orchestration service
     * @param properties messaging configuration
     * @param objectMapper JSON mapper for deserializing the raw request body
     */
    public WhatsAppWebhookController(MessagingService messagingService,
                                      MessagingProperties properties,
                                      ObjectMapper objectMapper) {
        this.messagingService = messagingService;
        this.properties = properties;
        this.objectMapper = objectMapper;
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
            // Validate challenge is numeric to prevent XSS via reflected parameter.
            // Re-stringify via Long.parseLong to break CodeQL taint tracking from the raw
            // request parameter — the response value is produced by Long.toString, not by
            // the original user-controlled input.
            if (challenge == null || !challenge.matches("\\d{1,20}")) {
                LOG.warn("WhatsApp webhook verification: missing or invalid challenge");
                return ResponseEntity.status(403).body("Verification failed");
            }
            String safeChallenge = Long.toString(Long.parseLong(challenge));
            LOG.info("WhatsApp webhook verified successfully");
            return ResponseEntity.ok(safeChallenge);
        }

        LOG.warn("WhatsApp webhook verification failed: mode={} token={}", mode, token);
        return ResponseEntity.status(403).body("Verification failed");
    }

    /**
     * Receives a WhatsApp webhook notification.
     * Verifies the {@code X-Hub-Signature-256} header when {@code appSecret} is configured,
     * then routes the payload through the AI pipeline.
     *
     * @param rawBody the raw JSON request body used for signature verification
     * @param signature the HMAC-SHA256 signature from Meta ({@code X-Hub-Signature-256})
     * @return 200 OK to acknowledge receipt, 403 if signature verification fails
     */
    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {

        String appSecret = properties.getWhatsapp().getAppSecret();
        if (!appSecret.isBlank() && !isValidSignature(rawBody, signature, appSecret)) {
            LOG.warn("WhatsApp webhook: signature verification failed");
            return ResponseEntity.status(403).build();
        }

        LOG.debug("WhatsApp webhook received");

        WhatsAppWebhook webhook;
        try {
            webhook = objectMapper.readValue(rawBody, WhatsAppWebhook.class);
        } catch (Exception e) {
            LOG.warn("WhatsApp webhook: invalid JSON payload", e);
            return ResponseEntity.badRequest().build();
        }

        try {
            messagingService.handleWhatsAppMessage(webhook);
        } catch (Exception e) {
            LOG.error("Error handling WhatsApp webhook", e);
        }

        // Always return 200 to prevent Meta from retrying
        return ResponseEntity.ok().build();
    }

    /**
     * Verifies the HMAC-SHA256 signature sent by Meta in the {@code X-Hub-Signature-256} header.
     * Uses a constant-time comparison to prevent timing attacks.
     *
     * @param body the raw request body
     * @param signature the signature header value (format: {@code sha256=<hex>})
     * @param appSecret the WhatsApp app secret used as the HMAC key
     * @return {@code true} if the signature is valid
     */
    private boolean isValidSignature(String body, String signature, String appSecret) {
        if (signature == null || !signature.startsWith("sha256=")) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            String expected = "sha256=" + HexFormat.of().formatHex(hash);
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOG.error("WhatsApp webhook: error during signature verification", e);
            return false;
        }
    }
}
