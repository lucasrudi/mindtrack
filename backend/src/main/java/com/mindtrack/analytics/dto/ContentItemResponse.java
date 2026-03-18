package com.mindtrack.analytics.dto;

/**
 * A curated content item for the dashboard widgets.
 */
public class ContentItemResponse {

    private String type;
    private String title;
    private String body;
    private String category;
    private String url;
    private String sourceType;
    private String sourceLabel;

    public ContentItemResponse() {
    }

    public ContentItemResponse(String type, String title, String body,
                               String category, String url,
                               String sourceType, String sourceLabel) {
        this.type = type;
        this.title = title;
        this.body = body;
        this.category = category;
        this.url = url;
        this.sourceType = sourceType;
        this.sourceLabel = sourceLabel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    public void setSourceLabel(String sourceLabel) {
        this.sourceLabel = sourceLabel;
    }
}
