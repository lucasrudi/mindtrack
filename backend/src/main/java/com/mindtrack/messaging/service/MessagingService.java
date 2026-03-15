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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Orchestrates inbound messages from Telegram and WhatsApp.
 * Resolves the user from their linked messaging account, routes through the AI pipeline,
 * and sends the AI response back via the originating channel.
 */
@Service
public class MessagingService {

    private static final Logger LOG = LoggerFactory.getLogger(MessagingService.class);

    private final UserProfileRepository userProfileRepository;
    private final ConversationService conversationService;
    private final TelegramService telegramService;
    private final WhatsAppService whatsAppService;

    /**
     * Creates the messaging service.
     *
     * @param userProfileRepository user profile repo for resolving linked accounts
     * @param conversationService AI conversation pipeline
     * @param telegramService Telegram Bot API client
     * @param whatsAppService WhatsApp Cloud API client
     */
    public MessagingService(UserProfileRepository userProfileRepository,
                            ConversationService conversationService,
                            TelegramService telegramService,
                            WhatsAppService whatsAppService) {
        this.userProfileRepository = userProfileRepository;
        this.conversationService = conversationService;
        this.telegramService = telegramService;
        this.whatsAppService = whatsAppService;
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

        LOG.info("Telegram message received from chat={}", chatId);

        // Handle /start command
        if (text.startsWith("/start")) {
            telegramService.sendMessage(chatId,
                    "Welcome to MindTrack! Link your Telegram account in your profile settings "
                            + "to start chatting with your AI coach.");
            return;
        }

        // Resolve user by Telegram chat ID.
        // The column is KMS-encrypted, so exact-match DB queries do not work; we load all
        // linked profiles and compare against the decrypted value in application code.
        Optional<UserProfile> profileOpt = userProfileRepository.findAllByTelegramChatIdNotNull()
                .stream()
                .filter(p -> chatId.equals(p.getTelegramChatId()))
                .findFirst();
        if (profileOpt.isEmpty()) {
            telegramService.sendMessage(chatId,
                    "Your Telegram account is not linked to MindTrack. "
                            + "Please add your Telegram Chat ID in your profile settings.");
            return;
        }

        Long userId = profileOpt.get().getUserId();

        try {
            ChatRequest chatRequest = new ChatRequest(null, text, ConversationType.QUICK_CHECKIN);
            ChatResponse response = conversationService.chatWithChannel(
                    userId, chatRequest, Channel.TELEGRAM);

            telegramService.sendMessage(chatId, response.content());
        } catch (Exception e) {
            LOG.error("Error processing Telegram message for chat={}", chatId, e);
            telegramService.sendMessage(chatId,
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
            if (!"text".equals(msg.getType()) || msg.getText() == null) {
                LOG.debug("Ignoring non-text WhatsApp message type={}", msg.getType());
                continue;
            }

            String phoneNumber = msg.getFrom();
            String text = msg.getText().getBody();

            LOG.info("WhatsApp message received from phone={}", phoneNumber);

            // Resolve user by WhatsApp number; see Telegram lookup above for encryption rationale.
            Optional<UserProfile> profileOpt = userProfileRepository.findAllByWhatsappNumberNotNull()
                    .stream()
                    .filter(p -> phoneNumber.equals(p.getWhatsappNumber()))
                    .findFirst();
            if (profileOpt.isEmpty()) {
                whatsAppService.sendMessage(phoneNumber,
                        "Your WhatsApp number is not linked to MindTrack. "
                                + "Please add your number in your profile settings.");
                continue;
            }

            Long userId = profileOpt.get().getUserId();

            try {
                ChatRequest chatRequest = new ChatRequest(null, text, ConversationType.QUICK_CHECKIN);
                ChatResponse response = conversationService.chatWithChannel(
                        userId, chatRequest, Channel.WHATSAPP);

                whatsAppService.sendMessage(phoneNumber, response.content());
            } catch (Exception e) {
                LOG.error("Error processing WhatsApp message from phone={}", phoneNumber, e);
                whatsAppService.sendMessage(phoneNumber,
                        "Sorry, I encountered an error processing your message. Please try again later.");
            }
        }
    }
}
