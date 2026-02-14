package com.mindtrack.ai.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration for the AI module: cache manager and REST client.
 */
@Configuration
@EnableCaching
public class AiConfig {

    /**
     * Caffeine cache manager for AI response caching.
     *
     * @param aiProperties the AI configuration properties
     * @return configured cache manager
     */
    @Bean
    public CacheManager aiCacheManager(AiProperties aiProperties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("ai-responses");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(aiProperties.getCache().getMaxSize())
                .expireAfterWrite(aiProperties.getCache().getTtlHours(), TimeUnit.HOURS)
                .recordStats());
        return cacheManager;
    }

    /**
     * REST client for Claude API calls.
     *
     * @param aiProperties the AI configuration properties
     * @return configured REST client
     */
    @Bean
    public RestClient claudeRestClient(AiProperties aiProperties) {
        return RestClient.builder()
                .baseUrl(aiProperties.getApiUrl())
                .defaultHeader("x-api-key", aiProperties.getApiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", "application/json")
                .build();
    }
}
