package com.jichengtong.app.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jichengtong.app.R;
import com.jichengtong.app.utils.Analytics;
import com.jichengtong.app.utils.AdManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executors;

public class AnalyticsDashboardActivity extends AppCompatActivity {
    private static final String DEV_PREFS = "dev_prefs";
    private TextView uploadStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics_dashboard);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("📊 数据看板");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_right);
        toolbar.getNavigationIcon().setAutoMirrored(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        uploadStatusText = findViewById(R.id.upload_status_text);
        refreshData();
        refreshAdStatus();

        findViewById(R.id.btn_copy_json).setOnClickListener(v -> {
            String json = Analytics.getInstance(this).exportFullReport();
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("analytics", json));
            Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_share_json).setOnClickListener(v -> shareReport());

        findViewById(R.id.btn_clear_log).setOnClickListener(v -> {
            Analytics.getInstance(this).clearEventLog();
            Toast.makeText(this, "事件日志已清空", Toast.LENGTH_SHORT).show();
            refreshData();
        });

        findViewById(R.id.btn_upload_gist).setOnClickListener(v -> onUploadGist());
        findViewById(R.id.btn_config_token).setOnClickListener(v -> showTokenDialog());
    }

    private void refreshData() {
        Analytics analytics = Analytics.getInstance(this);
        Map<String, Long> stats = analytics.getUsageStats();

        StringBuilder sb = new StringBuilder();
        sb.append("📱 设备ID: ").append(analytics.getDeviceId().substring(0, 8)).append("...\n\n");
        sb.append("━━━ 事件统计 ━━━\n\n");
        sb.append("🚀 启动次数: ").append(stats.getOrDefault("sessions", 0L)).append("\n");
        sb.append("👁 页面浏览: ").append(stats.getOrDefault("screen_views", 0L)).append("\n");
        sb.append("📖 内容查看: ").append(stats.getOrDefault("content_views", 0L)).append("\n");
        sb.append("🔍 搜索次数: ").append(stats.getOrDefault("searches", 0L)).append("\n");
        sb.append("🤖 AI提问: ").append(stats.getOrDefault("ai_queries", 0L)).append("\n");
        sb.append("⭐ 收藏操作: ").append(stats.getOrDefault("favorites", 0L)).append("\n");
        sb.append("📤 分享操作: ").append(stats.getOrDefault("shares", 0L)).append("\n");

        ((TextView) findViewById(R.id.stats_text)).setText(sb.toString());

        String token = getDevPrefs().getString("github_token", "");
        String gistId = getDevPrefs().getString("gist_id", "");
        StringBuilder statusSb = new StringBuilder();
        statusSb.append("GitHub Token: ").append(token.isEmpty() ? "❌ 未配置" : "✅ 已配置").append("\n");
        statusSb.append("Gist ID: ").append(gistId.isEmpty() ? "尚未上传" : gistId.substring(0, Math.min(12, gistId.length())) + "...");
        if (!gistId.isEmpty()) {
            statusSb.append("\n🔗 https://gist.github.com/").append(gistId);
        }
        uploadStatusText.setText(statusSb.toString());

        try {
            JSONArray log = new JSONArray(analytics.getEventLogJson());
            StringBuilder logSb = new StringBuilder();
            logSb.append("━━━ 最近事件（共 ").append(log.length()).append(" 条）━━━\n\n");
            int start = Math.max(0, log.length() - 30);
            for (int i = log.length() - 1; i >= start; i--) {
                JSONObject entry = log.getJSONObject(i);
                logSb.append(entry.getString("t")).append("  ");
                logSb.append(entry.getString("e"));
                if (entry.has("p")) {
                    JSONObject p = entry.getJSONObject("p");
                    logSb.append("  ");
                    java.util.Iterator<String> keys = p.keys();
                    while (keys.hasNext()) {
                        String k = keys.next();
                        logSb.append(k).append("=").append(p.getString(k));
                        if (keys.hasNext()) logSb.append(", ");
                    }
                }
                logSb.append("\n");
            }
            if (log.length() > 30) {
                logSb.append("\n... 还有 ").append(log.length() - 30).append(" 条更早的事件\n");
            }
            ((TextView) findViewById(R.id.event_log_text)).setText(logSb.toString());
        } catch (Exception e) {
            ((TextView) findViewById(R.id.event_log_text)).setText("日志解析错误: " + e.getMessage());
        }
    }

    private void refreshAdStatus() {
        AdManager adManager = AdManager.getInstance(this);
        TextView adText = findViewById(R.id.ad_status_text);
        StringBuilder sb = new StringBuilder();
        sb.append("━━━ 广告状态 ━━━\n\n");
        sb.append("广告总开关: ").append(adManager.isAdEnabled() ? "✅ 开启" : "⬜ 关闭").append("\n");
        sb.append("Banner广告: ").append(adManager.isBannerEnabled() ? "✅" : "⬜").append("\n");
        sb.append("插屏广告: ").append(adManager.isInterstitialEnabled() ? "✅" : "⬜").append("\n");
        sb.append("激励视频: ").append(adManager.isRewardedEnabled() ? "✅" : "⬜").append("\n");
        sb.append("原生广告: ").append(adManager.isNativeAdEnabled() ? "✅" : "⬜").append("\n");
        sb.append("广告商: ").append(adManager.getProvider()).append("\n");
        sb.append("新用户免广告天数: ").append(adManager.getNewUserAdFreeDays()).append("\n\n");
        sb.append("💡 通过 remote_config.json 控制以上所有参数");
        adText.setText(sb.toString());
    }

    private void showTokenDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("ghp_xxxx... (需要gist权限)");
        String existing = getDevPrefs().getString("github_token", "");
        if (!existing.isEmpty()) input.setText(existing);

        new MaterialAlertDialogBuilder(this)
                .setTitle("配置 GitHub Token")
                .setMessage("请输入你的 GitHub Personal Access Token（需要 gist 权限）\n\n" +
                        "获取方式：GitHub → Settings → Developer settings → Personal access tokens → Generate new token → 勾选 gist")
                .setView(input)
                .setPositiveButton("保存", (d, w) -> {
                    String token = input.getText().toString().trim();
                    getDevPrefs().edit().putString("github_token", token).apply();
                    Toast.makeText(this, token.isEmpty() ? "Token已清除" : "Token已保存", Toast.LENGTH_SHORT).show();
                    refreshData();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void onUploadGist() {
        String token = getDevPrefs().getString("github_token", "");
        if (token.isEmpty()) {
            Toast.makeText(this, "请先配置 GitHub Token", Toast.LENGTH_SHORT).show();
            showTokenDialog();
            return;
        }

        uploadStatusText.setText("⏳ 正在上传到 GitHub Gist...");
        String json = Analytics.getInstance(this).exportFullReport();
        String existingGistId = getDevPrefs().getString("gist_id", "");

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String deviceId = Analytics.getInstance(this).getDeviceId().substring(0, 8);
                JSONObject gistPayload = new JSONObject();
                gistPayload.put("description", "遗产通 Analytics - Device " + deviceId);
                gistPayload.put("public", false);
                JSONObject files = new JSONObject();
                JSONObject fileContent = new JSONObject();
                fileContent.put("content", json);
                files.put("analytics_" + deviceId + ".json", fileContent);
                gistPayload.put("files", files);

                String urlStr;
                String method;
                if (existingGistId.isEmpty()) {
                    urlStr = "https://api.github.com/gists";
                    method = "POST";
                } else {
                    urlStr = "https://api.github.com/gists/" + existingGistId;
                    method = "PATCH";
                }

                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod(method);
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/vnd.github+json");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(gistPayload.toString().getBytes("UTF-8"));
                os.close();

                int code = conn.getResponseCode();
                if (code == 200 || code == 201) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();
                    JSONObject resp = new JSONObject(sb.toString());
                    String gistId = resp.getString("id");
                    String htmlUrl = resp.getString("html_url");
                    getDevPrefs().edit().putString("gist_id", gistId).apply();

                    runOnUiThread(() -> {
                        uploadStatusText.setText("✅ 上传成功!\n🔗 " + htmlUrl);
                        Toast.makeText(this, "数据已上传到 GitHub Gist", Toast.LENGTH_SHORT).show();
                        refreshData();
                    });
                } else {
                    BufferedReader er = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder esb = new StringBuilder();
                    String eline;
                    while ((eline = er.readLine()) != null) esb.append(eline);
                    er.close();
                    String errorMsg = esb.toString();
                    runOnUiThread(() -> {
                        uploadStatusText.setText("❌ 上传失败 (HTTP " + code + ")\n" + errorMsg.substring(0, Math.min(100, errorMsg.length())));
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    uploadStatusText.setText("❌ 上传异常: " + e.getMessage());
                });
            }
        });
    }

    private void shareReport() {
        try {
            String json = Analytics.getInstance(this).exportFullReport();
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/json");
            share.putExtra(Intent.EXTRA_SUBJECT, "遗产通数据报告");
            share.putExtra(Intent.EXTRA_TEXT, json);
            startActivity(Intent.createChooser(share, "分享数据报告"));
        } catch (Exception e) {
            Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private SharedPreferences getDevPrefs() {
        return getSharedPreferences(DEV_PREFS, MODE_PRIVATE);
    }
}
