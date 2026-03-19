package com.mindtrack.common.exception;

import com.mindtrack.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Global exception handler that returns sanitized error responses without leaking
 * internal IDs, stack traces, or implementation details.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ErrorResponse buildError(String error, String message, HttpServletRequest request) {
        return new ErrorResponse(
                error,
                message,
                Instant.now().toString(),
                request.getRequestURI(),
                MDC.get("requestId")
        );
    }

    /**
     * Handles illegal argument exceptions with a 400 response.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(buildError("Bad Request", ex.getMessage(), request));
    }

    /**
     * Handles validation failures with a 400 response listing field errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(buildError("Validation Failed", message, request));
    }

    /**
     * Handles access denied exceptions with a 403 response.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError("Forbidden", "Access denied", request));
    }

    /**
     * Handles ResponseStatusException by forwarding its status and reason.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(
            ResponseStatusException ex, HttpServletRequest request) {
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        return ResponseEntity.status(ex.getStatusCode())
                .body(buildError("Error", message, request));
    }

    /**
     * Handles NoSuchElementException with a 404 response.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NoSuchElementException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError("Not Found", ex.getMessage(), request));
    }

    /**
     * Handles malformed request bodies with a 400 response.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(buildError("Bad Request", "Malformed request body", request));
    }

    /**
     * Handles path/query parameter type mismatches with a 400 response.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(buildError("Bad Request", "Invalid parameter: " + ex.getName(), request));
    }

    /**
     * Handles unsupported HTTP methods with a 405 response.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(buildError("Method Not Allowed", ex.getMessage(), request));
    }

    /**
     * Handles constraint violations with a 409 response.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError("Conflict", ex.getMessage(), request));
    }

    /**
     * Catch-all handler for unexpected exceptions. Logs full stack trace but returns
     * a generic message to prevent leaking internal implementation details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        LOG.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError("Internal Server Error", "An unexpected error occurred", request));
    }
}
