package com.mindtrack.ai.config;

import com.mindtrack.ai.model.ConversationType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the AI service.
 * Bound to the {@code mindtrack.ai} prefix in application.yml.
 */
@Component
@ConfigurationProperties(prefix = "mindtrack.ai")
public class AiProperties {

    private String apiKey;
    private String apiUrl = "https://api.anthropic.com/v1/messages";
    private String model = "claude-sonnet-4-20250514";
    private TokenTiers tokenTiers = new TokenTiers();
    private CacheConfig cache = new CacheConfig();

    /** Token tier budgets by conversation type. */
    public static class TokenTiers {
        private int quickCheckin = 200;
        private int coaching = 500;
        private int sessionSummary = 1500;

        public int getQuickCheckin() {
            return quickCheckin;
        }

        public void setQuickCheckin(int quickCheckin) {
            this.quickCheckin = quickCheckin;
        }

        public int getCoaching() {
            return coaching;
        }

        public void setCoaching(int coaching) {
            this.coaching = coaching;
        }

        public int getSessionSummary() {
            return sessionSummary;
        }

        public void setSessionSummary(int sessionSummary) {
            this.sessionSummary = sessionSummary;
        }
    }

    /** Caffeine cache configuration. */
    public static class CacheConfig {
        private int ttlHours = 24;
        private int maxSize = 500;

        public int getTtlHours() {
            return ttlHours;
        }

        public void setTtlHours(int ttlHours) {
            this.ttlHours = ttlHours;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public TokenTiers getTokenTiers() {
        return tokenTiers;
    }

    public void setTokenTiers(TokenTiers tokenTiers) {
        this.tokenTiers = tokenTiers;
    }

    public CacheConfig getCache() {
        return cache;
    }

    public void setCache(CacheConfig cache) {
        this.cache = cache;
    }

    /**
     * Returns the max_tokens budget for the given conversation type.
     *
     * @param type the conversation type
     * @return max tokens allowed
     */
    public int getMaxTokensFor(ConversationType type) {
        return switch (type) {
            case QUICK_CHECKIN -> tokenTiers.getQuickCheckin();
            case COACHING -> tokenTiers.getCoaching();
            case SESSION_SUMMARY -> tokenTiers.getSessionSummary();
            case ONBOARDING -> 1000;
        };
    }
}
