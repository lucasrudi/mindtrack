package com.mindtrack.goals.service;

import com.mindtrack.goals.dto.GoalRequest;
import com.mindtrack.goals.dto.GoalResponse;
import com.mindtrack.goals.dto.MilestoneRequest;
import com.mindtrack.goals.dto.MilestoneResponse;
import com.mindtrack.goals.model.Goal;
import com.mindtrack.goals.model.GoalStatus;
import com.mindtrack.goals.model.GoalValidationStatus;
import com.mindtrack.goals.model.Milestone;
import com.mindtrack.goals.repository.GoalRepository;
import com.mindtrack.goals.repository.MilestoneRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for goal and milestone CRUD operations.
 */
@Service
public class GoalService {

    private static final Logger LOG = LoggerFactory.getLogger(GoalService.class);

    private final GoalRepository goalRepository;
    private final MilestoneRepository milestoneRepository;
    private final GoalMapper goalMapper;

    public GoalService(GoalRepository goalRepository,
                       MilestoneRepository milestoneRepository,
                       GoalMapper goalMapper) {
        this.goalRepository = goalRepository;
        this.milestoneRepository = milestoneRepository;
        this.goalMapper = goalMapper;
    }

    /**
     * Creates a new goal for the given user.
     */
    @Transactional
    public GoalResponse create(Long userId, GoalRequest request) {
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        goal.setValidationStatus(GoalValidationStatus.PENDING_VALIDATION);
        goal.setCreatedBy(userId);
        goalMapper.applyRequest(request, goal);

        Goal saved = goalRepository.save(goal);
        LOG.info("Created goal {} for user {}", saved.getId(), userId);
        return goalMapper.toGoalResponse(saved);
    }

    /**
     * Lists goals for the given user, optionally filtered by status.
     */
    public List<GoalResponse> listByUser(Long userId, GoalStatus status) {
        List<Goal> goals;
        if (status != null) {
            goals = goalRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        } else {
            goals = goalRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return goals.stream().map(goalMapper::toGoalResponse).toList();
    }

    /**
     * Gets a single goal by ID, only if it belongs to the given user.
     */
    public GoalResponse getByIdAndUser(Long goalId, Long userId) {
        return goalRepository.findByIdAndUserId(goalId, userId)
                .map(goalMapper::toGoalResponse)
                .orElse(null);
    }

    /**
     * Updates an existing goal.
     */
    @Transactional
    public GoalResponse update(Long goalId, Long userId, GoalRequest request) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId).orElse(null);
        if (goal == null) {
            return null;
        }

        goalMapper.applyRequest(request, goal);
        goal.setUpdatedAt(LocalDateTime.now());
        Goal saved = goalRepository.save(goal);
        LOG.info("Updated goal {} for user {}", saved.getId(), userId);
        return goalMapper.toGoalResponse(saved);
    }

    /**
     * Updates the status of a goal.
     */
    @Transactional
    public GoalResponse updateStatus(Long goalId, Long userId, GoalStatus newStatus) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId).orElse(null);
        if (goal == null) {
            return null;
        }

        goal.setStatus(newStatus);
        goal.setUpdatedAt(LocalDateTime.now());
        Goal saved = goalRepository.save(goal);
        LOG.info("Updated goal {} status to {} for user {}",
                saved.getId(), newStatus, userId);
        return goalMapper.toGoalResponse(saved);
    }

    /**
     * Deletes a goal if it belongs to the given user.
     */
    @Transactional
    public boolean delete(Long goalId, Long userId) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId).orElse(null);
        if (goal == null) {
            return false;
        }

        goalRepository.delete(goal);
        LOG.info("Deleted goal {} for user {}", goalId, userId);
        return true;
    }

    // --- Milestone operations ---

    /**
     * Adds a milestone to a goal.
     */
    @Transactional
    public MilestoneResponse addMilestone(Long goalId, Long userId, MilestoneRequest request) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId).orElse(null);
        if (goal == null) {
            return null;
        }

        Milestone milestone = new Milestone();
        milestone.setGoal(goal);
        milestone.setCreatedAt(LocalDateTime.now());
        goalMapper.applyMilestoneRequest(request, milestone);

        Milestone saved = milestoneRepository.save(milestone);
        LOG.info("Added milestone {} to goal {} for user {}",
                saved.getId(), goalId, userId);
        return goalMapper.toMilestoneResponse(saved);
    }

    /**
     * Toggles the completed status of a milestone.
     */
    @Transactional
    public MilestoneResponse toggleMilestoneCompletion(
            Long goalId, Long userId, Long milestoneId) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId).orElse(null);
        if (goal == null) {
            return null;
        }

        Milestone milestone = milestoneRepository.findByIdAndGoalId(milestoneId, goalId)
                .orElse(null);
        if (milestone == null) {
            return null;
        }

        if (milestone.getCompletedAt() != null) {
            milestone.setCompletedAt(null);
        } else {
            milestone.setCompletedAt(LocalDateTime.now());
        }

        Milestone saved = milestoneRepository.save(milestone);
        LOG.info("Toggled milestone {} completed={} in goal {} for user {}",
                milestoneId, saved.getCompletedAt() != null, goalId, userId);
        return goalMapper.toMilestoneResponse(saved);
    }

    /**
     * Deletes a milestone from a goal.
     */
    @Transactional
    public boolean deleteMilestone(Long goalId, Long userId, Long milestoneId) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId).orElse(null);
        if (goal == null) {
            return false;
        }

        Milestone milestone = milestoneRepository.findByIdAndGoalId(milestoneId, goalId)
                .orElse(null);
        if (milestone == null) {
            return false;
        }

        milestoneRepository.delete(milestone);
        LOG.info("Deleted milestone {} from goal {} for user {}",
                milestoneId, goalId, userId);
        return true;
    }
}
