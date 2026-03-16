package com.jichengtong.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.jichengtong.app.R;
import com.jichengtong.app.data.DataProvider;
import com.jichengtong.app.models.GlossaryItem;
import com.jichengtong.app.models.LawArticle;
import com.jichengtong.app.utils.FavoritesManager;
import com.jichengtong.app.utils.GlossaryHelper;
import java.util.List;

public class LawDetailActivity extends AppCompatActivity {
    private FavoritesManager favMgr;
    private String lawId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_law_detail);
        favMgr = FavoritesManager.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_right);
        toolbar.getNavigationIcon().setAutoMirrored(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        lawId = getIntent().getStringExtra("law_id");
        if (lawId == null) { finish(); return; }
        com.jichengtong.app.utils.Analytics.getInstance(this).logContentView("law", lawId);

        LawArticle law = DataProvider.getInstance(this).getLawArticleById(lawId);
        if (law == null) { finish(); return; }

        favMgr.addHistory("law", lawId, law.getTitle());
        toolbar.setTitle(law.getTitle());

        ((TextView) findViewById(R.id.law_title)).setText(law.getTitle());

        List<String> keywords = law.getKeywords();
        TextView plainTv = findViewById(R.id.plain_explanation);
        plainTv.setText(highlightWithGlossary(law.getPlainExplanation(), keywords));
        plainTv.setMovementMethod(LinkMovementMethod.getInstance());

        TextView lifeTv = findViewById(R.id.life_example);
        lifeTv.setText(highlightWithGlossary(law.getLifeExample(), keywords));
        lifeTv.setMovementMethod(LinkMovementMethod.getInstance());

        String lawRef = "《民法典》" + law.getArticle();
        String originalWithRef = lawRef + "\n" + law.getOriginalText();
        TextView origTv = findViewById(R.id.original_text);
        origTv.setText(highlightWithGlossary(originalWithRef, keywords));
        origTv.setMovementMethod(LinkMovementMethod.getInstance());

        findViewById(R.id.btn_view_official).setOnClickListener(v -> {
            Intent webIntent = new Intent(this, WebViewActivity.class);
            webIntent.putExtra("html_content", generateOfficialHtml(law));
            webIntent.putExtra("title", "《民法典》" + law.getArticle() + " 官方原文");
            startActivity(webIntent);
        });

        ((TextView) findViewById(R.id.judicial_interpretation)).setText(
                law.getJudicialInterpretation() != null ? law.getJudicialInterpretation() : "暂无相关司法解释");
        ((TextView) findViewById(R.id.legislative_history)).setText(
                law.getLegislativeHistory() != null ? law.getLegislativeHistory() : "暂无立法沿革信息");

        MaterialButton btnCollect = findViewById(R.id.btn_collect);
        updateFavButton(btnCollect);
        btnCollect.setOnClickListener(v -> {
            if (favMgr.isFavorite("law", lawId)) {
                favMgr.removeFavorite("law", lawId);
                Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show();
            } else {
                favMgr.addFavorite("law", lawId, law.getTitle());
                Toast.makeText(this, "已收藏", Toast.LENGTH_SHORT).show();
            }
            updateFavButton(btnCollect);
        });

        findViewById(R.id.btn_note).setOnClickListener(v -> showNoteDialog(law.getTitle()));
    }

    private void updateFavButton(MaterialButton btn) {
        btn.setText(favMgr.isFavorite("law", lawId) ? "★ 已收藏" : "☆ 收藏");
    }

    private void showNoteDialog(String title) {
        EditText input = new EditText(this);
        input.setHint("输入笔记内容...");
        input.setText(favMgr.getNote("law", lawId));
        input.setMinLines(3);
        input.setPadding(48, 32, 48, 32);
        new AlertDialog.Builder(this)
                .setTitle("添加笔记 - " + title)
                .setView(input)
                .setPositiveButton("保存", (d, w) -> {
                    String note = input.getText().toString().trim();
                    if (!note.isEmpty()) {
                        favMgr.saveNote("law", lawId, title, note);
                        Toast.makeText(this, "笔记已保存", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private String generateOfficialHtml(LawArticle law) {
        String escapedText = law.getOriginalText().replace("\n", "<br/>");
        return "<!DOCTYPE html><html><head><meta charset='utf-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<style>" +
            "body{font-family:-apple-system,sans-serif;margin:0;padding:16px;background:#FFFDE7;color:#212121;}" +
            ".source{background:#1B5E20;color:white;padding:12px 16px;border-radius:8px;margin-bottom:16px;font-size:13px;line-height:1.6;}" +
            ".source a{color:#FFD54F;text-decoration:underline;}" +
            ".article-box{background:white;border-radius:12px;padding:20px;box-shadow:0 2px 8px rgba(0,0,0,0.1);}" +
            ".article-num{font-size:18px;font-weight:bold;color:#1B5E20;margin-bottom:12px;border-bottom:2px solid #1B5E20;padding-bottom:8px;}" +
            ".article-title{font-size:15px;color:#666;margin-bottom:16px;}" +
            ".article-text{font-size:17px;line-height:2;color:#333;text-align:justify;}" +
            ".verify-btn{display:block;text-align:center;background:#1B5E20;color:white;padding:14px;border-radius:8px;margin-top:20px;text-decoration:none;font-size:15px;font-weight:bold;}" +
            ".footer{text-align:center;color:#999;font-size:12px;margin-top:20px;line-height:1.6;}" +
            "</style></head><body>" +
            "<div class='source'>" +
            "<strong>📜 来源</strong><br/>" +
            "《中华人民共和国民法典》第六编 继承<br/>" +
            "2020年5月28日第十三届全国人民代表大会第三次会议通过<br/>" +
            "2021年1月1日起施行<br/>" +
            "<a href='https://flk.npc.gov.cn/detail2.html?ZmY4MDgxODE3NTJiN2Q0MzAxNzVlNDc2NmJhYjA5Zjk%3D'>国家法律法规数据库 ›</a>" +
            "</div>" +
            "<div class='article-box'>" +
            "<div class='article-num'>《民法典》" + law.getArticle() + "</div>" +
            "<div class='article-title'>" + law.getTitle() + " | " + law.getChapter() + "</div>" +
            "<div class='article-text'>" + escapedText + "</div>" +
            "</div>" +
            "<a class='verify-btn' href='https://flk.npc.gov.cn/detail2.html?ZmY4MDgxODE3NTJiN2Q0MzAxNzVlNDc2NmJhYjA5Zjk%3D'>" +
            "在国家法律法规数据库查看完整法律全文 ›" +
            "</a>" +
            "<div class='footer'>本页内容摘自全国人民代表大会官方发布的《中华人民共和国民法典》<br/>仅供参考，以官方发布为准</div>" +
            "</body></html>";
    }

    private SpannableStringBuilder highlightWithGlossary(String text, List<String> keywords) {
        SpannableStringBuilder sb = highlightKeywords(text, keywords);
        DataProvider dp = DataProvider.getInstance(this);
        String str = sb.toString();
        for (GlossaryItem g : dp.getGlossary()) {
            String term = g.getTerm();
            if (term.length() < 3) continue;
            if (!"hard".equals(g.getDifficulty())) continue;
            int idx = str.indexOf(term);
            if (idx < 0) continue;
            final GlossaryItem item = g;
            sb.setSpan(new ClickableSpan() {
                @Override public void onClick(@NonNull View w) {
                    GlossaryHelper.showTermDialog(LawDetailActivity.this, item);
                }
                @Override public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(0xFF6A1B9A);
                    ds.setUnderlineText(true);
                }
            }, idx, idx + term.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return sb;
    }

    private SpannableStringBuilder highlightKeywords(String text, List<String> keywords) {
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        if (keywords == null || keywords.isEmpty()) return sb;
        for (String keyword : keywords) {
            int start = 0;
            while (start < sb.length()) {
                int idx = sb.toString().indexOf(keyword, start);
                if (idx == -1) break;
                sb.setSpan(new ForegroundColorSpan(Color.parseColor("#C62828")),
                        idx, idx + keyword.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new StyleSpan(Typeface.BOLD),
                        idx, idx + keyword.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                start = idx + keyword.length();
            }
        }
        return sb;
    }
}
