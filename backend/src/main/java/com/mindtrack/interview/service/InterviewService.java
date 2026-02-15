package com.mindtrack.interview.service;

import com.mindtrack.interview.dto.InterviewRequest;
import com.mindtrack.interview.dto.InterviewResponse;
import com.mindtrack.interview.model.Interview;
import com.mindtrack.interview.repository.InterviewRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for interview CRUD operations.
 */
@Service
public class InterviewService {

    private static final Logger LOG = LoggerFactory.getLogger(InterviewService.class);

    private final InterviewRepository interviewRepository;
    private final InterviewMapper interviewMapper;

    public InterviewService(InterviewRepository interviewRepository, InterviewMapper interviewMapper) {
        this.interviewRepository = interviewRepository;
        this.interviewMapper = interviewMapper;
    }

    /**
     * Creates a new interview for the given user.
     */
    @Transactional
    public InterviewResponse create(Long userId, InterviewRequest request) {
        Interview interview = new Interview();
        interview.setUserId(userId);
        interview.setCreatedAt(LocalDateTime.now());
        interview.setUpdatedAt(LocalDateTime.now());
        interviewMapper.applyRequest(request, interview);

        Interview saved = interviewRepository.save(interview);
        LOG.info("Created interview {} for user {}", saved.getId(), userId);
        return interviewMapper.toResponse(saved);
    }

    /**
     * Returns all interviews for the given user, sorted by date descending.
     */
    public List<InterviewResponse> listByUser(Long userId) {
        return interviewRepository.findByUserIdOrderByInterviewDateDesc(userId).stream()
                .map(interviewMapper::toResponse)
                .toList();
    }

    /**
     * Returns a single interview by ID, only if it belongs to the given user.
     */
    public InterviewResponse getByIdAndUser(Long interviewId, Long userId) {
        return interviewRepository.findByIdAndUserId(interviewId, userId)
                .map(interviewMapper::toResponse)
                .orElse(null);
    }

    /**
     * Updates an existing interview.
     */
    @Transactional
    public InterviewResponse update(Long interviewId, Long userId, InterviewRequest request) {
        Interview interview = interviewRepository.findByIdAndUserId(interviewId, userId)
                .orElse(null);
        if (interview == null) {
            return null;
        }

        interviewMapper.applyRequest(request, interview);
        interview.setUpdatedAt(LocalDateTime.now());

        Interview saved = interviewRepository.save(interview);
        LOG.info("Updated interview {} for user {}", saved.getId(), userId);
        return interviewMapper.toResponse(saved);
    }

    /**
     * Deletes an interview if it belongs to the given user.
     */
    @Transactional
    public boolean delete(Long interviewId, Long userId) {
        Interview interview = interviewRepository.findByIdAndUserId(interviewId, userId)
                .orElse(null);
        if (interview == null) {
            return false;
        }

        interviewRepository.delete(interview);
        LOG.info("Deleted interview {} for user {}", interviewId, userId);
        return true;
    }
}
