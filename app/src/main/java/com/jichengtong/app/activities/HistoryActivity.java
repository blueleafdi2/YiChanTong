package com.jichengtong.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.jichengtong.app.R;
import com.jichengtong.app.utils.FavoritesManager;
import java.util.*;

public class HistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_page);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("阅读历史");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_right);
        toolbar.getNavigationIcon().setAutoMirrored(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        refreshList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        RecyclerView rv = findViewById(R.id.list_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        TextView emptyView = findViewById(R.id.empty_text);
        List<Map<String, String>> history = FavoritesManager.getInstance(this).getHistory();
        if (history.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("暂无阅读记录\n浏览法条或案例时会自动记录");
            rv.setAdapter(null);
        } else {
            emptyView.setVisibility(View.GONE);
            rv.setAdapter(new FavoritesActivity.SimpleListAdapter(history, item -> {
                String type = item.get("type");
                String id = item.get("id");
                Intent intent;
                if ("law".equals(type)) {
                    intent = new Intent(this, LawDetailActivity.class);
                    intent.putExtra("law_id", id);
                } else if ("case".equals(type)) {
                    intent = new Intent(this, CaseDetailActivity.class);
                    intent.putExtra("case_id", id);
                } else {
                    intent = new Intent(this, TopicDetailActivity.class);
                    intent.putExtra("topic_id", id);
                }
                startActivity(intent);
            }));
        }
    }
}
