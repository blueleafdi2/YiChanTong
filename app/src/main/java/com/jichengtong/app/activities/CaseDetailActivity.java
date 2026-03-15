package com.jichengtong.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.jichengtong.app.R;
import com.jichengtong.app.data.DataProvider;
import com.jichengtong.app.models.CourtCase;
import com.jichengtong.app.models.LawArticle;
import com.jichengtong.app.utils.FavoritesManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaseDetailActivity extends AppCompatActivity {
    private FavoritesManager favMgr;
    private String caseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_detail);
        favMgr = FavoritesManager.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_right);
        toolbar.getNavigationIcon().setAutoMirrored(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        caseId = getIntent().getStringExtra("case_id");
        if (caseId == null) { finish(); return; }

        CourtCase courtCase = DataProvider.getInstance(this).getCourtCaseById(caseId);
        if (courtCase == null) { finish(); return; }

        favMgr.addHistory("case", caseId, courtCase.getTitle());
        toolbar.setTitle("案例详情");

        ((TextView) findViewById(R.id.case_title)).setText(courtCase.getTitle());
        ((TextView) findViewById(R.id.case_number)).setText("案号：" + courtCase.getCaseNumber());
        ((TextView) findViewById(R.id.case_court)).setText("审理法院：" + courtCase.getCourt());
        ((TextView) findViewById(R.id.case_date)).setText("审判日期：" + courtCase.getJudgeDate());
        ((TextView) findViewById(R.id.case_summary)).setText(courtCase.getCaseSummary());
        ((TextView) findViewById(R.id.case_judgment)).setText(courtCase.getJudgment());

        LinearLayout tagsContainer = findViewById(R.id.case_tags_container);
        if (courtCase.getTags() != null) {
            for (String tag : courtCase.getTags()) {
                TextView tagView = new TextView(this);
                tagView.setText(tag);
                tagView.setTextSize(11);
                tagView.setTextColor(0xFF0D47A1);
                tagView.setBackgroundResource(R.drawable.bg_tag);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMarginEnd(8);
                params.setMargins(0, 0, 8, 8);
                tagView.setLayoutParams(params);
                tagsContainer.addView(tagView);
            }
        }

        TextView legalBasisTv = findViewById(R.id.case_legal_basis);
        if (courtCase.getLegalBasis() != null && !courtCase.getLegalBasis().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String basis : courtCase.getLegalBasis()) {
                sb.append("• ").append(basis).append("\n");
            }
            String basisText = sb.toString().trim();
            SpannableString ss = new SpannableString(basisText);
            Pattern p = Pattern.compile("第[一二三四五六七八九十百千零\\d]+条");
            Matcher m = p.matcher(basisText);
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                String matched = m.group();
                String articleId = chineseToArticleId(matched);
                if (articleId != null) {
                    final String fId = articleId;
                    ss.setSpan(new ClickableSpan() {
                        @Override public void onClick(@NonNull View w) {
                            LawArticle law = DataProvider.getInstance(CaseDetailActivity.this).getLawArticleById(fId);
                            if (law != null) {
                                Intent li = new Intent(CaseDetailActivity.this, LawDetailActivity.class);
                                li.putExtra("law_id", fId);
                                startActivity(li);
                            } else {
                                Intent wi = new Intent(CaseDetailActivity.this, WebViewActivity.class);
                                wi.putExtra("url", "https://flk.npc.gov.cn/detail2.html?ZmY4MDgxODE3NTJiN2Q0MzAxNzVlNDc2NmJhYjA5Zjk%3D");
                                wi.putExtra("title", "《民法典》官方原文");
                                startActivity(wi);
                            }
                        }
                        @Override public void updateDrawState(@NonNull TextPaint ds) {
                            ds.setColor(0xFF1565C0);
                            ds.setUnderlineText(true);
                            ds.setFakeBoldText(true);
                        }
                    }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            legalBasisTv.setText(ss);
            legalBasisTv.setMovementMethod(LinkMovementMethod.getInstance());
        }
        ((TextView) findViewById(R.id.case_ruling_gist)).setText(courtCase.getRulingGist());
        ((TextView) findViewById(R.id.case_source)).setText("来源：" + courtCase.getSource());

        MaterialButton btnCollect = findViewById(R.id.btn_collect);
        updateFavButton(btnCollect);
        btnCollect.setOnClickListener(v -> {
            if (favMgr.isFavorite("case", caseId)) {
                favMgr.removeFavorite("case", caseId);
                Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show();
            } else {
                favMgr.addFavorite("case", caseId, courtCase.getTitle());
                Toast.makeText(this, "已收藏", Toast.LENGTH_SHORT).show();
            }
            updateFavButton(btnCollect);
        });

        findViewById(R.id.btn_note).setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("输入笔记内容...");
            input.setText(favMgr.getNote("case", caseId));
            input.setMinLines(3);
            input.setPadding(48, 32, 48, 32);
            new AlertDialog.Builder(this)
                    .setTitle("添加笔记")
                    .setView(input)
                    .setPositiveButton("保存", (d, w) -> {
                        String note = input.getText().toString().trim();
                        if (!note.isEmpty()) {
                            favMgr.saveNote("case", caseId, courtCase.getTitle(), note);
                            Toast.makeText(this, "笔记已保存", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    private void updateFavButton(MaterialButton btn) {
        btn.setText(favMgr.isFavorite("case", caseId) ? "★ 已收藏" : "☆ 收藏");
    }

    private String chineseToArticleId(String chinese) {
        DataProvider dp = DataProvider.getInstance(this);
        for (LawArticle law : dp.getLawArticles()) {
            if (chinese.equals(law.getArticle())) return law.getId();
        }
        Pattern numP = Pattern.compile("第(\\d+)条");
        Matcher numM = numP.matcher(chinese);
        if (numM.find()) return numM.group(1);
        return null;
    }
}
