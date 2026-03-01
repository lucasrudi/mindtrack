package com.mindtrack.interview.service;

import com.mindtrack.interview.config.StorageProperties;
import com.mindtrack.interview.dto.AudioUploadResponse;
import com.mindtrack.interview.model.Interview;
import com.mindtrack.interview.model.TranscriptionStatus;
import com.mindtrack.interview.repository.InterviewRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AudioServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private TranscriptionService transcriptionService;

    @Mock
    private MultipartFile multipartFile;

    private StorageProperties storageProperties;
    private AudioService audioService;

    @BeforeEach
    void setUp() {
        storageProperties = new StorageProperties();
        audioService = new AudioService(interviewRepository, storageService, storageProperties,
                transcriptionService);
    }

    @Test
    void shouldUploadAudioFile() throws IOException {
        Interview interview = createInterview(1L, null);
        when(interviewRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(interview));
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getOriginalFilename()).thenReturn("recording.mp3");
        when(multipartFile.getContentType()).thenReturn("audio/mpeg");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[1024]));
        when(interviewRepository.save(any(Interview.class))).thenReturn(interview);
        when(storageService.generateAccessUrl(anyString())).thenReturn("http://example.com/audio.mp3");

        AudioUploadResponse result = audioService.uploadAudio(1L, 1L, multipartFile);

        assertNotNull(result);
        assertEquals("http://example.com/audio.mp3", result.getAudioUrl());
        assertNull(result.getTranscriptionText());
        assertNotNull(result.getAudioExpiresAt());

        ArgumentCaptor<Interview> captor = ArgumentCaptor.forClass(Interview.class);
        verify(interviewRepository).save(captor.capture());
        assertNotNull(captor.getValue().getAudioS3Key());
        assertTrue(captor.getValue().getAudioS3Key().endsWith(".mp3"));
        assertNotNull(captor.getValue().getAudioExpiresAt());
        verify(storageService).upload(anyString(), any(), eq("audio/mpeg"), eq(1024L));
    }

    @Test
    void shouldReplaceExistingAudio() throws IOException {
        Interview interview = createInterview(1L, "audio/1/1/old-key.mp3");
        when(interviewRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(interview));
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getOriginalFilename()).thenReturn("new-recording.wav");
        when(multipartFile.getContentType()).thenReturn("audio/wav");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[1024]));
        when(interviewRepository.save(any(Interview.class))).thenReturn(interview);
        when(storageService.generateAccessUrl(anyString())).thenReturn("http://example.com/new.wav");

        audioService.uploadAudio(1L, 1L, multipartFile);

        verify(storageService).delete("audio/1/1/old-key.mp3");
        verify(storageService).upload(anyString(), any(), eq("audio/wav"), eq(1024L));
    }

    @Test
    void shouldRejectEmptyFile() {
        Interview interview = createInterview(1L, null);
        when(interviewRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(interview));
        when(multipartFile.isEmpty()).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> audioService.uploadAudio(1L, 1L, multipartFile));

        verify(storageService, never()).upload(anyString(), any(), anyString(), anyLong());
    }

    @Test
    void shouldRejectOversizedFile() {
        Interview interview = createInterview(1L, null);
        when(interviewRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(interview));
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(100_000_000L);

        assertThrows(IllegalArgumentException.class,
                () -> audioService.uploadAudio(1L, 1L, multipartFile));
    }

    @Test
    void shouldRejectUnsupportedFormat() {
        Interview interview = createInterview(1L, null);
        when(interviewRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(interview));
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getOriginalFilename()).thenReturn("document.pdf");

        assertThrows(IllegalArgumentException.class,
                () -> audioService.uploadAudio(1L, 1L, multipartFile));
    }

    @Test
    void shouldGetAudioForInterview() {
        Interview interview = createInterview(1L, "audio/1/1/file.mp3");
        interview.setTranscriptionText("Hello world");
        when(interviewRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(interview));
        when(storageService.generateAccessUrl("audio/1/1/file.mp3"))
                .thenReturn("http://example.com/file.mp3");

        AudioUploadResponse result = audioService.getAudio(1L, 1L);

        assertNotNull(result);
        assertEquals("http://example.com/file.mp3", result.getAudioUrl());
        assertEquals("Hello world", result.getTranscriptionText());
    }

    @Test
    void shouldReturnNullWhenNoAudioExists() {
        Interview interview = createInterview(1L, null);
        when(interviewRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(interview));

        AudioUploadResponse result = audioService.getAudio(1L, 1L);

        assertNull(result);
    }

    @Test
    void shouldDeleteAudio() {
        Interview interview = createInterview(1L, "audio/1/1/file.mp3");
        when(interviewRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(interview));
        when(interviewRepository.save(any(Interview.class))).thenReturn(interview);

        boolean result = audioService.deleteAudio(1L, 1L);

        assertTrue(result);
        verify(storageService).delete("audio/1/1/file.mp3");
        assertNull(interview.getAudioS3Key());
        assertNull(interview.getTranscriptionText());
        assertNull(interview.getAudioExpiresAt());
    }

    @Test
    void shouldReturnFalseWhenDeletingNonExistentAudio() {
        Interview interview = createInterview(1L, null);
        when(interviewRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(interview));

        boolean result = audioService.deleteAudio(1L, 1L);

        assertFalse(result);
        verify(storageService, never()).delete(anyString());
    }

    @Test
    void shouldThrowWhenInterviewNotFoundForUpload() {
        when(interviewRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> audioService.uploadAudio(999L, 1L, multipartFile));
    }

    @Test
    void shouldSetTranscriptionStatusToInProgressAfterUpload() throws IOException {
        Interview interview = createInterview(1L, null);
        when(interviewRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(interview));
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getOriginalFilename()).thenReturn("rec.webm");
        when(multipartFile.getContentType()).thenReturn("audio/webm");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[100]));
        when(interviewRepository.save(any(Interview.class))).thenReturn(interview);
        when(storageService.generateAccessUrl(anyString())).thenReturn("http://s3/audio.webm");
        doNothing().when(transcriptionService).transcribeAsync(any(), anyString());

        audioService.uploadAudio(1L, 1L, multipartFile);

        assertEquals(TranscriptionStatus.IN_PROGRESS, interview.getTranscriptionStatus());
        verify(transcriptionService).transcribeAsync(any(), anyString());
    }

    private Interview createInterview(Long id, String audioS3Key) {
        Interview interview = new Interview();
        interview.setId(id);
        interview.setUserId(1L);
        interview.setAudioS3Key(audioS3Key);
        interview.setAudioExpiresAt(audioS3Key != null ? LocalDateTime.now().plusDays(7) : null);
        interview.setCreatedAt(LocalDateTime.now());
        interview.setUpdatedAt(LocalDateTime.now());
        return interview;
    }
}
