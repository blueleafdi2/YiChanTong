package com.jichengtong.app.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
            "6. 保持专业但亲切的语气，回答控制在500字以内\n" +
            "7. 如果用户的问题与遗产、继承、遗嘱完全无关（如天气、笑话、编程等），你可以简短友好地回答，但必须在回答末尾加上：\"\\n\\n💡 温馨提示：本助手专注于遗产继承法律领域，如有继承相关问题，随时向我提问！\"，且不要引用任何法条或案例标记。";

    private static final String PREFS_NAME = "ai_chat_prefs";
    private static final String KEY_MESSAGES = "chat_messages";
    private static final int MAX_SAVED_MESSAGES = 100;

    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;
    private EditText inputMessage;
    private View btnSend;
    private DataProvider dataProvider;
    private RecyclerView chatRv;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final String API_KEY = "sk-fcd3d027f43b4113bee5ab9b9c3551c0";

    private static final String WELCOME_MESSAGE =
            "您好！我是遗产通AI法律助手（DeepSeek驱动），专精中国遗产继承法律。\n\n" +
            "您可以问我任何继承相关问题，例如：\n" +
            "• 父母去世后房产怎么继承？\n" +
            "• 自书遗嘱需要什么条件才有效？\n" +
            "• 独生子女能继承全部遗产吗？\n" +
            "• 数字资产（加密货币）能继承吗？\n\n" +
            "我的回答会关联App内的法条和案例，点击即可查看详情。";

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
        toolbar.inflateMenu(R.menu.ai_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_clear_history) {
                showClearHistoryDialog();
                return true;
            }
            return false;
        });

        chatRv = findViewById(R.id.chat_rv);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        chatRv.setLayoutManager(lm);
        adapter = new ChatAdapter(this, messages, dataProvider);
        chatRv.setAdapter(adapter);

        inputMessage = findViewById(R.id.input_message);
        btnSend = findViewById(R.id.btn_send);

        loadConversation();
        if (messages.isEmpty()) {
            messages.add(new ChatMessage("assistant", WELCOME_MESSAGE));
        }
        adapter.notifyDataSetChanged();
        scrollToBottom();

        btnSend.setOnClickListener(v -> sendMessage());

        String presetQuestion = getIntent().getStringExtra("question");
        if (presetQuestion != null && !presetQuestion.isEmpty()) {
            inputMessage.setText(presetQuestion);
            sendMessage();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveConversation();
    }

    private void showClearHistoryDialog() {
        new AlertDialog.Builder(this)
            .setTitle("清除对话历史")
            .setMessage("确定要清除所有对话记录吗？此操作不可撤销。")
            .setPositiveButton("清除", (d, w) -> {
                messages.clear();
                messages.add(new ChatMessage("assistant", WELCOME_MESSAGE));
                adapter.notifyDataSetChanged();
                saveConversation();
                Toast.makeText(this, "对话历史已清除", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void saveConversation() {
        try {
            JSONArray arr = new JSONArray();
            int start = Math.max(0, messages.size() - MAX_SAVED_MESSAGES);
            for (int i = start; i < messages.size(); i++) {
                ChatMessage msg = messages.get(i);
                JSONObject obj = new JSONObject();
                obj.put("role", msg.role);
                obj.put("content", msg.content);
                if (msg.relatedItems != null && !msg.relatedItems.isEmpty()) {
                    JSONArray relArr = new JSONArray();
                    for (RelatedItem ri : msg.relatedItems) {
                        JSONObject riObj = new JSONObject();
                        riObj.put("type", ri.type);
                        riObj.put("id", ri.id);
                        riObj.put("title", ri.title);
                        riObj.put("subtitle", ri.subtitle);
                        relArr.put(riObj);
                    }
                    obj.put("related", relArr);
                }
                arr.put(obj);
            }
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_MESSAGES, arr.toString())
                .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadConversation() {
        try {
            String json = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_MESSAGES, null);
            if (json == null) return;
            JSONArray arr = new JSONArray(json);
            messages.clear();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                ChatMessage msg = new ChatMessage(obj.getString("role"), obj.getString("content"));
                if (obj.has("related")) {
                    msg.relatedItems = new ArrayList<>();
                    JSONArray relArr = obj.getJSONArray("related");
                    for (int j = 0; j < relArr.length(); j++) {
                        JSONObject riObj = relArr.getJSONObject(j);
                        msg.relatedItems.add(new RelatedItem(
                            riObj.optString("type", "law"),
                            riObj.optString("id", ""),
                            riObj.optString("title", ""),
                            riObj.optString("subtitle", "")
                        ));
                    }
                }
                messages.add(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String text = inputMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        messages.add(new ChatMessage("user", text));
        adapter.notifyItemInserted(messages.size() - 1);
        inputMessage.setText("");
        scrollToBottom();

        messages.add(new ChatMessage("assistant", "💭 正在思考..."));
        int loadingIdx = messages.size() - 1;
        adapter.notifyItemInserted(loadingIdx);
        scrollToBottom();
        btnSend.setEnabled(false);

        new Thread(() -> streamDeepSeekAPI(text, loadingIdx)).start();
    }

    private void streamDeepSeekAPI(String userMessage, int msgIdx) {
        try {
            JSONArray msgArray = new JSONArray();
            msgArray.put(new JSONObject().put("role", "system").put("content", SYSTEM_PROMPT));
            int start = Math.max(0, messages.size() - 21);
            for (int i = start; i < messages.size() - 1; i++) {
                ChatMessage m = messages.get(i);
                if (m.content.startsWith("💭 ")) continue;
                if (WELCOME_MESSAGE.equals(m.content)) continue;
                msgArray.put(new JSONObject().put("role", m.role).put("content", m.content));
            }

            JSONObject body = new JSONObject();
            body.put("model", MODEL);
            body.put("messages", msgArray);
            body.put("max_tokens", 2000);
            body.put("temperature", 0.7);
            body.put("stream", true);

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                String errorMsg = getErrorMessage(code, conn);
                mainHandler.post(() -> {
                    messages.get(msgIdx).content = errorMsg;
                    adapter.notifyItemChanged(msgIdx);
                    btnSend.setEnabled(true);
                    scrollToBottom();
                    saveConversation();
                });
                return;
            }

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder fullResponse = new StringBuilder();
            String line;
            long lastUIUpdate = 0;
            boolean needsScroll = true;

            messages.get(msgIdx).isStreaming = true;

            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data: ")) continue;
                String data = line.substring(6).trim();
                if ("[DONE]".equals(data)) break;

                try {
                    JSONObject chunk = new JSONObject(data);
                    JSONObject delta = chunk.getJSONArray("choices")
                        .getJSONObject(0).optJSONObject("delta");
                    if (delta == null) continue;
                    String content = delta.optString("content", "");
                    if (content.isEmpty()) continue;

                    fullResponse.append(content);

                    long now = System.currentTimeMillis();
                    if (now - lastUIUpdate > 120) {
                        lastUIUpdate = now;
                        final String current = fullResponse.toString();
                        final boolean doScroll = needsScroll;
                        mainHandler.post(() -> {
                            messages.get(msgIdx).content = current;
                            RecyclerView.ViewHolder vh =
                                chatRv.findViewHolderForAdapterPosition(msgIdx);
                            if (vh instanceof ChatAdapter.VH) {
                                ((ChatAdapter.VH) vh).content.setText(current);
                            }
                            if (doScroll) {
                                chatRv.scrollToPosition(msgIdx);
                            }
                        });
                        needsScroll = fullResponse.length() % 200 < 20;
                    }
                } catch (Exception ignored) {}
            }
            reader.close();

            messages.get(msgIdx).isStreaming = false;
            final String finalResponse = fullResponse.toString();

            mainHandler.post(() -> {
                messages.get(msgIdx).content = finalResponse;
                adapter.notifyItemChanged(msgIdx);
                scrollToBottom();
            });

            List<RelatedItem> related = findRelatedContent(finalResponse);
            mainHandler.post(() -> {
                if (!related.isEmpty()) {
                    messages.get(msgIdx).relatedItems = related;
                    adapter.notifyItemChanged(msgIdx);
                    scrollToBottom();
                }
                btnSend.setEnabled(true);
                saveConversation();
            });

        } catch (Exception e) {
            mainHandler.post(() -> {
                messages.get(msgIdx).isStreaming = false;
                messages.get(msgIdx).content =
                    "网络连接失败，请检查网络后重试。\n\n您也可以：\n• 使用App内的法律库查询法条\n• 在案例库搜索相关判例\n• 拨打12348免费法律热线";
                adapter.notifyItemChanged(msgIdx);
                btnSend.setEnabled(true);
                scrollToBottom();
                saveConversation();
            });
        }
    }

    private String getErrorMessage(int code, HttpURLConnection conn) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String l;
            while ((l = r.readLine()) != null) sb.append(l);
            r.close();
        } catch (Exception ignored) {}

        if (code == 401) return "API Key 无效或已过期。\n\n请联系开发者更新 API Key。";
        if (code == 402) return "AI 服务账户余额不足，暂时无法使用。\n\n" +
            "开发者正在处理中，请稍后重试。\n\n" +
            "您也可以直接使用 App 内的丰富资源：\n" +
            "• 📚 法律库：45条继承法条逐条解读\n• 📋 案例库：390+全国真实判例\n" +
            "• 📚 法律词典：125个术语通俗解释\n• 💡 知识专题：25个继承法常见场景\n• 🔥 热门问题：40个高频法律疑问\n\n" +
            "如需人工咨询，请前往「我的 → 联系法律专家」";
        return "AI 服务暂时不可用（错误码 " + code + "）。\n\n请稍后重试。";
    }

    private void scrollToBottom() {
        chatRv.postDelayed(() -> chatRv.scrollToPosition(messages.size() - 1), 50);
    }

    private List<RelatedItem> findRelatedContent(String response) {
        List<RelatedItem> items = new ArrayList<>();
        Set<String> addedIds = new HashSet<>();

        Pattern articlePattern = Pattern.compile("第(\\d{3,4})条");
        Matcher matcher = articlePattern.matcher(response);
        while (matcher.find()) {
            String articleNum = matcher.group(1);
            LawArticle law = dataProvider.getLawArticleById(articleNum);
            if (law != null && addedIds.add("law_" + articleNum)) {
                items.add(new RelatedItem("law", law.getId(), law.getTitle(),
                        "《民法典》" + law.getArticle()));
            }
        }

        for (LawArticle law : dataProvider.getLawArticles()) {
            if (response.contains(law.getArticle()) && addedIds.add("law_" + law.getId())) {
                items.add(new RelatedItem("law", law.getId(), law.getTitle(),
                        "《民法典》" + law.getArticle()));
            }
        }

        Pattern casePattern = Pattern.compile("【案例[:：](.+?)】");
        Matcher caseMatcher = casePattern.matcher(response);
        while (caseMatcher.find()) {
            String keyword = caseMatcher.group(1).trim();
            for (CourtCase c : dataProvider.getCourtCases()) {
                if (addedIds.size() >= 8) break;
                boolean match = c.getTitle().contains(keyword) ||
                        c.getCaseType().contains(keyword) ||
                        (c.getTags() != null && c.getTags().stream().anyMatch(t -> t.contains(keyword)));
                if (match && addedIds.add("case_" + c.getId())) {
                    items.add(new RelatedItem("case", c.getId(), c.getTitle(), c.getCourt()));
                    break;
                }
            }
        }

        for (LawArticle law : dataProvider.getLawArticles()) {
            if (addedIds.size() >= 8) break;
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
        if (items.size() > 8) return items.subList(0, 8);
        return items;
    }

    static class ChatMessage {
        String role;
        String content;
        List<RelatedItem> relatedItems;
        boolean isStreaming;
        ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    static class RelatedItem {
        String type, id, title, subtitle;
        RelatedItem(String type, String id, String title, String subtitle) {
            this.type = type; this.id = id; this.title = title; this.subtitle = subtitle;
        }
    }

    static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {
        private final Context context;
        private final List<ChatMessage> items;
        private final DataProvider dataProvider;

        ChatAdapter(Context ctx, List<ChatMessage> items, DataProvider dp) {
            this.context = ctx; this.items = items; this.dataProvider = dp;
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
            } else if (msg.isStreaming) {
                holder.content.setText(msg.content);
                holder.content.setMovementMethod(null);
            } else {
                SpannableStringBuilder ssb = buildClickableContent(msg.content);
                holder.content.setText(ssb);
                holder.content.setMovementMethod(LinkMovementMethod.getInstance());
            }

            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) holder.card.getLayoutParams();
            if (isUser) { mlp.setMarginStart(dpToPx(60)); mlp.setMarginEnd(0); }
            else { mlp.setMarginStart(0); mlp.setMarginEnd(dpToPx(60)); }
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
                    chip.setText(("law".equals(item.type) ? "📜 " : "⚖️ ") + item.title);
                    chip.setTextSize(12);
                    chip.setChipBackgroundColorResource(
                            "law".equals(item.type) ? R.color.primary_container : R.color.secondary_container);
                    chip.setOnClickListener(v -> {
                        Intent intent;
                        if ("law".equals(item.type)) {
                            intent = new Intent(context, LawDetailActivity.class);
                            intent.putExtra("law_id", item.id);
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
            Pattern p = Pattern.compile("【?第(\\d{3,4})条】?");
            Matcher m = p.matcher(text);
            while (m.find()) {
                final String articleId = m.group(1);
                LawArticle law = dataProvider.getLawArticleById(articleId);
                if (law != null) {
                    ssb.setSpan(new ClickableSpan() {
                        @Override public void onClick(@NonNull View w) {
                            Intent i = new Intent(context, LawDetailActivity.class);
                            i.putExtra("law_id", articleId);
                            context.startActivity(i);
                        }
                        @Override public void updateDrawState(@NonNull TextPaint ds) {
                            ds.setColor(0xFF1565C0); ds.setUnderlineText(true); ds.setFakeBoldText(true);
                        }
                    }, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            Pattern cp = Pattern.compile("【案例[:：](.+?)】");
            Matcher cm = cp.matcher(text);
            while (cm.find()) {
                final String keyword = cm.group(1).trim();
                ssb.setSpan(new ClickableSpan() {
                    @Override public void onClick(@NonNull View w) {
                        for (CourtCase c : dataProvider.getCourtCases()) {
                            if (c.getTitle().contains(keyword) || c.getCaseType().contains(keyword) ||
                                (c.getTags() != null && c.getTags().stream().anyMatch(t -> t.contains(keyword)))) {
                                Intent i = new Intent(context, CaseDetailActivity.class);
                                i.putExtra("case_id", c.getId());
                                context.startActivity(i);
                                return;
                            }
                        }
                        Toast.makeText(context, "未找到匹配案例，请在案例库中搜索", Toast.LENGTH_SHORT).show();
                    }
                    @Override public void updateDrawState(@NonNull TextPaint ds) {
                        ds.setColor(0xFF2E7D32); ds.setUnderlineText(true); ds.setFakeBoldText(true);
                    }
                }, cm.start(), cm.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return ssb;
        }

        private int dpToPx(int dp) {
            return (int) (dp * context.getResources().getDisplayMetrics().density);
        }
        @Override public int getItemCount() { return items.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView role, content; CardView card; LinearLayout relatedContainer;
            VH(View v) { super(v); role=v.findViewById(R.id.chat_role); content=v.findViewById(R.id.chat_content);
                card=v.findViewById(R.id.chat_card); relatedContainer=v.findViewById(R.id.related_container); }
        }
    }
}
