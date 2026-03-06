package com.mindtrack.common.config;

import io.sentry.Hint;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import io.sentry.protocol.SentryTransaction;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sentry configuration that redacts sensitive resource IDs from event breadcrumbs
 * before they are transmitted. Prevents user-specific paths (e.g. /api/interviews/42)
 * from leaking into error reports.
 */
@Configuration
public class SentryConfig {

    private static final List<String> SENSITIVE_PREFIXES = List.of(
            "/api/interviews",
            "/api/journal",
            "/api/goals",
            "/api/chat",
            "/api/profile"
    );

    /**
     * Registers a BeforeSendCallback that scrubs numeric IDs from breadcrumb URLs
     * on sensitive paths.
     */
    @Bean
    public SentryOptions.BeforeSendCallback beforeSendCallback() {
        return (SentryEvent event, Hint hint) -> {
            if (event.getBreadcrumbs() != null) {
                event.getBreadcrumbs().forEach(breadcrumb -> {
                    String url = breadcrumb.getData() != null
                            ? (String) breadcrumb.getData().get("url")
                            : null;
                    if (url != null && isSensitivePath(url)) {
                        breadcrumb.getData().put("url", redactIds(url));
                    }
                });
            }
            return event;
        };
    }

    /**
     * Registers a BeforeSendTransactionCallback that scrubs numeric IDs from
     * breadcrumb URLs on sensitive paths in transaction events.
     */
    @Bean
    public SentryOptions.BeforeSendTransactionCallback beforeSendTransactionCallback() {
        return (SentryTransaction transaction, Hint hint) -> {
            if (transaction.getBreadcrumbs() != null) {
                transaction.getBreadcrumbs().forEach(breadcrumb -> {
                    String url = breadcrumb.getData() != null
                            ? (String) breadcrumb.getData().get("url")
                            : null;
                    if (url != null && isSensitivePath(url)) {
                        breadcrumb.getData().put("url", redactIds(url));
                    }
                });
            }
            return transaction;
        };
    }

    private boolean isSensitivePath(String path) {
        return SENSITIVE_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private String redactIds(String path) {
        return path.replaceAll("/\\d+", "/[redacted]");
    }
}
