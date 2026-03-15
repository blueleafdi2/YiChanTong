package com.jichengtong.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.jichengtong.app.R;
import com.jichengtong.app.models.LawArticle;
import java.util.ArrayList;
import java.util.List;

public class LawAdapter extends RecyclerView.Adapter<LawAdapter.ViewHolder> {
    private List<LawArticle> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener { void onItemClick(LawArticle item); }
    public void setOnItemClickListener(OnItemClickListener listener) { this.listener = listener; }
    public void setItems(List<LawArticle> items) { this.items = items; notifyDataSetChanged(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_law, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LawArticle item = items.get(position);
        holder.articleNumber.setText(item.getTitle());
        holder.summary.setText(item.getPlainExplanation());
        holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onItemClick(item); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView articleNumber, summary;
        ViewHolder(View view) { super(view); articleNumber = view.findViewById(R.id.law_article_number); summary = view.findViewById(R.id.law_summary); }
    }
}
