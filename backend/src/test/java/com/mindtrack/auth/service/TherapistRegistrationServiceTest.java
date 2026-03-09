package com.mindtrack.auth.service;

import com.mindtrack.auth.dto.TherapistTokenResponse;
import com.mindtrack.auth.model.TherapistRegistrationToken;
import com.mindtrack.auth.repository.RoleRepository;
import com.mindtrack.auth.repository.TherapistRegistrationTokenRepository;
import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.Role;
import com.mindtrack.common.model.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TherapistRegistrationServiceTest {

    @Mock private TherapistRegistrationTokenRepository tokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;

    private TherapistRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new TherapistRegistrationService(tokenRepository, userRepository, roleRepository);
    }

    @Test
    void createToken_shouldGenerateUniqueToken() {
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TherapistTokenResponse response = service.createToken(1L);

        assertNotNull(response.getToken());
        assertEquals(64, response.getToken().length());
        assertNull(response.getUsedAt());
        assertNotNull(response.getExpiresAt());
    }

    @Test
    void listTokens_shouldReturnAllTokensOrderedByCreatedAt() {
        TherapistRegistrationToken t1 = makeToken("aaa", false, false);
        TherapistRegistrationToken t2 = makeToken("bbb", true, false);
        when(tokenRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(t1, t2));

        List<TherapistTokenResponse> result = service.listTokens();

        assertEquals(2, result.size());
        assertEquals("aaa", result.get(0).getToken());
    }

    @Test
    void redeemToken_shouldUpgradeUserToTherapist() {
        TherapistRegistrationToken token = makeToken("abc123", false, false);
        User user = makeUser("USER");
        Role therapistRole = makeRole("THERAPIST");
        when(tokenRepository.findByToken("abc123")).thenReturn(Optional.of(token));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("THERAPIST")).thenReturn(Optional.of(therapistRole));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.redeemToken("abc123", 10L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("THERAPIST", userCaptor.getValue().getRole().getName());

        ArgumentCaptor<TherapistRegistrationToken> tokenCaptor =
                ArgumentCaptor.forClass(TherapistRegistrationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertNotNull(tokenCaptor.getValue().getUsedAt());
        assertEquals(10L, tokenCaptor.getValue().getUsedBy());
    }

    @Test
    void redeemToken_shouldFail_whenTokenAlreadyUsed() {
        TherapistRegistrationToken token = makeToken("abc123", true, false);
        when(tokenRepository.findByToken("abc123")).thenReturn(Optional.of(token));

        assertThrows(ResponseStatusException.class,
                () -> service.redeemToken("abc123", 10L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void redeemToken_shouldFail_whenTokenExpired() {
        TherapistRegistrationToken token = makeToken("abc123", false, true);
        when(tokenRepository.findByToken("abc123")).thenReturn(Optional.of(token));

        assertThrows(ResponseStatusException.class,
                () -> service.redeemToken("abc123", 10L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void redeemToken_shouldFail_whenTokenNotFound() {
        when(tokenRepository.findByToken("bad")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.redeemToken("bad", 10L));
    }

    @Test
    void redeemToken_shouldFail_whenUserIsAdmin() {
        TherapistRegistrationToken token = makeToken("abc123", false, false);
        User user = makeUser("ADMIN");
        when(tokenRepository.findByToken("abc123")).thenReturn(Optional.of(token));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class,
                () -> service.redeemToken("abc123", 10L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void redeemToken_shouldSucceedSilently_whenUserAlreadyTherapist() {
        TherapistRegistrationToken token = makeToken("abc123", false, false);
        User user = makeUser("THERAPIST");
        when(tokenRepository.findByToken("abc123")).thenReturn(Optional.of(token));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        service.redeemToken("abc123", 10L);

        verify(userRepository, never()).save(any());
    }

    // --- helpers ---

    private TherapistRegistrationToken makeToken(String tokenValue, boolean used, boolean expired) {
        TherapistRegistrationToken t = new TherapistRegistrationToken();
        t.setToken(tokenValue);
        t.setCreatedBy(1L);
        t.setCreatedAt(LocalDateTime.now().minusDays(1));
        t.setExpiresAt(expired
                ? LocalDateTime.now().minusHours(1)
                : LocalDateTime.now().plusDays(29));
        if (used) {
            t.setUsedAt(LocalDateTime.now().minusHours(2));
            t.setUsedBy(99L);
        }
        return t;
    }

    private User makeUser(String roleName) {
        User u = new User();
        u.setId(10L);
        u.setEmail("user@example.com");
        u.setRole(makeRole(roleName));
        return u;
    }

    private Role makeRole(String name) {
        Role r = new Role();
        r.setName(name);
        return r;
    }
}
