package com.jichengtong.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jichengtong.app.R;
import com.jichengtong.app.fragments.*;
import com.jichengtong.app.utils.Analytics;
import com.jichengtong.app.utils.RemoteConfig;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_HOME = "frag_home";
    private static final String TAG_LAWS = "frag_laws";
    private static final String TAG_CASES = "frag_cases";
    private static final String TAG_TOOLS = "frag_tools";
    private static final String TAG_MINE = "frag_mine";
    private String activeTag = TAG_HOME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        Analytics.getInstance(this).logEvent("app_open");
        RemoteConfig.getInstance(this).fetchAsync(() -> checkForUpdates());

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new HomeFragment(), TAG_HOME)
                    .commit();
        } else {
            activeTag = savedInstanceState.getString("active_tag", TAG_HOME);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            String tag;
            int id = item.getItemId();
            if (id == R.id.nav_home) tag = TAG_HOME;
            else if (id == R.id.nav_laws) tag = TAG_LAWS;
            else if (id == R.id.nav_cases) tag = TAG_CASES;
            else if (id == R.id.nav_tools) tag = TAG_TOOLS;
            else if (id == R.id.nav_mine) tag = TAG_MINE;
            else return false;
            switchFragment(tag);
            return true;
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("active_tag", activeTag);
    }

    private void switchFragment(String tag) {
        if (tag.equals(activeTag)) return;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment current = getSupportFragmentManager().findFragmentByTag(activeTag);
        if (current != null) ft.hide(current);

        Fragment target = getSupportFragmentManager().findFragmentByTag(tag);
        if (target == null) {
            target = createFragment(tag);
            ft.add(R.id.fragment_container, target, tag);
        } else {
            ft.show(target);
        }
        ft.commit();
        activeTag = tag;
    }

    private void checkForUpdates() {
        RemoteConfig rc = RemoteConfig.getInstance(this);
        int currentVersion = 12;
        try {
            currentVersion = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionCode;
        } catch (Exception ignored) {}

        if (rc.isAnnouncementEnabled()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(rc.getAnnouncementTitle())
                    .setMessage(rc.getAnnouncementMessage())
                    .setPositiveButton("查看", (d, w) -> {
                        String url = rc.getAnnouncementUrl();
                        if (!url.isEmpty()) {
                            startActivity(new Intent(this, WebViewActivity.class)
                                    .putExtra("url", url).putExtra("title", "公告"));
                        }
                    })
                    .setNegativeButton("关闭", null)
                    .show();
        } else if (currentVersion < rc.getMinVersion()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("发现新版本")
                    .setMessage("当前版本过低，请更新到最新版本以获得最佳体验。")
                    .setPositiveButton("立即更新", (d, w) -> {
                        String url = rc.getUpdateUrl();
                        if (!url.isEmpty()) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    private Fragment createFragment(String tag) {
        switch (tag) {
            case TAG_LAWS: return new LawsFragment();
            case TAG_CASES: return new CasesFragment();
            case TAG_TOOLS: return new ToolsFragment();
            case TAG_MINE: return new MineFragment();
            default: return new HomeFragment();
        }
    }
}
