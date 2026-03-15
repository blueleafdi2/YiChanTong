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
import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DataProvider data = DataProvider.getInstance(requireContext());

        // Search bar
        view.findViewById(R.id.search_bar).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), SearchActivity.class));
        });

        // Scenario chips
        setupScenarioChips(view);

        // AI entry card
        view.findViewById(R.id.ai_entry_card).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AIActivity.class));
        });

        // Today's case
        setupTodayCase(view, data);

        // Hot questions
        setupHotQuestions(view, data);

        // Topics
        setupTopics(view, data);
    }

    private void setupScenarioChips(View view) {
        ChipGroup chipGroup = view.findViewById(R.id.scenario_chips);
        String[] scenarios = {"亲人去世了怎么办", "我想提前立遗嘱", "对遗产分配有争议",
                "了解继承法知识", "房产如何继承", "债务要继承吗"};
        String[] topicIds = {"topic_01", "topic_02", "topic_01", "topic_04", "topic_10", "topic_09"};

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

    private void setupTodayCase(View view, DataProvider data) {
        List<CourtCase> cases = data.getCourtCases();
        if (cases.isEmpty()) return;

        int index = new Random().nextInt(cases.size());
        CourtCase todayCase = cases.get(index);

        TextView title = view.findViewById(R.id.today_case_title);
        TextView court = view.findViewById(R.id.today_case_court);
        TextView date = view.findViewById(R.id.today_case_date);
        LinearLayout tagsLayout = view.findViewById(R.id.today_case_tags);

        title.setText(todayCase.getTitle());
        court.setText(todayCase.getCourt());
        date.setText(todayCase.getJudgeDate());

        if (todayCase.getTags() != null) {
            for (int i = 0; i < Math.min(todayCase.getTags().size(), 3); i++) {
                TextView tag = new TextView(requireContext());
                tag.setText("#" + todayCase.getTags().get(i));
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
            intent.putExtra("case_id", todayCase.getId());
            startActivity(intent);
        });
    }

    private void setupHotQuestions(View view, DataProvider data) {
        RecyclerView rv = view.findViewById(R.id.hot_questions_rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        QuestionAdapter adapter = new QuestionAdapter();
        List<FAQ> faqs = data.getFAQs();
        adapter.setItems(faqs.subList(0, Math.min(8, faqs.size())));
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
        adapter.setItems(topics.subList(0, Math.min(6, topics.size())));
        adapter.setOnItemClickListener(topic -> {
            Intent intent = new Intent(requireContext(), TopicDetailActivity.class);
            intent.putExtra("topic_id", topic.getId());
            startActivity(intent);
        });
        rv.setAdapter(adapter);
    }
}
