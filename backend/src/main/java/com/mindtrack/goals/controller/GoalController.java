package com.mindtrack.goals.controller;

import com.mindtrack.audit.model.AuditAction;
import com.mindtrack.audit.service.AuditService;
import com.mindtrack.goals.dto.GoalRequest;
import com.mindtrack.goals.dto.GoalResponse;
import com.mindtrack.goals.dto.MilestoneRequest;
import com.mindtrack.goals.dto.MilestoneResponse;
import com.mindtrack.goals.model.GoalStatus;
import com.mindtrack.goals.service.GoalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for goal and milestone operations.
 */
@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;
    private final AuditService auditService;

    public GoalController(GoalService goalService, AuditService auditService) {
        this.goalService = goalService;
        this.auditService = auditService;
    }

    /**
     * Creates a new goal.
     */
    @PostMapping
    public ResponseEntity<GoalResponse> create(
            @RequestBody @Valid GoalRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        GoalResponse response = goalService.create(userId, request);
        auditService.log(userId, AuditAction.WRITE, "GOAL", response.getId(), userId,
                getClientIp(httpRequest), "WEB");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists goals for the authenticated user, with optional status filter.
     */
    @GetMapping
    public ResponseEntity<List<GoalResponse>> list(
            @RequestParam(required = false) GoalStatus status,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        List<GoalResponse> goals = goalService.listByUser(userId, status);
        for (GoalResponse goal : goals) {
            auditService.log(userId, AuditAction.READ, "GOAL", goal.getId(), userId,
                    getClientIp(httpRequest), "WEB");
        }
        return ResponseEntity.ok(goals);
    }

    /**
     * Gets a single goal by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getById(
            @PathVariable Long id, Authentication authentication,
            HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        GoalResponse response = goalService.getByIdAndUser(id, userId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        auditService.log(userId, AuditAction.READ, "GOAL", id, userId,
                getClientIp(httpRequest), "WEB");
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing goal.
     */
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid GoalRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        GoalResponse response = goalService.update(id, userId, request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        auditService.log(userId, AuditAction.WRITE, "GOAL", id, userId,
                getClientIp(httpRequest), "WEB");
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the status of a goal.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<GoalResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        GoalStatus newStatus;
        try {
            newStatus = GoalStatus.valueOf(body.get("status"));
        } catch (IllegalArgumentException | NullPointerException ex) {
            return ResponseEntity.badRequest().build();
        }
        GoalResponse response = goalService.updateStatus(id, userId, newStatus);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        auditService.log(userId, AuditAction.WRITE, "GOAL", id, userId,
                getClientIp(httpRequest), "WEB");
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a goal.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id, Authentication authentication,
            HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        boolean deleted = goalService.delete(id, userId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        auditService.log(userId, AuditAction.DELETE, "GOAL", id, userId,
                getClientIp(httpRequest), "WEB");
        return ResponseEntity.noContent().build();
    }

    /**
     * Adds a milestone to a goal.
     */
    @PostMapping("/{goalId}/milestones")
    public ResponseEntity<MilestoneResponse> addMilestone(
            @PathVariable Long goalId,
            @RequestBody @Valid MilestoneRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        MilestoneResponse response = goalService.addMilestone(goalId, userId, request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        auditService.log(userId, AuditAction.WRITE, "GOAL", goalId, userId,
                getClientIp(httpRequest), "WEB");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Toggles the completion status of a milestone.
     */
    @PatchMapping("/{goalId}/milestones/{milestoneId}/toggle")
    public ResponseEntity<MilestoneResponse> toggleMilestone(
            @PathVariable Long goalId,
            @PathVariable Long milestoneId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        MilestoneResponse response = goalService.toggleMilestoneCompletion(
                goalId, userId, milestoneId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        auditService.log(userId, AuditAction.WRITE, "GOAL", goalId, userId,
                getClientIp(httpRequest), "WEB");
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a milestone from a goal.
     */
    @DeleteMapping("/{goalId}/milestones/{milestoneId}")
    public ResponseEntity<Void> deleteMilestone(
            @PathVariable Long goalId,
            @PathVariable Long milestoneId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        boolean deleted = goalService.deleteMilestone(goalId, userId, milestoneId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        auditService.log(userId, AuditAction.DELETE, "GOAL", goalId, userId,
                getClientIp(httpRequest), "WEB");
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
