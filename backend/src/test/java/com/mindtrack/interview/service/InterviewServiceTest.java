package com.mindtrack.interview.service;

import com.mindtrack.interview.dto.InterviewRequest;
import com.mindtrack.interview.dto.InterviewResponse;
import com.mindtrack.interview.model.Interview;
import com.mindtrack.interview.repository.InterviewRepository;
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
class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private InterviewMapper interviewMapper;

    private InterviewService interviewService;

    @BeforeEach
    void setUp() {
        interviewService = new InterviewService(interviewRepository, interviewMapper);
    }

    @Test
    void shouldCreateInterview() {
        InterviewRequest request = createRequest();
        InterviewResponse expectedResponse = createResponse(1L);

        when(interviewRepository.save(any(Interview.class))).thenAnswer(invocation -> {
            Interview saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(interviewMapper.toResponse(any(Interview.class))).thenReturn(expectedResponse);

        InterviewResponse result = interviewService.create(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        ArgumentCaptor<Interview> captor = ArgumentCaptor.forClass(Interview.class);
        verify(interviewRepository).save(captor.capture());
        Interview captured = captor.getValue();
        assertEquals(1L, captured.getUserId());
        assertNotNull(captured.getCreatedAt());
        assertNotNull(captured.getUpdatedAt());
        verify(interviewMapper).applyRequest(request, captured);
    }

    @Test
    void shouldListInterviewsByUser() {
        Interview interview1 = createInterview(1L);
        Interview interview2 = createInterview(2L);
        InterviewResponse response1 = createResponse(1L);
        InterviewResponse response2 = createResponse(2L);

        when(interviewRepository.findByUserIdOrderByInterviewDateDesc(1L))
                .thenReturn(List.of(interview1, interview2));
        when(interviewMapper.toResponse(interview1)).thenReturn(response1);
        when(interviewMapper.toResponse(interview2)).thenReturn(response2);

        List<InterviewResponse> results = interviewService.listByUser(1L);

        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).getId());
        assertEquals(2L, results.get(1).getId());
    }

    @Test
    void shouldReturnEmptyListWhenNoInterviews() {
        when(interviewRepository.findByUserIdOrderByInterviewDateDesc(1L))
                .thenReturn(List.of());

        List<InterviewResponse> results = interviewService.listByUser(1L);

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldGetInterviewByIdAndUser() {
        Interview interview = createInterview(1L);
        InterviewResponse expectedResponse = createResponse(1L);

        when(interviewRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(interview));
        when(interviewMapper.toResponse(interview)).thenReturn(expectedResponse);

        InterviewResponse result = interviewService.getByIdAndUser(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldReturnNullWhenInterviewNotFound() {
        when(interviewRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        InterviewResponse result = interviewService.getByIdAndUser(999L, 1L);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenInterviewBelongsToDifferentUser() {
        when(interviewRepository.findByIdAndUserId(1L, 2L))
                .thenReturn(Optional.empty());

        InterviewResponse result = interviewService.getByIdAndUser(1L, 2L);

        assertNull(result);
    }

    @Test
    void shouldUpdateInterview() {
        Interview existing = createInterview(1L);
        InterviewRequest request = createRequest();
        InterviewResponse expectedResponse = createResponse(1L);

        when(interviewRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(existing));
        when(interviewRepository.save(any(Interview.class))).thenReturn(existing);
        when(interviewMapper.toResponse(existing)).thenReturn(expectedResponse);

        InterviewResponse result = interviewService.update(1L, 1L, request);

        assertNotNull(result);
        verify(interviewMapper).applyRequest(request, existing);
        verify(interviewRepository).save(existing);
        assertNotNull(existing.getUpdatedAt());
    }

    @Test
    void shouldReturnNullWhenUpdatingNonExistentInterview() {
        when(interviewRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        InterviewResponse result = interviewService.update(999L, 1L, createRequest());

        assertNull(result);
        verify(interviewRepository, never()).save(any());
    }

    @Test
    void shouldDeleteInterview() {
        Interview existing = createInterview(1L);
        when(interviewRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(existing));

        boolean result = interviewService.delete(1L, 1L);

        assertTrue(result);
        verify(interviewRepository).delete(existing);
    }

    @Test
    void shouldReturnFalseWhenDeletingNonExistentInterview() {
        when(interviewRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        boolean result = interviewService.delete(999L, 1L);

        assertFalse(result);
        verify(interviewRepository, never()).delete(any());
    }

    private InterviewRequest createRequest() {
        InterviewRequest request = new InterviewRequest();
        request.setInterviewDate(LocalDate.of(2025, 1, 15));
        request.setMoodBefore(5);
        request.setMoodAfter(7);
        request.setTopics(List.of("anxiety", "sleep"));
        request.setMedicationChanges("Increased dosage");
        request.setRecommendations("Try meditation");
        request.setNotes("Good session");
        return request;
    }

    private Interview createInterview(Long id) {
        Interview interview = new Interview();
        interview.setId(id);
        interview.setUserId(1L);
        interview.setInterviewDate(LocalDate.of(2025, 1, 15));
        interview.setMoodBefore(5);
        interview.setMoodAfter(7);
        interview.setTopics("[\"anxiety\",\"sleep\"]");
        interview.setCreatedAt(LocalDateTime.now());
        interview.setUpdatedAt(LocalDateTime.now());
        return interview;
    }

    private InterviewResponse createResponse(Long id) {
        InterviewResponse response = new InterviewResponse();
        response.setId(id);
        response.setInterviewDate(LocalDate.of(2025, 1, 15));
        response.setMoodBefore(5);
        response.setMoodAfter(7);
        response.setTopics(List.of("anxiety", "sleep"));
        return response;
    }
}
