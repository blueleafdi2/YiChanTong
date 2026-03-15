package com.jichengtong.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.jichengtong.app.R;
import com.jichengtong.app.data.DataProvider;
import com.jichengtong.app.models.ToolItem;
import com.jichengtong.app.utils.LawLinkHelper;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ToolsDetailActivity extends AppCompatActivity {

    private static final int REQUEST_SAVE_DOC = 1001;
    private String pendingHtml;
    private ToolItem currentTool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_right);
        toolbar.getNavigationIcon().setAutoMirrored(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        String toolId = getIntent().getStringExtra("tool_id");
        if (toolId == null) { finish(); return; }

        DataProvider dp = DataProvider.getInstance(this);
        currentTool = dp.getToolById(toolId);
        if (currentTool == null) { finish(); return; }

        toolbar.setTitle(currentTool.getTitle());
        ((TextView) findViewById(R.id.tool_title)).setText(currentTool.getIcon() + " " + currentTool.getTitle());

        TextView contentView = findViewById(R.id.tool_content);
        String cleaned = cleanMarkdown(currentTool.getContent());
        contentView.setText(LawLinkHelper.linkifyLawReferences(this, cleaned, dp));
        LawLinkHelper.enableLinkClicks(contentView);

        MaterialButton exportBtn = findViewById(R.id.btn_export_word);
        exportBtn.setOnClickListener(v -> exportToWord());
    }

    private void exportToWord() {
        pendingHtml = generateWordHtml(currentTool);
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/msword");
        intent.putExtra(Intent.EXTRA_TITLE, currentTool.getTitle() + ".doc");
        startActivityForResult(intent, REQUEST_SAVE_DOC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SAVE_DOC && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;
            try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                if (os != null) {
                    os.write(pendingHtml.getBytes(StandardCharsets.UTF_8));
                    Toast.makeText(this, "✅ 文档已保存", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String cleanMarkdown(String md) {
        if (md == null) return "";
        StringBuilder sb = new StringBuilder();
        boolean inCodeBlock = false;
        for (String line : md.split("\n")) {
            if (line.trim().startsWith("```")) {
                inCodeBlock = !inCodeBlock;
                continue;
            }
            if (inCodeBlock) {
                sb.append("    ").append(line).append("\n");
                continue;
            }
            String cleaned = line;
            if (cleaned.startsWith("## ")) cleaned = cleaned.substring(3);
            else if (cleaned.startsWith("### ")) cleaned = cleaned.substring(4);
            cleaned = cleaned.replace("**", "");
            if (cleaned.trim().equals("---")) {
                sb.append("──────────────────\n");
                continue;
            }
            sb.append(cleaned).append("\n");
        }
        return sb.toString().trim();
    }

    private String generateWordHtml(ToolItem tool) {
        StringBuilder html = new StringBuilder();
        html.append("<html xmlns:o='urn:schemas-microsoft-com:office:office' ")
            .append("xmlns:w='urn:schemas-microsoft-com:office:word' ")
            .append("xmlns='http://www.w3.org/TR/REC-html40'>\n<head>\n")
            .append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>\n")
            .append("<!--[if gte mso 9]><xml><w:WordDocument>")
            .append("<w:View>Print</w:View></w:WordDocument></xml><![endif]-->\n")
            .append("<style>\n")
            .append("body{font-family:SimSun,STSong,'宋体',serif;font-size:12pt;line-height:1.8;margin:2cm;}\n")
            .append("h1{font-size:20pt;text-align:center;margin-bottom:16pt;}\n")
            .append("h2{font-size:16pt;border-bottom:1px solid #333;padding-bottom:4pt;margin-top:16pt;}\n")
            .append("h3{font-size:14pt;margin-top:12pt;}\n")
            .append("table{border-collapse:collapse;width:100%;margin:8pt 0;}\n")
            .append("td,th{border:1px solid #333;padding:4pt 6pt;font-size:11pt;}\n")
            .append("th{background-color:#f0f0f0;font-weight:bold;}\n")
            .append("pre{background:#f5f5f5;padding:10pt;font-family:'Courier New',monospace;")
            .append("font-size:10pt;white-space:pre-wrap;border:1px solid #ddd;}\n")
            .append("code{font-family:'Courier New',monospace;background:#f0f0f0;padding:1pt 3pt;}\n")
            .append(".footer{text-align:center;color:#999;font-size:9pt;margin-top:2cm;border-top:1px solid #eee;padding-top:12pt;}\n")
            .append("</style>\n</head>\n<body>\n");

        html.append(markdownToHtml(tool.getContent()));

        html.append("<div class='footer'>")
            .append("<p>本文档由「遗产通」App 生成</p>")
            .append("<p>法律依据：《中华人民共和国民法典》继承编</p>")
            .append("<p>仅供参考，具体问题请咨询执业律师</p>")
            .append("</div>\n</body>\n</html>");

        return html.toString();
    }

    private String markdownToHtml(String md) {
        if (md == null) return "";
        StringBuilder html = new StringBuilder();
        String[] lines = md.split("\n");
        boolean inTable = false;
        boolean inCodeBlock = false;
        boolean inList = false;
        boolean isFirstTableRow = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.trim().startsWith("```")) {
                if (inCodeBlock) {
                    html.append("</pre>\n");
                    inCodeBlock = false;
                } else {
                    if (inList) { html.append("</ul>\n"); inList = false; }
                    html.append("<pre>");
                    inCodeBlock = true;
                }
                continue;
            }
            if (inCodeBlock) {
                html.append(escapeHtml(line)).append("\n");
                continue;
            }

            if (line.trim().equals("---")) {
                if (inList) { html.append("</ul>\n"); inList = false; }
                if (inTable) { html.append("</table>\n"); inTable = false; }
                html.append("<hr>\n");
                continue;
            }

            if (line.startsWith("### ")) {
                if (inList) { html.append("</ul>\n"); inList = false; }
                if (inTable) { html.append("</table>\n"); inTable = false; }
                html.append("<h3>").append(formatInline(line.substring(4))).append("</h3>\n");
                continue;
            }
            if (line.startsWith("## ")) {
                if (inList) { html.append("</ul>\n"); inList = false; }
                if (inTable) { html.append("</table>\n"); inTable = false; }
                html.append("<h2>").append(formatInline(line.substring(3))).append("</h2>\n");
                continue;
            }

            if (line.contains("|") && line.trim().startsWith("|")) {
                if (inList) { html.append("</ul>\n"); inList = false; }
                if (!inTable) {
                    html.append("<table>\n");
                    inTable = true;
                    isFirstTableRow = true;
                }
                if (line.trim().matches("\\|[-\\s|:]+\\|?")) {
                    continue;
                }
                String tag = isFirstTableRow ? "th" : "td";
                html.append("<tr>");
                String[] cells = line.split("\\|");
                for (String cell : cells) {
                    cell = cell.trim();
                    if (!cell.isEmpty()) {
                        html.append("<").append(tag).append(">")
                            .append(formatInline(cell))
                            .append("</").append(tag).append(">");
                    }
                }
                html.append("</tr>\n");
                isFirstTableRow = false;
                continue;
            } else if (inTable) {
                html.append("</table>\n");
                inTable = false;
            }

            String trimmed = line.trim();
            if (trimmed.startsWith("- ") || trimmed.startsWith("□ ") || trimmed.matches("^\\d+\\.\\s.*")) {
                if (!inList) { html.append("<ul>\n"); inList = true; }
                String content;
                if (trimmed.startsWith("- ")) content = trimmed.substring(2);
                else if (trimmed.startsWith("□ ")) content = "☐ " + trimmed.substring(2);
                else content = trimmed.replaceFirst("^\\d+\\.\\s", "");
                html.append("<li>").append(formatInline(content)).append("</li>\n");
                continue;
            } else if (inList && trimmed.isEmpty()) {
                html.append("</ul>\n");
                inList = false;
            }

            if (trimmed.isEmpty()) {
                html.append("<br>\n");
                continue;
            }

            html.append("<p>").append(formatInline(line)).append("</p>\n");
        }

        if (inTable) html.append("</table>\n");
        if (inList) html.append("</ul>\n");
        if (inCodeBlock) html.append("</pre>\n");
        return html.toString();
    }

    private String formatInline(String text) {
        text = text.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
        text = text.replaceAll("`(.+?)`", "<code>$1</code>");
        return text;
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
