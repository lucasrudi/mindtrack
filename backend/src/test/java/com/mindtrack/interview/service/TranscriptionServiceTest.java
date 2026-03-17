package com.mindtrack.interview.service;

import com.mindtrack.interview.config.WhisperProperties;
import com.mindtrack.interview.model.Interview;
import com.mindtrack.interview.model.TranscriptionStatus;
import com.mindtrack.interview.repository.InterviewRepository;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranscriptionServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private RestTemplate restTemplate;

    private TranscriptionService service;

    @BeforeEach
    void setUp() {
        WhisperProperties whisperProperties = new WhisperProperties();
        whisperProperties.setWhisperApiKey("test-key");
        whisperProperties.setWhisperApiUrl("https://example.com/transcribe");
        service = new TranscriptionService(interviewRepository, storageService, whisperProperties, restTemplate);
    }

    @Test
    void shouldSaveCompletedTranscriptWhenWhisperSucceeds() {
        Interview interview = interview(1L);
        when(storageService.download("audio/key.webm")).thenReturn("audio".getBytes());
        when(restTemplate.postForObject(eq("https://example.com/transcribe"), any(), eq(Map.class)))
                .thenReturn(Map.of("text", "Transcribed text"));
        when(interviewRepository.findById(1L)).thenReturn(Optional.of(interview));

        service.transcribeAsync(1L, "audio/key.webm");

        ArgumentCaptor<Interview> interviewCaptor = ArgumentCaptor.forClass(Interview.class);
        verify(interviewRepository).save(interviewCaptor.capture());
        Interview saved = interviewCaptor.getValue();
        assertEquals(TranscriptionStatus.COMPLETED, saved.getTranscriptionStatus());
        assertEquals("Transcribed text", saved.getTranscriptionText());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void shouldMarkInterviewFailedWhenTranscriptionThrows() {
        Interview interview = interview(2L);
        when(storageService.download("audio/failure.webm")).thenThrow(new RuntimeException("boom"));
        when(interviewRepository.findById(2L)).thenReturn(Optional.of(interview));

        service.transcribeAsync(2L, "audio/failure.webm");

        ArgumentCaptor<Interview> interviewCaptor = ArgumentCaptor.forClass(Interview.class);
        verify(interviewRepository).save(interviewCaptor.capture());
        Interview saved = interviewCaptor.getValue();
        assertEquals(TranscriptionStatus.FAILED, saved.getTranscriptionStatus());
        assertNull(saved.getTranscriptionText());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void shouldPersistNullTranscriptWhenWhisperReturnsNoText() {
        Interview interview = interview(3L);
        when(storageService.download("audio/empty.webm")).thenReturn("audio".getBytes());
        when(restTemplate.postForObject(eq("https://example.com/transcribe"), any(), eq(Map.class)))
                .thenReturn(Map.of());
        when(interviewRepository.findById(3L)).thenReturn(Optional.of(interview));

        service.transcribeAsync(3L, "audio/empty.webm");

        ArgumentCaptor<Interview> interviewCaptor = ArgumentCaptor.forClass(Interview.class);
        verify(interviewRepository).save(interviewCaptor.capture());
        Interview saved = interviewCaptor.getValue();
        assertEquals(TranscriptionStatus.COMPLETED, saved.getTranscriptionStatus());
        assertNull(saved.getTranscriptionText());
        assertNotNull(saved.getUpdatedAt());
    }

    private Interview interview(Long id) {
        Interview interview = new Interview();
        interview.setId(id);
        interview.setUserId(1L);
        interview.setInterviewDate(LocalDate.of(2026, 3, 1));
        return interview;
    }
}
