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

public class NotesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_page);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("我的笔记");
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
        List<Map<String, String>> notes = FavoritesManager.getInstance(this).getNotes();
        if (notes.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("暂无笔记\n浏览法条或案例时点击「笔记」按钮即可添加");
            rv.setAdapter(null);
        } else {
            emptyView.setVisibility(View.GONE);
            rv.setAdapter(new NoteAdapter(notes, item -> {
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

    static class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.VH> {
        private final List<Map<String, String>> items;
        private final OnClick onClick;
        interface OnClick { void onClick(Map<String, String> item); }

        NoteAdapter(List<Map<String, String>> items, OnClick onClick) {
            this.items = items;
            this.onClick = onClick;
        }

        @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(VH holder, int position) {
            Map<String, String> item = items.get(position);
            holder.title.setText(item.get("title"));
            holder.notePreview.setText(item.get("note"));
            try {
                long ts = Long.parseLong(item.get("time"));
                holder.time.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(new Date(ts)));
            } catch (Exception e) { holder.time.setText(""); }
            holder.itemView.setOnClickListener(v -> onClick.onClick(item));
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView title, notePreview, time;
            VH(View v) {
                super(v);
                title = v.findViewById(R.id.note_title);
                notePreview = v.findViewById(R.id.note_preview);
                time = v.findViewById(R.id.note_time);
            }
        }
    }
}
