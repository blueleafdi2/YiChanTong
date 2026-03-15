package com.jichengtong.app.activities;

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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.jichengtong.app.R;
import com.jichengtong.app.data.DataProvider;
import com.jichengtong.app.models.GlossaryItem;
import com.jichengtong.app.utils.GlossaryHelper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GlossaryActivity extends AppCompatActivity {

    private GlossaryAdapter adapter;
    private List<GlossaryItem> allItems;
    private String selectedCategory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glossary);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_right);
        toolbar.getNavigationIcon().setAutoMirrored(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        DataProvider dp = DataProvider.getInstance(this);
        allItems = dp.getGlossary();

        TextView countTv = findViewById(R.id.term_count);
        countTv.setText("共 " + allItems.size() + " 个术语");

        ChipGroup chipGroup = findViewById(R.id.category_chips);
        Set<String> categories = new LinkedHashSet<>();
        categories.add("全部");
        categories.add("⚠️ 小白必看");
        for (GlossaryItem g : allItems) categories.add(g.getCategory());
        for (String cat : categories) {
            Chip chip = new Chip(this);
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setChecked("全部".equals(cat));
            chipGroup.addView(chip);
        }
        chipGroup.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) { filterItems(null, ""); return; }
            Chip selected = group.findViewById(ids.get(0));
            String cat = selected != null ? selected.getText().toString() : null;
            if ("全部".equals(cat)) cat = null;
            selectedCategory = cat;
            EditText search = findViewById(R.id.search_input);
            filterItems(cat, search.getText().toString().trim());
        });

        RecyclerView rv = findViewById(R.id.glossary_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GlossaryAdapter(allItems);
        rv.setAdapter(adapter);

        EditText searchInput = findViewById(R.id.search_input);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                filterItems(selectedCategory, s.toString().trim());
            }
        });
    }

    private void filterItems(String category, String query) {
        List<GlossaryItem> filtered = new ArrayList<>();
        String q = query.toLowerCase();
        for (GlossaryItem g : allItems) {
            boolean catMatch;
            if (category == null) catMatch = true;
            else if ("⚠️ 小白必看".equals(category)) catMatch = "hard".equals(g.getDifficulty()) || "medium".equals(g.getDifficulty());
            else catMatch = category.equals(g.getCategory());

            boolean queryMatch = q.isEmpty() ||
                g.getTerm().toLowerCase().contains(q) ||
                g.getDefinition().toLowerCase().contains(q) ||
                g.getExample().toLowerCase().contains(q);

            if (catMatch && queryMatch) filtered.add(g);
        }
        adapter.setItems(filtered);
        TextView countTv = findViewById(R.id.term_count);
        countTv.setText("共 " + filtered.size() + " 个术语");
    }

    class GlossaryAdapter extends RecyclerView.Adapter<GlossaryAdapter.VH> {
        private List<GlossaryItem> items;
        GlossaryAdapter(List<GlossaryItem> items) { this.items = new ArrayList<>(items); }
        void setItems(List<GlossaryItem> items) { this.items = new ArrayList<>(items); notifyDataSetChanged(); }

        @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_glossary, parent, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(VH h, int pos) {
            GlossaryItem g = items.get(pos);
            String diffIcon;
            int badgeColor;
            switch (g.getDifficulty()) {
                case "hard": diffIcon = "⚠️"; badgeColor = 0xFFC62828; break;
                case "medium": diffIcon = "📝"; badgeColor = 0xFFEF6C00; break;
                default: diffIcon = "✅"; badgeColor = 0xFF2E7D32; break;
            }
            h.badge.setText(diffIcon);
            h.badge.setBackgroundColor(badgeColor);
            h.term.setText(g.getTerm());
            h.category.setText(g.getCategory());
            String def = g.getDefinition();
            h.preview.setText(def.length() > 60 ? def.substring(0, 60) + "..." : def);
            h.itemView.setOnClickListener(v -> GlossaryHelper.showTermDialog(GlossaryActivity.this, g));
        }

        @Override public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView badge, term, category, preview;
            VH(View v) {
                super(v);
                badge = v.findViewById(R.id.glossary_badge);
                term = v.findViewById(R.id.glossary_term);
                category = v.findViewById(R.id.glossary_category);
                preview = v.findViewById(R.id.glossary_preview);
            }
        }
    }
}
