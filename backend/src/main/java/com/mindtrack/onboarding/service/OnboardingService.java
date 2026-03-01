package com.mindtrack.onboarding.service;

import com.mindtrack.goals.dto.GoalResponse;
import com.mindtrack.goals.model.Goal;
import com.mindtrack.goals.model.GoalStatus;
import com.mindtrack.goals.model.GoalValidationStatus;
import com.mindtrack.goals.repository.GoalRepository;
import com.mindtrack.goals.service.GoalMapper;
import com.mindtrack.onboarding.dto.SurveyRequest;
import com.mindtrack.profile.service.ProfileService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for AI-driven onboarding goal generation from a survey.
 */
@Service
public class OnboardingService {

    private static final Logger LOG = LoggerFactory.getLogger(OnboardingService.class);

    private final GoalRepository goalRepository;
    private final GoalMapper goalMapper;
    private final ProfileService profileService;

    public OnboardingService(GoalRepository goalRepository, GoalMapper goalMapper,
                             ProfileService profileService) {
        this.goalRepository = goalRepository;
        this.goalMapper = goalMapper;
        this.profileService = profileService;
    }

    /**
     * Generates up to 5 initial goals based on the user's survey responses,
     * then marks onboarding complete.
     */
    @Transactional
    public List<GoalResponse> generateGoalsFromSurvey(Long userId, SurveyRequest request) {
        LOG.info("Generating goals from survey for user {}", userId);
        List<Goal> proposed = new ArrayList<>();

        if (request.getMoodBaseline() != null && request.getMoodBaseline() < 6) {
            proposed.add(buildGoal(userId, "Daily mood journaling",
                    "Track your mood every day to identify patterns", "Mental Health"));
        }

        if (request.getSleepQuality() != null && request.getSleepQuality() < 6) {
            proposed.add(buildGoal(userId, "Improve sleep routine",
                    "Establish a consistent bedtime and wind-down routine", "Wellness"));
        }

        if (request.getLifeAreas() != null) {
            for (String area : request.getLifeAreas()) {
                String title = "Improve " + area.toLowerCase();
                String desc = "Set and work towards specific improvements in: " + area;
                proposed.add(buildGoal(userId, title, desc, area));
            }
        }

        List<Goal> capped = proposed.subList(0, Math.min(proposed.size(), 5));
        List<GoalResponse> saved = new ArrayList<>();
        for (Goal goal : capped) {
            saved.add(goalMapper.toGoalResponse(goalRepository.save(goal)));
        }

        profileService.completeOnboarding(userId);
        LOG.info("Created {} onboarding goals for user {}", saved.size(), userId);
        return saved;
    }

    private Goal buildGoal(Long userId, String title, String description, String category) {
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setTitle(title);
        goal.setDescription(description);
        goal.setCategory(category);
        goal.setStatus(GoalStatus.NOT_STARTED);
        goal.setValidationStatus(GoalValidationStatus.PENDING_VALIDATION);
        goal.setCreatedBy(userId);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        return goal;
    }
}
