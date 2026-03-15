package com.mindtrack.profile.service;

import com.mindtrack.common.service.KmsEncryptionService;
import com.mindtrack.profile.model.UserProfile;
import com.mindtrack.profile.repository.UserProfileRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * One-time data migration that encrypts any remaining plaintext PII columns on startup.
 *
 * <p>Runs after the full Spring context is ready so {@link KmsEncryptionService} is available.
 * It finds rows where {@code telegram_chat_id} or {@code whatsapp_number} do not yet carry the
 * {@code ENC:} prefix (i.e. pre-encryption plaintext), saves them through JPA so the
 * {@link com.mindtrack.common.service.KmsEncryptionConverter} encrypts them on the way out.
 *
 * <p>When encryption is disabled (no {@code ENCRYPTION_KEY_ARN}, local / test profile) the
 * runner logs a warning and exits immediately without touching any data.
 */
@Component
public class PiiEncryptionMigrationRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(PiiEncryptionMigrationRunner.class);

    private final UserProfileRepository userProfileRepository;
    private final KmsEncryptionService kmsEncryptionService;

    /**
     * Creates the runner.
     *
     * @param userProfileRepository repository used to find and save profiles
     * @param kmsEncryptionService service used to check whether encryption is enabled
     */
    public PiiEncryptionMigrationRunner(UserProfileRepository userProfileRepository,
                                        KmsEncryptionService kmsEncryptionService) {
        this.userProfileRepository = userProfileRepository;
        this.kmsEncryptionService = kmsEncryptionService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!kmsEncryptionService.isEnabled()) {
            LOG.warn("KMS encryption is disabled — skipping PII column migration");
            return;
        }

        List<UserProfile> profiles = userProfileRepository.findProfilesNeedingEncryption();
        if (profiles.isEmpty()) {
            LOG.info("PII encryption migration: no plaintext rows found, nothing to do");
            return;
        }

        LOG.info("PII encryption migration: encrypting {} profile row(s)", profiles.size());
        userProfileRepository.saveAll(profiles);
        LOG.info("PII encryption migration: complete");
    }
}
