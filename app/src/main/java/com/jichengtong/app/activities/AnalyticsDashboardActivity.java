package com.jichengtong.app.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.jichengtong.app.R;
import com.jichengtong.app.utils.Analytics;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;

public class AnalyticsDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics_dashboard);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("📊 数据看板");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_right);
        toolbar.getNavigationIcon().setAutoMirrored(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        refreshData();

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

    private void shareReport() {
        try {
            String json = Analytics.getInstance(this).exportFullReport();
            File dir = new File(getExternalCacheDir(), "analytics");
            dir.mkdirs();
            File file = new File(dir, "analytics_report.json");
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/json");
            share.putExtra(Intent.EXTRA_SUBJECT, "遗产通数据报告");
            share.putExtra(Intent.EXTRA_TEXT, json);
            startActivity(Intent.createChooser(share, "分享数据报告"));
        } catch (Exception e) {
            Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
