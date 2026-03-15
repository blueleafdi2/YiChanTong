package com.jichengtong.app.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.jichengtong.app.R;
import com.jichengtong.app.data.DataProvider;
import com.jichengtong.app.models.FAQ;
import com.jichengtong.app.models.Topic;

public class TopicDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_right);
        toolbar.getNavigationIcon().setAutoMirrored(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        DataProvider data = DataProvider.getInstance(this);
        
        String topicId = getIntent().getStringExtra("topic_id");
        String faqId = getIntent().getStringExtra("faq_id");

        if (topicId != null) {
            Topic topic = data.getTopicById(topicId);
            if (topic != null) {
                toolbar.setTitle(topic.getTitle());
                ((TextView) findViewById(R.id.topic_title)).setText(topic.getTitle());
                ((TextView) findViewById(R.id.topic_content)).setText(topic.getContent());
                
                StringBuilder laws = new StringBuilder();
                if (topic.getRelatedLaws() != null) {
                    laws.append("\n📖 相关法律条文：\n");
                    for (String law : topic.getRelatedLaws()) {
                        laws.append("• ").append(law).append("\n");
                    }
                }
                ((TextView) findViewById(R.id.topic_laws)).setText(laws.toString());
            }
        } else if (faqId != null) {
            for (FAQ faq : data.getFAQs()) {
                if (faq.getId().equals(faqId)) {
                    toolbar.setTitle("问题详情");
                    ((TextView) findViewById(R.id.topic_title)).setText(faq.getQuestion());
                    ((TextView) findViewById(R.id.topic_content)).setText(faq.getAnswer());
                    
                    StringBuilder laws = new StringBuilder();
                    if (faq.getRelatedLaws() != null) {
                        laws.append("\n📖 相关法律条文：\n");
                        for (String law : faq.getRelatedLaws()) {
                            laws.append("• ").append(law).append("\n");
                        }
                    }
                    ((TextView) findViewById(R.id.topic_laws)).setText(laws.toString());
                    break;
                }
            }
        }
    }
}
