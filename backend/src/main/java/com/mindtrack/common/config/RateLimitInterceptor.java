package com.mindtrack.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Fixed-window per-user rate limiter.
 * General endpoints: 60 requests per minute.
 * AI endpoints (/api/ai/**): 10 requests per minute.
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int WINDOW_MS = 60_000;
    private static final int GENERAL_LIMIT = 60;
    private static final int AI_LIMIT = 10;

    /** [0] = window start timestamp, [1] = count in current window. */
    private final ConcurrentHashMap<String, long[]> generalCounters = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, long[]> aiCounters = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        String userId = resolveUserId(request);
        if (userId == null) {
            return true; // unauthenticated — let security layer handle it
        }

        boolean isAiPath = request.getRequestURI().startsWith("/api/ai/");
        ConcurrentHashMap<String, long[]> counters = isAiPath ? aiCounters : generalCounters;
        int limit = isAiPath ? AI_LIMIT : GENERAL_LIMIT;

        long now = System.currentTimeMillis();
        long[] window = counters.computeIfAbsent(userId, k -> new long[]{now, 0});

        synchronized (window) {
            if (now - window[0] >= WINDOW_MS) {
                window[0] = now;
                window[1] = 0;
            }
            window[1]++;
            if (window[1] > limit) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
                return false;
            }
        }
        return true;
    }

    /**
     * Resolves the user identifier from the current security context.
     *
     * @param request the current HTTP request (unused, present for potential IP fallback)
     * @return the user ID as a string, or null if the user is not authenticated
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    protected String resolveUserId(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Long) {
            return auth.getPrincipal().toString();
        }
        return null;
    }

}
