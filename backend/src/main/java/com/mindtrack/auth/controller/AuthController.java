package com.mindtrack.auth.controller;

import com.mindtrack.auth.dto.UserInfo;
import com.mindtrack.auth.service.UserService;
import com.mindtrack.common.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller for user info and token validation endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
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

    private UserInfo toUserInfo(User user) {
        return new UserInfo(user.getId(), user.getEmail(), user.getName(), user.getRole().getName());
    }
}
