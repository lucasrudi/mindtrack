package com.mindtrack.therapist.service;

import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.User;
import com.mindtrack.therapist.dto.InviteGenerateResponse;
import com.mindtrack.therapist.dto.InvitePreviewResponse;
import com.mindtrack.therapist.model.InitiatorRole;
import com.mindtrack.therapist.model.InviteToken;
import com.mindtrack.therapist.model.TherapistPatient;
import com.mindtrack.therapist.model.TherapistPatientStatus;
import com.mindtrack.therapist.repository.InviteTokenRepository;
import com.mindtrack.therapist.repository.TherapistPatientRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for invite token generation and acceptance.
 */
@Service
public class InviteService {

    private static final Logger LOG = LoggerFactory.getLogger(InviteService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final InviteTokenRepository inviteTokenRepository;
    private final TherapistPatientRepository therapistPatientRepository;
    private final UserRepository userRepository;
    private final String frontendUrl;

    public InviteService(InviteTokenRepository inviteTokenRepository,
                         TherapistPatientRepository therapistPatientRepository,
                         UserRepository userRepository,
                         @Value("${mindtrack.auth.frontend-url}") String frontendUrl) {
        this.inviteTokenRepository = inviteTokenRepository;
        this.therapistPatientRepository = therapistPatientRepository;
        this.userRepository = userRepository;
        this.frontendUrl = frontendUrl;
    }

    /**
     * Generates a 64-character hex invite token for the given initiator.
     */
    @Transactional
    public InviteGenerateResponse generateToken(Long initiatorId, InitiatorRole role) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);

        InviteToken inviteToken = new InviteToken();
        inviteToken.setToken(token);
        inviteToken.setInitiatorId(initiatorId);
        inviteToken.setInitiatorRole(role);
        inviteToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        inviteToken.setCreatedAt(LocalDateTime.now());
        inviteTokenRepository.save(inviteToken);

        String url = frontendUrl + "/invite/" + token;
        LOG.info("Generated invite token for {} (role={})", initiatorId, role);
        return new InviteGenerateResponse(token, url);
    }

    /**
     * Returns a preview of who sent the invite (for display before accepting).
     */
    public InvitePreviewResponse previewInvite(String token) {
        InviteToken inviteToken = findValidToken(token);
        User initiator = userRepository.findById(inviteToken.getInitiatorId())
                .orElseThrow(() -> new IllegalArgumentException("Initiator not found"));
        return new InvitePreviewResponse(initiator.getName(),
                inviteToken.getInitiatorRole().name());
    }

    /**
     * Accepts an invite token, creating a therapist-patient relationship.
     * Therapist-initiated invites become ACTIVE; patient-initiated become PENDING.
     */
    @Transactional
    public void acceptInvite(String token, Long acceptorId, InitiatorRole acceptorRole) {
        InviteToken inviteToken = findValidToken(token);

        if (inviteToken.getInitiatorRole() == acceptorRole) {
            throw new IllegalArgumentException("Cannot accept your own role's invite");
        }

        Long therapistId;
        Long patientId;
        TherapistPatientStatus status;

        if (inviteToken.getInitiatorRole() == InitiatorRole.THERAPIST) {
            therapistId = inviteToken.getInitiatorId();
            patientId = acceptorId;
            status = TherapistPatientStatus.ACTIVE;
        } else {
            therapistId = acceptorId;
            patientId = inviteToken.getInitiatorId();
            status = TherapistPatientStatus.PENDING;
        }

        TherapistPatient rel = new TherapistPatient(therapistId, patientId, status);
        therapistPatientRepository.save(rel);

        inviteToken.setUsedAt(LocalDateTime.now());
        inviteTokenRepository.save(inviteToken);

        LOG.info("Accepted invite: therapist={} patient={} status={}",
                therapistId, patientId, status);
    }

    /**
     * Scheduled task that removes expired invite tokens from the database.
     * Runs daily at 03:00 to keep the table free of stale rows.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        inviteTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        LOG.info("Expired invite tokens cleaned up");
    }

    private InviteToken findValidToken(String token) {
        InviteToken inviteToken = inviteTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invite token not found"));
        if (inviteToken.getUsedAt() != null) {
            throw new IllegalArgumentException("Invite token already used");
        }
        if (inviteToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invite token expired");
        }
        return inviteToken;
    }
}
