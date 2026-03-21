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
     * Therapist-initiated request for a specific patient by email.
     */
    @Transactional
    public InviteGenerateResponse requestPatient(Long therapistId, String patientEmail) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientEmail));
        if (therapistId.equals(patient.getId())) {
            throw new IllegalArgumentException("Therapist cannot request themselves");
        }

        TherapistPatient relationship = therapistPatientRepository
                .findByTherapistIdAndPatientId(therapistId, patient.getId())
                .orElse(null);
        if (relationship != null && relationship.getStatus() != TherapistPatientStatus.INACTIVE) {
            throw new IllegalArgumentException("Relationship already exists or is pending");
        }

        if (inviteTokenRepository
                .findByInitiatorIdAndRecipientIdAndUsedAtIsNullAndExpiresAtAfter(
                        therapistId, patient.getId(), LocalDateTime.now())
                .isPresent()) {
            throw new IllegalArgumentException("A request is already pending for this patient");
        }

        if (relationship == null) {
            relationship = new TherapistPatient(therapistId, patient.getId(),
                    TherapistPatientStatus.PENDING);
        } else {
            relationship.setStatus(TherapistPatientStatus.PENDING);
        }
        therapistPatientRepository.save(relationship);

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);

        InviteToken inviteToken = new InviteToken();
        inviteToken.setToken(token);
        inviteToken.setInitiatorId(therapistId);
        inviteToken.setInitiatorRole(InitiatorRole.THERAPIST);
        inviteToken.setRecipientId(patient.getId());
        inviteToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        inviteToken.setCreatedAt(LocalDateTime.now());
        inviteTokenRepository.save(inviteToken);

        String url = frontendUrl + "/invite/" + token;
        LOG.info("Generated therapist request token for therapist {} patient {}", therapistId,
                patient.getId());
        return new InviteGenerateResponse(token, url);
    }

    /**
     * Returns a preview of who sent the invite (for display before accepting).
     */
    public InvitePreviewResponse previewInvite(String token) {
        InviteToken inviteToken = findValidToken(token);
        User initiator = userRepository.findById(inviteToken.getInitiatorId())
                .orElseThrow(() -> new IllegalArgumentException("Initiator not found"));
        String status = null;
        if (inviteToken.getRecipientId() != null) {
            status = therapistPatientRepository
                    .findByTherapistIdAndPatientId(inviteToken.getInitiatorId(),
                            inviteToken.getRecipientId())
                    .map(relationship -> relationship.getStatus().name())
                    .orElse(TherapistPatientStatus.PENDING.name());
        }
        return new InvitePreviewResponse(initiator.getName(),
                inviteToken.getInitiatorRole().name(),
                status);
    }

    /**
     * Accepts an invite token, creating a therapist-patient relationship.
     * Therapist-initiated invites become ACTIVE; patient-initiated become PENDING.
     */
    @Transactional
    public void acceptInvite(String token, Long acceptorId, InitiatorRole acceptorRole) {
        InviteToken inviteToken = findValidToken(token);
        respondToInvite(inviteToken, acceptorId, acceptorRole, resolveAcceptedStatus(inviteToken));
    }

    /**
     * Rejects an invite token, recording the relationship as INACTIVE.
     */
    @Transactional
    public void rejectInvite(String token, Long rejectorId, InitiatorRole rejectorRole) {
        respondToInvite(findValidToken(token), rejectorId, rejectorRole,
                TherapistPatientStatus.INACTIVE);
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

    private TherapistPatientStatus resolveAcceptedStatus(InviteToken inviteToken) {
        if (inviteToken.getRecipientId() != null) {
            return TherapistPatientStatus.ACTIVE;
        }
        return inviteToken.getInitiatorRole() == InitiatorRole.THERAPIST
                ? TherapistPatientStatus.ACTIVE
                : TherapistPatientStatus.PENDING;
    }

    private void respondToInvite(InviteToken inviteToken, Long responderId,
                                 InitiatorRole responderRole,
                                 TherapistPatientStatus newStatus) {
        if (inviteToken.getInitiatorRole() == responderRole) {
            throw new IllegalArgumentException("Cannot respond to your own role's invite");
        }
        if (inviteToken.getRecipientId() != null && !inviteToken.getRecipientId().equals(responderId)) {
            throw new IllegalArgumentException("Invite is not assigned to this user");
        }

        Long therapistId;
        Long patientId;

        if (inviteToken.getRecipientId() != null) {
            therapistId = inviteToken.getInitiatorId();
            patientId = inviteToken.getRecipientId();
        } else if (inviteToken.getInitiatorRole() == InitiatorRole.THERAPIST) {
            therapistId = inviteToken.getInitiatorId();
            patientId = responderId;
        } else {
            therapistId = responderId;
            patientId = inviteToken.getInitiatorId();
        }

        TherapistPatient relationship = therapistPatientRepository
                .findByTherapistIdAndPatientId(therapistId, patientId)
                .orElse(new TherapistPatient(therapistId, patientId, newStatus));
        relationship.setStatus(newStatus);
        therapistPatientRepository.save(relationship);

        inviteToken.setUsedAt(LocalDateTime.now());
        inviteTokenRepository.save(inviteToken);

        LOG.info("Responded to invite: therapist={} patient={} status={}",
                therapistId, patientId, newStatus);
    }
}
