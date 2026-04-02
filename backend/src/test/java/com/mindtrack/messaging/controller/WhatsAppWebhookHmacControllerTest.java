package com.mindtrack.messaging.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.messaging.dto.WhatsAppWebhook;
import com.mindtrack.messaging.service.MessagingService;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HMAC-SHA256 signature verification for the WhatsApp webhook endpoint.
 * Requires a non-blank {@code appSecret} to activate signature verification.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class WhatsAppWebhookHmacControllerTest {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String VERIFY_TOKEN = "test-verify-token";
    private static final String APP_SECRET = randomSecret();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessagingService messagingService;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("mindtrack.messaging.whatsapp.enabled", () -> "true");
        registry.add("mindtrack.messaging.whatsapp.verify-token", () -> VERIFY_TOKEN);
        registry.add("mindtrack.messaging.whatsapp.app-secret", () -> APP_SECRET);
    }

    @Test
    void shouldRejectWebhookWithInvalidHmacSignature() throws Exception {
        WhatsAppWebhook.Change change = new WhatsAppWebhook.Change();
        change.setField("messages");

        WhatsAppWebhook.Entry entry = new WhatsAppWebhook.Entry();
        entry.setId("123");
        entry.setChanges(List.of(change));

        WhatsAppWebhook webhook = new WhatsAppWebhook();
        webhook.setObject("whatsapp_business_account");
        webhook.setEntry(List.of(entry));

        String body = objectMapper.writeValueAsString(webhook);

        mockMvc.perform(post("/api/webhooks/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", "sha256=deadbeefdeadbeef")
                        .content(body))
                .andExpect(status().isForbidden());

        verify(messagingService, never()).handleWhatsAppMessage(any());
    }

    @Test
    void shouldAcceptWebhookWithValidHmacSignature() throws Exception {
        WhatsAppWebhook.Change change = new WhatsAppWebhook.Change();
        change.setField("messages");

        WhatsAppWebhook.Entry entry = new WhatsAppWebhook.Entry();
        entry.setId("123456789");
        entry.setChanges(List.of(change));

        WhatsAppWebhook webhook = new WhatsAppWebhook();
        webhook.setObject("whatsapp_business_account");
        webhook.setEntry(List.of(entry));

        String body = objectMapper.writeValueAsString(webhook);
        String signature = computeHmac(body, APP_SECRET);

        mockMvc.perform(post("/api/webhooks/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", signature)
                        .content(body))
                .andExpect(status().isOk());

        verify(messagingService).handleWhatsAppMessage(any(WhatsAppWebhook.class));
    }

    @Test
    void shouldRejectWebhookWhenSignatureHeaderIsMissing() throws Exception {
        WhatsAppWebhook.Change change = new WhatsAppWebhook.Change();
        change.setField("messages");

        WhatsAppWebhook.Entry entry = new WhatsAppWebhook.Entry();
        entry.setId("123");
        entry.setChanges(List.of(change));

        WhatsAppWebhook webhook = new WhatsAppWebhook();
        webhook.setObject("whatsapp_business_account");
        webhook.setEntry(List.of(entry));

        mockMvc.perform(post("/api/webhooks/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        // No X-Hub-Signature-256 header
                        .content(objectMapper.writeValueAsString(webhook)))
                .andExpect(status().isForbidden());

        verify(messagingService, never()).handleWhatsAppMessage(any());
    }

    @Test
    void shouldRejectWebhookWithSignatureForWrongSecret() throws Exception {
        WhatsAppWebhook.Change change = new WhatsAppWebhook.Change();
        change.setField("messages");

        WhatsAppWebhook.Entry entry = new WhatsAppWebhook.Entry();
        entry.setId("123456789");
        entry.setChanges(List.of(change));

        WhatsAppWebhook webhook = new WhatsAppWebhook();
        webhook.setObject("whatsapp_business_account");
        webhook.setEntry(List.of(entry));

        String body = objectMapper.writeValueAsString(webhook);
        // Compute HMAC with the WRONG secret
        String wrongSignature = computeHmac(body, randomSecret());

        mockMvc.perform(post("/api/webhooks/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", wrongSignature)
                        .content(body))
                .andExpect(status().isForbidden());

        verify(messagingService, never()).handleWhatsAppMessage(any());
    }

    private String computeHmac(String body, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        return "sha256=" + HexFormat.of().formatHex(hash);
    }

    private static String randomSecret() {
        byte[] secret = new byte[32];
        SECURE_RANDOM.nextBytes(secret);
        return HexFormat.of().formatHex(secret);
    }
}
