package com.mindtrack.mood.controller;

import com.mindtrack.mood.dto.MoodEntryRequest;
import com.mindtrack.mood.dto.MoodEntryResponse;
import com.mindtrack.mood.service.MoodEntryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for mood entry operations.
 */
@RestController
@RequestMapping("/api/mood")
public class MoodEntryController {

    private final MoodEntryService moodEntryService;

    /**
     * Constructs the controller with the given service.
     */
    public MoodEntryController(MoodEntryService moodEntryService) {
        this.moodEntryService = moodEntryService;
    }

    /**
     * Creates a new mood entry for the authenticated user.
     */
    @PostMapping
    public ResponseEntity<MoodEntryResponse> create(
            @RequestBody @Valid MoodEntryRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        MoodEntryResponse response = moodEntryService.createEntry(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists all mood entries for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<MoodEntryResponse>> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(moodEntryService.getUserEntries(userId));
    }
}
