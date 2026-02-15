package com.mindtrack.auth.service;

import com.mindtrack.auth.repository.RoleRepository;
import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.Role;
import com.mindtrack.common.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository);
    }

    @Test
    void shouldReturnExistingUserByGoogleId() {
        User existing = createUser(1L, "test@example.com", "Test User", "google-123");
        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.findOrCreateFromGoogle("google-123", "test@example.com", "Updated Name");

        assertEquals("Updated Name", result.getName());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void shouldLinkGoogleIdToExistingEmailUser() {
        User existing = createUser(1L, "admin@mindtrack.app", "Admin", null);
        when(userRepository.findByGoogleId("google-456")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@mindtrack.app")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.findOrCreateFromGoogle("google-456", "admin@mindtrack.app", "Admin Updated");

        assertEquals("google-456", result.getGoogleId());
        assertEquals("Admin Updated", result.getName());
    }

    @Test
    void shouldCreateNewUser() {
        Role userRole = new Role("USER");
        userRole.setId(2L);

        when(userRepository.findByGoogleId("google-789")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.findOrCreateFromGoogle("google-789", "new@example.com", "New User");

        assertEquals("new@example.com", result.getEmail());
        assertEquals("New User", result.getName());
        assertEquals("google-789", result.getGoogleId());
        assertEquals("USER", result.getRole().getName());
        assertTrue(result.isEnabled());
    }

    @Test
    void shouldFindUserById() {
        User user = createUser(1L, "test@example.com", "Test", "google-123");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void shouldFindUserByEmail() {
        User user = createUser(1L, "test@example.com", "Test", "google-123");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    private User createUser(Long id, String email, String name, String googleId) {
        Role role = new Role("USER");
        role.setId(2L);

        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        user.setGoogleId(googleId);
        user.setRole(role);
        user.setEnabled(true);
        return user;
    }
}
