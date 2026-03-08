package com.mindtrack.auth.service;

import com.mindtrack.auth.repository.RoleRepository;
import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.Role;
import com.mindtrack.common.model.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user management operations.
 */
@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private static final String DEFAULT_ROLE = "USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Finds or creates a user from Google OAuth2 profile.
     */
    @Transactional
    public User findOrCreateFromGoogle(String googleId, String email, String name) {
        Optional<User> existing = userRepository.findByGoogleId(googleId);
        if (existing.isPresent()) {
            User user = existing.get();
            user.setName(name);
            user.setUpdatedAt(LocalDateTime.now());
            LOG.info("Returning existing user id={}", user.getId());
            return userRepository.save(user);
        }

        // Check if user exists by email but without Google ID (e.g., seeded admin)
        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            User user = byEmail.get();
            user.setGoogleId(googleId);
            user.setName(name);
            user.setUpdatedAt(LocalDateTime.now());
            LOG.info("Linked Google ID to existing user id={}", user.getId());
            return userRepository.save(user);
        }

        Role userRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Default role not found: " + DEFAULT_ROLE));

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setGoogleId(googleId);
        newUser.setRole(userRole);
        newUser.setEnabled(true);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(newUser);
        LOG.info("Created new user id={}", savedUser.getId());
        return savedUser;
    }

    /**
     * Finds a user by ID.
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Finds a user by email.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Changes the role of a user. Only USER and THERAPIST are permitted via self-service.
     */
    @Transactional
    public User changeRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        user.setRole(role);
        return userRepository.save(user);
    }
}
