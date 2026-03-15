package com.jichengtong.app.models;

import java.util.List;

public class Topic {
    private String id;
    private String icon;
    private String title;
    private String description;
    private String content;
    private List<String> relatedLaws;
    private List<String> relatedCaseIds;

    // Getters
    public String getId() { return id; }
    public String getIcon() { return icon; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getContent() { return content; }
    public List<String> getRelatedLaws() { return relatedLaws; }
    public List<String> getRelatedCaseIds() { return relatedCaseIds; }
}
