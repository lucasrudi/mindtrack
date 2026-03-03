package com.mindtrack.messaging.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.messaging.config.MessagingProperties;
import com.mindtrack.messaging.dto.WhatsAppWebhook;
import com.mindtrack.messaging.service.MessagingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class WhatsAppWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessagingService messagingService;

    @MockitoBean
    private MessagingProperties properties;

    @Test
    void shouldVerifyWebhookWithCorrectToken() throws Exception {
        MessagingProperties.Whatsapp whatsapp = new MessagingProperties.Whatsapp();
        whatsapp.setVerifyToken("test-verify-token");
        org.mockito.Mockito.when(properties.getWhatsapp()).thenReturn(whatsapp);

        // hub.challenge is a numeric string per Meta's webhook API spec
        mockMvc.perform(get("/api/webhooks/whatsapp")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "test-verify-token")
                        .param("hub.challenge", "12345"))
                .andExpect(status().isOk())
                .andExpect(content().string("12345"));
    }

    @Test
    void shouldRejectWebhookWithInvalidToken() throws Exception {
        MessagingProperties.Whatsapp whatsapp = new MessagingProperties.Whatsapp();
        whatsapp.setVerifyToken("test-verify-token");
        org.mockito.Mockito.when(properties.getWhatsapp()).thenReturn(whatsapp);

        mockMvc.perform(get("/api/webhooks/whatsapp")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "wrong-token")
                        .param("hub.challenge", "challenge-12345"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAcceptWhatsAppWebhookWithoutAuth() throws Exception {
        // WhatsApp webhooks are public endpoints — no JWT needed.
        // appSecret is blank so signature verification is skipped.
        org.mockito.Mockito.when(properties.getWhatsapp()).thenReturn(new MessagingProperties.Whatsapp());

        WhatsAppWebhook webhook = new WhatsAppWebhook();
        webhook.setObject("whatsapp_business_account");

        mockMvc.perform(post("/api/webhooks/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhook)))
                .andExpect(status().isOk());

        verify(messagingService).handleWhatsAppMessage(any(WhatsAppWebhook.class));
    }

    @Test
    void shouldRejectVerificationWithWrongMode() throws Exception {
        MessagingProperties.Whatsapp whatsapp = new MessagingProperties.Whatsapp();
        whatsapp.setVerifyToken("test-verify-token");
        org.mockito.Mockito.when(properties.getWhatsapp()).thenReturn(whatsapp);

        mockMvc.perform(get("/api/webhooks/whatsapp")
                        .param("hub.mode", "unsubscribe")
                        .param("hub.verify_token", "test-verify-token")
                        .param("hub.challenge", "challenge-12345"))
                .andExpect(status().isForbidden());
    }
}
