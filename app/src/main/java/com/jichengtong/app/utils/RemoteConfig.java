package com.jichengtong.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

/**
 * Remote configuration for flexible page control, ad slots, and announcements.
 * Fetches config from a JSON URL (GitHub raw, your server, etc.).
 * Falls back to local defaults when offline.
 *
 * Config JSON schema:
 * {
 *   "announcement": { "enabled": false, "title": "", "message": "", "url": "" },
 *   "ad_config": { "banner_enabled": false, "interstitial_enabled": false, "ad_unit_id": "" },
 *   "contact": { "wechat_id": "LawServicePro", "phone": "12348" },
 *   "feature_flags": { "ai_enabled": true, "glossary_enabled": true },
 *   "min_version": 1,
 *   "latest_version": 12,
 *   "update_url": ""
 * }
 */
public class RemoteConfig {
    private static final String TAG = "RemoteConfig";
    private static final String PREFS = "remote_config";
    private static final String CONFIG_URL_KEY = "config_url";
    private static final String DEFAULT_CONFIG_URL =
            "https://raw.githubusercontent.com/blueleafdi2/YiChanTong/main/remote_config.json";

    private static RemoteConfig instance;
    private final SharedPreferences prefs;
    private JSONObject config;

    private RemoteConfig(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String cached = prefs.getString("cached_config", "{}");
        try { config = new JSONObject(cached); } catch (Exception e) { config = new JSONObject(); }
    }

    public static synchronized RemoteConfig getInstance(Context ctx) {
        if (instance == null) instance = new RemoteConfig(ctx);
        return instance;
    }

    public void fetchAsync(Runnable onComplete) {
        String url = prefs.getString(CONFIG_URL_KEY, DEFAULT_CONFIG_URL);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                config = new JSONObject(sb.toString());
                prefs.edit().putString("cached_config", sb.toString()).apply();
                Log.d(TAG, "Remote config fetched successfully");
            } catch (Exception e) {
                Log.w(TAG, "Failed to fetch remote config, using cached: " + e.getMessage());
            }
            if (onComplete != null) {
                new Handler(Looper.getMainLooper()).post(onComplete);
            }
        });
    }

    public boolean isAnnouncementEnabled() {
        try { return config.optJSONObject("announcement") != null
                && config.getJSONObject("announcement").optBoolean("enabled", false); }
        catch (Exception e) { return false; }
    }

    public String getAnnouncementTitle() {
        try { return config.getJSONObject("announcement").optString("title", ""); }
        catch (Exception e) { return ""; }
    }

    public String getAnnouncementMessage() {
        try { return config.getJSONObject("announcement").optString("message", ""); }
        catch (Exception e) { return ""; }
    }

    public String getAnnouncementUrl() {
        try { return config.getJSONObject("announcement").optString("url", ""); }
        catch (Exception e) { return ""; }
    }

    public boolean isBannerAdEnabled() {
        try { return config.optJSONObject("ad_config") != null
                && config.getJSONObject("ad_config").optBoolean("banner_enabled", false); }
        catch (Exception e) { return false; }
    }

    public String getAdUnitId() {
        try { return config.getJSONObject("ad_config").optString("ad_unit_id", ""); }
        catch (Exception e) { return ""; }
    }

    public JSONObject getAdConfig() {
        return config.optJSONObject("ad_config");
    }

    public boolean isFeatureEnabled(String feature) {
        try { return config.optJSONObject("feature_flags") == null
                || config.getJSONObject("feature_flags").optBoolean(feature, true); }
        catch (Exception e) { return true; }
    }

    public int getMinVersion() {
        return config.optInt("min_version", 1);
    }

    public int getLatestVersion() {
        return config.optInt("latest_version", 1);
    }

    public String getUpdateUrl() {
        return config.optString("update_url", "");
    }

    public String getString(String key, String defaultValue) {
        return config.optString(key, defaultValue);
    }
}
