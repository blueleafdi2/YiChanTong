package com.jichengtong.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.jichengtong.app.R;
import com.jichengtong.app.utils.FavoritesManager;
import java.text.SimpleDateFormat;
import java.util.*;

public class FavoritesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_page);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("我的收藏");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_right);
        toolbar.getNavigationIcon().setAutoMirrored(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.list_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        TextView emptyView = findViewById(R.id.empty_text);

        List<Map<String, String>> favorites = FavoritesManager.getInstance(this).getFavorites();
        if (favorites.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("暂无收藏内容\n浏览法条或案例时点击「收藏」按钮即可添加");
        } else {
            emptyView.setVisibility(View.GONE);
            rv.setAdapter(new SimpleListAdapter(favorites, this::onItemClick));
        }
    }

    private void onItemClick(Map<String, String> item) {
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView rv = findViewById(R.id.list_rv);
        TextView emptyView = findViewById(R.id.empty_text);
        List<Map<String, String>> favorites = FavoritesManager.getInstance(this).getFavorites();
        if (favorites.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            rv.setAdapter(null);
        } else {
            emptyView.setVisibility(View.GONE);
            rv.setAdapter(new SimpleListAdapter(favorites, this::onItemClick));
        }
    }

    static class SimpleListAdapter extends RecyclerView.Adapter<SimpleListAdapter.VH> {
        private final List<Map<String, String>> items;
        private final OnClick onClick;
        interface OnClick { void onClick(Map<String, String> item); }

        SimpleListAdapter(List<Map<String, String>> items, OnClick onClick) {
            this.items = items;
            this.onClick = onClick;
        }

        @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_list, parent, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(VH holder, int position) {
            Map<String, String> item = items.get(position);
            String typeLabel = "law".equals(item.get("type")) ? "📚 法条" : "case".equals(item.get("type")) ? "📋 案例" : "📖 专题";
            holder.title.setText(item.get("title"));
            holder.subtitle.setText(typeLabel + " · " + formatTime(item.get("time")));
            holder.itemView.setOnClickListener(v -> onClick.onClick(item));
        }

        @Override public int getItemCount() { return items.size(); }

        private String formatTime(String timestamp) {
            try {
                long ts = Long.parseLong(timestamp);
                return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(new Date(ts));
            } catch (Exception e) { return ""; }
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView title, subtitle;
            VH(View v) {
                super(v);
                title = v.findViewById(R.id.item_title);
                subtitle = v.findViewById(R.id.item_subtitle);
            }
        }
    }
}
