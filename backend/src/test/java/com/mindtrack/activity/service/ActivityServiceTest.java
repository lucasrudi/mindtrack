package com.mindtrack.activity.service;

import com.mindtrack.activity.dto.ActivityLogRequest;
import com.mindtrack.activity.dto.ActivityLogResponse;
import com.mindtrack.activity.dto.ActivityRequest;
import com.mindtrack.activity.dto.ActivityResponse;
import com.mindtrack.activity.dto.DailyChecklistResponse;
import com.mindtrack.activity.model.Activity;
import com.mindtrack.activity.model.ActivityLog;
import com.mindtrack.activity.model.ActivityType;
import com.mindtrack.activity.repository.ActivityLogRepository;
import com.mindtrack.activity.repository.ActivityRepository;
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
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivityLogRepository activityLogRepository;

    private ActivityService activityService;

    @BeforeEach
    void setUp() {
        ActivityMapper mapper = new ActivityMapper();
        activityService = new ActivityService(
                activityRepository, activityLogRepository, mapper);
    }

    @Test
    void shouldCreateActivity() {
        ActivityRequest request = createRequest();
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        ActivityResponse result = activityService.create(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(ActivityType.EXERCISE, result.getType());
        assertEquals("Morning jog", result.getName());

        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
        verify(activityRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getUserId());
    }

    @Test
    void shouldListAllActivitiesByUser() {
        Activity a1 = createActivity(1L);
        Activity a2 = createActivity(2L);
        when(activityRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(a1, a2));

        List<ActivityResponse> results = activityService.listByUser(1L, null);

        assertEquals(2, results.size());
    }

    @Test
    void shouldListActiveActivitiesOnly() {
        Activity a1 = createActivity(1L);
        when(activityRepository.findByUserIdAndActiveOrderByCreatedAtDesc(1L, true))
                .thenReturn(List.of(a1));

        List<ActivityResponse> results = activityService.listByUser(1L, true);

        assertEquals(1, results.size());
    }

    @Test
    void shouldGetActivityByIdAndUser() {
        Activity activity = createActivity(1L);
        when(activityRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(activity));

        ActivityResponse result = activityService.getByIdAndUser(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldReturnNullWhenActivityNotFound() {
        when(activityRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        assertNull(activityService.getByIdAndUser(999L, 1L));
    }

    @Test
    void shouldUpdateActivity() {
        Activity existing = createActivity(1L);
        ActivityRequest request = createRequest();
        request.setName("Evening jog");

        when(activityRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(existing));
        when(activityRepository.save(any(Activity.class))).thenReturn(existing);

        ActivityResponse result = activityService.update(1L, 1L, request);

        assertNotNull(result);
        assertEquals("Evening jog", result.getName());
    }

    @Test
    void shouldReturnNullWhenUpdatingNonExistent() {
        when(activityRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        assertNull(activityService.update(999L, 1L, createRequest()));
        verify(activityRepository, never()).save(any());
    }

    @Test
    void shouldToggleActiveStatus() {
        Activity activity = createActivity(1L);
        assertTrue(activity.isActive());

        when(activityRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(activity));
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        ActivityResponse result = activityService.toggleActive(1L, 1L);

        assertNotNull(result);
        assertFalse(result.isActive());
    }

    @Test
    void shouldDeleteActivity() {
        Activity activity = createActivity(1L);
        when(activityRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(activity));

        assertTrue(activityService.delete(1L, 1L));
        verify(activityRepository).delete(activity);
    }

    @Test
    void shouldReturnFalseWhenDeletingNonExistent() {
        when(activityRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        assertFalse(activityService.delete(999L, 1L));
        verify(activityRepository, never()).delete(any());
    }

    @Test
    void shouldLogActivity() {
        Activity activity = createActivity(1L);
        when(activityRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(activity));
        when(activityLogRepository.save(any(ActivityLog.class))).thenAnswer(invocation -> {
            ActivityLog saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        ActivityLogRequest logRequest = new ActivityLogRequest();
        logRequest.setLogDate(LocalDate.of(2025, 1, 15));
        logRequest.setCompleted(true);
        logRequest.setNotes("Great session");
        logRequest.setMoodRating(8);

        ActivityLogResponse result = activityService.logActivity(1L, 1L, logRequest);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertTrue(result.isCompleted());
        assertEquals("Great session", result.getNotes());
        assertEquals(8, result.getMoodRating());
    }

    @Test
    void shouldReturnNullWhenLoggingNonExistentActivity() {
        when(activityRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        ActivityLogRequest logRequest = new ActivityLogRequest();
        logRequest.setLogDate(LocalDate.now());

        assertNull(activityService.logActivity(999L, 1L, logRequest));
    }

    @Test
    void shouldGetDailyChecklist() {
        Activity a1 = createActivity(1L);
        a1.setName("Morning jog");
        Activity a2 = createActivity(2L);
        a2.setName("Meditation");

        ActivityLog log = new ActivityLog();
        log.setId(10L);
        log.setActivity(a1);
        log.setLogDate(LocalDate.of(2025, 1, 15));
        log.setCompleted(true);
        log.setCreatedAt(LocalDateTime.now());

        when(activityRepository.findByUserIdAndActiveOrderByCreatedAtDesc(1L, true))
                .thenReturn(List.of(a1, a2));
        when(activityLogRepository
                .findByActivity_UserIdAndLogDateOrderByActivity_NameAsc(
                        1L, LocalDate.of(2025, 1, 15)))
                .thenReturn(List.of(log));

        List<DailyChecklistResponse> checklist =
                activityService.getDailyChecklist(1L, LocalDate.of(2025, 1, 15));

        assertEquals(2, checklist.size());

        DailyChecklistResponse item1 = checklist.get(0);
        assertEquals("Morning jog", item1.getActivityName());
        assertTrue(item1.isCompleted());
        assertEquals(10L, item1.getLogId());

        DailyChecklistResponse item2 = checklist.get(1);
        assertEquals("Meditation", item2.getActivityName());
        assertFalse(item2.isCompleted());
        assertNull(item2.getLogId());
    }

    private ActivityRequest createRequest() {
        ActivityRequest request = new ActivityRequest();
        request.setType(ActivityType.EXERCISE);
        request.setName("Morning jog");
        request.setDescription("30 minutes");
        request.setFrequency("Daily");
        return request;
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
}
