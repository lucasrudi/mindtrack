package com.mindtrack.activity.service;

import com.mindtrack.activity.dto.ActivityLogResponse;
import com.mindtrack.activity.dto.ActivityRequest;
import com.mindtrack.activity.dto.ActivityResponse;
import com.mindtrack.activity.dto.DailyChecklistResponse;
import com.mindtrack.activity.model.Activity;
import com.mindtrack.activity.model.ActivityLog;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/**
 * Maps between Activity/ActivityLog entities and DTOs.
 */
@Component
public class ActivityMapper {

    /**
     * Converts an Activity entity to a response DTO.
     */
    public ActivityResponse toActivityResponse(Activity activity) {
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setType(activity.getType());
        response.setName(activity.getName());
        response.setDescription(activity.getDescription());
        response.setFrequency(activity.getFrequency());
        response.setLinkedInterviewId(activity.getLinkedInterviewId());
        response.setActive(activity.isActive());
        response.setCreatedAt(activity.getCreatedAt());
        return response;
    }

    /**
     * Applies request DTO fields to an Activity entity.
     */
    public void applyRequest(ActivityRequest request, Activity activity) {
        activity.setType(request.getType());
        activity.setName(request.getName());
        activity.setDescription(request.getDescription());
        activity.setFrequency(request.getFrequency());
        activity.setLinkedInterviewId(request.getLinkedInterviewId());
    }

    /**
     * Converts an ActivityLog entity to a response DTO.
     */
    public ActivityLogResponse toLogResponse(ActivityLog log) {
        ActivityLogResponse response = new ActivityLogResponse();
        response.setId(log.getId());
        response.setActivityId(log.getActivity().getId());
        response.setActivityName(log.getActivity().getName());
        response.setLogDate(log.getLogDate());
        response.setCompleted(log.isCompleted());
        response.setNotes(log.getNotes());
        response.setMoodRating(log.getMoodRating());
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }

    /**
     * Creates a daily checklist response combining an activity and optional log.
     */
    public DailyChecklistResponse toChecklistItem(Activity activity, ActivityLog log,
                                                   LocalDate date) {
        DailyChecklistResponse item = new DailyChecklistResponse();
        item.setActivityId(activity.getId());
        item.setActivityName(activity.getName());
        item.setActivityType(activity.getType().name());
        item.setDate(date);
        if (log != null) {
            item.setLogId(log.getId());
            item.setCompleted(log.isCompleted());
            item.setNotes(log.getNotes());
            item.setMoodRating(log.getMoodRating());
        }
        return item;
    }
}
