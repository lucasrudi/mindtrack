package com.mindtrack.activity.controller;

import com.mindtrack.activity.dto.ActivityLogRequest;
import com.mindtrack.activity.dto.ActivityLogResponse;
import com.mindtrack.activity.dto.ActivityRequest;
import com.mindtrack.activity.dto.ActivityResponse;
import com.mindtrack.activity.dto.DailyChecklistResponse;
import com.mindtrack.activity.service.ActivityService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
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
 * REST controller for activity and activity log operations.
 */
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /**
     * Creates a new activity.
     */
    @PostMapping
    public ResponseEntity<ActivityResponse> create(
            @RequestBody @Valid ActivityRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ActivityResponse response = activityService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists activities for the authenticated user, with optional active filter.
     */
    @GetMapping
    public ResponseEntity<List<ActivityResponse>> list(
            @RequestParam(required = false) Boolean active,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<ActivityResponse> activities = activityService.listByUser(userId, active);
        return ResponseEntity.ok(activities);
    }

    /**
     * Gets a single activity by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> getById(
            @PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ActivityResponse response = activityService.getByIdAndUser(id, userId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing activity.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid ActivityRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ActivityResponse response = activityService.update(id, userId, request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Toggles the active status of an activity.
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ActivityResponse> toggleActive(
            @PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ActivityResponse response = activityService.toggleActive(id, userId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes an activity.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean deleted = activityService.delete(id, userId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Logs an activity completion for a specific date.
     */
    @PostMapping("/{id}/logs")
    public ResponseEntity<ActivityLogResponse> logActivity(
            @PathVariable Long id,
            @RequestBody @Valid ActivityLogRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ActivityLogResponse response = activityService.logActivity(id, userId, request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists all logs for a specific activity.
     */
    @GetMapping("/{id}/logs")
    public ResponseEntity<List<ActivityLogResponse>> listLogs(
            @PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<ActivityLogResponse> logs = activityService.listLogs(id, userId);
        return ResponseEntity.ok(logs);
    }

    /**
     * Gets the daily checklist for the authenticated user.
     */
    @GetMapping("/checklist")
    public ResponseEntity<List<DailyChecklistResponse>> dailyChecklist(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        LocalDate targetDate = date != null ? date : LocalDate.now();
        List<DailyChecklistResponse> checklist =
                activityService.getDailyChecklist(userId, targetDate);
        return ResponseEntity.ok(checklist);
    }
}
