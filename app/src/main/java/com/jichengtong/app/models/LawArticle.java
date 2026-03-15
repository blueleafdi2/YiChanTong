package com.jichengtong.app.models;

import java.util.List;

public class LawArticle {
    private String id;
    private String article;
    private String chapter;
    private String title;
    private String originalText;
    private String plainExplanation;
    private String lifeExample;
    private String judicialInterpretation;
    private String legislativeHistory;
    private List<String> keywords;

    // Getters
    public String getId() { return id; }
    public String getArticle() { return article; }
    public String getChapter() { return chapter; }
    public String getTitle() { return title; }
    public String getOriginalText() { return originalText; }
    public String getPlainExplanation() { return plainExplanation; }
    public String getLifeExample() { return lifeExample; }
    public String getJudicialInterpretation() { return judicialInterpretation; }
    public String getLegislativeHistory() { return legislativeHistory; }
    public List<String> getKeywords() { return keywords; }
}
