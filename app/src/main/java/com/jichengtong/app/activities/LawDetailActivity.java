package com.jichengtong.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.jichengtong.app.R;
import com.jichengtong.app.data.DataProvider;
import com.jichengtong.app.models.LawArticle;
import com.jichengtong.app.utils.FavoritesManager;
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

        LawArticle law = DataProvider.getInstance(this).getLawArticleById(lawId);
        if (law == null) { finish(); return; }

        favMgr.addHistory("law", lawId, law.getTitle());
        toolbar.setTitle(law.getTitle());

        ((TextView) findViewById(R.id.law_title)).setText(law.getTitle());

        List<String> keywords = law.getKeywords();
        ((TextView) findViewById(R.id.plain_explanation)).setText(highlightKeywords(law.getPlainExplanation(), keywords));
        ((TextView) findViewById(R.id.life_example)).setText(highlightKeywords(law.getLifeExample(), keywords));

        String lawRef = "《民法典》" + law.getArticle();
        String originalWithRef = lawRef + "\n\n" + law.getOriginalText();
        ((TextView) findViewById(R.id.original_text)).setText(highlightKeywords(originalWithRef, keywords));

        findViewById(R.id.btn_view_official).setOnClickListener(v -> {
            String officialUrl = "https://flk.npc.gov.cn/detail2.html?ZmY4MDgxODE3NTJiN2Q0MzAxNzVlNDc2NmJhYjA5Zjk%3D";
            Intent webIntent = new Intent(this, WebViewActivity.class);
            webIntent.putExtra("url", officialUrl);
            webIntent.putExtra("title", "《民法典》官方原文 - " + law.getArticle());
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
