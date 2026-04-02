package com.mindtrack.messaging.service;

import com.mindtrack.ai.dto.ChatRequest;
import com.mindtrack.ai.dto.ChatResponse;
import com.mindtrack.ai.model.Channel;
import com.mindtrack.ai.model.ConversationType;
import com.mindtrack.ai.service.ConversationService;
import com.mindtrack.messaging.dto.TelegramUpdate;
import com.mindtrack.messaging.dto.WhatsAppWebhook;
import com.mindtrack.profile.model.UserProfile;
import com.mindtrack.profile.repository.UserProfileRepository;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * Orchestrates inbound messages from Telegram and WhatsApp.
 * Resolves the user from their linked messaging account, routes through the AI pipeline,
 * and sends the AI response back via the originating channel.
 */
@Service
public class MessagingService {

    private static final Logger LOG = LoggerFactory.getLogger(MessagingService.class);
    private static final Pattern TELEGRAM_CHAT_ID = Pattern.compile("^-?\\d{5,20}$");
    private static final Pattern WHATSAPP_PHONE_NUMBER = Pattern.compile("^\\+?\\d{8,15}$");

    private final UserProfileRepository userProfileRepository;
    private final ConversationService conversationService;
    private final TelegramService telegramService;
    @Nullable
    private final WhatsAppService whatsAppService;

    /**
     * Creates the messaging service.
     *
     * @param userProfileRepository user profile repo for resolving linked accounts
     * @param conversationService AI conversation pipeline
     * @param telegramService Telegram Bot API client
     * @param whatsAppService WhatsApp Cloud API client (absent when WhatsApp is disabled)
     */
    public MessagingService(UserProfileRepository userProfileRepository,
                            ConversationService conversationService,
                            TelegramService telegramService,
                            Optional<WhatsAppService> whatsAppService) {
        this.userProfileRepository = userProfileRepository;
        this.conversationService = conversationService;
        this.telegramService = telegramService;
        this.whatsAppService = whatsAppService.orElse(null);
    }

    /**
     * Handles an inbound Telegram message.
     * Resolves the user by Telegram chat ID, processes via AI, and replies.
     *
     * @param update the Telegram webhook update
     */
    public void handleTelegramMessage(TelegramUpdate update) {
        if (update.getMessage() == null || update.getMessage().getText() == null) {
            LOG.debug("Ignoring non-text Telegram update");
            return;
        }

        String chatId = String.valueOf(update.getMessage().getChat().getId());
        String text = update.getMessage().getText();
        String safeChatId = sanitizeTelegramChatId(chatId);

        if (safeChatId == null) {
            LOG.warn("Ignoring Telegram message with invalid chat id");
            return;
        }

        LOG.info("Telegram message received from chat={}", safeChatId);

        // Handle /start command
        if (text.startsWith("/start")) {
            telegramService.sendMessage(safeChatId,
                    "Welcome to MindTrack! Link your Telegram account in your profile settings "
                            + "to start chatting with your AI coach.");
            return;
        }

        // Resolve user by Telegram chat ID.
        // The column is KMS-encrypted, so exact-match DB queries do not work; we load all
        // linked profiles and compare against the decrypted value in application code.
        Optional<UserProfile> profileOpt = userProfileRepository.findAllByTelegramChatIdNotNull()
                .stream()
                .filter(p -> safeChatId.equals(sanitizeTelegramChatId(p.getTelegramChatId())))
                .findFirst();
        if (profileOpt.isEmpty()) {
            telegramService.sendMessage(safeChatId,
                    "Your Telegram account is not linked to MindTrack. "
                            + "Please add your Telegram Chat ID in your profile settings.");
            return;
        }

        Long userId = profileOpt.get().getUserId();

        try {
            ChatRequest chatRequest = new ChatRequest(null, text, ConversationType.QUICK_CHECKIN);
            ChatResponse response = conversationService.chatWithChannel(
                    userId, chatRequest, Channel.TELEGRAM);

            telegramService.sendMessage(safeChatId, response.content());
        } catch (Exception e) {
            LOG.error("Error processing Telegram message for chat={}", safeChatId, e);
            telegramService.sendMessage(safeChatId,
                    "Sorry, I encountered an error processing your message. Please try again later.");
        }
    }

    /**
     * Handles an inbound WhatsApp webhook notification.
     * Resolves the user by WhatsApp phone number, processes via AI, and replies.
     *
     * @param webhook the WhatsApp webhook payload
     */
    public void handleWhatsAppMessage(WhatsAppWebhook webhook) {
        if (whatsAppService == null) {
            LOG.warn("WhatsApp message received but WhatsApp integration is disabled");
            return;
        }

        if (webhook.getEntry() == null) {
            return;
        }

        for (WhatsAppWebhook.Entry entry : webhook.getEntry()) {
            if (entry.getChanges() == null) {
                continue;
            }
            for (WhatsAppWebhook.Change change : entry.getChanges()) {
                if (change.getValue() == null || change.getValue().getMessages() == null) {
                    continue;
                }
                processWhatsAppMessages(change.getValue().getMessages());
            }
        }
    }

    private void processWhatsAppMessages(List<WhatsAppWebhook.Message> messages) {
        for (WhatsAppWebhook.Message msg : messages) {
            if (isNonTextWhatsAppMessage(msg)) {
                LOG.debug("Ignoring non-text WhatsApp message type={}", msg.getType());
            } else {
                processWhatsAppMessage(msg);
            }
        }
    }

    private boolean isNonTextWhatsAppMessage(WhatsAppWebhook.Message message) {
        return !"text".equals(message.getType()) || message.getText() == null;
    }

    private void processWhatsAppMessage(WhatsAppWebhook.Message message) {
        WhatsAppService service = whatsAppService;
        if (service == null) {
            return;
        }
        String phoneNumber = message.getFrom();
        String text = message.getText().getBody();
        String safePhoneNumber = sanitizeWhatsappNumber(phoneNumber);

        if (safePhoneNumber == null) {
            LOG.warn("Ignoring WhatsApp message with invalid phone number");
            return;
        }

        LOG.info("WhatsApp message received from phone={}", safePhoneNumber);

        // Resolve user by WhatsApp number; see Telegram lookup above for encryption rationale.
        Optional<UserProfile> profileOpt = userProfileRepository.findAllByWhatsappNumberNotNull()
                .stream()
                .filter(profile -> safePhoneNumber.equals(sanitizeWhatsappNumber(profile.getWhatsappNumber())))
                .findFirst();
        if (profileOpt.isEmpty()) {
            LOG.warn("Ignoring WhatsApp message from unlinked phone={}", safePhoneNumber);
            return;
        }

        Long userId = profileOpt.get().getUserId();

        try {
            ChatRequest chatRequest = new ChatRequest(null, text, ConversationType.QUICK_CHECKIN);
            ChatResponse response = conversationService.chatWithChannel(
                    userId, chatRequest, Channel.WHATSAPP);

            service.sendMessage(safePhoneNumber, response.content());
        } catch (Exception e) {
            LOG.error("Error processing WhatsApp message from phone={}", safePhoneNumber, e);
            service.sendMessage(safePhoneNumber,
                    "Sorry, I encountered an error processing your message. Please try again later.");
        }
    }

    @Nullable
    private String sanitizeTelegramChatId(@Nullable String chatId) {
        if (chatId == null) {
            return null;
        }
        String trimmed = chatId.trim();
        return TELEGRAM_CHAT_ID.matcher(trimmed).matches() ? trimmed : null;
    }

    @Nullable
    private String sanitizeWhatsappNumber(@Nullable String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        String trimmed = phoneNumber.trim();
        String digitsOnly = trimmed.replaceAll("\\D", "");
        String canonical = trimmed.startsWith("+") ? "+" + digitsOnly : digitsOnly;
        return WHATSAPP_PHONE_NUMBER.matcher(canonical).matches() ? canonical : null;
    }
}
