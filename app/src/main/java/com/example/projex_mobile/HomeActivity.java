package com.example.projex_mobile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.projex_mobile.fragments.HomeFragment;
import com.example.projex_mobile.fragments.NotificationFragment;
import com.example.projex_mobile.fragments.SpaceListFragment;
import com.example.projex_mobile.fragments.TaskFragment;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout navHome, navSpaces, navNotifications, navTask;
    private ImageView ivHome, ivSpaces, ivNotifications, ivTask;
    private TextView tvHome, tvSpaces, tvNotifications, tvTask;

    private static final int ACTIVE_COLOR = 0xFF85ADFF;
    private static final int INACTIVE_COLOR = 0xFF6B7280;
    private static final String KEY_SELECTED_TAB = "key_selected_tab";

    private int selectedTabId = R.id.nav_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        initViews();
        setupNavigation();

        if (savedInstanceState != null) {
            selectedTabId = savedInstanceState.getInt(KEY_SELECTED_TAB, R.id.nav_home);
        }

        showFragmentByTab(selectedTabId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SELECTED_TAB, selectedTabId);
        super.onSaveInstanceState(outState);
    }

    private void initViews() {
        navHome = findViewById(R.id.nav_home);
        navSpaces = findViewById(R.id.nav_spaces);
        navNotifications = findViewById(R.id.nav_notifications);
        navTask = findViewById(R.id.nav_tasks);

        ivHome = findViewById(R.id.iv_home);
        ivSpaces = findViewById(R.id.iv_spaces);
        ivNotifications = findViewById(R.id.iv_notifications);
        ivTask = findViewById(R.id.iv_tasks);

        tvHome = findViewById(R.id.tv_home);
        tvSpaces = findViewById(R.id.tv_spaces);
        tvNotifications = findViewById(R.id.tv_notifications);
        tvTask = findViewById(R.id.tv_tasks);
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> showHome());
        navSpaces.setOnClickListener(v -> showSpaces());
        navNotifications.setOnClickListener(v -> showNotifications());
        navTask.setOnClickListener(v -> showTask());
    }

    private void showHome() {
        selectedTabId = R.id.nav_home;
        showFragmentByTab(selectedTabId);
    }

    private void showSpaces() {
        selectedTabId = R.id.nav_spaces;
        showFragmentByTab(selectedTabId);
    }

    private void showNotifications() {
        selectedTabId = R.id.nav_notifications;
        showFragmentByTab(selectedTabId);
    }

    private void showTask() {
        selectedTabId = R.id.nav_tasks;
        showFragmentByTab(selectedTabId);
    }

    private void showFragmentByTab(int tabId) {
        Fragment fragment;

        if (tabId == R.id.nav_spaces) {
            fragment = new SpaceListFragment();
        } else if (tabId == R.id.nav_notifications) {
            fragment = new NotificationFragment();
        } else if (tabId == R.id.nav_tasks) {
            fragment = new TaskFragment();
        } else {
            fragment = new HomeFragment();
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment current = fm.findFragmentById(R.id.frame_container);

        if (current != null && current.getClass().equals(fragment.getClass())) {
            setSelectedNav(tabId);
            return;
        }

        fm.beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();

        setSelectedNav(tabId);
    }

    private void setSelectedNav(int selectedId) {
        int[] ids = {R.id.nav_home, R.id.nav_spaces, R.id.nav_notifications, R.id.nav_tasks};
        ImageView[] icons = {ivHome, ivSpaces, ivNotifications, ivTask};
        TextView[] texts = {tvHome, tvSpaces, tvNotifications, tvTask};

        for (int i = 0; i < ids.length; i++) {
            int color = ids[i] == selectedId ? ACTIVE_COLOR : INACTIVE_COLOR;
            icons[i].setColorFilter(color);
            texts[i].setTextColor(color);
        }
    }
}