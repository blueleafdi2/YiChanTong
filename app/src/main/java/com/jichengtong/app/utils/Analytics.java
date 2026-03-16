package com.jichengtong.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class Analytics {
    private static final String TAG = "Analytics";
    private static final String PREFS = "analytics_prefs";
    private static final String EVENT_LOG_KEY = "event_log";
    private static final int MAX_LOG_ENTRIES = 500;
    private static Analytics instance;
    private final SharedPreferences prefs;
    private final String deviceId;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

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

        appendEventLog(event, params);
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
        stats.put("screen_views", getEventCount("screen_view"));
        stats.put("searches", getEventCount("search"));
        stats.put("content_views", getEventCount("content_view"));
        stats.put("ai_queries", getEventCount("ai_query"));
        stats.put("favorites", getEventCount("favorite"));
        stats.put("shares", getEventCount("share"));
        return stats;
    }

    private void appendEventLog(String event, Map<String, String> params) {
        try {
            String existing = prefs.getString(EVENT_LOG_KEY, "[]");
            JSONArray arr = new JSONArray(existing);
            JSONObject entry = new JSONObject();
            entry.put("t", sdf.format(new Date()));
            entry.put("e", event);
            if (params != null && !params.isEmpty()) {
                JSONObject p = new JSONObject();
                for (Map.Entry<String, String> e : params.entrySet()) {
                    p.put(e.getKey(), e.getValue());
                }
                entry.put("p", p);
            }
            arr.put(entry);
            while (arr.length() > MAX_LOG_ENTRIES) arr.remove(0);
            prefs.edit().putString(EVENT_LOG_KEY, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    public String getEventLogJson() {
        return prefs.getString(EVENT_LOG_KEY, "[]");
    }

    public String exportFullReport() {
        try {
            JSONObject report = new JSONObject();
            report.put("device_id", deviceId);
            report.put("export_time", sdf.format(new Date()));
            report.put("android_version", Build.VERSION.RELEASE);
            report.put("device_model", Build.MANUFACTURER + " " + Build.MODEL);

            JSONObject counts = new JSONObject();
            for (Map.Entry<String, Long> e : getUsageStats().entrySet()) {
                counts.put(e.getKey(), e.getValue());
            }
            report.put("event_counts", counts);

            report.put("recent_events", new JSONArray(getEventLogJson()));
            return report.toString(2);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    public void clearEventLog() {
        prefs.edit().putString(EVENT_LOG_KEY, "[]").apply();
    }
}
