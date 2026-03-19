package com.mindtrack.common.exception;

import com.mindtrack.common.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void notFoundHandlerReturns404() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
                new NoSuchElementException("Item not found"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getPath()).isEqualTo("/test");
    }

    @Test
    void catchAllHandlerObfuscatesInternalDetail() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(
                new RuntimeException("Sensitive internal detail"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).doesNotContain("Sensitive internal detail");
    }

    @Test
    void illegalArgumentHandlerReturns400() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/test");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
                new IllegalArgumentException("Invalid input"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
    }
}
