package com.mindtrack.therapist.controller;

import com.mindtrack.therapist.dto.InviteGenerateResponse;
import com.mindtrack.therapist.dto.InvitePreviewResponse;
import com.mindtrack.therapist.dto.PatientRequestCreateRequest;
import com.mindtrack.therapist.model.InitiatorRole;
import com.mindtrack.therapist.service.InviteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for invite token generation and acceptance.
 */
@RestController
@RequestMapping("/api/invites")
public class InviteController {

    private final InviteService inviteService;

    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    /**
     * Generates an invite token for the authenticated user.
     */
    @PostMapping("/generate")
    public ResponseEntity<InviteGenerateResponse> generate(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        InitiatorRole role = resolveRole(authentication);
        return ResponseEntity.ok(inviteService.generateToken(userId, role));
    }

    /**
     * Generates a therapist request for a specific patient's email address.
     */
    @PostMapping("/request")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<InviteGenerateResponse> requestPatient(
            @Valid @RequestBody PatientRequestCreateRequest request,
            Authentication authentication) {
        Long therapistId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(inviteService.requestPatient(therapistId, request.getPatientEmail()));
    }

    /**
     * Returns a preview of the invite (who sent it) without accepting.
     */
    @GetMapping("/{token}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<InvitePreviewResponse> preview(@PathVariable String token) {
        return ResponseEntity.ok(inviteService.previewInvite(token));
    }

    /**
     * Accepts an invite token, creating a therapist-patient relationship.
     */
    @PostMapping("/{token}/accept")
    public ResponseEntity<Void> accept(@PathVariable String token, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        InitiatorRole role = resolveRole(authentication);
        inviteService.acceptInvite(token, userId, role);
        return ResponseEntity.ok().build();
    }

    /**
     * Rejects an invite token.
     */
    @PostMapping("/{token}/reject")
    public ResponseEntity<Void> reject(@PathVariable String token, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        InitiatorRole role = resolveRole(authentication);
        inviteService.rejectInvite(token, userId, role);
        return ResponseEntity.ok().build();
    }

    private InitiatorRole resolveRole(Authentication authentication) {
        boolean isTherapist = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_THERAPIST"::equals);
        return isTherapist ? InitiatorRole.THERAPIST : InitiatorRole.PATIENT;
    }
}
