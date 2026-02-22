package com.mindtrack.messaging.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.messaging.config.MessagingProperties;
import com.mindtrack.messaging.dto.TelegramUpdate;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class TelegramWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessagingService messagingService;

    @MockitoBean
    private MessagingProperties properties;

    @Test
    void shouldAcceptTelegramWebhookWithoutAuth() throws Exception {
        // Telegram webhooks are public endpoints — no JWT needed
        MessagingProperties.Telegram telegram = new MessagingProperties.Telegram();
        telegram.setWebhookSecret("");
        org.mockito.Mockito.when(properties.getTelegram()).thenReturn(telegram);

        TelegramUpdate update = new TelegramUpdate();
        update.setUpdateId(1L);

        mockMvc.perform(post("/api/webhooks/telegram")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());

        verify(messagingService).handleTelegramMessage(any(TelegramUpdate.class));
    }

    @Test
    void shouldRejectInvalidWebhookSecret() throws Exception {
        MessagingProperties.Telegram telegram = new MessagingProperties.Telegram();
        telegram.setWebhookSecret("my-secret");
        org.mockito.Mockito.when(properties.getTelegram()).thenReturn(telegram);

        TelegramUpdate update = new TelegramUpdate();
        update.setUpdateId(1L);

        mockMvc.perform(post("/api/webhooks/telegram")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Telegram-Bot-Api-Secret-Token", "wrong-secret")
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden());

        verify(messagingService, never()).handleTelegramMessage(any());
    }

    @Test
    void shouldAcceptValidWebhookSecret() throws Exception {
        MessagingProperties.Telegram telegram = new MessagingProperties.Telegram();
        telegram.setWebhookSecret("my-secret");
        org.mockito.Mockito.when(properties.getTelegram()).thenReturn(telegram);

        TelegramUpdate update = new TelegramUpdate();
        update.setUpdateId(2L);

        mockMvc.perform(post("/api/webhooks/telegram")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Telegram-Bot-Api-Secret-Token", "my-secret")
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());

        verify(messagingService).handleTelegramMessage(any(TelegramUpdate.class));
    }
}
