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
import com.mindtrack.journal.repository.JournalEntryRepository;
import com.mindtrack.journal.service.JournalMapper;
import com.mindtrack.therapist.dto.PatientDetailResponse;
import com.mindtrack.therapist.dto.PatientSummaryResponse;
import com.mindtrack.therapist.model.TherapistPatient;
import com.mindtrack.therapist.model.TherapistPatientStatus;
import com.mindtrack.therapist.repository.TherapistPatientRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for therapist read-only patient data access.
 */
@Service
public class TherapistService {

    private static final Logger LOG = LoggerFactory.getLogger(TherapistService.class);

    private final TherapistPatientRepository therapistPatientRepository;
    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewMapper interviewMapper;
    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;
    private final GoalRepository goalRepository;
    private final GoalMapper goalMapper;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalMapper journalMapper;
    private final TherapistMapper therapistMapper;

    public TherapistService(TherapistPatientRepository therapistPatientRepository,
                            UserRepository userRepository,
                            InterviewRepository interviewRepository,
                            InterviewMapper interviewMapper,
                            ActivityRepository activityRepository,
                            ActivityMapper activityMapper,
                            GoalRepository goalRepository,
                            GoalMapper goalMapper,
                            JournalEntryRepository journalEntryRepository,
                            JournalMapper journalMapper,
                            TherapistMapper therapistMapper) {
        this.therapistPatientRepository = therapistPatientRepository;
        this.userRepository = userRepository;
        this.interviewRepository = interviewRepository;
        this.interviewMapper = interviewMapper;
        this.activityRepository = activityRepository;
        this.activityMapper = activityMapper;
        this.goalRepository = goalRepository;
        this.goalMapper = goalMapper;
        this.journalEntryRepository = journalEntryRepository;
        this.journalMapper = journalMapper;
        this.therapistMapper = therapistMapper;
    }

    /**
     * Lists active patients assigned to the therapist.
     */
    public List<PatientSummaryResponse> listPatients(Long therapistId) {
        LOG.info("Listing patients for therapist {}", therapistId);
        return therapistPatientRepository
                .findByTherapistIdAndStatus(therapistId, TherapistPatientStatus.ACTIVE)
                .stream()
                .map(rel -> {
                    User patient = userRepository.findById(rel.getPatientId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Patient not found: " + rel.getPatientId()));
                    List<Interview> interviews = interviewRepository
                            .findByUserIdOrderByInterviewDateDesc(rel.getPatientId());
                    List<Goal> activeGoals = goalRepository
                            .findByUserIdAndStatusOrderByCreatedAtDesc(
                                    rel.getPatientId(), GoalStatus.IN_PROGRESS);
                    List<Activity> activities = activityRepository
                            .findByUserIdAndActiveOrderByCreatedAtDesc(rel.getPatientId(), true);
                    return therapistMapper.toPatientSummary(
                            patient,
                            interviews.size(),
                            activeGoals.size(),
                            activities.size(),
                            interviews.isEmpty() ? null : interviews.get(0).getCreatedAt());
                })
                .toList();
    }

    /**
     * Gets all read-only data for a patient, validating the therapist relationship.
     */
    public PatientDetailResponse getPatientDetail(Long therapistId, Long patientId) {
        LOG.info("Getting patient detail for therapist {} patient {}", therapistId, patientId);
        validateTherapistPatientRelationship(therapistId, patientId);

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

        List<InterviewResponse> interviews = interviewRepository
                .findByUserIdOrderByInterviewDateDesc(patientId).stream()
                .map(interviewMapper::toResponse).toList();

        List<ActivityResponse> activities = activityRepository
                .findByUserIdOrderByCreatedAtDesc(patientId).stream()
                .map(activityMapper::toActivityResponse).toList();

        List<GoalResponse> goals = goalRepository
                .findByUserIdOrderByCreatedAtDesc(patientId).stream()
                .map(goalMapper::toGoalResponse).toList();

        List<JournalEntryResponse> sharedJournal = journalEntryRepository
                .findByUserIdAndSharedWithTherapistOrderByEntryDateDesc(patientId, true)
                .stream().map(journalMapper::toResponse).toList();

        return new PatientDetailResponse(patientId, patient.getName(), patient.getEmail(),
                interviews, activities, goals, sharedJournal);
    }

    /**
     * Gets patient's interviews (read-only).
     */
    public List<InterviewResponse> getPatientInterviews(Long therapistId, Long patientId) {
        LOG.info("Getting patient interviews for therapist {} patient {}", therapistId, patientId);
        validateTherapistPatientRelationship(therapistId, patientId);
        return interviewRepository.findByUserIdOrderByInterviewDateDesc(patientId).stream()
                .map(interviewMapper::toResponse).toList();
    }

    /**
     * Gets patient's activities (read-only).
     */
    public List<ActivityResponse> getPatientActivities(Long therapistId, Long patientId) {
        LOG.info("Getting patient activities for therapist {} patient {}", therapistId, patientId);
        validateTherapistPatientRelationship(therapistId, patientId);
        return activityRepository.findByUserIdOrderByCreatedAtDesc(patientId).stream()
                .map(activityMapper::toActivityResponse).toList();
    }

    /**
     * Gets patient's goals (read-only).
     */
    public List<GoalResponse> getPatientGoals(Long therapistId, Long patientId) {
        LOG.info("Getting patient goals for therapist {} patient {}", therapistId, patientId);
        validateTherapistPatientRelationship(therapistId, patientId);
        return goalRepository.findByUserIdOrderByCreatedAtDesc(patientId).stream()
                .map(goalMapper::toGoalResponse).toList();
    }

    /**
     * Gets patient's shared journal entries (only sharedWithTherapist == true).
     */
    public List<JournalEntryResponse> getPatientSharedJournal(Long therapistId, Long patientId) {
        LOG.info("Getting shared journal for therapist {} patient {}", therapistId, patientId);
        validateTherapistPatientRelationship(therapistId, patientId);
        return journalEntryRepository
                .findByUserIdAndSharedWithTherapistOrderByEntryDateDesc(patientId, true)
                .stream().map(journalMapper::toResponse).toList();
    }

    /**
     * Sets the status of a therapist-patient relationship (e.g. approve PENDING → ACTIVE).
     */
    @Transactional
    public void setPatientStatus(Long therapistId, Long patientId,
                                 TherapistPatientStatus newStatus) {
        TherapistPatient rel = therapistPatientRepository
                .findByTherapistIdAndPatientId(therapistId, patientId)
                .orElseThrow(() -> new IllegalArgumentException("Relationship not found"));
        rel.setStatus(newStatus);
        therapistPatientRepository.save(rel);
        LOG.info("Set therapist-patient status: therapist={} patient={} status={}",
                therapistId, patientId, newStatus);
    }

    /**
     * Creates a goal for a patient, pre-validated by the therapist.
     */
    @Transactional
    public GoalResponse createGoalForPatient(Long therapistId, Long patientId, GoalRequest request) {
        LOG.info("Therapist {} creating goal for patient {}", therapistId, patientId);
        validateTherapistPatientRelationship(therapistId, patientId);

        Goal goal = new Goal();
        goal.setUserId(patientId);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        goal.setValidationStatus(GoalValidationStatus.VALIDATED);
        goal.setCreatedBy(therapistId);
        goal.setValidatedBy(therapistId);
        goal.setValidatedAt(LocalDateTime.now());
        goalMapper.applyRequest(request, goal);

        Goal saved = goalRepository.save(goal);
        return goalMapper.toGoalResponse(saved);
    }

    /**
     * Edits a patient's goal and marks it as OVERRIDDEN by the therapist.
     */
    @Transactional
    public GoalResponse editGoalForPatient(Long therapistId, Long patientId,
                                           Long goalId, GoalRequest request) {
        LOG.info("Therapist {} editing goal {} for patient {}", therapistId, goalId, patientId);
        validateTherapistPatientRelationship(therapistId, patientId);

        Goal goal = goalRepository.findByIdAndUserId(goalId, patientId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));
        goalMapper.applyRequest(request, goal);
        goal.setValidationStatus(GoalValidationStatus.OVERRIDDEN);
        goal.setValidatedBy(therapistId);
        goal.setValidatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());

        Goal saved = goalRepository.save(goal);
        return goalMapper.toGoalResponse(saved);
    }

    /**
     * Validates a patient's goal.
     */
    @Transactional
    public GoalResponse validateGoal(Long therapistId, Long patientId, Long goalId) {
        LOG.info("Therapist {} validating goal {} for patient {}", therapistId, goalId, patientId);
        validateTherapistPatientRelationship(therapistId, patientId);

        Goal goal = goalRepository.findByIdAndUserId(goalId, patientId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));
        goal.setValidationStatus(GoalValidationStatus.VALIDATED);
        goal.setValidatedBy(therapistId);
        goal.setValidatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());

        Goal saved = goalRepository.save(goal);
        return goalMapper.toGoalResponse(saved);
    }

    /**
     * Rejects a patient's goal.
     */
    @Transactional
    public GoalResponse rejectGoal(Long therapistId, Long patientId, Long goalId) {
        LOG.info("Therapist {} rejecting goal {} for patient {}", therapistId, goalId, patientId);
        validateTherapistPatientRelationship(therapistId, patientId);

        Goal goal = goalRepository.findByIdAndUserId(goalId, patientId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));
        goal.setValidationStatus(GoalValidationStatus.REJECTED);
        goal.setValidatedBy(therapistId);
        goal.setValidatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());

        Goal saved = goalRepository.save(goal);
        return goalMapper.toGoalResponse(saved);
    }

    private void validateTherapistPatientRelationship(Long therapistId, Long patientId) {
        boolean exists = therapistPatientRepository
                .existsByTherapistIdAndPatientIdAndStatus(
                        therapistId, patientId, TherapistPatientStatus.ACTIVE);
        if (!exists) {
            throw new IllegalArgumentException(
                    "No active therapist-patient relationship for therapist "
                    + therapistId + " and patient " + patientId);
        }
    }
}
