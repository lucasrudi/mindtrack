package com.mindtrack.onboarding.service;

import com.mindtrack.goals.model.GoalValidationStatus;
import com.mindtrack.goals.repository.GoalRepository;
import com.mindtrack.goals.service.GoalMapper;
import com.mindtrack.onboarding.dto.SurveyRequest;
import com.mindtrack.profile.service.ProfileService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock private GoalRepository goalRepository;
    @Mock private ProfileService profileService;

    private OnboardingService onboardingService;

    @BeforeEach
    void setUp() {
        GoalMapper goalMapper = new GoalMapper();
        onboardingService = new OnboardingService(goalRepository, goalMapper, profileService);
    }

    @Test
    void shouldCreateGoalsFromSurveyWithPendingValidation() {
        SurveyRequest request = new SurveyRequest();
        request.setMoodBaseline(5);
        request.setAnxietyLevel(7);
        request.setSleepQuality(4);
        request.setLifeAreas(List.of("work", "health"));
        request.setChallenges(List.of("stress at work"));
        request.setGoalCategories(List.of("wellness"));

        when(goalRepository.save(any())).thenAnswer(inv -> {
            var g = inv.getArgument(0, com.mindtrack.goals.model.Goal.class);
            g.setId((long) (Math.random() * 1000 + 1));
            return g;
        });

        var goals = onboardingService.generateGoalsFromSurvey(1L, request);

        assertFalse(goals.isEmpty());
        assertTrue(goals.size() >= 1 && goals.size() <= 5);
        goals.forEach(g ->
                assertEquals(GoalValidationStatus.PENDING_VALIDATION, g.getValidationStatus()));
        verify(profileService).completeSurvey(1L);
    }

    @Test
    void shouldCreateAtLeastOneGoalPerLifeArea() {
        SurveyRequest request = new SurveyRequest();
        request.setMoodBaseline(6);
        request.setAnxietyLevel(5);
        request.setSleepQuality(6);
        request.setLifeAreas(List.of("fitness", "mindfulness"));

        when(goalRepository.save(any())).thenAnswer(inv -> {
            var g = inv.getArgument(0, com.mindtrack.goals.model.Goal.class);
            g.setId(1L);
            return g;
        });

        var goals = onboardingService.generateGoalsFromSurvey(1L, request);

        assertTrue(goals.size() >= 2);
    }
}
