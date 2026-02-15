package com.mindtrack.goals.service;

import com.mindtrack.goals.dto.GoalRequest;
import com.mindtrack.goals.dto.GoalResponse;
import com.mindtrack.goals.dto.MilestoneRequest;
import com.mindtrack.goals.dto.MilestoneResponse;
import com.mindtrack.goals.model.Goal;
import com.mindtrack.goals.model.GoalStatus;
import com.mindtrack.goals.model.Milestone;
import com.mindtrack.goals.repository.GoalRepository;
import com.mindtrack.goals.repository.MilestoneRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private MilestoneRepository milestoneRepository;

    private GoalService goalService;

    @BeforeEach
    void setUp() {
        GoalMapper mapper = new GoalMapper();
        goalService = new GoalService(goalRepository, milestoneRepository, mapper);
    }

    @Test
    void shouldCreateGoal() {
        GoalRequest request = createGoalRequest();
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> {
            Goal saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        GoalResponse result = goalService.create(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Learn guitar", result.getTitle());
        assertEquals(GoalStatus.NOT_STARTED, result.getStatus());

        ArgumentCaptor<Goal> captor = ArgumentCaptor.forClass(Goal.class);
        verify(goalRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getUserId());
    }

    @Test
    void shouldListAllGoals() {
        Goal g1 = createGoal(1L);
        Goal g2 = createGoal(2L);
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(g1, g2));

        List<GoalResponse> results = goalService.listByUser(1L, null);

        assertEquals(2, results.size());
    }

    @Test
    void shouldListGoalsByStatus() {
        Goal g1 = createGoal(1L);
        when(goalRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                1L, GoalStatus.IN_PROGRESS))
                .thenReturn(List.of(g1));

        List<GoalResponse> results = goalService.listByUser(1L, GoalStatus.IN_PROGRESS);

        assertEquals(1, results.size());
    }

    @Test
    void shouldGetGoalByIdAndUser() {
        Goal goal = createGoal(1L);
        when(goalRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(goal));

        GoalResponse result = goalService.getByIdAndUser(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldReturnNullWhenGoalNotFound() {
        when(goalRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        assertNull(goalService.getByIdAndUser(999L, 1L));
    }

    @Test
    void shouldUpdateGoal() {
        Goal existing = createGoal(1L);
        GoalRequest request = createGoalRequest();
        request.setTitle("Updated goal");

        when(goalRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(existing));
        when(goalRepository.save(any(Goal.class))).thenReturn(existing);

        GoalResponse result = goalService.update(1L, 1L, request);

        assertNotNull(result);
        assertEquals("Updated goal", result.getTitle());
    }

    @Test
    void shouldReturnNullWhenUpdatingNonExistent() {
        when(goalRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        assertNull(goalService.update(999L, 1L, createGoalRequest()));
        verify(goalRepository, never()).save(any());
    }

    @Test
    void shouldUpdateGoalStatus() {
        Goal goal = createGoal(1L);
        when(goalRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);

        GoalResponse result = goalService.updateStatus(1L, 1L, GoalStatus.COMPLETED);

        assertNotNull(result);
        assertEquals(GoalStatus.COMPLETED, result.getStatus());
    }

    @Test
    void shouldDeleteGoal() {
        Goal goal = createGoal(1L);
        when(goalRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(goal));

        assertTrue(goalService.delete(1L, 1L));
        verify(goalRepository).delete(goal);
    }

    @Test
    void shouldReturnFalseWhenDeletingNonExistent() {
        when(goalRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        assertFalse(goalService.delete(999L, 1L));
        verify(goalRepository, never()).delete(any());
    }

    @Test
    void shouldAddMilestone() {
        Goal goal = createGoal(1L);
        when(goalRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(goal));
        when(milestoneRepository.save(any(Milestone.class))).thenAnswer(invocation -> {
            Milestone saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        MilestoneRequest request = new MilestoneRequest();
        request.setTitle("Learn chords");
        request.setTargetDate(LocalDate.of(2025, 3, 31));

        MilestoneResponse result = goalService.addMilestone(1L, 1L, request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Learn chords", result.getTitle());
        assertFalse(result.isCompleted());
    }

    @Test
    void shouldReturnNullWhenAddingMilestoneToNonExistentGoal() {
        when(goalRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        MilestoneRequest request = new MilestoneRequest();
        request.setTitle("Step");

        assertNull(goalService.addMilestone(999L, 1L, request));
    }

    @Test
    void shouldToggleMilestoneCompletion() {
        Goal goal = createGoal(1L);
        Milestone milestone = createMilestone(10L, goal, false);

        when(goalRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(goal));
        when(milestoneRepository.findByIdAndGoalId(10L, 1L))
                .thenReturn(Optional.of(milestone));
        when(milestoneRepository.save(any(Milestone.class))).thenReturn(milestone);

        MilestoneResponse result = goalService.toggleMilestoneCompletion(1L, 1L, 10L);

        assertNotNull(result);
        assertTrue(result.isCompleted());
    }

    @Test
    void shouldUncompleteCompletedMilestone() {
        Goal goal = createGoal(1L);
        Milestone milestone = createMilestone(10L, goal, true);

        when(goalRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(goal));
        when(milestoneRepository.findByIdAndGoalId(10L, 1L))
                .thenReturn(Optional.of(milestone));
        when(milestoneRepository.save(any(Milestone.class))).thenReturn(milestone);

        MilestoneResponse result = goalService.toggleMilestoneCompletion(1L, 1L, 10L);

        assertNotNull(result);
        assertFalse(result.isCompleted());
        assertNull(result.getCompletedAt());
    }

    @Test
    void shouldDeleteMilestone() {
        Goal goal = createGoal(1L);
        Milestone milestone = createMilestone(10L, goal, false);

        when(goalRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(goal));
        when(milestoneRepository.findByIdAndGoalId(10L, 1L))
                .thenReturn(Optional.of(milestone));

        assertTrue(goalService.deleteMilestone(1L, 1L, 10L));
        verify(milestoneRepository).delete(milestone);
    }

    @Test
    void shouldReturnFalseWhenDeletingMilestoneFromNonExistentGoal() {
        when(goalRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        assertFalse(goalService.deleteMilestone(999L, 1L, 10L));
        verify(milestoneRepository, never()).delete(any());
    }

    private GoalRequest createGoalRequest() {
        GoalRequest request = new GoalRequest();
        request.setTitle("Learn guitar");
        request.setDescription("Practice daily");
        request.setCategory("Personal");
        request.setTargetDate(LocalDate.of(2025, 12, 31));
        return request;
    }

    private Goal createGoal(Long id) {
        Goal goal = new Goal();
        goal.setId(id);
        goal.setUserId(1L);
        goal.setTitle("Learn guitar");
        goal.setDescription("Practice daily");
        goal.setCategory("Personal");
        goal.setTargetDate(LocalDate.of(2025, 12, 31));
        goal.setStatus(GoalStatus.IN_PROGRESS);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        return goal;
    }

    private Milestone createMilestone(Long id, Goal goal, boolean completed) {
        Milestone milestone = new Milestone();
        milestone.setId(id);
        milestone.setGoal(goal);
        milestone.setTitle("Learn chords");
        milestone.setTargetDate(LocalDate.of(2025, 3, 31));
        milestone.setCreatedAt(LocalDateTime.now());
        if (completed) {
            milestone.setCompletedAt(LocalDateTime.now());
        }
        return milestone;
    }
}
