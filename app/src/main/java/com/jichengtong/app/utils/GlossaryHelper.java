package com.jichengtong.app.utils;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jichengtong.app.R;
import com.jichengtong.app.data.DataProvider;
import com.jichengtong.app.models.GlossaryItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlossaryHelper {

    private static Map<String, GlossaryItem> termMap;

    private static void ensureMap(Context ctx) {
        if (termMap != null) return;
        termMap = new HashMap<>();
        List<GlossaryItem> items = DataProvider.getInstance(ctx).getGlossary();
        for (GlossaryItem g : items) {
            termMap.put(g.getTerm(), g);
        }
    }

    public static GlossaryItem findTerm(Context ctx, String term) {
        ensureMap(ctx);
        return termMap.get(term);
    }

    public static void showTermDialog(Context ctx, String term) {
        GlossaryItem g = findTerm(ctx, term);
        if (g == null) return;
        showTermDialog(ctx, g);
    }

    public static void showTermDialog(Context ctx, GlossaryItem g) {
        StringBuilder body = new StringBuilder();
        body.append("📖 释义\n").append(g.getDefinition()).append("\n");

        if (!g.getExample().isEmpty()) {
            body.append("\n💡 通俗举例\n").append(g.getExample()).append("\n");
        }

        if (g.getRelatedLaw() != null && !g.getRelatedLaw().isEmpty()) {
            body.append("\n📜 相关法条\n").append(g.getRelatedLaw());
        }

        String diffLabel;
        switch (g.getDifficulty()) {
            case "hard": diffLabel = "⚠️ 高级概念"; break;
            case "medium": diffLabel = "📝 进阶概念"; break;
            default: diffLabel = "✅ 基础概念"; break;
        }

        new MaterialAlertDialogBuilder(ctx)
            .setTitle("📚 " + g.getTerm() + "  " + diffLabel)
            .setMessage(body.toString().trim())
            .setPositiveButton("我知道了", null)
            .show();
    }

    public static SpannableStringBuilder highlightGlossaryTerms(Context ctx, String text) {
        ensureMap(ctx);
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);

        for (Map.Entry<String, GlossaryItem> entry : termMap.entrySet()) {
            String term = entry.getKey();
            if (term.length() < 3) continue;
            GlossaryItem g = entry.getValue();
            if (!"hard".equals(g.getDifficulty())) continue;

            int idx = text.indexOf(term);
            if (idx < 0) continue;
            final GlossaryItem glossaryItem = g;
            ssb.setSpan(new ClickableSpan() {
                @Override public void onClick(@NonNull View widget) {
                    showTermDialog(widget.getContext(), glossaryItem);
                }
                @Override public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(0xFF6A1B9A);
                    ds.setUnderlineText(false);
                    ds.setFakeBoldText(false);
                }
            }, idx, idx + term.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ssb;
    }
}
