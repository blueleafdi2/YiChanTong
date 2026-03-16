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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.jichengtong.app.R;
import com.jichengtong.app.data.DataProvider;
import com.jichengtong.app.models.CourtCase;
import com.jichengtong.app.models.GlossaryItem;
import com.jichengtong.app.models.LawArticle;
import com.jichengtong.app.utils.FavoritesManager;
import com.jichengtong.app.utils.GlossaryHelper;
import com.jichengtong.app.utils.LawLinkHelper;
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
        com.jichengtong.app.utils.Analytics.getInstance(this).logContentView("case", caseId);

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

        ChipGroup tagsContainer = findViewById(R.id.case_tags_container);
        if (courtCase.getTags() != null) {
            for (String tag : courtCase.getTags()) {
                Chip chip = new Chip(this);
                chip.setText(tag);
                chip.setTextSize(11);
                chip.setTextColor(0xFF0D47A1);
                chip.setChipBackgroundColorResource(R.color.primary_container);
                chip.setChipStrokeColorResource(R.color.primary);
                chip.setChipStrokeWidth(1f);
                GlossaryItem gi = GlossaryHelper.findTerm(this, tag);
                chip.setClickable(gi != null);
                chip.setCheckable(false);
                if (gi != null) {
                    chip.setChipIconResource(R.drawable.ic_help);
                    chip.setChipIconTintResource(R.color.primary);
                    chip.setOnClickListener(v -> GlossaryHelper.showTermDialog(this, gi));
                }
                chip.setEnsureMinTouchTargetSize(false);
                chip.setChipMinHeight(0);
                tagsContainer.addView(chip);
            }
        }

        TextView legalBasisTv = findViewById(R.id.case_legal_basis);
        if (courtCase.getLegalBasis() != null && !courtCase.getLegalBasis().isEmpty()) {
            DataProvider dp = DataProvider.getInstance(this);
            legalBasisTv.setText(LawLinkHelper.linkifyLawList(this, courtCase.getLegalBasis(), dp));
            LawLinkHelper.enableLinkClicks(legalBasisTv);
        }
        ((TextView) findViewById(R.id.case_ruling_gist)).setText(courtCase.getRulingGist());

        String sourceText = "来源：" + courtCase.getSource() + "\n案号：" + courtCase.getCaseNumber();
        if (courtCase.getSourceUrl() != null) {
            sourceText += "\n网址：" + courtCase.getSourceUrl();
        }
        ((TextView) findViewById(R.id.case_source)).setText(sourceText);

        MaterialButton btnSource = findViewById(R.id.btn_view_source);
        if (courtCase.getSourceUrl() != null && !courtCase.getSourceUrl().isEmpty()) {
            btnSource.setOnClickListener(v -> {
                Intent webIntent = new Intent(this, WebViewActivity.class);
                webIntent.putExtra("html_content", generateSourceHtml(courtCase));
                webIntent.putExtra("title", courtCase.getSource() + " - 官方来源");
                startActivity(webIntent);
            });
        } else {
            btnSource.setVisibility(View.GONE);
        }

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

    private String generateSourceHtml(CourtCase c) {
        String url = c.getSourceUrl() != null ? c.getSourceUrl() : "";
        String searchUrl = url;
        if (url.contains("wenshu.court.gov.cn")) {
            searchUrl = "https://wenshu.court.gov.cn/website/wenshu/181107ANFZ0BXSK4/index.html?docId=" + c.getCaseNumber();
        }
        return "<!DOCTYPE html><html><head><meta charset='utf-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no'>" +
            "<style>" +
            "body{font-family:-apple-system,sans-serif;margin:0;padding:16px;background:#FFFDE7;color:#212121;font-size:16px;line-height:1.6;}" +
            ".header{background:#E65100;color:white;padding:16px;border-radius:12px;margin-bottom:16px;}" +
            ".header h2{margin:0 0 8px 0;font-size:18px;}" +
            ".header p{margin:4px 0;font-size:13px;opacity:0.9;}" +
            ".info-box{background:white;border-radius:12px;padding:16px;margin-bottom:12px;box-shadow:0 1px 4px rgba(0,0,0,0.1);}" +
            ".info-box h3{margin:0 0 8px 0;font-size:16px;color:#1B5E20;}" +
            ".info-row{display:flex;margin:6px 0;font-size:14px;}" +
            ".info-label{color:#666;min-width:70px;}" +
            ".info-value{color:#333;font-weight:500;}" +
            ".btn{display:block;text-align:center;padding:16px;border-radius:10px;margin:8px 0;text-decoration:none;font-size:16px;font-weight:bold;}" +
            ".btn-primary{background:#1B5E20;color:white;}" +
            ".btn-secondary{background:#E65100;color:white;}" +
            ".note{background:#FFF3E0;padding:12px 16px;border-radius:8px;margin-top:16px;font-size:13px;color:#E65100;line-height:1.6;}" +
            ".footer{text-align:center;color:#999;font-size:12px;margin-top:16px;}" +
            "</style></head><body>" +
            "<div class='header'>" +
            "<h2>🔗 " + c.getSource() + "</h2>" +
            "<p>案号：" + c.getCaseNumber() + "</p>" +
            "<p>法院：" + c.getCourt() + "</p>" +
            "</div>" +
            "<div class='info-box'>" +
            "<h3>📋 案件信息</h3>" +
            "<div class='info-row'><span class='info-label'>案件名称</span><span class='info-value'>" + c.getTitle() + "</span></div>" +
            "<div class='info-row'><span class='info-label'>审判日期</span><span class='info-value'>" + c.getJudgeDate() + "</span></div>" +
            "<div class='info-row'><span class='info-label'>案件类型</span><span class='info-value'>" + c.getCaseType() + "</span></div>" +
            "<div class='info-row'><span class='info-label'>数据来源</span><span class='info-value'>" + c.getSource() + "</span></div>" +
            "</div>" +
            "<a class='btn btn-primary' href='" + url + "'>前往 " + c.getSource() + " 官网 ›</a>" +
            "<a class='btn btn-secondary' href='" + searchUrl + "'>搜索本案裁判文书 ›</a>" +
            "<div class='note'>" +
            "💡 <strong>温馨提示</strong><br/>" +
            "• 部分裁判文书需在官网注册后查看全文<br/>" +
            "• 可在官网搜索栏输入案号「" + c.getCaseNumber() + "」查找<br/>" +
            "• 如遇页面加载缓慢，建议使用浏览器直接访问官网" +
            "</div>" +
            "<div class='footer'>本案例数据摘录自公开司法裁判文书<br/>以官方数据库原文为准</div>" +
            "</body></html>";
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
