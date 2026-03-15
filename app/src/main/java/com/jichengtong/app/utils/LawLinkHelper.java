package com.jichengtong.app.utils;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

import com.jichengtong.app.activities.LawDetailActivity;
import com.jichengtong.app.data.DataProvider;
import com.jichengtong.app.models.LawArticle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to convert law article references in text into clickable spans
 * that navigate to LawDetailActivity.
 */
public final class LawLinkHelper {

    private static final int LINK_COLOR = 0xFF1B5E20; // #1B5E20 dark green

    // Pattern 1: 《民法典》第(\d+)条 (Arabic numerals)
    private static final Pattern PATTERN_ARABIC = Pattern.compile("《民法典》第(\\d+)条");

    // Pattern 2: 第(一千[一二三四五六七八九零百十]+)条 (Chinese numerals)
    private static final Pattern PATTERN_CHINESE = Pattern.compile("第(一千[一二三四五六七八九零百十]+)条");

    /**
     * Lookup map for Chinese numerals 一千一百一十九 through 一千一百六十三 (articles 1119-1163).
     */
    private static final Map<String, String> CHINESE_TO_ARABIC = new HashMap<>();

    static {
        CHINESE_TO_ARABIC.put("一千一百一十九", "1119");
        CHINESE_TO_ARABIC.put("一千一百二十", "1120");
        CHINESE_TO_ARABIC.put("一千一百二十一", "1121");
        CHINESE_TO_ARABIC.put("一千一百二十二", "1122");
        CHINESE_TO_ARABIC.put("一千一百二十三", "1123");
        CHINESE_TO_ARABIC.put("一千一百二十四", "1124");
        CHINESE_TO_ARABIC.put("一千一百二十五", "1125");
        CHINESE_TO_ARABIC.put("一千一百二十六", "1126");
        CHINESE_TO_ARABIC.put("一千一百二十七", "1127");
        CHINESE_TO_ARABIC.put("一千一百二十八", "1128");
        CHINESE_TO_ARABIC.put("一千一百二十九", "1129");
        CHINESE_TO_ARABIC.put("一千一百三十", "1130");
        CHINESE_TO_ARABIC.put("一千一百三十一", "1131");
        CHINESE_TO_ARABIC.put("一千一百三十二", "1132");
        CHINESE_TO_ARABIC.put("一千一百三十三", "1133");
        CHINESE_TO_ARABIC.put("一千一百三十四", "1134");
        CHINESE_TO_ARABIC.put("一千一百三十五", "1135");
        CHINESE_TO_ARABIC.put("一千一百三十六", "1136");
        CHINESE_TO_ARABIC.put("一千一百三十七", "1137");
        CHINESE_TO_ARABIC.put("一千一百三十八", "1138");
        CHINESE_TO_ARABIC.put("一千一百三十九", "1139");
        CHINESE_TO_ARABIC.put("一千一百四十", "1140");
        CHINESE_TO_ARABIC.put("一千一百四十一", "1141");
        CHINESE_TO_ARABIC.put("一千一百四十二", "1142");
        CHINESE_TO_ARABIC.put("一千一百四十三", "1143");
        CHINESE_TO_ARABIC.put("一千一百四十四", "1144");
        CHINESE_TO_ARABIC.put("一千一百四十五", "1145");
        CHINESE_TO_ARABIC.put("一千一百四十六", "1146");
        CHINESE_TO_ARABIC.put("一千一百四十七", "1147");
        CHINESE_TO_ARABIC.put("一千一百四十八", "1148");
        CHINESE_TO_ARABIC.put("一千一百四十九", "1149");
        CHINESE_TO_ARABIC.put("一千一百五十", "1150");
        CHINESE_TO_ARABIC.put("一千一百五十一", "1151");
        CHINESE_TO_ARABIC.put("一千一百五十二", "1152");
        CHINESE_TO_ARABIC.put("一千一百五十三", "1153");
        CHINESE_TO_ARABIC.put("一千一百五十四", "1154");
        CHINESE_TO_ARABIC.put("一千一百五十五", "1155");
        CHINESE_TO_ARABIC.put("一千一百五十六", "1156");
        CHINESE_TO_ARABIC.put("一千一百五十七", "1157");
        CHINESE_TO_ARABIC.put("一千一百五十八", "1158");
        CHINESE_TO_ARABIC.put("一千一百五十九", "1159");
        CHINESE_TO_ARABIC.put("一千一百六十", "1160");
        CHINESE_TO_ARABIC.put("一千一百六十一", "1161");
        CHINESE_TO_ARABIC.put("一千一百六十二", "1162");
        CHINESE_TO_ARABIC.put("一千一百六十三", "1163");
    }

    private LawLinkHelper() {
    }

    /**
     * Converts text containing law article references into a clickable SpannableStringBuilder.
     * Each reference is replaced with "第{id}条·{keywords}" and styled as a link.
     *
     * @param context      Context for starting LawDetailActivity
     * @param text         Input text that may contain law references
     * @param dataProvider DataProvider for looking up law articles
     * @return SpannableStringBuilder with clickable law links
     */
    public static SpannableStringBuilder linkifyLawReferences(Context context, String text,
                                                            DataProvider dataProvider) {
        if (text == null) return new SpannableStringBuilder();
        SpannableStringBuilder sb = new SpannableStringBuilder(text);

        // Collect all matches (start, end, id, replacement) and process in reverse order
        List<MatchInfo> matches = new ArrayList<>();

        Matcher arabicMatcher = PATTERN_ARABIC.matcher(sb);
        while (arabicMatcher.find()) {
            String id = arabicMatcher.group(1);
            LawArticle article = dataProvider.getLawArticleById(id);
            if (article != null) {
                matches.add(new MatchInfo(arabicMatcher.start(), arabicMatcher.end(),
                        id, formatArticleDisplay(id, article)));
            }
        }

        Matcher chineseMatcher = PATTERN_CHINESE.matcher(sb);
        while (chineseMatcher.find()) {
            String chineseNum = chineseMatcher.group(1);
            String id = chineseNumeralToArabic(chineseNum);
            if (id != null) {
                LawArticle article = dataProvider.getLawArticleById(id);
                if (article != null) {
                    matches.add(new MatchInfo(chineseMatcher.start(), chineseMatcher.end(),
                            id, formatArticleDisplay(id, article)));
                }
            }
        }

        // Sort by start position descending so we process rightmost first (indices stay valid)
        matches.sort(Comparator.comparingInt((MatchInfo m) -> m.start).reversed());

        for (MatchInfo info : matches) {
            applyLinkSpan(context, sb, info.start, info.end, info.replacement, info.id);
        }

        return sb;
    }

    /**
     * Creates a formatted, clickable list from a list of law references.
     * Format per line: "▸ 第{id}条 {title} [{keyword1} {keyword2}...]"
     *
     * @param context      Context for starting LawDetailActivity
     * @param lawRefs      List of law reference strings (e.g. ["《民法典》第1127条", "《民法典》第1128条"])
     * @param dataProvider DataProvider for looking up law articles
     * @return SpannableStringBuilder with one law per line, each clickable
     */
    public static SpannableStringBuilder linkifyLawList(Context context, List<String> lawRefs,
                                                       DataProvider dataProvider) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        if (lawRefs == null) return sb;

        for (int i = 0; i < lawRefs.size(); i++) {
            String ref = lawRefs.get(i);
            String id = extractArticleId(ref);
            if (id == null) continue;

            LawArticle article = dataProvider.getLawArticleById(id);
            if (article == null) continue;

            String line = formatListLine(id, article);
            int start = sb.length();
            sb.append(line);
            int end = sb.length();

            applyLinkSpan(context, sb, start, end, null, id);

            if (i < lawRefs.size() - 1) {
                sb.append("\n");
            }
        }

        return sb;
    }

    /**
     * Converts Chinese numeral (e.g. "一千一百一十九") to Arabic string (e.g. "1119").
     * Handles articles 1119 through 1163. Returns null if not in range.
     */
    public static String chineseNumeralToArabic(String chineseNumeral) {
        if (chineseNumeral == null) return null;
        return CHINESE_TO_ARABIC.get(chineseNumeral.trim());
    }

    /**
     * Call this on a TextView to enable click handling for links created by linkifyLawReferences
     * or linkifyLawList.
     */
    public static void enableLinkClicks(TextView textView) {
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private static String extractArticleId(String ref) {
        if (ref == null) return null;

        Matcher arabicMatcher = PATTERN_ARABIC.matcher(ref);
        if (arabicMatcher.find()) {
            return arabicMatcher.group(1);
        }

        Matcher chineseMatcher = PATTERN_CHINESE.matcher(ref);
        if (chineseMatcher.find()) {
            return chineseNumeralToArabic(chineseMatcher.group(1));
        }

        return null;
    }

    private static String formatArticleDisplay(String id, LawArticle article) {
        StringBuilder sb = new StringBuilder();
        sb.append("第").append(id).append("条");
        List<String> keywords = article.getKeywords();
        if (keywords != null && !keywords.isEmpty()) {
            sb.append("·").append(String.join("/", keywords));
        }
        return sb.toString();
    }

    private static String formatListLine(String id, LawArticle article) {
        StringBuilder sb = new StringBuilder();
        sb.append("▸ 第").append(id).append("条 ");
        if (article.getTitle() != null) {
            sb.append(article.getTitle());
        }
        List<String> keywords = article.getKeywords();
        if (keywords != null && !keywords.isEmpty()) {
            sb.append(" [");
            sb.append(String.join(" ", keywords));
            sb.append("]");
        }
        return sb.toString();
    }

    private static void applyLinkSpan(Context context, SpannableStringBuilder sb,
                                     int start, int end, String replacement, String lawId) {
        if (replacement != null) {
            sb.replace(start, end, replacement);
            end = start + replacement.length();
        }

        final String id = lawId;
        ClickableSpan span = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(context, LawDetailActivity.class);
                intent.putExtra("law_id", id);
                context.startActivity(intent);
            }
        };

        sb.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(LINK_COLOR), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static class MatchInfo {
        final int start;
        final int end;
        final String id;
        final String replacement;

        MatchInfo(int start, int end, String id, String replacement) {
            this.start = start;
            this.end = end;
            this.id = id;
            this.replacement = replacement;
        }
    }
}
