package com.mindtrack.interview.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for audio upload and audio status operations.
 */
public class AudioUploadResponse {

    private String audioUrl;
    private String transcriptionText;
    private LocalDateTime audioExpiresAt;

    public AudioUploadResponse() {
    }

    public AudioUploadResponse(String audioUrl, String transcriptionText, LocalDateTime audioExpiresAt) {
        this.audioUrl = audioUrl;
        this.transcriptionText = transcriptionText;
        this.audioExpiresAt = audioExpiresAt;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getTranscriptionText() {
        return transcriptionText;
    }

    public void setTranscriptionText(String transcriptionText) {
        this.transcriptionText = transcriptionText;
    }

    public LocalDateTime getAudioExpiresAt() {
        return audioExpiresAt;
    }

    public void setAudioExpiresAt(LocalDateTime audioExpiresAt) {
        this.audioExpiresAt = audioExpiresAt;
    }
}
