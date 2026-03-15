package com.jichengtong.app.activities;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.jichengtong.app.R;
import com.jichengtong.app.data.DataProvider;
import com.jichengtong.app.models.FAQ;
import com.jichengtong.app.models.Topic;
import com.jichengtong.app.utils.FavoritesManager;
import com.jichengtong.app.utils.LawLinkHelper;

public class TopicDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_right);
        toolbar.getNavigationIcon().setAutoMirrored(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        DataProvider dataProvider = DataProvider.getInstance(this);
        FavoritesManager favMgr = FavoritesManager.getInstance(this);

        TextView topicTitle = findViewById(R.id.topic_title);
        TextView topicContent = findViewById(R.id.topic_content);
        TextView topicLaws = findViewById(R.id.topic_laws);

        topicContent.setMovementMethod(LinkMovementMethod.getInstance());
        topicLaws.setMovementMethod(LinkMovementMethod.getInstance());

        String topicId = getIntent().getStringExtra("topic_id");
        String faqId = getIntent().getStringExtra("faq_id");

        if (topicId != null) {
            Topic topic = dataProvider.getTopicById(topicId);
            if (topic != null) {
                favMgr.addHistory("topic", topicId, topic.getTitle());
                toolbar.setTitle(topic.getTitle());
                topicTitle.setText(topic.getTitle());
                topicContent.setText(LawLinkHelper.linkifyLawReferences(this, topic.getContent(), dataProvider));
                topicLaws.setText(LawLinkHelper.linkifyLawList(this, topic.getRelatedLaws(), dataProvider));
            }
        } else if (faqId != null) {
            for (FAQ faq : dataProvider.getFAQs()) {
                if (faq.getId().equals(faqId)) {
                    favMgr.addHistory("faq", faqId, faq.getQuestion());
                    toolbar.setTitle("问题详情");
                    topicTitle.setText(faq.getQuestion());
                    topicContent.setText(LawLinkHelper.linkifyLawReferences(this, faq.getAnswer(), dataProvider));
                    topicLaws.setText(LawLinkHelper.linkifyLawList(this, faq.getRelatedLaws(), dataProvider));
                    break;
                }
            }
        }
    }
}
