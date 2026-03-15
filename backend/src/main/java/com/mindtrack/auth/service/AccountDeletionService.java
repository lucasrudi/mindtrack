package com.mindtrack.auth.service;

import com.mindtrack.audit.model.AuditAction;
import com.mindtrack.audit.service.AuditService;
import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.User;
import com.mindtrack.profile.repository.UserProfileRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles the GDPR/CCPA right-to-erasure lifecycle:
 *
 * <ol>
 *   <li>Immediate pseudonymisation — PII is replaced with opaque tokens; the account is disabled
 *       and a 30-day hard-deletion timestamp is set.
 *   <li>Scheduled hard deletion — runs nightly and permanently removes records whose
 *       {@code deletion_scheduled_at} timestamp has elapsed.
 * </ol>
 *
 * <p>Audit logs are intentionally retained; they contain no PII beyond the anonymised user ID,
 * which satisfies HIPAA 45 CFR §164.530 minimum 6-year retention of medical-record audit trails.
 */
@Service
public class AccountDeletionService {

    private static final Logger LOG = LoggerFactory.getLogger(AccountDeletionService.class);
    private static final int RETENTION_DAYS = 30;

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final AuditService auditService;

    public AccountDeletionService(UserRepository userRepository,
                                  UserProfileRepository profileRepository,
                                  AuditService auditService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.auditService = auditService;
    }

    /**
     * Immediately pseudonymises all PII for the given user and schedules their account for
     * hard-deletion after {@value RETENTION_DAYS} days. The account is disabled so that
     * existing JWTs and refresh tokens are rejected immediately.
     *
     * @param userId    the ID of the user requesting deletion
     * @param ipAddress the client IP address (for the audit log)
     */
    @Transactional
    public void requestDeletion(Long userId, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        user.setEmail("deleted-" + token + "@deleted.invalid");
        user.setName("Deleted User");
        user.setGoogleId(null);
        user.setEnabled(false);
        user.setTokenVersion(user.getTokenVersion() + 1);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletionScheduledAt(LocalDateTime.now().plusDays(RETENTION_DAYS));
        userRepository.save(user);

        profileRepository.findByUserId(userId).ifPresent(profile -> {
            profile.setDisplayName(null);
            profile.setAvatarUrl(null);
            profile.setTelegramChatId(null);
            profile.setWhatsappNumber(null);
            profile.setAnonymizedAt(LocalDateTime.now());
            profileRepository.save(profile);
        });

        auditService.log(userId, AuditAction.ACCOUNT_DELETION_REQUESTED,
                "user", userId, userId, ipAddress, "WEB");
        LOG.info("Account deletion requested for user {} — hard-delete scheduled at {}",
                userId, user.getDeletionScheduledAt());
    }

    /**
     * Nightly job that permanently removes accounts whose retention window has elapsed.
     * Runs at 03:00 UTC to avoid peak traffic.
     *
     * <p>Each deleted user triggers a SYSTEM audit-log entry (actor = the user's ID, channel =
     * SYSTEM) so the deletion event is traceable without retaining PII.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void hardDeleteExpiredAccounts() {
        List<User> expired = userRepository
                .findByDeletionScheduledAtBeforeAndDeletedAtIsNotNull(LocalDateTime.now());
        if (expired.isEmpty()) {
            return;
        }
        LOG.info("Hard-deleting {} expired accounts", expired.size());
        for (User user : expired) {
            Long userId = user.getId();
            userRepository.deleteById(userId);
            auditService.log(userId, AuditAction.ACCOUNT_HARD_DELETED,
                    "user", userId, userId, "SYSTEM", "SYSTEM");
            LOG.info("Hard-deleted account for user {}", userId);
        }
    }
}
