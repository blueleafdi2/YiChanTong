package com.jichengtong.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.jichengtong.app.R;
import com.jichengtong.app.activities.*;

public class MineFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.menu_favorites).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), FavoritesActivity.class)));
        view.findViewById(R.id.menu_notes).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NotesActivity.class)));
        view.findViewById(R.id.menu_history).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), HistoryActivity.class)));
        view.findViewById(R.id.contact_card).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ContactActivity.class)));
        view.findViewById(R.id.ai_card).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AIActivity.class)));
        view.findViewById(R.id.btn_privacy_policy).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), WebViewActivity.class);
            intent.putExtra("url", "https://blueleafdi2.github.io/YiChanTong/privacy-policy.html");
            intent.putExtra("title", "隐私政策");
            startActivity(intent);
        });
    }
}
