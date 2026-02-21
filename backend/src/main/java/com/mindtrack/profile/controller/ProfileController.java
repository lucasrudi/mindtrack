package com.mindtrack.profile.controller;

import com.mindtrack.profile.dto.ProfileRequest;
import com.mindtrack.profile.dto.ProfileResponse;
import com.mindtrack.profile.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user profile operations.
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Gets the current user's profile.
     */
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    /**
     * Updates the current user's profile.
     */
    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(@RequestBody ProfileRequest request,
                                                          Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }
}
