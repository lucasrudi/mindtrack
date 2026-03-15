package com.mindtrack.profile.repository;

import com.mindtrack.profile.model.UserProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository for user profile operations.
 */
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserId(Long userId);

    /**
     * Returns all profiles that have a Telegram chat ID set.
     *
     * <p>Used by the messaging layer to resolve incoming Telegram messages: the caller
     * compares the inbound chat ID against the decrypted field values in application code,
     * because the column is KMS-encrypted and cannot be searched with an exact-match predicate.
     *
     * @return profiles with a non-null telegram_chat_id
     */
    List<UserProfile> findAllByTelegramChatIdNotNull();

    /**
     * Returns all profiles that have a WhatsApp number set.
     *
     * <p>Used by the messaging layer to resolve incoming WhatsApp messages; see
     * {@link #findAllByTelegramChatIdNotNull()} for the encryption rationale.
     *
     * @return profiles with a non-null whatsapp_number
     */
    List<UserProfile> findAllByWhatsappNumberNotNull();

    /**
     * Returns profiles whose messaging PII columns contain un-encrypted (legacy) plaintext.
     *
     * <p>Used exclusively by {@link com.mindtrack.profile.service.PiiEncryptionMigrationRunner}
     * on first deployment to encrypt existing rows. The {@code NOT LIKE 'ENC:%'} filter works
     * on raw column bytes and is safe to run against the widened VARCHAR(512) columns.
     *
     * @return profiles with at least one plaintext PII column
     */
    @Query(value = "SELECT * FROM user_profiles WHERE "
            + "(telegram_chat_id IS NOT NULL AND telegram_chat_id NOT LIKE 'ENC:%') "
            + "OR (whatsapp_number IS NOT NULL AND whatsapp_number NOT LIKE 'ENC:%')",
            nativeQuery = true)
    List<UserProfile> findProfilesNeedingEncryption();
}
