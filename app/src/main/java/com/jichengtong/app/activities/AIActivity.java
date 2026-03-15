package com.jichengtong.app.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.jichengtong.app.R;
import com.jichengtong.app.data.DataProvider;
import com.jichengtong.app.models.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIActivity extends AppCompatActivity {

    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private static final String MODEL = "deepseek-chat";

    private static final String SYSTEM_PROMPT =
            "你是「遗产通」App的AI法律助手，专精中国遗产继承法律领域。回答规则：\n" +
            "1. 基于《民法典》继承编（第1119条-第1163条）及相关司法解释回答问题\n" +
            "2. 必须引用具体法条编号，格式为【第XXXX条】，例如【第1121条】【第1127条】\n" +
            "3. 用通俗白话解释法律概念，让非法律专业人士也能理解\n" +
            "4. 如果涉及的问题可以关联到特定类型案例，请用【案例:关键词】标记，例如【案例:房产继承】【案例:遗嘱效力】\n" +
            "5. 每次回答结尾提醒：具体问题建议咨询执业律师\n" +
            "6. 保持专业但亲切的语气，回答控制在500字以内";

    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;
    private EditText inputMessage;
    private View btnSend;
    private DataProvider dataProvider;

    private static final String API_KEY = "sk-fcd3d027f43b4113bee5ab9b9c3551c0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

        dataProvider = DataProvider.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("AI 法律助手");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_right);
        toolbar.getNavigationIcon().setAutoMirrored(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView chatRv = findViewById(R.id.chat_rv);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        chatRv.setLayoutManager(lm);
        adapter = new ChatAdapter(this, messages, dataProvider);
        chatRv.setAdapter(adapter);

        inputMessage = findViewById(R.id.input_message);
        btnSend = findViewById(R.id.btn_send);

        messages.add(new ChatMessage("assistant",
                "您好！我是遗产通AI法律助手（DeepSeek驱动），专精中国遗产继承法律。\n\n" +
                "您可以问我任何继承相关问题，例如：\n" +
                "• 父母去世后房产怎么继承？\n" +
                "• 自书遗嘱需要什么条件才有效？\n" +
                "• 独生子女能继承全部遗产吗？\n" +
                "• 数字资产（加密货币）能继承吗？\n\n" +
                "我的回答会关联App内的法条和案例，点击即可查看详情。"));
        adapter.notifyItemInserted(0);

        btnSend.setOnClickListener(v -> sendMessage());

        String presetQuestion = getIntent().getStringExtra("question");
        if (presetQuestion != null && !presetQuestion.isEmpty()) {
            inputMessage.setText(presetQuestion);
            sendMessage();
        }
    }

    private void sendMessage() {
        String text = inputMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        messages.add(new ChatMessage("user", text));
        adapter.notifyItemInserted(messages.size() - 1);
        inputMessage.setText("");
        scrollToBottom();

        messages.add(new ChatMessage("assistant", "正在思考中..."));
        int loadingIdx = messages.size() - 1;
        adapter.notifyItemInserted(loadingIdx);
        scrollToBottom();
        btnSend.setEnabled(false);

        new Thread(() -> {
            String response = callDeepSeekAPI(text);
            List<RelatedItem> related = findRelatedContent(response);
            runOnUiThread(() -> {
                ChatMessage msg = messages.get(loadingIdx);
                msg.content = response;
                msg.relatedItems = related;
                adapter.notifyItemChanged(loadingIdx);
                btnSend.setEnabled(true);
                scrollToBottom();
            });
        }).start();
    }

    private void scrollToBottom() {
        RecyclerView rv = findViewById(R.id.chat_rv);
        rv.postDelayed(() -> rv.scrollToPosition(messages.size() - 1), 100);
    }

    private String callDeepSeekAPI(String userMessage) {
        try {
            JSONArray msgArray = new JSONArray();
            msgArray.put(new JSONObject().put("role", "system").put("content", SYSTEM_PROMPT));

            int start = Math.max(0, messages.size() - 11);
            for (int i = start; i < messages.size() - 1; i++) {
                ChatMessage m = messages.get(i);
                if ("正在思考中...".equals(m.content)) continue;
                msgArray.put(new JSONObject().put("role", m.role).put("content", m.content));
            }

            JSONObject body = new JSONObject();
            body.put("model", MODEL);
            body.put("messages", msgArray);
            body.put("max_tokens", 2000);
            body.put("temperature", 0.7);
            body.put("stream", false);

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            BufferedReader reader;
            if (code >= 200 && code < 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            }

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            if (code >= 200 && code < 300) {
                JSONObject resp = new JSONObject(sb.toString());
                return resp.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            } else if (code == 401) {
                return "API Key 无效或已过期，请重新配置。\n\n访问 platform.deepseek.com 获取新的 API Key。";
            } else {
                return "AI服务暂时不可用（错误码" + code + "）。\n\n请稍后重试，或直接使用App内的法律库和案例库查询。";
            }
        } catch (Exception e) {
            return "网络连接失败，请检查网络后重试。\n\n您也可以：\n• 使用App内的法律库查询法条\n• 在案例库搜索相关判例\n• 拨打12348免费法律热线";
        }
    }

    private List<RelatedItem> findRelatedContent(String response) {
        List<RelatedItem> items = new ArrayList<>();
        Set<String> addedIds = new HashSet<>();

        // Match 【第XXXX条】 or 第XXXX条 (article number patterns)
        Pattern articlePattern = Pattern.compile("第(\\d{4})条");
        Matcher matcher = articlePattern.matcher(response);
        while (matcher.find()) {
            String articleNum = matcher.group(1);
            LawArticle law = dataProvider.getLawArticleById(articleNum);
            if (law != null && addedIds.add("law_" + articleNum)) {
                items.add(new RelatedItem("law", law.getId(), law.getTitle(),
                        "《民法典》" + law.getArticle()));
            }
        }

        // Match Chinese number article references: 第一千一百XX条
        for (LawArticle law : dataProvider.getLawArticles()) {
            if (response.contains(law.getArticle()) && addedIds.add("law_" + law.getId())) {
                items.add(new RelatedItem("law", law.getId(), law.getTitle(),
                        "《民法典》" + law.getArticle()));
            }
        }

        // Match 【案例:关键词】 patterns
        Pattern casePattern = Pattern.compile("【案例[:：](.+?)】");
        Matcher caseMatcher = casePattern.matcher(response);
        while (caseMatcher.find()) {
            String keyword = caseMatcher.group(1).trim();
            for (CourtCase c : dataProvider.getCourtCases()) {
                if (addedIds.size() >= 6) break;
                boolean match = c.getTitle().contains(keyword) ||
                        c.getCaseType().contains(keyword) ||
                        (c.getTags() != null && c.getTags().stream().anyMatch(t -> t.contains(keyword)));
                if (match && addedIds.add("case_" + c.getId())) {
                    items.add(new RelatedItem("case", c.getId(), c.getTitle(), c.getCourt()));
                    break;
                }
            }
        }

        // Keyword-based matching from law keywords
        for (LawArticle law : dataProvider.getLawArticles()) {
            if (addedIds.size() >= 6) break;
            if (law.getKeywords() != null) {
                for (String kw : law.getKeywords()) {
                    if (kw.length() >= 2 && response.contains(kw) && addedIds.add("law_" + law.getId())) {
                        items.add(new RelatedItem("law", law.getId(), law.getTitle(),
                                "《民法典》" + law.getArticle()));
                        break;
                    }
                }
            }
        }

        if (items.size() > 6) return items.subList(0, 6);
        return items;
    }

    static class ChatMessage {
        String role;
        String content;
        List<RelatedItem> relatedItems;
        ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    static class RelatedItem {
        String type;
        String id;
        String title;
        String subtitle;
        RelatedItem(String type, String id, String title, String subtitle) {
            this.type = type;
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {
        private final Context context;
        private final List<ChatMessage> items;
        private final DataProvider dataProvider;

        ChatAdapter(Context context, List<ChatMessage> items, DataProvider dataProvider) {
            this.context = context;
            this.items = items;
            this.dataProvider = dataProvider;
        }

        @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_message, parent, false));
        }

        @Override public void onBindViewHolder(VH holder, int position) {
            ChatMessage msg = items.get(position);
            boolean isUser = "user".equals(msg.role);

            holder.role.setText(isUser ? "我" : "🤖 AI 法律助手");
            holder.role.setGravity(isUser ? Gravity.END : Gravity.START);

            if (isUser) {
                holder.content.setText(msg.content);
                holder.content.setMovementMethod(null);
            } else {
                SpannableStringBuilder ssb = buildClickableContent(msg.content);
                holder.content.setText(ssb);
                holder.content.setMovementMethod(LinkMovementMethod.getInstance());
            }

            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) holder.card.getLayoutParams();
            if (isUser) {
                mlp.setMarginStart(dpToPx(60));
                mlp.setMarginEnd(0);
            } else {
                mlp.setMarginStart(0);
                mlp.setMarginEnd(dpToPx(60));
            }
            holder.card.setLayoutParams(mlp);
            ((LinearLayout) holder.itemView).setGravity(isUser ? Gravity.END : Gravity.START);
            holder.card.setCardBackgroundColor(isUser ? 0xFFE8F5E9 : 0xFFFFFFFF);

            holder.relatedContainer.removeAllViews();
            holder.relatedContainer.setVisibility(View.GONE);

            if (!isUser && msg.relatedItems != null && !msg.relatedItems.isEmpty()) {
                holder.relatedContainer.setVisibility(View.VISIBLE);

                TextView header = new TextView(context);
                header.setText("📎 相关内容（点击查看详情）");
                header.setTextSize(12);
                header.setTextColor(0xFF757575);
                header.setPadding(0, dpToPx(8), 0, dpToPx(4));
                holder.relatedContainer.addView(header);

                for (RelatedItem item : msg.relatedItems) {
                    Chip chip = new Chip(context);
                    String label = ("law".equals(item.type) ? "📜 " : "⚖️ ") + item.title;
                    chip.setText(label);
                    chip.setTextSize(12);
                    chip.setChipBackgroundColorResource(
                            "law".equals(item.type) ? R.color.primary_container : R.color.secondary_container);
                    chip.setOnClickListener(v -> {
                        Intent intent;
                        if ("law".equals(item.type)) {
                            intent = new Intent(context, LawDetailActivity.class);
                            intent.putExtra("article_id", item.id);
                        } else {
                            intent = new Intent(context, CaseDetailActivity.class);
                            intent.putExtra("case_id", item.id);
                        }
                        context.startActivity(intent);
                    });
                    holder.relatedContainer.addView(chip);
                }
            }
        }

        private SpannableStringBuilder buildClickableContent(String text) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(text);

            // Highlight 【第XXXX条】 as clickable
            Pattern p = Pattern.compile("【?第(\\d{4})条】?");
            Matcher m = p.matcher(text);
            while (m.find()) {
                final String articleId = m.group(1);
                LawArticle law = dataProvider.getLawArticleById(articleId);
                if (law != null) {
                    int start = m.start();
                    int end = m.end();
                    ssb.setSpan(new ClickableSpan() {
                        @Override public void onClick(@NonNull View widget) {
                            Intent intent = new Intent(context, LawDetailActivity.class);
                            intent.putExtra("article_id", articleId);
                            context.startActivity(intent);
                        }
                        @Override public void updateDrawState(@NonNull TextPaint ds) {
                            ds.setColor(0xFF1565C0);
                            ds.setUnderlineText(true);
                            ds.setFakeBoldText(true);
                        }
                    }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            // Highlight 【案例:xxx】 as styled tag
            Pattern cp = Pattern.compile("【案例[:：](.+?)】");
            Matcher cm = cp.matcher(text);
            while (cm.find()) {
                ssb.setSpan(new ForegroundColorSpan(0xFF2E7D32),
                        cm.start(), cm.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new StyleSpan(Typeface.BOLD),
                        cm.start(), cm.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            return ssb;
        }

        private int dpToPx(int dp) {
            return (int) (dp * context.getResources().getDisplayMetrics().density);
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView role, content;
            CardView card;
            LinearLayout relatedContainer;
            VH(View v) {
                super(v);
                role = v.findViewById(R.id.chat_role);
                content = v.findViewById(R.id.chat_content);
                card = v.findViewById(R.id.chat_card);
                relatedContainer = v.findViewById(R.id.related_container);
            }
        }
    }
}
