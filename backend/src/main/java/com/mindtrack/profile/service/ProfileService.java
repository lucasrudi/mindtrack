package com.mindtrack.profile.service;

import com.mindtrack.profile.dto.ProfileRequest;
import com.mindtrack.profile.dto.ProfileResponse;
import com.mindtrack.profile.model.UserProfile;
import com.mindtrack.profile.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user profile operations.
 */
@Service
public class ProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileService.class);

    private final UserProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    public ProfileService(UserProfileRepository profileRepository, ProfileMapper profileMapper) {
        this.profileRepository = profileRepository;
        this.profileMapper = profileMapper;
    }

    /**
     * Gets the current user's profile, creating a default if it doesn't exist.
     */
    public ProfileResponse getProfile(Long userId) {
        LOG.info("Getting profile for user {}", userId);
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));
        return profileMapper.toResponse(profile);
    }

    /**
     * Updates the current user's profile.
     */
    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileRequest request) {
        LOG.info("Updating profile for user {}", userId);
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));
        profileMapper.applyRequest(request, profile);
        UserProfile saved = profileRepository.save(profile);
        return profileMapper.toResponse(saved);
    }

    /**
     * Marks the onboarding flow as completed for the given user.
     */
    @Transactional
    public void completeOnboarding(Long userId) {
        LOG.info("Marking onboarding complete for user {}", userId);
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));
        profile.setOnboardingCompleted(true);
        profileRepository.save(profile);
    }

    /**
     * Marks both the survey and onboarding as completed for the given user.
     */
    @Transactional
    public void completeSurvey(Long userId) {
        LOG.info("Marking survey and onboarding complete for user {}", userId);
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));
        profile.setOnboardingCompleted(true);
        profile.setSurveyCompleted(true);
        profileRepository.save(profile);
    }

    /**
     * Marks onboarding as skipped (complete without survey) for the given user.
     */
    @Transactional
    public void skipOnboarding(Long userId) {
        LOG.info("Marking onboarding skipped for user {}", userId);
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));
        profile.setOnboardingCompleted(true);
        profileRepository.save(profile);
    }

    private UserProfile createDefaultProfile(Long userId) {
        LOG.info("Creating default profile for user {}", userId);
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        return profileRepository.save(profile);
    }
}
