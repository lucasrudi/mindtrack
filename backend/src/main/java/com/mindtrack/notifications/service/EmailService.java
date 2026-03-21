package com.mindtrack.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending email notifications via JavaMailSender.
 *
 * <p>When no mail sender is configured (local profile), emails are logged instead of sent.
 */
@Service
public class EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

    @Nullable
    private final JavaMailSender mailSender;

    public EmailService(java.util.Optional<JavaMailSender> mailSender) {
        this.mailSender = mailSender.orElse(null);
    }

    /**
     * Sends a plain-text email to the given recipient.
     *
     * <p>If the mail sender is not configured, the email content is logged at INFO level
     * as a no-op for local development.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param body    plain-text email body
     */
    public void sendEmail(String to, String subject, String body) {
        if (mailSender == null) {
            LOG.info("[EMAIL LOG] To={} Subject={} Body={}", to, subject, body);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            LOG.info("Sent email to={} subject={}", to, subject);
        } catch (MailException ex) {
            LOG.warn("Failed to send email to={}: {}", to, ex.getMessage());
        }
    }
}
