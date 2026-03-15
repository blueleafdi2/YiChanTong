package com.jichengtong.app.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.jichengtong.app.R;

public class WebViewActivity extends AppCompatActivity {
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        String url = getIntent().getStringExtra("url");
        String title = getIntent().getStringExtra("title");
        String searchText = getIntent().getStringExtra("search_text");
        String htmlContent = getIntent().getStringExtra("html_content");
        if (url == null && htmlContent == null) { finish(); return; }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(title != null ? title : "官方原文");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_right);
        toolbar.getNavigationIcon().setAutoMirrored(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setTextZoom(120);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String pageUrl) {
                super.onPageFinished(view, pageUrl);
                injectMobileFriendlyCSS(view);
                if (searchText != null && !searchText.isEmpty()) {
                    scrollToArticle(view, searchText);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient());

        if (htmlContent != null) {
            webView.loadDataWithBaseURL("https://flk.npc.gov.cn/", htmlContent, "text/html", "UTF-8", null);
        } else {
            webView.loadUrl(url);
        }
    }

    private void injectMobileFriendlyCSS(WebView view) {
        String css = "javascript:(function(){" +
            "var style=document.createElement('style');" +
            "style.textContent='" +
            "body{font-size:17px !important;line-height:1.8 !important;padding:12px !important;max-width:100vw !important;overflow-x:hidden !important;}" +
            "p,div,span,td{font-size:17px !important;line-height:1.8 !important;}" +
            ".highlight-article{background:#FFEB3B !important;padding:8px 4px !important;border-left:4px solid #1B5E20 !important;border-radius:4px !important;}" +
            "';" +
            "document.head.appendChild(style);" +
            "})()";
        view.loadUrl(css);
    }

    private void scrollToArticle(WebView view, String articleText) {
        String js = "javascript:(function(){" +
            "var searchText='" + articleText.replace("'", "\\'") + "';" +
            "function tryFind(attempts){" +
            "  if(attempts<=0)return;" +
            "  var body=document.body;" +
            "  if(!body){setTimeout(function(){tryFind(attempts-1);},500);return;}" +
            "  var walker=document.createTreeWalker(body,NodeFilter.SHOW_TEXT,null,false);" +
            "  var node;" +
            "  while(node=walker.nextNode()){" +
            "    var idx=node.textContent.indexOf(searchText);" +
            "    if(idx>=0){" +
            "      var range=document.createRange();" +
            "      range.setStart(node,idx);" +
            "      range.setEnd(node,idx+searchText.length);" +
            "      var span=document.createElement('span');" +
            "      span.className='highlight-article';" +
            "      span.id='target-article';" +
            "      range.surroundContents(span);" +
            "      span.scrollIntoView({behavior:'smooth',block:'center'});" +
            "      return;" +
            "    }" +
            "  }" +
            "  setTimeout(function(){tryFind(attempts-1);},800);" +
            "}" +
            "setTimeout(function(){tryFind(5);},1000);" +
            "})()";
        view.loadUrl(js);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
