package com.mindtrack.interview.controller;

import com.mindtrack.audit.model.AuditAction;
import com.mindtrack.audit.service.AuditService;
import com.mindtrack.interview.dto.AudioUploadResponse;
import com.mindtrack.interview.dto.InterviewRequest;
import com.mindtrack.interview.dto.InterviewResponse;
import com.mindtrack.interview.service.AudioService;
import com.mindtrack.interview.service.InterviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for interview CRUD operations.
 */
@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;
    private final AudioService audioService;
    private final AuditService auditService;

    public InterviewController(InterviewService interviewService, AudioService audioService,
                               AuditService auditService) {
        this.interviewService = interviewService;
        this.audioService = audioService;
        this.auditService = auditService;
    }

    /**
     * Creates a new interview.
     */
    @PostMapping
    public ResponseEntity<InterviewResponse> create(@RequestBody @Valid InterviewRequest request,
                                                    Authentication authentication,
                                                    HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        InterviewResponse response = interviewService.create(userId, request);
        auditService.log(userId, AuditAction.WRITE, "INTERVIEW", response.getId(), userId,
                getClientIp(httpRequest), "WEB");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists all interviews for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<InterviewResponse>> list(Authentication authentication,
                                                        HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        List<InterviewResponse> interviews = interviewService.listByUser(userId);
        for (InterviewResponse interview : interviews) {
            auditService.log(userId, AuditAction.READ, "INTERVIEW", interview.getId(), userId,
                    getClientIp(httpRequest), "WEB");
        }
        return ResponseEntity.ok(interviews);
    }

    /**
     * Gets a single interview by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InterviewResponse> getById(@PathVariable Long id,
                                                     Authentication authentication,
                                                     HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        InterviewResponse response = interviewService.getByIdAndUser(id, userId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        auditService.log(userId, AuditAction.READ, "INTERVIEW", id, userId,
                getClientIp(httpRequest), "WEB");
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing interview.
     */
    @PutMapping("/{id}")
    public ResponseEntity<InterviewResponse> update(@PathVariable Long id,
                                                    @RequestBody @Valid InterviewRequest request,
                                                    Authentication authentication,
                                                    HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        InterviewResponse response = interviewService.update(id, userId, request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        auditService.log(userId, AuditAction.WRITE, "INTERVIEW", id, userId,
                getClientIp(httpRequest), "WEB");
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes an interview.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication,
                                       HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        boolean deleted = interviewService.delete(id, userId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        auditService.log(userId, AuditAction.DELETE, "INTERVIEW", id, userId,
                getClientIp(httpRequest), "WEB");
        return ResponseEntity.noContent().build();
    }

    /**
     * Uploads an audio file for an interview.
     */
    @PostMapping("/{id}/audio")
    public ResponseEntity<AudioUploadResponse> uploadAudio(@PathVariable Long id,
                                                            @RequestParam("file") MultipartFile file,
                                                            Authentication authentication,
                                                            HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        AudioUploadResponse response = audioService.uploadAudio(id, userId, file);
        auditService.log(userId, AuditAction.WRITE, "INTERVIEW", id, userId,
                getClientIp(httpRequest), "WEB");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets the audio URL and transcription for an interview.
     */
    @GetMapping("/{id}/audio")
    public ResponseEntity<AudioUploadResponse> getAudio(@PathVariable Long id,
                                                         Authentication authentication,
                                                         HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        AudioUploadResponse response = audioService.getAudio(id, userId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        auditService.log(userId, AuditAction.READ, "INTERVIEW", id, userId,
                getClientIp(httpRequest), "WEB");
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes the audio file for an interview.
     */
    @DeleteMapping("/{id}/audio")
    public ResponseEntity<Void> deleteAudio(@PathVariable Long id, Authentication authentication,
                                             HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        boolean deleted = audioService.deleteAudio(id, userId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        auditService.log(userId, AuditAction.DELETE, "INTERVIEW", id, userId,
                getClientIp(httpRequest), "WEB");
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
