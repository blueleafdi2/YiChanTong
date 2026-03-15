package com.jichengtong.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.jichengtong.app.R;
import com.jichengtong.app.models.ToolItem;
import java.util.ArrayList;
import java.util.List;

public class ToolAdapter extends RecyclerView.Adapter<ToolAdapter.ViewHolder> {
    private List<ToolItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ToolItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ToolItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tool, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ToolItem item = items.get(position);
        holder.icon.setText(item.getIcon());
        holder.title.setText(item.getTitle());
        holder.desc.setText(item.getDescription());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView icon, title, desc;
        ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.tool_icon);
            title = view.findViewById(R.id.tool_title);
            desc = view.findViewById(R.id.tool_desc);
        }
    }
}
