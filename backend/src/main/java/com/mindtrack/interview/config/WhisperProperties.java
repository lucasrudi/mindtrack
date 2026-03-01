package com.mindtrack.interview.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the OpenAI Whisper transcription API.
 * Bound to the {@code mindtrack.ai} prefix in application.yml.
 */
@Configuration
@ConfigurationProperties(prefix = "mindtrack.ai")
public class WhisperProperties {

    private String whisperApiUrl = "https://api.openai.com/v1/audio/transcriptions";
    private String whisperApiKey = "dummy-key-for-local";

    public String getWhisperApiUrl() {
        return whisperApiUrl;
    }

    public void setWhisperApiUrl(String whisperApiUrl) {
        this.whisperApiUrl = whisperApiUrl;
    }

    public String getWhisperApiKey() {
        return whisperApiKey;
    }

    public void setWhisperApiKey(String whisperApiKey) {
        this.whisperApiKey = whisperApiKey;
    }
}
