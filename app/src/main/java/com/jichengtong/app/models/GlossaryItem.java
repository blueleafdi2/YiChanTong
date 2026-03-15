package com.jichengtong.app.models;

public class GlossaryItem {
    private String term;
    private String definition;
    private String relatedLaw;
    private String example;
    private String category;
    private String difficulty;

    public String getTerm() { return term; }
    public String getDefinition() { return definition; }
    public String getRelatedLaw() { return relatedLaw; }
    public String getExample() { return example != null ? example : ""; }
    public String getCategory() { return category != null ? category : "基本概念"; }
    public String getDifficulty() { return difficulty != null ? difficulty : "easy"; }
}
