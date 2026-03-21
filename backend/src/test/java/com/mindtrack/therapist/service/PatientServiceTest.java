package com.mindtrack.therapist.service;

import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.User;
import com.mindtrack.therapist.dto.TherapistRequestResponse;
import com.mindtrack.therapist.model.TherapistPatient;
import com.mindtrack.therapist.model.TherapistPatientStatus;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock private TherapistPatientRepository therapistPatientRepository;
    @Mock private UserRepository userRepository;

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(therapistPatientRepository, userRepository);
    }

    @Test
    void getRequestsShouldReturnPendingAndActiveOnly() {
        TherapistPatient pending = makeRelationship(1L, 10L, 20L, TherapistPatientStatus.PENDING);
        TherapistPatient active = makeRelationship(2L, 11L, 20L, TherapistPatientStatus.ACTIVE);
        User therapist1 = makeUser(10L, "Dr. Smith", "smith@example.com");
        User therapist2 = makeUser(11L, "Dr. Jones", "jones@example.com");
        when(therapistPatientRepository.findByPatientIdAndStatusIn(eq(20L), any()))
                .thenReturn(List.of(pending, active));
        when(userRepository.findById(10L)).thenReturn(Optional.of(therapist1));
        when(userRepository.findById(11L)).thenReturn(Optional.of(therapist2));

        List<TherapistRequestResponse> result = patientService.getRequests(20L);

        assertEquals(2, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
        assertEquals("Dr. Smith", result.get(0).getTherapistName());
        assertEquals("ACTIVE", result.get(1).getStatus());
        assertEquals("Dr. Jones", result.get(1).getTherapistName());
    }

    @Test
    void getRequestsShouldReturnEmptyListWhenNoRequests() {
        when(therapistPatientRepository.findByPatientIdAndStatusIn(eq(20L), any()))
                .thenReturn(List.of());

        List<TherapistRequestResponse> result = patientService.getRequests(20L);

        assertEquals(0, result.size());
    }

    @Test
    void acceptRequestShouldTransitionPendingToActive() {
        TherapistPatient pending = makeRelationship(1L, 10L, 20L, TherapistPatientStatus.PENDING);
        when(therapistPatientRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(therapistPatientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        patientService.acceptRequest(1L, 20L);

        ArgumentCaptor<TherapistPatient> captor = ArgumentCaptor.forClass(TherapistPatient.class);
        verify(therapistPatientRepository).save(captor.capture());
        assertEquals(TherapistPatientStatus.ACTIVE, captor.getValue().getStatus());
    }

    @Test
    void rejectRequestShouldTransitionPendingToInactive() {
        TherapistPatient pending = makeRelationship(1L, 10L, 20L, TherapistPatientStatus.PENDING);
        when(therapistPatientRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(therapistPatientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        patientService.rejectRequest(1L, 20L);

        ArgumentCaptor<TherapistPatient> captor = ArgumentCaptor.forClass(TherapistPatient.class);
        verify(therapistPatientRepository).save(captor.capture());
        assertEquals(TherapistPatientStatus.INACTIVE, captor.getValue().getStatus());
    }

    @Test
    void acceptRequestShouldThrowWhenRelationshipNotFound() {
        when(therapistPatientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> patientService.acceptRequest(99L, 20L));
    }

    @Test
    void acceptRequestShouldThrowWhenPatientDoesNotOwnRelationship() {
        TherapistPatient pending = makeRelationship(1L, 10L, 20L, TherapistPatientStatus.PENDING);
        when(therapistPatientRepository.findById(1L)).thenReturn(Optional.of(pending));

        assertThrows(IllegalArgumentException.class,
                () -> patientService.acceptRequest(1L, 99L));
    }

    @Test
    void acceptRequestShouldThrowWhenRelationshipNotPending() {
        TherapistPatient active = makeRelationship(1L, 10L, 20L, TherapistPatientStatus.ACTIVE);
        when(therapistPatientRepository.findById(1L)).thenReturn(Optional.of(active));

        assertThrows(IllegalArgumentException.class,
                () -> patientService.acceptRequest(1L, 20L));
    }

    private static TherapistPatient makeRelationship(Long id, Long therapistId, Long patientId,
                                                     TherapistPatientStatus status) {
        TherapistPatient r = new TherapistPatient(therapistId, patientId, status);
        r.setId(id);
        r.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        return r;
    }

    private static User makeUser(Long id, String name, String email) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmail(email);
        return u;
    }
}
