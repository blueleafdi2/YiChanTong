package com.jichengtong.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.jichengtong.app.R;
import com.jichengtong.app.activities.LawDetailActivity;
import com.jichengtong.app.adapters.LawAdapter;
import com.jichengtong.app.data.DataProvider;
import com.jichengtong.app.models.LawArticle;
import java.util.List;

public class LawsFragment extends Fragment {
    private LawAdapter adapter;
    private DataProvider data;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_laws, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        data = DataProvider.getInstance(requireContext());

        RecyclerView rv = view.findViewById(R.id.laws_rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new LawAdapter();
        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(requireContext(), LawDetailActivity.class);
            intent.putExtra("law_id", item.getId());
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        TabLayout tabs = view.findViewById(R.id.law_tabs);
        List<String> chapters = data.getLawChapters();

        tabs.addTab(tabs.newTab().setText("全部"));
        for (String chapter : chapters) {
            String shortName = chapter.length() > 6 ? chapter.substring(3) : chapter;
            tabs.addTab(tabs.newTab().setText(shortName));
        }

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    adapter.setItems(data.getLawArticles());
                } else {
                    String chapter = chapters.get(tab.getPosition() - 1);
                    adapter.setItems(data.getLawsByChapter(chapter));
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        adapter.setItems(data.getLawArticles());
    }
}
