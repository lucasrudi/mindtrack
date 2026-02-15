package com.mindtrack.activity.service;

import com.mindtrack.activity.dto.ActivityLogResponse;
import com.mindtrack.activity.dto.ActivityRequest;
import com.mindtrack.activity.dto.ActivityResponse;
import com.mindtrack.activity.dto.DailyChecklistResponse;
import com.mindtrack.activity.model.Activity;
import com.mindtrack.activity.model.ActivityLog;
import com.mindtrack.activity.model.ActivityType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivityMapperTest {

    private ActivityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ActivityMapper();
    }

    @Test
    void shouldMapActivityToResponse() {
        Activity activity = createActivity(1L);

        ActivityResponse response = mapper.toActivityResponse(activity);

        assertEquals(1L, response.getId());
        assertEquals(ActivityType.EXERCISE, response.getType());
        assertEquals("Morning jog", response.getName());
        assertEquals("30 minutes", response.getDescription());
        assertEquals("Daily", response.getFrequency());
        assertNull(response.getLinkedInterviewId());
        assertTrue(response.isActive());
    }

    @Test
    void shouldApplyRequestToActivity() {
        ActivityRequest request = new ActivityRequest();
        request.setType(ActivityType.MEDITATION);
        request.setName("Evening meditation");
        request.setDescription("15 minute guided meditation");
        request.setFrequency("Daily");
        request.setLinkedInterviewId(5L);

        Activity activity = new Activity();
        mapper.applyRequest(request, activity);

        assertEquals(ActivityType.MEDITATION, activity.getType());
        assertEquals("Evening meditation", activity.getName());
        assertEquals("15 minute guided meditation", activity.getDescription());
        assertEquals("Daily", activity.getFrequency());
        assertEquals(5L, activity.getLinkedInterviewId());
    }

    @Test
    void shouldMapActivityLogToResponse() {
        Activity activity = createActivity(1L);
        ActivityLog log = createLog(10L, activity);

        ActivityLogResponse response = mapper.toLogResponse(log);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getActivityId());
        assertEquals("Morning jog", response.getActivityName());
        assertEquals(LocalDate.of(2025, 1, 15), response.getLogDate());
        assertTrue(response.isCompleted());
        assertEquals("Felt great", response.getNotes());
        assertEquals(8, response.getMoodRating());
    }

    @Test
    void shouldMapChecklistItemWithLog() {
        Activity activity = createActivity(1L);
        ActivityLog log = createLog(10L, activity);
        LocalDate date = LocalDate.of(2025, 1, 15);

        DailyChecklistResponse item = mapper.toChecklistItem(activity, log, date);

        assertEquals(1L, item.getActivityId());
        assertEquals("Morning jog", item.getActivityName());
        assertEquals("EXERCISE", item.getActivityType());
        assertEquals(date, item.getDate());
        assertEquals(10L, item.getLogId());
        assertTrue(item.isCompleted());
        assertEquals("Felt great", item.getNotes());
        assertEquals(8, item.getMoodRating());
    }

    @Test
    void shouldMapChecklistItemWithoutLog() {
        Activity activity = createActivity(1L);
        LocalDate date = LocalDate.of(2025, 1, 15);

        DailyChecklistResponse item = mapper.toChecklistItem(activity, null, date);

        assertEquals(1L, item.getActivityId());
        assertEquals("Morning jog", item.getActivityName());
        assertEquals(date, item.getDate());
        assertNull(item.getLogId());
        assertFalse(item.isCompleted());
        assertNull(item.getNotes());
        assertNull(item.getMoodRating());
    }

    private Activity createActivity(Long id) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setUserId(1L);
        activity.setType(ActivityType.EXERCISE);
        activity.setName("Morning jog");
        activity.setDescription("30 minutes");
        activity.setFrequency("Daily");
        activity.setActive(true);
        activity.setCreatedAt(LocalDateTime.now());
        return activity;
    }

    private ActivityLog createLog(Long id, Activity activity) {
        ActivityLog log = new ActivityLog();
        log.setId(id);
        log.setActivity(activity);
        log.setLogDate(LocalDate.of(2025, 1, 15));
        log.setCompleted(true);
        log.setNotes("Felt great");
        log.setMoodRating(8);
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }
}
