package com.jichengtong.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.jichengtong.app.R;
import com.jichengtong.app.activities.CaseDetailActivity;
import com.jichengtong.app.adapters.CaseAdapter;
import com.jichengtong.app.data.DataProvider;
import com.jichengtong.app.models.CourtCase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class CasesFragment extends Fragment {
    private CaseAdapter adapter;
    private DataProvider data;
    private String selectedType = null;
    private String selectedYear = null;
    private String selectedProvince = null;
    private String selectedTag = null;
    private TextView caseCountText;
    private View emptyState;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cases, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        data = DataProvider.getInstance(requireContext());
        caseCountText = view.findViewById(R.id.case_count_text);
        emptyState = view.findViewById(R.id.empty_state);

        RecyclerView rv = view.findViewById(R.id.cases_rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CaseAdapter();
        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(requireContext(), CaseDetailActivity.class);
            intent.putExtra("case_id", item.getId());
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        setupTagSpinner(view);
        setupTypeChips(view);
        setupYearSpinner(view);
        setupProvinceSpinner(view);
        applyFilters();
    }

    private void setupTagSpinner(View view) {
        Spinner tagSpinner = view.findViewById(R.id.tag_spinner);
        TreeSet<String> allTags = new TreeSet<>();
        for (CourtCase c : data.getCourtCases()) {
            if (c.getTags() != null) allTags.addAll(c.getTags());
        }
        List<String> tagList = new ArrayList<>();
        tagList.add("全部标签");
        tagList.addAll(allTags);
        ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tagList);
        tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagSpinner.setAdapter(tagAdapter);
        tagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedTag = pos == 0 ? null : tagList.get(pos);
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void setupTypeChips(View view) {
        ChipGroup filterChips = view.findViewById(R.id.filter_chips);
        List<String> caseTypes = data.getCaseTypes();

        Chip allChip = new Chip(requireContext());
        allChip.setText("全部类型");
        allChip.setChecked(true);
        allChip.setCheckable(true);
        allChip.setOnClickListener(v -> { selectedType = null; applyFilters(); });
        filterChips.addView(allChip);

        for (String type : caseTypes) {
            Chip chip = new Chip(requireContext());
            chip.setText(type);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> { selectedType = type; applyFilters(); });
            filterChips.addView(chip);
        }
    }

    private void setupYearSpinner(View view) {
        Spinner yearSpinner = view.findViewById(R.id.year_spinner);
        TreeSet<String> years = new TreeSet<>((a, b) -> b.compareTo(a));
        for (CourtCase c : data.getCourtCases()) {
            if (c.getJudgeDate() != null && c.getJudgeDate().length() >= 4)
                years.add(c.getJudgeDate().substring(0, 4));
        }
        List<String> yearList = new ArrayList<>();
        yearList.add("全部年份");
        yearList.addAll(years);
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, yearList);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedYear = pos == 0 ? null : yearList.get(pos);
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void setupProvinceSpinner(View view) {
        Spinner provinceSpinner = view.findViewById(R.id.province_spinner);
        TreeSet<String> provinces = new TreeSet<>();
        for (CourtCase c : data.getCourtCases()) {
            if (c.getProvince() != null) provinces.add(c.getProvince());
        }
        List<String> provList = new ArrayList<>();
        provList.add("全部地区");
        provList.addAll(provinces);
        ArrayAdapter<String> provAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, provList);
        provAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        provinceSpinner.setAdapter(provAdapter);
        provinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedProvince = pos == 0 ? null : provList.get(pos);
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void applyFilters() {
        List<CourtCase> filtered = new ArrayList<>();
        for (CourtCase c : data.getCourtCases()) {
            if (selectedType != null && (c.getCaseType() == null || !c.getCaseType().contains(selectedType))) continue;
            if (selectedYear != null && (c.getJudgeDate() == null || !c.getJudgeDate().startsWith(selectedYear))) continue;
            if (selectedProvince != null && (c.getProvince() == null || !c.getProvince().equals(selectedProvince))) continue;
            if (selectedTag != null && (c.getTags() == null || !c.getTags().contains(selectedTag))) continue;
            filtered.add(c);
        }
        Collections.sort(filtered, (a, b) -> {
            String da = a.getJudgeDate() != null ? a.getJudgeDate() : "";
            String db = b.getJudgeDate() != null ? b.getJudgeDate() : "";
            return db.compareTo(da);
        });
        adapter.setItems(filtered);
        if (caseCountText != null) {
            caseCountText.setText("共 " + filtered.size() + " 件");
        }
        if (emptyState != null) {
            emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}
