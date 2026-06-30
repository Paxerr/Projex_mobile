package com.example.projex_mobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.projex_mobile.adapter.AuthPagerAdapter;
import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;
import com.example.projex_mobile.objects.User;
import com.example.projex_mobile.utils.AuthSessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        if (isSocialAuthCallback(getIntent())) {
            handleSocialAuthCallback(getIntent());
        } else {
            checkSavedToken();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleSocialAuthCallback(intent);
    }

    private void handleSocialAuthCallback(Intent intent) {
        if (!isSocialAuthCallback(intent)) {
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

    private void checkSavedToken() {
        if (!AuthSessionManager.hasToken(this)) {
            return;
        }

        String token = AuthSessionManager.getToken(this);
        ApiService apiService = RetrofitClient.getApiService(token);
        apiService.getProfile(token).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    AuthSessionManager.saveProfile(AuthActivity.this, response.body());
                    startActivity(new Intent(AuthActivity.this, HomeActivity.class));
                    finish();
                    return;
                }

                if (response.code() == 401 || response.code() == 403 || response.code() == 404) {
                    AuthSessionManager.clearSession(AuthActivity.this);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(AuthActivity.this, "Không thể kiểm tra phiên đăng nhập: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isSocialAuthCallback(Intent intent) {
        if (intent == null || intent.getData() == null) {
            return false;
        }

        Uri uri = intent.getData();
        return "projex".equals(uri.getScheme())
                && "auth".equals(uri.getHost())
                && "/callback".equals(uri.getPath());
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
