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
import com.jichengtong.app.R;
import com.jichengtong.app.activities.GlossaryActivity;
import com.jichengtong.app.activities.ToolsDetailActivity;
import com.jichengtong.app.adapters.ToolAdapter;
import com.jichengtong.app.data.DataProvider;

public class ToolsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tools, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DataProvider data = DataProvider.getInstance(requireContext());

        view.findViewById(R.id.glossary_card).setOnClickListener(v ->
            startActivity(new Intent(requireContext(), GlossaryActivity.class)));

        RecyclerView rv = view.findViewById(R.id.tools_rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        ToolAdapter adapter = new ToolAdapter();
        adapter.setItems(data.getTools());
        adapter.setOnItemClickListener(tool -> {
            Intent intent = new Intent(requireContext(), ToolsDetailActivity.class);
            intent.putExtra("tool_id", tool.getId());
            startActivity(intent);
        });
        rv.setAdapter(adapter);
    }
}
