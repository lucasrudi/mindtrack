package com.mindtrack.auth.service;

import com.mindtrack.auth.dto.TherapistTokenResponse;
import com.mindtrack.auth.model.TherapistRegistrationToken;
import com.mindtrack.auth.repository.RoleRepository;
import com.mindtrack.auth.repository.TherapistRegistrationTokenRepository;
import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.Role;
import com.mindtrack.common.model.User;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service for admin-issued therapist registration tokens.
 *
 * <p>Replaces the self-service THERAPIST role promotion (H-3). Admin creates single-use tokens
 * and distributes them to externally-verified therapists. On redemption the user's system role
 * is upgraded to THERAPIST without requiring per-user admin intervention.
 */
@Service
public class TherapistRegistrationService {

    private static final Logger LOG = LoggerFactory.getLogger(TherapistRegistrationService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_EXPIRY_DAYS = 30;

    private final TherapistRegistrationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public TherapistRegistrationService(TherapistRegistrationTokenRepository tokenRepository,
            UserRepository userRepository, RoleRepository roleRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Creates a single-use therapist registration token. Only admins may call this.
     */
    @Transactional
    public TherapistTokenResponse createToken(Long adminId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);

        TherapistRegistrationToken entity = new TherapistRegistrationToken();
        entity.setToken(token);
        entity.setCreatedBy(adminId);
        entity.setExpiresAt(LocalDateTime.now().plusDays(TOKEN_EXPIRY_DAYS));
        entity.setCreatedAt(LocalDateTime.now());
        tokenRepository.save(entity);

        LOG.info("Admin {} created therapist registration token {}", adminId, entity.getId());
        return toResponse(entity);
    }

    /**
     * Lists all therapist registration tokens (used and unused).
     */
    public List<TherapistTokenResponse> listTokens() {
        return tokenRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Redeems a token and upgrades the user's system role to THERAPIST.
     * Idempotent: users already holding the THERAPIST role succeed silently.
     *
     * @throws ResponseStatusException 400 if token is invalid, expired, or already used
     * @throws ResponseStatusException 400 if user is already an admin (role downgrade prevented)
     */
    @Transactional
    public void redeemToken(String token, Long userId) {
        TherapistRegistrationToken entity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid or expired therapist registration token"));

        if (entity.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Therapist registration token has already been used");
        }
        if (entity.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Therapist registration token has expired");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Authenticated user not found"));

        if ("ADMIN".equals(user.getRole().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Admin accounts cannot be changed to THERAPIST role");
        }

        if ("THERAPIST".equals(user.getRole().getName())) {
            LOG.info("User {} is already THERAPIST — token redemption skipped", userId);
            return;
        }

        Role therapistRole = roleRepository.findByName("THERAPIST")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "THERAPIST role not configured"));

        user.setRole(therapistRole);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        entity.setUsedBy(userId);
        entity.setUsedAt(LocalDateTime.now());
        tokenRepository.save(entity);

        LOG.info("User {} redeemed therapist registration token {} and became THERAPIST",
                userId, entity.getId());
    }

    private TherapistTokenResponse toResponse(TherapistRegistrationToken entity) {
        return new TherapistTokenResponse(
                entity.getId(),
                entity.getToken(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUsedBy(),
                entity.getUsedAt());
    }
}
