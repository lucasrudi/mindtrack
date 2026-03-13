package com.mindtrack.common.config;

import java.util.Optional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Configures Spring Data JPA auditing to populate {@code @CreatedBy} and
 * {@code @LastModifiedBy} fields with the ID of the currently authenticated user.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "jpaAuditingConfig")
public class JpaAuditingConfig implements AuditorAware<Long> {

    /**
     * Returns the ID of the currently authenticated user, or empty when no
     * authenticated principal is available (e.g. during async threads or
     * unauthenticated requests).
     *
     * @return an {@link Optional} containing the user ID, or empty
     */
    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Long)) {
            return Optional.empty();
        }
        return Optional.of((Long) auth.getPrincipal());
    }
}
