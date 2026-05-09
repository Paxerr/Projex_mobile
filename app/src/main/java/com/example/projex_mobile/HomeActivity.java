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

public class HomeActivity extends AppCompatActivity {

    private LinearLayout navHome, navSpaces, navNotifications;
    private ImageView ivHome, ivSpaces, ivNotifications;
    private TextView tvHome, tvSpaces, tvNotifications;

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

        ivHome = findViewById(R.id.iv_home);
        ivSpaces = findViewById(R.id.iv_spaces);
        ivNotifications = findViewById(R.id.iv_notifications);

        tvHome = findViewById(R.id.tv_home);
        tvSpaces = findViewById(R.id.tv_spaces);
        tvNotifications = findViewById(R.id.tv_notifications);
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> showHome());
        navSpaces.setOnClickListener(v -> showSpaces());
        navNotifications.setOnClickListener(v -> showNotifications());
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

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }

    private void setSelectedNav(int selectedId) {
        int[] ids = {R.id.nav_home, R.id.nav_spaces, R.id.nav_notifications};
        ImageView[] icons = {ivHome, ivSpaces, ivNotifications};
        TextView[] texts = {tvHome, tvSpaces, tvNotifications};

        for (int i = 0; i < ids.length; i++) {
            int color = ids[i] == selectedId ? ACTIVE_COLOR : INACTIVE_COLOR;
            icons[i].setColorFilter(color);
            texts[i].setTextColor(color);
        }
    }
}