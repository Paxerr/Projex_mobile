package com.example.projex_mobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.projex_mobile.adapter.AuthPagerAdapter;
import com.example.projex_mobile.utils.AuthSessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AuthActivity extends AppCompatActivity {
    public static final String SOCIAL_AUTH_CALLBACK_URI = "projex://auth/callback";

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

        handleSocialAuthCallback(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleSocialAuthCallback(intent);
    }

    private void handleSocialAuthCallback(Intent intent) {
        if (intent == null || intent.getData() == null) {
            return;
        }

        Uri uri = intent.getData();
        if (!"projex".equals(uri.getScheme())
                || !"auth".equals(uri.getHost())
                || !"/callback".equals(uri.getPath())) {
            return;
        }

        String error = getCallbackValue(uri, "error");
        if (error != null && !error.isEmpty()) {
            Toast.makeText(this, "Social login failed: " + error, Toast.LENGTH_LONG).show();
            return;
        }

        String token = firstNonEmpty(
                getCallbackValue(uri, "token"),
                getCallbackValue(uri, "access_token"),
                getCallbackValue(uri, "jwt")
        );

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Social login failed: backend did not return token", Toast.LENGTH_LONG).show();
            return;
        }

        String fullName = firstNonEmpty(
                getCallbackValue(uri, "fullName"),
                getCallbackValue(uri, "name"),
                getCallbackValue(uri, "user_name")
        );
        String email = getCallbackValue(uri, "email");
        int userId = parseInt(firstNonEmpty(
                getCallbackValue(uri, "userId"),
                getCallbackValue(uri, "user_id"),
                getCallbackValue(uri, "id")
        ));

        AuthSessionManager.saveLogin(this, token, fullName, email, userId);
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private static int parseInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static String getCallbackValue(Uri uri, String key) {
        String queryValue = uri.getQueryParameter(key);
        if (queryValue != null) {
            return queryValue;
        }

        String fragment = uri.getEncodedFragment();
        if (fragment == null || fragment.isEmpty()) {
            return null;
        }

        String[] pairs = fragment.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && key.equals(Uri.decode(parts[0]))) {
                return Uri.decode(parts[1]);
            }
        }
        return null;
    }
}
