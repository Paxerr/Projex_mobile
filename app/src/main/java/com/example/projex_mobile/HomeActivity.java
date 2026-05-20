package com.example.projex_mobile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.fragments.HomeFragment;
import com.example.projex_mobile.fragments.NotificationFragment;
import com.example.projex_mobile.fragments.SpaceFragment;
import com.example.projex_mobile.fragments.TaskFragment;
import com.example.projex_mobile.fragments.ProTaskFragment;
public class HomeActivity extends AppCompatActivity {

    private LinearLayout navHome, navSpaces, navNotifications,navTask;
    private ImageView ivHome, ivSpaces, ivNotifications,ivTask;
    private TextView tvHome, tvSpaces, tvNotifications,tvTask;

    private static final int ACTIVE_COLOR = 0xFF85ADFF;
    private static final int INACTIVE_COLOR = 0xFF6B7280;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        initViews();
        setupNavigation();

        if (savedInstanceState == null) {
            showHome();
        }
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
        replaceFragment(new HomeFragment());
        setSelectedNav(R.id.nav_home);
    }

    private void showSpaces() {
        replaceFragment(new SpaceFragment());
        setSelectedNav(R.id.nav_spaces);
    }

    private void showNotifications() {
        replaceFragment(new NotificationFragment());
        setSelectedNav(R.id.nav_notifications);
    }

    private void showTask() {
        replaceFragment(new TaskFragment());
        setSelectedNav(R.id.nav_tasks);
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }

    private void setSelectedNav(int selectedId) {
        int[] ids = {R.id.nav_home, R.id.nav_spaces, R.id.nav_notifications,R.id.nav_tasks};
        ImageView[] icons = {ivHome, ivSpaces, ivNotifications,ivTask};
        TextView[] texts = {tvHome, tvSpaces, tvNotifications,tvTask};

        for (int i = 0; i < ids.length; i++) {
            int color = ids[i] == selectedId ? ACTIVE_COLOR : INACTIVE_COLOR;
            icons[i].setColorFilter(color);
            texts[i].setTextColor(color);
        }
    }
}