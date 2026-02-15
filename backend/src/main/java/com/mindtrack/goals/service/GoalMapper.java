package com.mindtrack.goals.service;

import com.mindtrack.goals.dto.GoalRequest;
import com.mindtrack.goals.dto.GoalResponse;
import com.mindtrack.goals.dto.MilestoneRequest;
import com.mindtrack.goals.dto.MilestoneResponse;
import com.mindtrack.goals.model.Goal;
import com.mindtrack.goals.model.Milestone;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Maps between Goal/Milestone entities and DTOs.
 */
@Component
public class GoalMapper {

    /**
     * Converts a Goal entity to a response DTO.
     */
    public GoalResponse toGoalResponse(Goal goal) {
        GoalResponse response = new GoalResponse();
        response.setId(goal.getId());
        response.setTitle(goal.getTitle());
        response.setDescription(goal.getDescription());
        response.setCategory(goal.getCategory());
        response.setTargetDate(goal.getTargetDate());
        response.setStatus(goal.getStatus());
        response.setCreatedAt(goal.getCreatedAt());
        response.setUpdatedAt(goal.getUpdatedAt());

        List<Milestone> milestones = goal.getMilestones();
        response.setTotalMilestones(milestones.size());
        response.setCompletedMilestones(
                (int) milestones.stream().filter(m -> m.getCompletedAt() != null).count());
        response.setMilestones(milestones.stream().map(this::toMilestoneResponse).toList());
        return response;
    }

    /**
     * Converts a Milestone entity to a response DTO.
     */
    public MilestoneResponse toMilestoneResponse(Milestone milestone) {
        MilestoneResponse response = new MilestoneResponse();
        response.setId(milestone.getId());
        response.setGoalId(milestone.getGoal().getId());
        response.setTitle(milestone.getTitle());
        response.setTargetDate(milestone.getTargetDate());
        response.setCompletedAt(milestone.getCompletedAt());
        response.setCompleted(milestone.getCompletedAt() != null);
        response.setNotes(milestone.getNotes());
        response.setCreatedAt(milestone.getCreatedAt());
        return response;
    }

    /**
     * Applies request DTO fields to a Goal entity.
     */
    public void applyRequest(GoalRequest request, Goal goal) {
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setCategory(request.getCategory());
        goal.setTargetDate(request.getTargetDate());
    }

    /**
     * Applies request DTO fields to a Milestone entity.
     */
    public void applyMilestoneRequest(MilestoneRequest request, Milestone milestone) {
        milestone.setTitle(request.getTitle());
        milestone.setTargetDate(request.getTargetDate());
        milestone.setNotes(request.getNotes());
    }
}
