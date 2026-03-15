package com.jichengtong.app.models;

import java.util.List;

public class FAQ {
    private String id;
    private String question;
    private String answer;
    private List<String> relatedLaws;
    private String relatedTopicId;
    private List<String> tags;

    // Getters
    public String getId() { return id; }
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public List<String> getRelatedLaws() { return relatedLaws; }
    public String getRelatedTopicId() { return relatedTopicId; }
    public List<String> getTags() { return tags; }
}
