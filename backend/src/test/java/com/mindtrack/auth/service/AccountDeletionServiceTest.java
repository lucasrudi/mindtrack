package com.mindtrack.auth.service;

import com.mindtrack.audit.model.AuditAction;
import com.mindtrack.audit.service.AuditService;
import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.Role;
import com.mindtrack.common.model.User;
import com.mindtrack.profile.model.UserProfile;
import com.mindtrack.profile.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountDeletionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository profileRepository;

    @Mock
    private AuditService auditService;

    private AccountDeletionService service;

    @BeforeEach
    void setUp() {
        service = new AccountDeletionService(userRepository, profileRepository, auditService);
    }

    private User buildUser(Long id) {
        Role role = new Role();
        role.setName("USER");
        User user = new User();
        user.setId(id);
        user.setEmail("alice@example.com");
        user.setName("Alice");
        user.setGoogleId("google-123");
        user.setRole(role);
        user.setEnabled(true);
        user.setTokenVersion(0);
        return user;
    }

    @Test
    void requestDeletion_pseudonymisesEmail() {
        User user = buildUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        service.requestDeletion(1L, "127.0.0.1");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertTrue(captor.getValue().getEmail().startsWith("deleted-"));
        assertTrue(captor.getValue().getEmail().endsWith("@deleted.invalid"));
    }

    @Test
    void requestDeletion_disablesAccountAndBumpsTokenVersion() {
        User user = buildUser(1L);
        user.setTokenVersion(3);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        service.requestDeletion(1L, "127.0.0.1");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertFalse(captor.getValue().isEnabled());
        assertEquals(4, captor.getValue().getTokenVersion());
    }

    @Test
    void requestDeletion_clearsGoogleId() {
        User user = buildUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        service.requestDeletion(1L, "127.0.0.1");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertNull(captor.getValue().getGoogleId());
    }

    @Test
    void requestDeletion_setsDeletionTimestamps() {
        User user = buildUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        LocalDateTime before = LocalDateTime.now();
        service.requestDeletion(1L, "127.0.0.1");
        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertNotNull(saved.getDeletedAt());
        assertTrue(!saved.getDeletedAt().isBefore(before) && !saved.getDeletedAt().isAfter(after));
        assertNotNull(saved.getDeletionScheduledAt());
        assertTrue(saved.getDeletionScheduledAt().isAfter(saved.getDeletedAt()));
    }

    @Test
    void requestDeletion_anonymisesProfilePii() {
        User user = buildUser(1L);
        UserProfile profile = new UserProfile();
        profile.setUserId(1L);
        profile.setDisplayName("Alice Smith");
        profile.setTelegramChatId("chat-123");
        profile.setWhatsappNumber("+1234567890");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.requestDeletion(1L, "127.0.0.1");

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(profileRepository).save(captor.capture());
        assertNull(captor.getValue().getDisplayName());
        assertNull(captor.getValue().getTelegramChatId());
        assertNull(captor.getValue().getWhatsappNumber());
        assertNotNull(captor.getValue().getAnonymizedAt());
    }

    @Test
    void requestDeletion_writesAuditLog() {
        User user = buildUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        service.requestDeletion(1L, "10.0.0.1");

        verify(auditService).log(eq(1L), eq(AuditAction.ACCOUNT_DELETION_REQUESTED),
                eq("user"), eq(1L), eq(1L), eq("10.0.0.1"), eq("WEB"));
    }

    @Test
    void requestDeletion_throwsWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.requestDeletion(99L, "127.0.0.1"));
    }

    @Test
    void hardDeleteExpiredAccounts_deletesExpiredUsers() {
        User user = buildUser(2L);
        user.setDeletedAt(LocalDateTime.now().minusDays(35));
        user.setDeletionScheduledAt(LocalDateTime.now().minusDays(5));
        when(userRepository.findByDeletionScheduledAtBeforeAndDeletedAtIsNotNull(any()))
                .thenReturn(List.of(user));

        service.hardDeleteExpiredAccounts();

        verify(userRepository).deleteById(2L);
        verify(auditService).log(eq(2L), eq(AuditAction.ACCOUNT_HARD_DELETED),
                eq("user"), eq(2L), eq(2L), eq("SYSTEM"), eq("SYSTEM"));
    }

    @Test
    void hardDeleteExpiredAccounts_noOpWhenNoneExpired() {
        when(userRepository.findByDeletionScheduledAtBeforeAndDeletedAtIsNotNull(any()))
                .thenReturn(List.of());

        service.hardDeleteExpiredAccounts();

        verify(userRepository, never()).deleteById(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void hardDeleteExpiredAccounts_handlesMultipleUsers() {
        User user1 = buildUser(3L);
        User user2 = buildUser(4L);
        when(userRepository.findByDeletionScheduledAtBeforeAndDeletedAtIsNotNull(any()))
                .thenReturn(List.of(user1, user2));

        service.hardDeleteExpiredAccounts();

        verify(userRepository).deleteById(3L);
        verify(userRepository).deleteById(4L);
        verify(auditService, times(2)).log(any(), eq(AuditAction.ACCOUNT_HARD_DELETED),
                any(), any(), any(), eq("SYSTEM"), eq("SYSTEM"));
    }
}
