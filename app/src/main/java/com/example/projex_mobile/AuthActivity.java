package com.example.projex_mobile;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.projex_mobile.adapter.AuthPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AuthActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private View authContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_activity);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        authContent = findViewById(R.id.authContent);

        AuthPagerAdapter adapter = new AuthPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(
                tabLayout,
                viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Đăng nhập");
                    } else {
                        tab.setText("Đăng ký");
                    }
                }
        ).attach();

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            boolean hasOverlay = getSupportFragmentManager().getBackStackEntryCount() > 0;
            authContent.setVisibility(hasOverlay ? View.GONE : View.VISIBLE);
        });
    }
}
