package com.mindtrack.auth.controller;

import com.mindtrack.auth.dto.AuthResponse;
import com.mindtrack.auth.dto.SelfRolesRequest;
import com.mindtrack.auth.dto.UserInfo;
import com.mindtrack.auth.service.JwtService;
import com.mindtrack.auth.service.UserService;
import com.mindtrack.common.model.User;
import com.mindtrack.profile.model.UserProfile;
import com.mindtrack.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller for user info and token validation.
 * Role promotion is admin-only and is handled via the admin endpoint.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final ProfileService profileService;

    public AuthController(UserService userService, JwtService jwtService,
                          ProfileService profileService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.profileService = profileService;
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
     * Updates the current user's patient/therapist role flags and returns a refreshed JWT.
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
                user.getRole().getName(), profile.isPatient(), profile.isTherapist());
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(),
                user.getName(), user.getRole().getName(),
                profile.isPatient(), profile.isTherapist()));
    }

    private UserInfo toUserInfo(User user) {
        return new UserInfo(user.getId(), user.getEmail(), user.getName(), user.getRole().getName());
    }
}
