package com.jichengtong.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Lightweight analytics framework. Logs events locally and provides
 * a hook for sending to any backend (Firebase, Umeng, etc.) later.
 * Also tracks basic usage metrics for monetization readiness.
 */
public class Analytics {
    private static final String TAG = "Analytics";
    private static final String PREFS = "analytics_prefs";
    private static Analytics instance;
    private final SharedPreferences prefs;
    private final String deviceId;

    private Analytics(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String id = prefs.getString("device_id", null);
        if (id == null) {
            id = UUID.randomUUID().toString();
            prefs.edit().putString("device_id", id).apply();
        }
        deviceId = id;
    }

    public static synchronized Analytics getInstance(Context ctx) {
        if (instance == null) instance = new Analytics(ctx);
        return instance;
    }

    public String getDeviceId() { return deviceId; }

    public void logEvent(String event, Map<String, String> params) {
        long count = prefs.getLong("evt_" + event, 0) + 1;
        prefs.edit().putLong("evt_" + event, count).apply();
        Log.d(TAG, "Event: " + event + " #" + count + " " + (params != null ? params : ""));
    }

    public void logEvent(String event) { logEvent(event, null); }

    public void logScreenView(String screen) {
        Map<String, String> p = new HashMap<>();
        p.put("screen", screen);
        logEvent("screen_view", p);
    }

    public void logSearch(String query, int resultCount) {
        Map<String, String> p = new HashMap<>();
        p.put("query", query);
        p.put("results", String.valueOf(resultCount));
        logEvent("search", p);
    }

    public void logContentView(String type, String id) {
        Map<String, String> p = new HashMap<>();
        p.put("content_type", type);
        p.put("content_id", id);
        logEvent("content_view", p);
    }

    public void logAIQuery(String query) {
        Map<String, String> p = new HashMap<>();
        p.put("query", query.length() > 100 ? query.substring(0, 100) : query);
        logEvent("ai_query", p);
    }

    public void logShare(String type, String id) {
        Map<String, String> p = new HashMap<>();
        p.put("content_type", type);
        p.put("content_id", id);
        logEvent("share", p);
    }

    public void logFavorite(String type, String id) {
        Map<String, String> p = new HashMap<>();
        p.put("content_type", type);
        p.put("content_id", id);
        logEvent("favorite", p);
    }

    public long getEventCount(String event) {
        return prefs.getLong("evt_" + event, 0);
    }

    public long getTotalSessions() {
        return prefs.getLong("evt_app_open", 0);
    }

    public Map<String, Long> getUsageStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("sessions", getEventCount("app_open"));
        stats.put("searches", getEventCount("search"));
        stats.put("ai_queries", getEventCount("ai_query"));
        stats.put("law_views", getEventCount("law_view"));
        stats.put("case_views", getEventCount("case_view"));
        stats.put("favorites", getEventCount("favorite"));
        stats.put("shares", getEventCount("share"));
        return stats;
    }
}
