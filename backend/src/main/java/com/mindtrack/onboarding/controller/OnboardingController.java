package com.mindtrack.onboarding.controller;

import com.mindtrack.goals.dto.GoalResponse;
import com.mindtrack.onboarding.dto.SurveyRequest;
import com.mindtrack.onboarding.service.OnboardingService;
import com.mindtrack.profile.service.ProfileService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the onboarding survey flow.
 */
@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final ProfileService profileService;

    public OnboardingController(OnboardingService onboardingService,
                                ProfileService profileService) {
        this.onboardingService = onboardingService;
        this.profileService = profileService;
    }

    /**
     * Submits the onboarding survey and returns generated goal suggestions.
     */
    @PostMapping("/survey")
    public ResponseEntity<List<GoalResponse>> survey(
            @RequestBody @Valid SurveyRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(onboardingService.generateGoalsFromSurvey(userId, request));
    }

    /**
     * Skips the onboarding survey, marking onboarding complete without generating goals.
     */
    @PostMapping("/skip")
    public ResponseEntity<Void> skip(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        profileService.skipOnboarding(userId);
        return ResponseEntity.ok().build();
    }
}
