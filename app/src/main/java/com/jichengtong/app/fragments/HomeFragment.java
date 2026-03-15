package com.jichengtong.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.jichengtong.app.R;
import com.jichengtong.app.activities.*;
import com.jichengtong.app.adapters.QuestionAdapter;
import com.jichengtong.app.adapters.TopicAdapter;
import com.jichengtong.app.data.DataProvider;
import com.jichengtong.app.models.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String[] PAIN_POINT_FAQ_ORDER = {
        "faq_01", "faq_07", "faq_03", "faq_06",
        "faq_02", "faq_12", "faq_04", "faq_08"
    };

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DataProvider data = DataProvider.getInstance(requireContext());

        view.findViewById(R.id.search_bar).setOnClickListener(v ->
            startActivity(new Intent(requireContext(), SearchActivity.class)));

        setupScenarioChips(view);

        view.findViewById(R.id.ai_entry_card).setOnClickListener(v ->
            startActivity(new Intent(requireContext(), AIActivity.class)));

        setupFeaturedCase(view, data);
        setupHotQuestions(view, data);
        setupTopics(view, data);
    }

    private void setupScenarioChips(View view) {
        ChipGroup chipGroup = view.findViewById(R.id.scenario_chips);
        String[] scenarios = {
            "亲人去世后遗产怎么分", "我想立一份有效遗嘱",
            "遗产分配有争议怎么办", "房产继承过户流程",
            "父母的债务要子女还吗", "数字遗产怎么继承"
        };
        String[] topicIds = {"topic_01", "topic_02", "topic_01", "topic_10", "topic_09", "topic_11"};

        for (int i = 0; i < scenarios.length; i++) {
            Chip chip = new Chip(requireContext());
            chip.setText(scenarios[i]);
            chip.setChipBackgroundColorResource(R.color.primary_container);
            chip.setTextColor(getResources().getColor(R.color.on_primary_container, null));
            chip.setClickable(true);
            final String topicId = topicIds[i];
            chip.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), TopicDetailActivity.class);
                intent.putExtra("topic_id", topicId);
                startActivity(intent);
            });
            chipGroup.addView(chip);
        }
    }

    private void setupFeaturedCase(View view, DataProvider data) {
        List<CourtCase> cases = data.getCourtCases();
        if (cases.isEmpty()) return;

        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        List<CourtCase> complexCases = new ArrayList<>();
        for (CourtCase c : cases) {
            if (c.getTags() != null && c.getTags().size() >= 4) {
                complexCases.add(c);
            }
        }
        List<CourtCase> pool = complexCases.isEmpty() ? cases : complexCases;
        CourtCase featured = pool.get(dayOfYear % pool.size());

        TextView title = view.findViewById(R.id.today_case_title);
        TextView court = view.findViewById(R.id.today_case_court);
        TextView date = view.findViewById(R.id.today_case_date);
        LinearLayout tagsLayout = view.findViewById(R.id.today_case_tags);

        title.setText(featured.getTitle());
        court.setText(featured.getCourt());
        date.setText(featured.getJudgeDate());

        if (featured.getTags() != null) {
            for (int i = 0; i < Math.min(featured.getTags().size(), 4); i++) {
                TextView tag = new TextView(requireContext());
                tag.setText("#" + featured.getTags().get(i));
                tag.setTextSize(11);
                tag.setTextColor(0xFF0D47A1);
                tag.setBackgroundResource(R.drawable.bg_tag);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMarginEnd(8);
                tag.setLayoutParams(params);
                tagsLayout.addView(tag);
            }
        }

        view.findViewById(R.id.today_case_card).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CaseDetailActivity.class);
            intent.putExtra("case_id", featured.getId());
            startActivity(intent);
        });
    }

    private void setupHotQuestions(View view, DataProvider data) {
        RecyclerView rv = view.findViewById(R.id.hot_questions_rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        QuestionAdapter adapter = new QuestionAdapter();

        List<FAQ> allFaqs = data.getFAQs();
        List<String> order = Arrays.asList(PAIN_POINT_FAQ_ORDER);
        List<FAQ> orderedFaqs = new ArrayList<>();
        for (String id : order) {
            for (FAQ faq : allFaqs) {
                if (faq.getId().equals(id)) {
                    orderedFaqs.add(faq);
                    break;
                }
            }
        }

        adapter.setItems(orderedFaqs);
        adapter.setOnItemClickListener(faq -> {
            Intent intent = new Intent(requireContext(), TopicDetailActivity.class);
            intent.putExtra("faq_id", faq.getId());
            startActivity(intent);
        });
        rv.setAdapter(adapter);
    }

    private void setupTopics(View view, DataProvider data) {
        RecyclerView rv = view.findViewById(R.id.topics_rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        TopicAdapter adapter = new TopicAdapter();
        List<Topic> topics = data.getTopics();
        adapter.setItems(topics.subList(0, Math.min(8, topics.size())));
        adapter.setOnItemClickListener(topic -> {
            Intent intent = new Intent(requireContext(), TopicDetailActivity.class);
            intent.putExtra("topic_id", topic.getId());
            startActivity(intent);
        });
        rv.setAdapter(adapter);
    }
}
