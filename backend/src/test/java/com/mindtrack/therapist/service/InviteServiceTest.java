package com.mindtrack.therapist.service;

import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.User;
import com.mindtrack.therapist.dto.InviteGenerateResponse;
import com.mindtrack.therapist.dto.InvitePreviewResponse;
import com.mindtrack.therapist.model.InitiatorRole;
import com.mindtrack.therapist.model.InviteToken;
import com.mindtrack.therapist.model.TherapistPatient;
import com.mindtrack.therapist.model.TherapistPatientStatus;
import com.mindtrack.therapist.repository.InviteTokenRepository;
import com.mindtrack.therapist.repository.TherapistPatientRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InviteServiceTest {

    @Mock private InviteTokenRepository inviteTokenRepository;
    @Mock private TherapistPatientRepository therapistPatientRepository;
    @Mock private UserRepository userRepository;

    private InviteService inviteService;

    @BeforeEach
    void setUp() {
        inviteService = new InviteService(inviteTokenRepository,
                therapistPatientRepository, userRepository, "http://localhost:3000");
    }

    @Test
    void shouldGenerateInviteToken() {
        when(inviteTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InviteGenerateResponse response = inviteService.generateToken(1L, InitiatorRole.THERAPIST);

        assertNotNull(response.token());
        assertEquals(64, response.token().length());
        assertTrue(response.url().contains(response.token()));
    }

    @Test
    void shouldPreviewInvite() {
        InviteToken token = makeToken(InitiatorRole.THERAPIST, 1L);
        User user = makeUser(1L, "Dr. Smith");
        when(inviteTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        InvitePreviewResponse response = inviteService.previewInvite("abc");

        assertEquals("Dr. Smith", response.initiatorName());
        assertEquals("THERAPIST", response.initiatorRole());
        assertNull(response.status());
    }

    @Test
    void shouldCreateTherapistRequestForPatientEmail() {
        User patient = makeUser(20L, "Patient One");
        patient.setEmail("patient@test.com");
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(therapistPatientRepository.findByTherapistIdAndPatientId(10L, 20L))
                .thenReturn(Optional.empty());
        when(inviteTokenRepository.findByInitiatorIdAndRecipientIdAndUsedAtIsNullAndExpiresAtAfter(
                eq(10L), eq(20L), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(therapistPatientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inviteTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InviteGenerateResponse response = inviteService.requestPatient(10L, "patient@test.com");

        assertNotNull(response.token());
        assertTrue(response.url().contains(response.token()));
    }

    @Test
    void shouldRejectDuplicatePendingRequest() {
        User patient = makeUser(20L, "Patient One");
        patient.setEmail("patient@test.com");
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        TherapistPatient existing = new TherapistPatient(10L, 20L, TherapistPatientStatus.PENDING);
        when(therapistPatientRepository.findByTherapistIdAndPatientId(10L, 20L))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> inviteService.requestPatient(10L, "patient@test.com"));
    }

    @Test
    void shouldThrowWhenTokenExpired() {
        InviteToken token = makeToken(InitiatorRole.THERAPIST, 1L);
        token.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(inviteTokenRepository.findByToken("expired")).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class,
                () -> inviteService.previewInvite("expired"));
    }

    @Test
    void therapistInitiatesPatientAcceptsGoesActiveImmediately() {
        InviteToken token = makeToken(InitiatorRole.THERAPIST, 10L);
        when(inviteTokenRepository.findByToken("t1")).thenReturn(Optional.of(token));
        when(therapistPatientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inviteTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        inviteService.acceptInvite("t1", 20L, InitiatorRole.PATIENT);

        ArgumentCaptor<TherapistPatient> captor = ArgumentCaptor.forClass(TherapistPatient.class);
        verify(therapistPatientRepository).save(captor.capture());
        assertEquals(TherapistPatientStatus.ACTIVE, captor.getValue().getStatus());
    }

    @Test
    void patientInitiatesTherapistAcceptsGoesPending() {
        InviteToken token = makeToken(InitiatorRole.PATIENT, 20L);
        when(inviteTokenRepository.findByToken("t2")).thenReturn(Optional.of(token));
        when(therapistPatientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inviteTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        inviteService.acceptInvite("t2", 10L, InitiatorRole.THERAPIST);

        ArgumentCaptor<TherapistPatient> captor = ArgumentCaptor.forClass(TherapistPatient.class);
        verify(therapistPatientRepository).save(captor.capture());
        assertEquals(TherapistPatientStatus.PENDING, captor.getValue().getStatus());
    }

    @Test
    void shouldRejectTherapistRequestWhenWrongUserTriesToAccept() {
        InviteToken token = makeRequestToken(10L, 20L);
        when(inviteTokenRepository.findByToken("t3")).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class,
                () -> inviteService.acceptInvite("t3", 21L, InitiatorRole.PATIENT));
    }

    @Test
    void shouldRejectTherapistRequest() {
        InviteToken token = makeRequestToken(10L, 20L);
        when(inviteTokenRepository.findByToken("t4")).thenReturn(Optional.of(token));
        when(therapistPatientRepository.findByTherapistIdAndPatientId(10L, 20L))
                .thenReturn(Optional.of(new TherapistPatient(10L, 20L, TherapistPatientStatus.PENDING)));
        when(therapistPatientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inviteTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        inviteService.rejectInvite("t4", 20L, InitiatorRole.PATIENT);

        ArgumentCaptor<TherapistPatient> captor = ArgumentCaptor.forClass(TherapistPatient.class);
        verify(therapistPatientRepository).save(captor.capture());
        assertEquals(TherapistPatientStatus.INACTIVE, captor.getValue().getStatus());
    }

    @Test
    void cleanupShouldExpireStaleRelationshipsLinkedToExpiredTokens() {
        InviteToken expiredToken = makeRequestToken(10L, 20L);
        expiredToken.setExpiresAt(LocalDateTime.now().minusDays(1));
        TherapistPatient pending = new TherapistPatient(10L, 20L, TherapistPatientStatus.PENDING);
        when(inviteTokenRepository.findByExpiresAtBeforeAndUsedAtIsNull(any(LocalDateTime.class)))
                .thenReturn(List.of(expiredToken));
        when(therapistPatientRepository.findByTherapistIdAndPatientIdAndStatus(
                10L, 20L, TherapistPatientStatus.PENDING)).thenReturn(Optional.of(pending));
        when(therapistPatientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        inviteService.cleanupExpiredTokens();

        ArgumentCaptor<TherapistPatient> captor = ArgumentCaptor.forClass(TherapistPatient.class);
        verify(therapistPatientRepository).save(captor.capture());
        assertEquals(TherapistPatientStatus.EXPIRED, captor.getValue().getStatus());
        verify(inviteTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    @Test
    void cleanupShouldNotAffectRelationshipsWithNoExpiredTokens() {
        when(inviteTokenRepository.findByExpiresAtBeforeAndUsedAtIsNull(any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> inviteService.cleanupExpiredTokens());

        verify(therapistPatientRepository, never()).save(any());
        verify(inviteTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    @Test
    void cleanupShouldSkipGenericTokensWithNoRecipient() {
        InviteToken genericToken = makeToken(InitiatorRole.THERAPIST, 10L);
        genericToken.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(inviteTokenRepository.findByExpiresAtBeforeAndUsedAtIsNull(any(LocalDateTime.class)))
                .thenReturn(List.of(genericToken));

        inviteService.cleanupExpiredTokens();

        verify(therapistPatientRepository, never()).findByTherapistIdAndPatientIdAndStatus(
                any(), any(), any());
        verify(inviteTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    @Test
    void shouldAllowNewRequestAfterExpiredRelationship() {
        User patient = makeUser(20L, "Patient One");
        patient.setEmail("patient@test.com");
        TherapistPatient expired = new TherapistPatient(10L, 20L, TherapistPatientStatus.EXPIRED);
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(therapistPatientRepository.findByTherapistIdAndPatientId(10L, 20L))
                .thenReturn(Optional.of(expired));
        when(inviteTokenRepository.findByInitiatorIdAndRecipientIdAndUsedAtIsNullAndExpiresAtAfter(
                eq(10L), eq(20L), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(therapistPatientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inviteTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> inviteService.requestPatient(10L, "patient@test.com"));
    }

    private InviteToken makeToken(InitiatorRole role, Long initiatorId) {
        InviteToken t = new InviteToken();
        t.setToken("tok-" + initiatorId);
        t.setInitiatorRole(role);
        t.setInitiatorId(initiatorId);
        t.setExpiresAt(LocalDateTime.now().plusDays(7));
        t.setCreatedAt(LocalDateTime.now());
        return t;
    }

    private InviteToken makeRequestToken(Long therapistId, Long patientId) {
        InviteToken t = makeToken(InitiatorRole.THERAPIST, therapistId);
        t.setRecipientId(patientId);
        return t;
    }

    private User makeUser(Long id, String name) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        return u;
    }
}
