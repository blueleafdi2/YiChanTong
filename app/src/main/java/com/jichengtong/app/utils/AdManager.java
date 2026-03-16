package com.jichengtong.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;

/**
 * Centralized ad management controlled entirely by RemoteConfig.
 *
 * Architecture: When no ad SDK is installed, this shows nothing.
 * When an SDK is integrated (e.g. 穿山甲), the loadXxx methods delegate
 * to the real SDK. All ad decisions are made server-side via remote_config.json:
 *
 * "ad_config": {
 *   "enabled": false,
 *   "provider": "csj",                    // csj=穿山甲, gdt=优量汇, none
 *   "banner": { "enabled": false, "unit_id": "" },
 *   "interstitial": { "enabled": false, "unit_id": "", "frequency_minutes": 30 },
 *   "rewarded": { "enabled": false, "unit_id": "" },
 *   "native": { "enabled": false, "unit_id": "" },
 *   "new_user_ad_free_days": 3
 * }
 */
public class AdManager {
    private static final String TAG = "AdManager";
    private static AdManager instance;
    private final Context context;
    private JSONObject adConfig;
    private long installTimestamp;

    private AdManager(Context ctx) {
        this.context = ctx.getApplicationContext();
        SharedPreferences prefs = ctx.getSharedPreferences("ad_prefs", Context.MODE_PRIVATE);
        installTimestamp = prefs.getLong("install_time", 0);
        if (installTimestamp == 0) {
            installTimestamp = System.currentTimeMillis();
            prefs.edit().putLong("install_time", installTimestamp).apply();
        }
        refreshConfig();
    }

    public static synchronized AdManager getInstance(Context ctx) {
        if (instance == null) instance = new AdManager(ctx);
        return instance;
    }

    public void refreshConfig() {
        try {
            RemoteConfig rc = RemoteConfig.getInstance(context);
            JSONObject cfg = rc.getAdConfig();
            adConfig = cfg != null ? cfg : new JSONObject();
        } catch (Exception e) {
            adConfig = new JSONObject();
        }
    }

    public boolean isAdEnabled() {
        return adConfig.optBoolean("enabled", false);
    }

    public String getProvider() {
        String p = adConfig.optString("provider", "none");
        return p.isEmpty() ? "none" : p;
    }

    public boolean isNewUserAdFree() {
        int freeDays = getNewUserAdFreeDays();
        long elapsed = System.currentTimeMillis() - installTimestamp;
        return elapsed < (long) freeDays * 24 * 60 * 60 * 1000;
    }

    public int getNewUserAdFreeDays() {
        return adConfig.optInt("new_user_ad_free_days", 3);
    }

    public boolean isBannerEnabled() {
        if (!isAdEnabled() || isNewUserAdFree()) return false;
        try {
            return adConfig.optJSONObject("banner") != null
                    && adConfig.getJSONObject("banner").optBoolean("enabled", false);
        } catch (Exception e) { return false; }
    }

    public boolean isInterstitialEnabled() {
        if (!isAdEnabled() || isNewUserAdFree()) return false;
        try {
            return adConfig.optJSONObject("interstitial") != null
                    && adConfig.getJSONObject("interstitial").optBoolean("enabled", false);
        } catch (Exception e) { return false; }
    }

    public boolean isRewardedEnabled() {
        if (!isAdEnabled() || isNewUserAdFree()) return false;
        try {
            return adConfig.optJSONObject("rewarded") != null
                    && adConfig.getJSONObject("rewarded").optBoolean("enabled", false);
        } catch (Exception e) { return false; }
    }

    public boolean isNativeAdEnabled() {
        if (!isAdEnabled() || isNewUserAdFree()) return false;
        try {
            return adConfig.optJSONObject("native") != null
                    && adConfig.getJSONObject("native").optBoolean("enabled", false);
        } catch (Exception e) { return false; }
    }

    public String getBannerUnitId() {
        try { return adConfig.getJSONObject("banner").optString("unit_id", ""); }
        catch (Exception e) { return ""; }
    }

    public String getInterstitialUnitId() {
        try { return adConfig.getJSONObject("interstitial").optString("unit_id", ""); }
        catch (Exception e) { return ""; }
    }

    public int getInterstitialFrequencyMinutes() {
        try { return adConfig.getJSONObject("interstitial").optInt("frequency_minutes", 30); }
        catch (Exception e) { return 30; }
    }

    /**
     * Loads a banner ad into the given container.
     * When no SDK is integrated, this is a no-op.
     * After SDK integration, replace this method body with real ad loading.
     */
    public void loadBanner(ViewGroup container) {
        if (!isBannerEnabled()) {
            container.setVisibility(View.GONE);
            return;
        }

        String provider = getProvider();
        Log.d(TAG, "loadBanner: provider=" + provider + " unitId=" + getBannerUnitId());
        Analytics.getInstance(context).logEvent("ad_banner_request");

        // SDK integration point: replace the block below with real ad code
        // Example for 穿山甲:
        // AdSlot adSlot = new AdSlot.Builder()
        //     .setCodeId(getBannerUnitId())
        //     .setAdCount(1)
        //     .build();
        // TTAdNative adNative = TTAdSdk.getAdManager().createAdNative(context);
        // adNative.loadBannerExpressAd(adSlot, ...);

        container.setVisibility(View.GONE);
    }

    /**
     * Shows an interstitial ad if conditions are met.
     * Respects frequency capping from RemoteConfig.
     */
    public void showInterstitial() {
        if (!isInterstitialEnabled()) return;

        SharedPreferences prefs = context.getSharedPreferences("ad_prefs", Context.MODE_PRIVATE);
        long lastShow = prefs.getLong("last_interstitial", 0);
        long minInterval = (long) getInterstitialFrequencyMinutes() * 60 * 1000;
        if (System.currentTimeMillis() - lastShow < minInterval) return;

        Log.d(TAG, "showInterstitial: provider=" + getProvider());
        Analytics.getInstance(context).logEvent("ad_interstitial_request");
        prefs.edit().putLong("last_interstitial", System.currentTimeMillis()).apply();

        // SDK integration point: load and show interstitial ad
    }

    /**
     * Shows a rewarded video ad with callback.
     */
    public void showRewardedVideo(RewardCallback callback) {
        if (!isRewardedEnabled()) {
            if (callback != null) callback.onRewardGranted();
            return;
        }

        Log.d(TAG, "showRewardedVideo: provider=" + getProvider());
        Analytics.getInstance(context).logEvent("ad_rewarded_request");

        // SDK integration point: load and show rewarded video
        // For now, grant reward directly
        if (callback != null) callback.onRewardGranted();
    }

    public interface RewardCallback {
        void onRewardGranted();
    }
}
