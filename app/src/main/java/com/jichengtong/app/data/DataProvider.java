package com.jichengtong.app.data;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jichengtong.app.models.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DataProvider {
    private static DataProvider instance;
    private final Context context;
    private final Gson gson;

    private List<LawArticle> lawArticles;
    private List<CourtCase> courtCases;
    private List<Topic> topics;
    private List<FAQ> faqs;
    private List<ToolItem> tools;
    private List<GlossaryItem> glossary;

    private DataProvider(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
    }

    public static synchronized DataProvider getInstance(Context context) {
        if (instance == null) {
            instance = new DataProvider(context);
        }
        return instance;
    }

    private String loadJsonFromAsset(String fileName) {
        try {
            InputStream is = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            is.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "[]";
        }
    }

    public List<LawArticle> getLawArticles() {
        if (lawArticles == null) {
            String json = loadJsonFromAsset("laws/civil_code_inheritance.json");
            Type type = new TypeToken<List<LawArticle>>(){}.getType();
            lawArticles = gson.fromJson(json, type);
            if (lawArticles == null) lawArticles = new ArrayList<>();
        }
        return lawArticles;
    }

    public LawArticle getLawArticleById(String id) {
        for (LawArticle article : getLawArticles()) {
            if (article.getId().equals(id)) return article;
        }
        return null;
    }

    public List<LawArticle> getLawsByChapter(String chapter) {
        List<LawArticle> result = new ArrayList<>();
        for (LawArticle article : getLawArticles()) {
            if (article.getChapter().contains(chapter)) {
                result.add(article);
            }
        }
        return result;
    }

    public List<String> getLawChapters() {
        List<String> chapters = new ArrayList<>();
        for (LawArticle article : getLawArticles()) {
            if (!chapters.contains(article.getChapter())) {
                chapters.add(article.getChapter());
            }
        }
        return chapters;
    }

    public List<CourtCase> getCourtCases() {
        if (courtCases == null) {
            String json = loadJsonFromAsset("cases/court_cases.json");
            Type type = new TypeToken<List<CourtCase>>(){}.getType();
            courtCases = gson.fromJson(json, type);
            if (courtCases == null) courtCases = new ArrayList<>();
        }
        return courtCases;
    }

    public CourtCase getCourtCaseById(String id) {
        for (CourtCase c : getCourtCases()) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    public List<CourtCase> getCasesByType(String caseType) {
        List<CourtCase> result = new ArrayList<>();
        for (CourtCase c : getCourtCases()) {
            if (c.getCaseType().contains(caseType)) {
                result.add(c);
            }
        }
        return result;
    }

    public List<CourtCase> getCasesByTag(String tag) {
        List<CourtCase> result = new ArrayList<>();
        for (CourtCase c : getCourtCases()) {
            if (c.getTags() != null && c.getTags().contains(tag)) {
                result.add(c);
            }
        }
        return result;
    }

    public List<String> getCaseTypes() {
        List<String> types = new ArrayList<>();
        for (CourtCase c : getCourtCases()) {
            if (!types.contains(c.getCaseType())) {
                types.add(c.getCaseType());
            }
        }
        return types;
    }

    public List<Topic> getTopics() {
        if (topics == null) {
            String json = loadJsonFromAsset("knowledge/topics.json");
            Type type = new TypeToken<List<Topic>>(){}.getType();
            topics = gson.fromJson(json, type);
            if (topics == null) topics = new ArrayList<>();
        }
        return topics;
    }

    public Topic getTopicById(String id) {
        for (Topic t : getTopics()) {
            if (t.getId().equals(id)) return t;
        }
        return null;
    }

    public List<FAQ> getFAQs() {
        if (faqs == null) {
            String json = loadJsonFromAsset("knowledge/faq.json");
            Type type = new TypeToken<List<FAQ>>(){}.getType();
            faqs = gson.fromJson(json, type);
            if (faqs == null) faqs = new ArrayList<>();
        }
        return faqs;
    }

    public List<ToolItem> getTools() {
        if (tools == null) {
            String json = loadJsonFromAsset("tools/tools_data.json");
            Type type = new TypeToken<List<ToolItem>>(){}.getType();
            tools = gson.fromJson(json, type);
            if (tools == null) tools = new ArrayList<>();
        }
        return tools;
    }

    public ToolItem getToolById(String id) {
        for (ToolItem t : getTools()) {
            if (t.getId().equals(id)) return t;
        }
        return null;
    }

    public List<GlossaryItem> getGlossary() {
        if (glossary == null) {
            String json = loadJsonFromAsset("knowledge/glossary.json");
            Type type = new TypeToken<List<GlossaryItem>>(){}.getType();
            glossary = gson.fromJson(json, type);
            if (glossary == null) glossary = new ArrayList<>();
        }
        return glossary;
    }

    // Search across all content
    public List<Object> search(String query) {
        List<Object> results = new ArrayList<>();
        String q = query.toLowerCase();

        // Search laws
        for (LawArticle law : getLawArticles()) {
            if (matches(law, q)) {
                results.add(law);
            }
        }

        // Search cases
        for (CourtCase c : getCourtCases()) {
            if (matchesCase(c, q)) {
                results.add(c);
            }
        }

        // Search FAQs
        for (FAQ faq : getFAQs()) {
            if (faq.getQuestion().toLowerCase().contains(q) ||
                faq.getAnswer().toLowerCase().contains(q)) {
                results.add(faq);
            }
        }

        // Search topics
        for (Topic t : getTopics()) {
            if (t.getTitle().toLowerCase().contains(q) ||
                t.getDescription().toLowerCase().contains(q)) {
                results.add(t);
            }
        }

        // Search tools
        for (ToolItem t : getTools()) {
            if (t.getTitle().toLowerCase().contains(q) ||
                t.getDescription().toLowerCase().contains(q) ||
                t.getContent().toLowerCase().contains(q)) {
                results.add(t);
            }
        }

        // Search glossary
        for (GlossaryItem g : getGlossary()) {
            if (g.getTerm().toLowerCase().contains(q) ||
                g.getDefinition().toLowerCase().contains(q)) {
                results.add(g);
            }
        }

        return results;
    }

    private boolean matches(LawArticle law, String query) {
        return law.getTitle().toLowerCase().contains(query) ||
               law.getOriginalText().toLowerCase().contains(query) ||
               law.getPlainExplanation().toLowerCase().contains(query) ||
               (law.getKeywords() != null && law.getKeywords().stream()
                   .anyMatch(k -> k.toLowerCase().contains(query)));
    }

    private boolean matchesCase(CourtCase c, String query) {
        return c.getTitle().toLowerCase().contains(query) ||
               c.getCaseSummary().toLowerCase().contains(query) ||
               c.getCourt().toLowerCase().contains(query) ||
               (c.getTags() != null && c.getTags().stream()
                   .anyMatch(t -> t.toLowerCase().contains(query)));
    }
}
