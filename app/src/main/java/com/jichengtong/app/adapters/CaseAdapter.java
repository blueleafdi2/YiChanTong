package com.jichengtong.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.jichengtong.app.R;
import com.jichengtong.app.models.CourtCase;
import java.util.ArrayList;
import java.util.List;

public class CaseAdapter extends RecyclerView.Adapter<CaseAdapter.ViewHolder> {
    private List<CourtCase> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CourtCase item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CourtCase> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_case, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourtCase item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.court.setText(item.getCourt());
        holder.date.setText(item.getJudgeDate());
        holder.summary.setText(item.getCaseSummary());

        holder.tagsContainer.removeAllViews();
        if (item.getTags() != null) {
            for (int i = 0; i < Math.min(item.getTags().size(), 3); i++) {
                TextView tag = new TextView(holder.itemView.getContext());
                tag.setText(item.getTags().get(i));
                tag.setTextSize(11);
                tag.setTextColor(0xFF0D47A1);
                tag.setBackgroundResource(R.drawable.bg_tag);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMarginEnd(8);
                tag.setLayoutParams(params);
                holder.tagsContainer.addView(tag);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, court, date, summary;
        LinearLayout tagsContainer;
        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.case_title);
            court = view.findViewById(R.id.case_court);
            date = view.findViewById(R.id.case_date);
            summary = view.findViewById(R.id.case_summary);
            tagsContainer = view.findViewById(R.id.tags_container);
        }
    }
}
