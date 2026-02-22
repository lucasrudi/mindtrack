package com.mindtrack.profile.repository;

import com.mindtrack.profile.model.UserProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for user profile operations.
 */
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserId(Long userId);

    /**
     * Find a profile by its linked Telegram chat ID.
     *
     * @param telegramChatId the Telegram chat ID
     * @return the profile if found
     */
    Optional<UserProfile> findByTelegramChatId(String telegramChatId);

    /**
     * Find a profile by its linked WhatsApp phone number.
     *
     * @param whatsappNumber the WhatsApp phone number
     * @return the profile if found
     */
    Optional<UserProfile> findByWhatsappNumber(String whatsappNumber);
}
