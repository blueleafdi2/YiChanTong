package com.jichengtong.app.models;

import java.util.List;

public class CourtCase {
    private String id;
    private String caseNumber;
    private String title;
    private String court;
    private String courtLevel;
    private String province;
    private String city;
    private String judgeDate;
    private String caseType;
    private List<String> tags;
    private String caseSummary;
    private String disputeFocus;
    private String judgment;
    private List<String> legalBasis;
    private String rulingGist;
    private String source;

    // Getters
    public String getId() { return id; }
    public String getCaseNumber() { return caseNumber; }
    public String getTitle() { return title; }
    public String getCourt() { return court; }
    public String getCourtLevel() { return courtLevel; }
    public String getProvince() { return province; }
    public String getCity() { return city; }
    public String getJudgeDate() { return judgeDate; }
    public String getCaseType() { return caseType; }
    public List<String> getTags() { return tags; }
    public String getCaseSummary() { return caseSummary; }
    public String getDisputeFocus() { return disputeFocus; }
    public String getJudgment() { return judgment; }
    public List<String> getLegalBasis() { return legalBasis; }
    public String getRulingGist() { return rulingGist; }
    public String getSource() { return source; }
}
