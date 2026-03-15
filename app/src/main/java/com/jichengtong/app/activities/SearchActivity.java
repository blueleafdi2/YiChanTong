package com.jichengtong.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.jichengtong.app.R;
import com.jichengtong.app.data.DataProvider;
import com.jichengtong.app.models.*;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        EditText searchInput = findViewById(R.id.search_input);
        RecyclerView rv = findViewById(R.id.search_results_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        DataProvider data = DataProvider.getInstance(this);

        SearchResultAdapter adapter = new SearchResultAdapter(item -> {
            if (item instanceof LawArticle) {
                Intent intent = new Intent(this, LawDetailActivity.class);
                intent.putExtra("law_id", ((LawArticle) item).getId());
                startActivity(intent);
            } else if (item instanceof CourtCase) {
                Intent intent = new Intent(this, CaseDetailActivity.class);
                intent.putExtra("case_id", ((CourtCase) item).getId());
                startActivity(intent);
            } else if (item instanceof FAQ) {
                Intent intent = new Intent(this, TopicDetailActivity.class);
                intent.putExtra("faq_id", ((FAQ) item).getId());
                startActivity(intent);
            } else if (item instanceof Topic) {
                Intent intent = new Intent(this, TopicDetailActivity.class);
                intent.putExtra("topic_id", ((Topic) item).getId());
                startActivity(intent);
            }
        });
        rv.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() < 2) { adapter.setItems(new ArrayList<>()); return; }
                adapter.setItems(data.search(query));
            }
        });
        searchInput.requestFocus();
    }

    static class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.VH> {
        private List<Object> items = new ArrayList<>();
        private final OnClick onClick;
        interface OnClick { void onClick(Object item); }

        SearchResultAdapter(OnClick onClick) { this.onClick = onClick; }
        void setItems(List<Object> items) { this.items = items; notifyDataSetChanged(); }

        @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(VH holder, int position) {
            Object item = items.get(position);
            if (item instanceof LawArticle) {
                LawArticle law = (LawArticle) item;
                holder.typeBadge.setText("法条");
                holder.typeBadge.setBackgroundColor(0xFF1B5E20);
                holder.title.setText(law.getTitle());
                holder.summary.setText(law.getPlainExplanation());
            } else if (item instanceof CourtCase) {
                CourtCase c = (CourtCase) item;
                holder.typeBadge.setText("案例");
                holder.typeBadge.setBackgroundColor(0xFF0D47A1);
                holder.title.setText(c.getTitle());
                holder.summary.setText(c.getCourt() + " · " + c.getJudgeDate());
            } else if (item instanceof FAQ) {
                FAQ faq = (FAQ) item;
                holder.typeBadge.setText("问答");
                holder.typeBadge.setBackgroundColor(0xFFFF8F00);
                holder.title.setText(faq.getQuestion());
                String ans = faq.getAnswer();
                holder.summary.setText(ans != null ? ans.substring(0, Math.min(80, ans.length())) + "..." : "");
            } else if (item instanceof Topic) {
                Topic t = (Topic) item;
                holder.typeBadge.setText("专题");
                holder.typeBadge.setBackgroundColor(0xFF6A1B9A);
                holder.title.setText(t.getTitle());
                holder.summary.setText(t.getDescription());
            }
            holder.itemView.setOnClickListener(v -> onClick.onClick(item));
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView typeBadge, title, summary;
            VH(View v) { super(v); typeBadge = v.findViewById(R.id.result_type_badge); title = v.findViewById(R.id.result_title); summary = v.findViewById(R.id.result_summary); }
        }
    }
}
