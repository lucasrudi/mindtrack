package com.mindtrack.interview.service;

import com.mindtrack.interview.config.StorageProperties;
import com.mindtrack.interview.dto.AudioUploadResponse;
import com.mindtrack.interview.model.Interview;
import com.mindtrack.interview.repository.InterviewRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for audio file operations on interviews.
 * Handles upload, retrieval, and deletion of audio recordings.
 */
@Service
public class AudioService {

    private static final Logger LOG = LoggerFactory.getLogger(AudioService.class);

    private final InterviewRepository interviewRepository;
    private final StorageService storageService;
    private final StorageProperties storageProperties;

    public AudioService(InterviewRepository interviewRepository, StorageService storageService,
                        StorageProperties storageProperties) {
        this.interviewRepository = interviewRepository;
        this.storageService = storageService;
        this.storageProperties = storageProperties;
    }

    /**
     * Uploads an audio file for the given interview.
     */
    @Transactional
    public AudioUploadResponse uploadAudio(Long interviewId, Long userId, MultipartFile file) {
        Interview interview = interviewRepository.findByIdAndUserId(interviewId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found"));

        validateFile(file);

        // Delete existing audio if present
        if (interview.getAudioS3Key() != null) {
            storageService.delete(interview.getAudioS3Key());
        }

        String extension = getFileExtension(file.getOriginalFilename());
        String key = "audio/" + userId + "/" + interviewId + "/" + UUID.randomUUID() + "." + extension;

        try {
            storageService.upload(key, file.getInputStream(), file.getContentType(), file.getSize());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read uploaded file", ex);
        }

        interview.setAudioS3Key(key);
        interview.setAudioExpiresAt(LocalDateTime.now().plusDays(storageProperties.getAudioExpiryDays()));
        // Transcription is pending — AWS Transcribe will be triggered asynchronously via EventBridge
        // when the production storage service confirms the S3 upload. The transcription result
        // will be written back to interview.transcriptionText via a callback Lambda.
        interview.setTranscriptionText(null);
        interview.setUpdatedAt(LocalDateTime.now());
        interviewRepository.save(interview);

        LOG.info("Uploaded audio for interview {} by user {}; transcription pending", interviewId, userId);

        String audioUrl = storageService.generateAccessUrl(key);
        return new AudioUploadResponse(audioUrl, null, interview.getAudioExpiresAt());
    }

    /**
     * Returns audio access URL and transcription for the given interview.
     */
    public AudioUploadResponse getAudio(Long interviewId, Long userId) {
        Interview interview = interviewRepository.findByIdAndUserId(interviewId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found"));

        if (interview.getAudioS3Key() == null) {
            return null;
        }

        String audioUrl = storageService.generateAccessUrl(interview.getAudioS3Key());
        return new AudioUploadResponse(audioUrl, interview.getTranscriptionText(),
                interview.getAudioExpiresAt());
    }

    /**
     * Deletes audio for the given interview.
     */
    @Transactional
    public boolean deleteAudio(Long interviewId, Long userId) {
        Interview interview = interviewRepository.findByIdAndUserId(interviewId, userId)
                .orElse(null);
        if (interview == null || interview.getAudioS3Key() == null) {
            return false;
        }

        storageService.delete(interview.getAudioS3Key());
        interview.setAudioS3Key(null);
        interview.setTranscriptionText(null);
        interview.setAudioExpiresAt(null);
        interview.setUpdatedAt(LocalDateTime.now());
        interviewRepository.save(interview);

        LOG.info("Deleted audio for interview {} by user {}", interviewId, userId);
        return true;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > storageProperties.getMaxFileSizeBytes()) {
            throw new IllegalArgumentException("File size exceeds maximum of "
                    + (storageProperties.getMaxFileSizeBytes() / 1_048_576) + " MB");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!storageProperties.getAllowedFormats().contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                    "File format not allowed. Allowed: " + storageProperties.getAllowedFormats());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
