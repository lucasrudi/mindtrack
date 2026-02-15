package com.mindtrack.goals.service;

import com.mindtrack.goals.dto.GoalRequest;
import com.mindtrack.goals.dto.GoalResponse;
import com.mindtrack.goals.dto.MilestoneRequest;
import com.mindtrack.goals.dto.MilestoneResponse;
import com.mindtrack.goals.model.Goal;
import com.mindtrack.goals.model.GoalStatus;
import com.mindtrack.goals.model.Milestone;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalMapperTest {

    private GoalMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GoalMapper();
    }

    @Test
    void shouldMapGoalToResponse() {
        Goal goal = createGoal(1L);

        GoalResponse response = mapper.toGoalResponse(goal);

        assertEquals(1L, response.getId());
        assertEquals("Learn guitar", response.getTitle());
        assertEquals("Practice daily", response.getDescription());
        assertEquals("Personal", response.getCategory());
        assertEquals(GoalStatus.IN_PROGRESS, response.getStatus());
        assertEquals(0, response.getTotalMilestones());
        assertEquals(0, response.getCompletedMilestones());
    }

    @Test
    void shouldMapGoalWithMilestones() {
        Goal goal = createGoal(1L);
        Milestone m1 = createMilestone(10L, goal, false);
        Milestone m2 = createMilestone(11L, goal, true);
        goal.setMilestones(List.of(m1, m2));

        GoalResponse response = mapper.toGoalResponse(goal);

        assertEquals(2, response.getTotalMilestones());
        assertEquals(1, response.getCompletedMilestones());
        assertEquals(2, response.getMilestones().size());
    }

    @Test
    void shouldMapMilestoneToResponse() {
        Goal goal = createGoal(1L);
        Milestone milestone = createMilestone(10L, goal, false);

        MilestoneResponse response = mapper.toMilestoneResponse(milestone);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getGoalId());
        assertEquals("Learn chords", response.getTitle());
        assertNull(response.getCompletedAt());
        assertFalse(response.isCompleted());
    }

    @Test
    void shouldMapCompletedMilestone() {
        Goal goal = createGoal(1L);
        Milestone milestone = createMilestone(10L, goal, true);

        MilestoneResponse response = mapper.toMilestoneResponse(milestone);

        assertTrue(response.isCompleted());
    }

    @Test
    void shouldApplyGoalRequest() {
        GoalRequest request = new GoalRequest();
        request.setTitle("New goal");
        request.setDescription("New desc");
        request.setCategory("Health");
        request.setTargetDate(LocalDate.of(2025, 6, 1));

        Goal goal = new Goal();
        mapper.applyRequest(request, goal);

        assertEquals("New goal", goal.getTitle());
        assertEquals("New desc", goal.getDescription());
        assertEquals("Health", goal.getCategory());
        assertEquals(LocalDate.of(2025, 6, 1), goal.getTargetDate());
    }

    @Test
    void shouldApplyMilestoneRequest() {
        MilestoneRequest request = new MilestoneRequest();
        request.setTitle("Step one");
        request.setTargetDate(LocalDate.of(2025, 3, 1));
        request.setNotes("First step");

        Milestone milestone = new Milestone();
        mapper.applyMilestoneRequest(request, milestone);

        assertEquals("Step one", milestone.getTitle());
        assertEquals(LocalDate.of(2025, 3, 1), milestone.getTargetDate());
        assertEquals("First step", milestone.getNotes());
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
        milestone.setNotes("Basic open chords");
        milestone.setCreatedAt(LocalDateTime.now());
        if (completed) {
            milestone.setCompletedAt(LocalDateTime.now());
        }
        return milestone;
    }
}
