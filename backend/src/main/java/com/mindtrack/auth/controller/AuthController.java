package com.mindtrack.auth.controller;

import com.mindtrack.auth.config.OAuth2LoginSuccessHandler;
import com.mindtrack.auth.dto.AuthResponse;
import com.mindtrack.auth.dto.RefreshRequest;
import com.mindtrack.auth.dto.SelfRolesRequest;
import com.mindtrack.auth.dto.TherapistRegistrationRedeemRequest;
import com.mindtrack.auth.dto.UserInfo;
import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.auth.service.AccountDeletionService;
import com.mindtrack.auth.service.JwtService;
import com.mindtrack.auth.service.RefreshTokenService;
import com.mindtrack.auth.service.TherapistRegistrationService;
import com.mindtrack.auth.service.UserService;
import com.mindtrack.common.model.User;
import com.mindtrack.profile.model.UserProfile;
import com.mindtrack.profile.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Authentication controller for user info, session management, and therapist registration.
 * THERAPIST role is granted via admin-issued tokens, not self-service.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final ProfileService profileService;
    private final TherapistRegistrationService therapistRegistrationService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final AccountDeletionService accountDeletionService;
    private final boolean cookieSecure;

    public AuthController(UserService userService, JwtService jwtService,
                          ProfileService profileService,
                          TherapistRegistrationService therapistRegistrationService,
                          RefreshTokenService refreshTokenService,
                          UserRepository userRepository,
                          AccountDeletionService accountDeletionService,
                          @Value("${mindtrack.auth.cookie-secure:true}") boolean cookieSecure) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.profileService = profileService;
        this.therapistRegistrationService = therapistRegistrationService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.accountDeletionService = accountDeletionService;
        this.cookieSecure = cookieSecure;
    }

    /**
     * Returns the current authenticated user's information.
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Long userId = (Long) authentication.getPrincipal();
        return userService.findById(userId)
                .map(user -> ResponseEntity.ok(toUserInfo(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Clears the auth_token cookie and ends the session.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie clearCookie = ResponseCookie.from(OAuth2LoginSuccessHandler.AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates the current user's patient/therapist role flags and returns a refreshed JWT
     * together with a new refresh token.
     */
    @PatchMapping("/me/roles")
    public ResponseEntity<AuthResponse> updateRoles(
            @Valid @RequestBody SelfRolesRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserProfile profile = profileService.updateRoles(userId,
                request.getIsPatient(), request.getIsTherapist());
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        String token = jwtService.generateToken(user.getId(), user.getEmail(),
                user.getRole().getName(), profile.isPatient(), profile.isTherapist(),
                user.getTokenVersion());
        String newRefreshToken = refreshTokenService.createRefreshToken(userId);
        return ResponseEntity.ok(new AuthResponse(token, newRefreshToken, user.getEmail(),
                user.getName(), user.getRole().getName(),
                profile.isPatient(), profile.isTherapist()));
    }

    /**
     * Redeems an admin-issued therapist registration token and upgrades the caller's
     * system role to THERAPIST. Returns a refreshed JWT that includes the new role,
     * together with a new refresh token.
     *
     * <p>This is the safe replacement for the removed H-3 self-service endpoint:
     * THERAPIST role is still gated — it requires a single-use token that only an admin
     * can create — but the user redeems it without any further per-user admin interaction.
     */
    @PostMapping("/therapist-register")
    public ResponseEntity<AuthResponse> redeemTherapistToken(
            @Valid @RequestBody TherapistRegistrationRedeemRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        therapistRegistrationService.redeemToken(request.getToken(), userId);

        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        UserProfile profile = profileService.getOrCreateProfile(userId);
        String jwt = jwtService.generateToken(user.getId(), user.getEmail(),
                user.getRole().getName(), profile.isPatient(), profile.isTherapist(),
                user.getTokenVersion());
        String newRefreshToken = refreshTokenService.createRefreshToken(userId);
        return ResponseEntity.ok(new AuthResponse(jwt, newRefreshToken, user.getEmail(),
                user.getName(), user.getRole().getName(),
                profile.isPatient(), profile.isTherapist()));
    }

    /**
     * Rotates a refresh token and issues a new JWT + refresh token pair.
     * The submitted refresh token is immediately marked as used (single-use rotation).
     * Returns 401 if the token is invalid, already used, or expired.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        Long userId = refreshTokenService.rotateRefreshToken(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        UserProfile profile = profileService.getOrCreateProfile(userId);
        String jwt = jwtService.generateToken(user.getId(), user.getEmail(),
                user.getRole().getName(), profile.isPatient(), profile.isTherapist(),
                user.getTokenVersion());
        String newRefreshToken = refreshTokenService.createRefreshToken(userId);
        return ResponseEntity.ok(new AuthResponse(jwt, newRefreshToken, user.getEmail(),
                user.getName(), user.getRole().getName(),
                profile.isPatient(), profile.isTherapist()));
    }

    /**
     * Immediately pseudonymises all PII for the authenticated user (GDPR Art.17 / CCPA §1798.105)
     * and schedules the account for permanent deletion after 30 days. Clears the auth cookie so
     * the client is logged out before the response returns.
     */
    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(Authentication authentication,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        Long userId = (Long) authentication.getPrincipal();
        accountDeletionService.requestDeletion(userId, request.getRemoteAddr());
        ResponseCookie clearCookie = ResponseCookie.from(OAuth2LoginSuccessHandler.AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
        return ResponseEntity.noContent().build();
    }

    private UserInfo toUserInfo(User user) {
        return new UserInfo(user.getId(), user.getEmail(), user.getName(), user.getRole().getName());
    }
}
