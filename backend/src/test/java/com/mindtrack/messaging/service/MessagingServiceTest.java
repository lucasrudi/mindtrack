package com.mindtrack.messaging.service;

import com.mindtrack.ai.dto.ChatRequest;
import com.mindtrack.ai.dto.ChatResponse;
import com.mindtrack.ai.model.Channel;
import com.mindtrack.ai.service.ConversationService;
import com.mindtrack.messaging.dto.TelegramUpdate;
import com.mindtrack.messaging.dto.WhatsAppWebhook;
import com.mindtrack.profile.model.UserProfile;
import com.mindtrack.profile.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessagingServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private ConversationService conversationService;

    @Mock
    private TelegramService telegramService;

    @Mock
    private WhatsAppService whatsAppService;

    private MessagingService messagingService;

    @BeforeEach
    void setUp() {
        messagingService = new MessagingService(
                userProfileRepository, conversationService, telegramService, whatsAppService);
    }

    // --- Telegram tests ---

    @Test
    void shouldHandleTelegramMessageFromLinkedUser() {
        TelegramUpdate update = createTelegramUpdate("12345", "How am I doing?");

        UserProfile profile = new UserProfile();
        profile.setUserId(1L);
        profile.setTelegramChatId("12345");
        when(userProfileRepository.findByTelegramChatId("12345")).thenReturn(Optional.of(profile));

        ChatResponse response = new ChatResponse(1L, 10L, "You're doing great!", false, 100);
        when(conversationService.chatWithChannel(eq(1L), any(ChatRequest.class), eq(Channel.TELEGRAM)))
                .thenReturn(response);

        messagingService.handleTelegramMessage(update);

        verify(telegramService).sendMessage("12345", "You're doing great!");
    }

    @Test
    void shouldReplyUnlinkedForUnknownTelegramChat() {
        TelegramUpdate update = createTelegramUpdate("99999", "Hello");
        when(userProfileRepository.findByTelegramChatId("99999")).thenReturn(Optional.empty());

        messagingService.handleTelegramMessage(update);

        verify(telegramService).sendMessage(eq("99999"), any(String.class));
        verify(conversationService, never()).chatWithChannel(any(), any(), any());
    }

    @Test
    void shouldHandleTelegramStartCommand() {
        TelegramUpdate update = createTelegramUpdate("12345", "/start");

        messagingService.handleTelegramMessage(update);

        verify(telegramService).sendMessage(eq("12345"), any(String.class));
        verify(conversationService, never()).chatWithChannel(any(), any(), any());
    }

    @Test
    void shouldIgnoreNonTextTelegramUpdate() {
        TelegramUpdate update = new TelegramUpdate();
        update.setUpdateId(1L);
        // No message set

        messagingService.handleTelegramMessage(update);

        verify(telegramService, never()).sendMessage(any(), any());
    }

    @Test
    void shouldHandleTelegramAiError() {
        TelegramUpdate update = createTelegramUpdate("12345", "Check in");

        UserProfile profile = new UserProfile();
        profile.setUserId(1L);
        when(userProfileRepository.findByTelegramChatId("12345")).thenReturn(Optional.of(profile));
        when(conversationService.chatWithChannel(eq(1L), any(ChatRequest.class), eq(Channel.TELEGRAM)))
                .thenThrow(new RuntimeException("AI error"));

        messagingService.handleTelegramMessage(update);

        verify(telegramService).sendMessage(eq("12345"), any(String.class));
    }

    // --- WhatsApp tests ---

    @Test
    void shouldHandleWhatsAppMessageFromLinkedUser() {
        WhatsAppWebhook webhook = createWhatsAppWebhook("+1234567890", "How's my progress?");

        UserProfile profile = new UserProfile();
        profile.setUserId(2L);
        profile.setWhatsappNumber("+1234567890");
        when(userProfileRepository.findByWhatsappNumber("+1234567890")).thenReturn(Optional.of(profile));

        ChatResponse response = new ChatResponse(2L, 20L, "Great progress!", false, 80);
        when(conversationService.chatWithChannel(eq(2L), any(ChatRequest.class), eq(Channel.WHATSAPP)))
                .thenReturn(response);

        messagingService.handleWhatsAppMessage(webhook);

        verify(whatsAppService).sendMessage("+1234567890", "Great progress!");
    }

    @Test
    void shouldReplyUnlinkedForUnknownWhatsAppNumber() {
        WhatsAppWebhook webhook = createWhatsAppWebhook("+9999999999", "Hello");
        when(userProfileRepository.findByWhatsappNumber("+9999999999")).thenReturn(Optional.empty());

        messagingService.handleWhatsAppMessage(webhook);

        verify(whatsAppService).sendMessage(eq("+9999999999"), any(String.class));
        verify(conversationService, never()).chatWithChannel(any(), any(), any());
    }

    @Test
    void shouldIgnoreNonTextWhatsAppMessage() {
        WhatsAppWebhook webhook = createWhatsAppWebhookNonText("+1234567890");

        messagingService.handleWhatsAppMessage(webhook);

        verify(whatsAppService, never()).sendMessage(any(), any());
    }

    @Test
    void shouldHandleWhatsAppAiError() {
        WhatsAppWebhook webhook = createWhatsAppWebhook("+1234567890", "Check in");

        UserProfile profile = new UserProfile();
        profile.setUserId(2L);
        when(userProfileRepository.findByWhatsappNumber("+1234567890")).thenReturn(Optional.of(profile));
        when(conversationService.chatWithChannel(eq(2L), any(ChatRequest.class), eq(Channel.WHATSAPP)))
                .thenThrow(new RuntimeException("AI error"));

        messagingService.handleWhatsAppMessage(webhook);

        verify(whatsAppService).sendMessage(eq("+1234567890"), any(String.class));
    }

    @Test
    void shouldHandleEmptyWhatsAppWebhook() {
        WhatsAppWebhook webhook = new WhatsAppWebhook();
        // No entry set

        messagingService.handleWhatsAppMessage(webhook);

        verify(whatsAppService, never()).sendMessage(any(), any());
    }

    // --- Helpers ---

    private TelegramUpdate createTelegramUpdate(String chatId, String text) {
        TelegramUpdate update = new TelegramUpdate();
        update.setUpdateId(1L);

        TelegramUpdate.TelegramMessage message = new TelegramUpdate.TelegramMessage();
        message.setMessageId(1L);
        message.setText(text);

        TelegramUpdate.TelegramChat chat = new TelegramUpdate.TelegramChat();
        chat.setId(Long.parseLong(chatId));
        chat.setType("private");
        message.setChat(chat);

        TelegramUpdate.TelegramUser from = new TelegramUpdate.TelegramUser();
        from.setId(Long.parseLong(chatId));
        from.setFirstName("Test");
        message.setFrom(from);

        update.setMessage(message);
        return update;
    }

    private WhatsAppWebhook createWhatsAppWebhook(String phoneNumber, String text) {
        WhatsAppWebhook webhook = new WhatsAppWebhook();
        webhook.setObject("whatsapp_business_account");

        WhatsAppWebhook.Text textObj = new WhatsAppWebhook.Text();
        textObj.setBody(text);

        WhatsAppWebhook.Message msg = new WhatsAppWebhook.Message();
        msg.setFrom(phoneNumber);
        msg.setId("wamid.test123");
        msg.setType("text");
        msg.setText(textObj);

        WhatsAppWebhook.Value value = new WhatsAppWebhook.Value();
        value.setMessagingProduct("whatsapp");
        value.setMessages(List.of(msg));

        WhatsAppWebhook.Change change = new WhatsAppWebhook.Change();
        change.setField("messages");
        change.setValue(value);

        WhatsAppWebhook.Entry entry = new WhatsAppWebhook.Entry();
        entry.setId("123");
        entry.setChanges(List.of(change));

        webhook.setEntry(List.of(entry));
        return webhook;
    }

    private WhatsAppWebhook createWhatsAppWebhookNonText(String phoneNumber) {
        WhatsAppWebhook webhook = new WhatsAppWebhook();
        webhook.setObject("whatsapp_business_account");

        WhatsAppWebhook.Message msg = new WhatsAppWebhook.Message();
        msg.setFrom(phoneNumber);
        msg.setId("wamid.test456");
        msg.setType("image");
        // No text set

        WhatsAppWebhook.Value value = new WhatsAppWebhook.Value();
        value.setMessagingProduct("whatsapp");
        value.setMessages(List.of(msg));

        WhatsAppWebhook.Change change = new WhatsAppWebhook.Change();
        change.setField("messages");
        change.setValue(value);

        WhatsAppWebhook.Entry entry = new WhatsAppWebhook.Entry();
        entry.setId("123");
        entry.setChanges(List.of(change));

        webhook.setEntry(List.of(entry));
        return webhook;
    }
}
