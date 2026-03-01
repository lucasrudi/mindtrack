package com.mindtrack.therapist.service;

import com.mindtrack.activity.dto.ActivityResponse;
import com.mindtrack.activity.model.Activity;
import com.mindtrack.activity.repository.ActivityRepository;
import com.mindtrack.activity.service.ActivityMapper;
import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.User;
import com.mindtrack.goals.dto.GoalRequest;
import com.mindtrack.goals.dto.GoalResponse;
import com.mindtrack.goals.model.Goal;
import com.mindtrack.goals.model.GoalStatus;
import com.mindtrack.goals.model.GoalValidationStatus;
import com.mindtrack.goals.repository.GoalRepository;
import com.mindtrack.goals.service.GoalMapper;
import com.mindtrack.interview.dto.InterviewResponse;
import com.mindtrack.interview.model.Interview;
import com.mindtrack.interview.repository.InterviewRepository;
import com.mindtrack.interview.service.InterviewMapper;
import com.mindtrack.journal.dto.JournalEntryResponse;
import com.mindtrack.journal.model.JournalEntry;
import com.mindtrack.journal.repository.JournalEntryRepository;
import com.mindtrack.journal.service.JournalMapper;
import com.mindtrack.therapist.dto.PatientDetailResponse;
import com.mindtrack.therapist.dto.PatientSummaryResponse;
import com.mindtrack.therapist.model.TherapistPatient;
import com.mindtrack.therapist.model.TherapistPatientStatus;
import com.mindtrack.therapist.repository.TherapistPatientRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TherapistServiceTest {

    @Mock
    private TherapistPatientRepository therapistPatientRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InterviewRepository interviewRepository;
    @Mock
    private InterviewMapper interviewMapper;
    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private ActivityMapper activityMapper;
    @Mock
    private GoalRepository goalRepository;
    @Spy
    private GoalMapper goalMapper;
    @Mock
    private JournalEntryRepository journalEntryRepository;
    @Mock
    private JournalMapper journalMapper;
    @Spy
    private TherapistMapper therapistMapper;

    private TherapistService therapistService;

    @BeforeEach
    void setUp() {
        therapistService = new TherapistService(
                therapistPatientRepository, userRepository,
                interviewRepository, interviewMapper,
                activityRepository, activityMapper,
                goalRepository, goalMapper,
                journalEntryRepository, journalMapper,
                therapistMapper);
    }

    @Test
    void shouldListActivePatientsWithSummary() {
        TherapistPatient rel = new TherapistPatient(3L, 1L, TherapistPatientStatus.ACTIVE);
        User patient = createUser(1L, "John Patient", "john@example.com");
        Interview interview = new Interview();
        interview.setCreatedAt(LocalDateTime.of(2025, 1, 15, 10, 0));

        when(therapistPatientRepository.findByTherapistIdAndStatus(3L, TherapistPatientStatus.ACTIVE))
                .thenReturn(List.of(rel));
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(interviewRepository.findByUserIdOrderByInterviewDateDesc(1L))
                .thenReturn(List.of(interview));
        when(goalRepository.findByUserIdAndStatusOrderByCreatedAtDesc(1L, GoalStatus.IN_PROGRESS))
                .thenReturn(List.of(new Goal()));
        when(activityRepository.findByUserIdAndActiveOrderByCreatedAtDesc(1L, true))
                .thenReturn(List.of(new Activity(), new Activity()));

        List<PatientSummaryResponse> result = therapistService.listPatients(3L);

        assertEquals(1, result.size());
        assertEquals("John Patient", result.get(0).getName());
        assertEquals(1, result.get(0).getInterviewCount());
        assertEquals(1, result.get(0).getActiveGoalCount());
        assertEquals(2, result.get(0).getActivityCount());
    }

    @Test
    void shouldReturnEmptyListWhenNoPatients() {
        when(therapistPatientRepository.findByTherapistIdAndStatus(3L, TherapistPatientStatus.ACTIVE))
                .thenReturn(List.of());

        List<PatientSummaryResponse> result = therapistService.listPatients(3L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetPatientDetailWithAllData() {
        mockActiveRelationship(3L, 1L);
        User patient = createUser(1L, "John Patient", "john@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));

        Interview interview = new Interview();
        InterviewResponse interviewResponse = new InterviewResponse();
        when(interviewRepository.findByUserIdOrderByInterviewDateDesc(1L))
                .thenReturn(List.of(interview));
        when(interviewMapper.toResponse(interview)).thenReturn(interviewResponse);

        Activity activity = new Activity();
        ActivityResponse activityResponse = new ActivityResponse();
        when(activityRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(activity));
        when(activityMapper.toActivityResponse(activity)).thenReturn(activityResponse);

        Goal goal = new Goal();
        GoalResponse goalResponse = new GoalResponse();
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(goal));
        when(goalMapper.toGoalResponse(goal)).thenReturn(goalResponse);

        JournalEntry journal = new JournalEntry();
        JournalEntryResponse journalResponse = new JournalEntryResponse();
        when(journalEntryRepository.findByUserIdAndSharedWithTherapistOrderByEntryDateDesc(1L, true))
                .thenReturn(List.of(journal));
        when(journalMapper.toResponse(journal)).thenReturn(journalResponse);

        PatientDetailResponse result = therapistService.getPatientDetail(3L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getPatientId());
        assertEquals("John Patient", result.getPatientName());
        assertEquals(1, result.getInterviews().size());
        assertEquals(1, result.getActivities().size());
        assertEquals(1, result.getGoals().size());
        assertEquals(1, result.getSharedJournalEntries().size());
    }

    @Test
    void shouldThrowWhenNoRelationshipExists() {
        when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                3L, 1L, TherapistPatientStatus.ACTIVE)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> therapistService.getPatientDetail(3L, 1L));
    }

    @Test
    void shouldGetPatientInterviews() {
        mockActiveRelationship(3L, 1L);
        Interview interview = new Interview();
        InterviewResponse response = new InterviewResponse();
        when(interviewRepository.findByUserIdOrderByInterviewDateDesc(1L))
                .thenReturn(List.of(interview));
        when(interviewMapper.toResponse(interview)).thenReturn(response);

        List<InterviewResponse> result = therapistService.getPatientInterviews(3L, 1L);

        assertEquals(1, result.size());
    }

    @Test
    void shouldGetPatientActivities() {
        mockActiveRelationship(3L, 1L);
        Activity activity = new Activity();
        ActivityResponse response = new ActivityResponse();
        when(activityRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(activity));
        when(activityMapper.toActivityResponse(activity)).thenReturn(response);

        List<ActivityResponse> result = therapistService.getPatientActivities(3L, 1L);

        assertEquals(1, result.size());
    }

    @Test
    void shouldGetPatientGoals() {
        mockActiveRelationship(3L, 1L);
        Goal goal = new Goal();
        GoalResponse response = new GoalResponse();
        when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(goal));
        when(goalMapper.toGoalResponse(goal)).thenReturn(response);

        List<GoalResponse> result = therapistService.getPatientGoals(3L, 1L);

        assertEquals(1, result.size());
    }

    @Test
    void shouldGetOnlySharedJournalEntries() {
        mockActiveRelationship(3L, 1L);
        JournalEntry journal = new JournalEntry();
        JournalEntryResponse response = new JournalEntryResponse();
        when(journalEntryRepository.findByUserIdAndSharedWithTherapistOrderByEntryDateDesc(1L, true))
                .thenReturn(List.of(journal));
        when(journalMapper.toResponse(journal)).thenReturn(response);

        List<JournalEntryResponse> result = therapistService.getPatientSharedJournal(3L, 1L);

        assertEquals(1, result.size());
    }

    @Test
    void shouldThrowForInactiveRelationship() {
        when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                3L, 1L, TherapistPatientStatus.ACTIVE)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> therapistService.getPatientInterviews(3L, 1L));
    }

    @Test
    void shouldCreateGoalForPatientAsValidated() {
        when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                1L, 2L, TherapistPatientStatus.ACTIVE)).thenReturn(true);
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> {
            Goal g = inv.getArgument(0);
            g.setId(10L);
            return g;
        });

        GoalRequest request = new GoalRequest();
        request.setTitle("Therapist goal");

        GoalResponse result = therapistService.createGoalForPatient(1L, 2L, request);

        assertEquals(10L, result.getId());
        assertEquals(GoalValidationStatus.VALIDATED, result.getValidationStatus());
        assertEquals(1L, result.getCreatedBy());
    }

    @Test
    void shouldValidateGoal() {
        Goal goal = createGoal(10L, 2L);
        goal.setValidationStatus(GoalValidationStatus.PENDING_VALIDATION);
        when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                1L, 2L, TherapistPatientStatus.ACTIVE)).thenReturn(true);
        when(goalRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);

        GoalResponse result = therapistService.validateGoal(1L, 2L, 10L);

        assertEquals(GoalValidationStatus.VALIDATED, result.getValidationStatus());
        assertEquals(1L, result.getValidatedBy());
    }

    @Test
    void shouldRejectGoal() {
        Goal goal = createGoal(10L, 2L);
        when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                1L, 2L, TherapistPatientStatus.ACTIVE)).thenReturn(true);
        when(goalRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);

        GoalResponse result = therapistService.rejectGoal(1L, 2L, 10L);

        assertEquals(GoalValidationStatus.REJECTED, result.getValidationStatus());
    }

    @Test
    void shouldEditGoalAndSetOverridden() {
        Goal goal = createGoal(10L, 2L);
        goal.setValidationStatus(GoalValidationStatus.PENDING_VALIDATION);
        GoalRequest request = new GoalRequest();
        request.setTitle("Updated by therapist");

        when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                1L, 2L, TherapistPatientStatus.ACTIVE)).thenReturn(true);
        when(goalRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);

        GoalResponse result = therapistService.editGoalForPatient(1L, 2L, 10L, request);

        assertEquals(GoalValidationStatus.OVERRIDDEN, result.getValidationStatus());
        assertEquals(1L, result.getValidatedBy());
    }

    private void mockActiveRelationship(Long therapistId, Long patientId) {
        when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
                therapistId, patientId, TherapistPatientStatus.ACTIVE)).thenReturn(true);
    }

    private Goal createGoal(Long goalId, Long userId) {
        Goal goal = new Goal();
        goal.setId(goalId);
        goal.setUserId(userId);
        goal.setTitle("Test goal");
        goal.setStatus(GoalStatus.NOT_STARTED);
        goal.setValidationStatus(GoalValidationStatus.PENDING_VALIDATION);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        return goal;
    }

    private User createUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
