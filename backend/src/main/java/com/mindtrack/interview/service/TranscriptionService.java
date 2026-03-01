package com.mindtrack.interview.service;

import com.mindtrack.interview.config.WhisperProperties;
import com.mindtrack.interview.model.TranscriptionStatus;
import com.mindtrack.interview.repository.InterviewRepository;
import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Handles async Whisper transcription for interview audio recordings.
 * Kept in a separate Spring bean so that {@link Async} works via the Spring proxy.
 */
@Service
public class TranscriptionService {

    private static final Logger LOG = LoggerFactory.getLogger(TranscriptionService.class);

    private final InterviewRepository interviewRepository;
    private final StorageService storageService;
    private final WhisperProperties whisperProperties;
    private final RestTemplate restTemplate;

    public TranscriptionService(InterviewRepository interviewRepository,
                                StorageService storageService,
                                WhisperProperties whisperProperties,
                                RestTemplate restTemplate) {
        this.interviewRepository = interviewRepository;
        this.storageService = storageService;
        this.whisperProperties = whisperProperties;
        this.restTemplate = restTemplate;
    }

    /**
     * Sends audio to the Whisper API and writes the transcript back to the interview.
     * Runs asynchronously — caller is not blocked.
     *
     * @param interviewId the ID of the interview to transcribe
     * @param s3Key       the storage key of the audio file
     */
    @Async
    @SuppressWarnings("unchecked")
    public void transcribeAsync(Long interviewId, String s3Key) {
        LOG.info("Starting async Whisper transcription for interview {}", interviewId);
        try {
            byte[] audioBytes = storageService.download(s3Key);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + whisperProperties.getWhisperApiKey());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(audioBytes) {
                @Override
                public String getFilename() {
                    return "audio.webm";
                }
            });
            body.add("model", "whisper-1");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            Map<String, Object> response = restTemplate.postForObject(
                    whisperProperties.getWhisperApiUrl(), requestEntity, Map.class);

            String text = response != null ? (String) response.get("text") : null;

            interviewRepository.findById(interviewId).ifPresent(interview -> {
                interview.setTranscriptionText(text);
                interview.setTranscriptionStatus(TranscriptionStatus.COMPLETED);
                interview.setUpdatedAt(LocalDateTime.now());
                interviewRepository.save(interview);
                LOG.info("Transcription completed for interview {}", interviewId);
            });

        } catch (Exception e) {
            LOG.error("Whisper transcription failed for interview {}: {}", interviewId, e.getMessage());
            interviewRepository.findById(interviewId).ifPresent(interview -> {
                interview.setTranscriptionStatus(TranscriptionStatus.FAILED);
                interview.setUpdatedAt(LocalDateTime.now());
                interviewRepository.save(interview);
            });
        }
    }
}
