package com.mindtrack.activity.service;

import com.mindtrack.activity.dto.ActivityLogRequest;
import com.mindtrack.activity.dto.ActivityLogResponse;
import com.mindtrack.activity.dto.ActivityRequest;
import com.mindtrack.activity.dto.ActivityResponse;
import com.mindtrack.activity.dto.DailyChecklistResponse;
import com.mindtrack.activity.model.Activity;
import com.mindtrack.activity.model.ActivityLog;
import com.mindtrack.activity.repository.ActivityLogRepository;
import com.mindtrack.activity.repository.ActivityRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for activity CRUD and daily logging operations.
 */
@Service
public class ActivityService {

    private static final Logger LOG = LoggerFactory.getLogger(ActivityService.class);

    private final ActivityRepository activityRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ActivityMapper activityMapper;

    public ActivityService(ActivityRepository activityRepository,
                           ActivityLogRepository activityLogRepository,
                           ActivityMapper activityMapper) {
        this.activityRepository = activityRepository;
        this.activityLogRepository = activityLogRepository;
        this.activityMapper = activityMapper;
    }

    /**
     * Creates a new activity for the given user.
     */
    @Transactional
    public ActivityResponse create(Long userId, ActivityRequest request) {
        Activity activity = new Activity();
        activity.setUserId(userId);
        activity.setCreatedAt(LocalDateTime.now());
        activityMapper.applyRequest(request, activity);

        Activity saved = activityRepository.save(activity);
        LOG.info("Created activity {} for user {}", saved.getId(), userId);
        return activityMapper.toActivityResponse(saved);
    }

    /**
     * Lists activities for the given user, optionally filtered by active status.
     */
    public List<ActivityResponse> listByUser(Long userId, Boolean activeOnly) {
        List<Activity> activities;
        if (activeOnly != null) {
            activities = activityRepository.findByUserIdAndActiveOrderByCreatedAtDesc(
                    userId, activeOnly);
        } else {
            activities = activityRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return activities.stream()
                .map(activityMapper::toActivityResponse)
                .toList();
    }

    /**
     * Gets a single activity by ID, only if it belongs to the given user.
     */
    public ActivityResponse getByIdAndUser(Long activityId, Long userId) {
        return activityRepository.findByIdAndUserId(activityId, userId)
                .map(activityMapper::toActivityResponse)
                .orElse(null);
    }

    /**
     * Updates an existing activity.
     */
    @Transactional
    public ActivityResponse update(Long activityId, Long userId, ActivityRequest request) {
        Activity activity = activityRepository.findByIdAndUserId(activityId, userId)
                .orElse(null);
        if (activity == null) {
            return null;
        }

        activityMapper.applyRequest(request, activity);
        Activity saved = activityRepository.save(activity);
        LOG.info("Updated activity {} for user {}", saved.getId(), userId);
        return activityMapper.toActivityResponse(saved);
    }

    /**
     * Toggles the active status of an activity.
     */
    @Transactional
    public ActivityResponse toggleActive(Long activityId, Long userId) {
        Activity activity = activityRepository.findByIdAndUserId(activityId, userId)
                .orElse(null);
        if (activity == null) {
            return null;
        }

        activity.setActive(!activity.isActive());
        Activity saved = activityRepository.save(activity);
        LOG.info("Toggled activity {} active={} for user {}",
                saved.getId(), saved.isActive(), userId);
        return activityMapper.toActivityResponse(saved);
    }

    /**
     * Deletes an activity if it belongs to the given user.
     */
    @Transactional
    public boolean delete(Long activityId, Long userId) {
        Activity activity = activityRepository.findByIdAndUserId(activityId, userId)
                .orElse(null);
        if (activity == null) {
            return false;
        }

        activityRepository.delete(activity);
        LOG.info("Deleted activity {} for user {}", activityId, userId);
        return true;
    }

    // --- Activity Logging ---

    /**
     * Logs an activity completion for a specific date.
     */
    @Transactional
    public ActivityLogResponse logActivity(Long activityId, Long userId,
                                           ActivityLogRequest request) {
        Activity activity = activityRepository.findByIdAndUserId(activityId, userId)
                .orElse(null);
        if (activity == null) {
            return null;
        }

        ActivityLog log = new ActivityLog();
        log.setActivity(activity);
        log.setLogDate(request.getLogDate());
        log.setCompleted(request.isCompleted());
        log.setNotes(request.getNotes());
        log.setMoodRating(request.getMoodRating());
        log.setCreatedAt(LocalDateTime.now());

        ActivityLog saved = activityLogRepository.save(log);
        LOG.info("Logged activity {} for date {} user {}",
                activityId, request.getLogDate(), userId);
        return activityMapper.toLogResponse(saved);
    }

    /**
     * Lists all logs for a specific activity.
     */
    public List<ActivityLogResponse> listLogs(Long activityId, Long userId) {
        Activity activity = activityRepository.findByIdAndUserId(activityId, userId)
                .orElse(null);
        if (activity == null) {
            return List.of();
        }

        return activityLogRepository.findByActivityIdOrderByLogDateDesc(activityId).stream()
                .map(activityMapper::toLogResponse)
                .toList();
    }

    /**
     * Returns the daily checklist for a given user and date.
     * Shows all active activities with their log status for that date.
     */
    public List<DailyChecklistResponse> getDailyChecklist(Long userId, LocalDate date) {
        List<Activity> activeActivities =
                activityRepository.findByUserIdAndActiveOrderByCreatedAtDesc(userId, true);

        List<ActivityLog> logsForDate =
                activityLogRepository.findByActivity_UserIdAndLogDateOrderByActivity_NameAsc(
                        userId, date);

        Map<Long, ActivityLog> logsByActivityId = logsForDate.stream()
                .collect(Collectors.toMap(
                        log -> log.getActivity().getId(),
                        log -> log,
                        (a, b) -> a));

        return activeActivities.stream()
                .map(activity -> activityMapper.toChecklistItem(
                        activity, logsByActivityId.get(activity.getId()), date))
                .toList();
    }
}
