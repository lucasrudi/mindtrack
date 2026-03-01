package com.mindtrack.auth.controller;

import com.mindtrack.auth.dto.AuthResponse;
import com.mindtrack.auth.dto.SelfRoleRequest;
import com.mindtrack.auth.dto.UserInfo;
import com.mindtrack.auth.service.JwtService;
import com.mindtrack.auth.service.UserService;
import com.mindtrack.common.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller for user info, token validation, and self-service role change.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
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
     * Changes the current user's role to USER or THERAPIST and returns a refreshed JWT.
     */
    @PatchMapping("/me/role")
    public ResponseEntity<AuthResponse> changeRole(
            @Valid @RequestBody SelfRoleRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userService.changeRole(userId, request.getRole());
        String token = jwtService.generateToken(user.getId(), user.getEmail(),
                user.getRole().getName());
        return ResponseEntity.ok(
                new AuthResponse(token, user.getEmail(), user.getName(), user.getRole().getName()));
    }

    private UserInfo toUserInfo(User user) {
        return new UserInfo(user.getId(), user.getEmail(), user.getName(), user.getRole().getName());
    }
}
