package com.mindtrack.admin.dto;

/**
 * DTO for permission data.
 */
public class PermissionResponse {

    private Long id;
    private String resource;
    private String action;

    public PermissionResponse() {
    }

    public PermissionResponse(Long id, String resource, String action) {
        this.id = id;
        this.resource = resource;
        this.action = action;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
