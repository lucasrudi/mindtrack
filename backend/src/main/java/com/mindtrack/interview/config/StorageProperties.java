package com.mindtrack.interview.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for audio file storage.
 */
@Configuration
@ConfigurationProperties(prefix = "mindtrack.storage")
public class StorageProperties {

    private String bucketName = "mindtrack-audio";
    private int presignedUrlExpiryMinutes = 60;
    private long maxFileSizeBytes = 52_428_800L; // 50 MB
    private List<String> allowedFormats = List.of("mp3", "wav", "m4a", "flac", "ogg", "webm");
    private int audioExpiryDays = 7;
    private String localStoragePath = System.getProperty("java.io.tmpdir") + "/mindtrack-audio";

    public StorageProperties() {
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public int getPresignedUrlExpiryMinutes() {
        return presignedUrlExpiryMinutes;
    }

    public void setPresignedUrlExpiryMinutes(int presignedUrlExpiryMinutes) {
        this.presignedUrlExpiryMinutes = presignedUrlExpiryMinutes;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public List<String> getAllowedFormats() {
        return allowedFormats;
    }

    public void setAllowedFormats(List<String> allowedFormats) {
        this.allowedFormats = allowedFormats;
    }

    public int getAudioExpiryDays() {
        return audioExpiryDays;
    }

    public void setAudioExpiryDays(int audioExpiryDays) {
        this.audioExpiryDays = audioExpiryDays;
    }

    public String getLocalStoragePath() {
        return localStoragePath;
    }

    public void setLocalStoragePath(String localStoragePath) {
        this.localStoragePath = localStoragePath;
    }
}
