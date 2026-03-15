package com.jichengtong.app.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.jichengtong.app.R;
import com.jichengtong.app.data.DataProvider;
import com.jichengtong.app.models.ToolItem;

public class ToolsDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_right);
        toolbar.getNavigationIcon().setAutoMirrored(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        String toolId = getIntent().getStringExtra("tool_id");
        if (toolId == null) { finish(); return; }

        ToolItem tool = DataProvider.getInstance(this).getToolById(toolId);
        if (tool == null) { finish(); return; }

        toolbar.setTitle(tool.getTitle());
        ((TextView) findViewById(R.id.tool_title)).setText(tool.getIcon() + " " + tool.getTitle());
        ((TextView) findViewById(R.id.tool_content)).setText(tool.getContent());
    }
}
