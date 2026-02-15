package com.mindtrack.journal.controller;

import com.mindtrack.journal.dto.JournalEntryRequest;
import com.mindtrack.journal.dto.JournalEntryResponse;
import com.mindtrack.journal.service.JournalService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for journal entry operations.
 */
@RestController
@RequestMapping("/api/journal")
public class JournalController {

    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    /**
     * Creates a new journal entry.
     */
    @PostMapping
    public ResponseEntity<JournalEntryResponse> create(
            @RequestBody @Valid JournalEntryRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        JournalEntryResponse response = journalService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists journal entries for the authenticated user, with optional date filtering.
     */
    @GetMapping
    public ResponseEntity<List<JournalEntryResponse>> list(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<JournalEntryResponse> entries;
        if (startDate != null && endDate != null) {
            entries = journalService.listByUserAndDateRange(userId, startDate, endDate);
        } else {
            entries = journalService.listByUser(userId);
        }
        return ResponseEntity.ok(entries);
    }

    /**
     * Gets a single journal entry by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<JournalEntryResponse> getById(
            @PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        JournalEntryResponse response = journalService.getByIdAndUser(id, userId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing journal entry.
     */
    @PutMapping("/{id}")
    public ResponseEntity<JournalEntryResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid JournalEntryRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        JournalEntryResponse response = journalService.update(id, userId, request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a journal entry.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean deleted = journalService.delete(id, userId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggles the shared-with-therapist status of a journal entry.
     */
    @PatchMapping("/{id}/share")
    public ResponseEntity<JournalEntryResponse> toggleSharing(
            @PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        JournalEntryResponse response = journalService.toggleSharing(id, userId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}
