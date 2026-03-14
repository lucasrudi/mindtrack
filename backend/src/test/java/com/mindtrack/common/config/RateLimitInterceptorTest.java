package com.mindtrack.common.config;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitInterceptorTest {

    private RateLimitInterceptor interceptor;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        interceptor = new RateLimitInterceptor();
        var auth = new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void allowsRequestsUnderGeneralLimit() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/interviews");
        for (int i = 0; i < 60; i++) {
            MockHttpServletResponse resp = new MockHttpServletResponse();
            boolean result = interceptor.preHandle(req, resp, null);
            assertThat(result).isTrue();
        }
    }

    @Test
    void blocksRequestsOverGeneralLimit() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/interviews");
        for (int i = 0; i < 60; i++) {
            interceptor.preHandle(req, new MockHttpServletResponse(), null);
        }
        MockHttpServletResponse resp = new MockHttpServletResponse();
        boolean result = interceptor.preHandle(req, resp, null);
        assertThat(result).isFalse();
        assertThat(resp.getStatus()).isEqualTo(429);
    }

    @Test
    void aiEndpointHasLowerLimit() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/ai/chat");
        for (int i = 0; i < 10; i++) {
            interceptor.preHandle(req, new MockHttpServletResponse(), null);
        }
        MockHttpServletResponse resp = new MockHttpServletResponse();
        boolean result = interceptor.preHandle(req, resp, null);
        assertThat(result).isFalse();
        assertThat(resp.getStatus()).isEqualTo(429);
    }

}
