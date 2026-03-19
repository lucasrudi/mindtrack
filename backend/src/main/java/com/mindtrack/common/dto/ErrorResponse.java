package com.mindtrack.common.dto;

/**
 * Structured error response returned by the global exception handler.
 * Contains enough context for debugging without leaking internal implementation details.
 */
public class ErrorResponse {

    private String error;
    private String message;
    private String timestamp;
    private String path;
    private String requestId;

    public ErrorResponse() {
        // Required by Jackson for serialization/deserialization.
    }

    public ErrorResponse(String error, String message, String timestamp, String path, String requestId) {
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
        this.requestId = requestId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
