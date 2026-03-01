package com.mindtrack.interview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Spring configuration providing a shared {@link RestTemplate} bean.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a default RestTemplate for outbound HTTP calls.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
