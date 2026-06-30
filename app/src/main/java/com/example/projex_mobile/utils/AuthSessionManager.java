package com.example.projex_mobile.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.projex_mobile.objects.User;

public final class AuthSessionManager {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_AVATAR_URL = "avatar_url";

    private AuthSessionManager() {
    }

    public static void saveLogin(
            Context context,
            String token,
            String fullName,
            String email,
            int userId
    ) {
        String authToken = token == null ? "" : token.trim();
        if (!authToken.startsWith("Bearer ")) {
            authToken = "Bearer " + authToken;
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_TOKEN, authToken)
                .putString(KEY_USER_NAME, fullName == null ? "" : fullName)
                .putString(KEY_USER_EMAIL, email == null ? "" : email)
                .putInt(KEY_USER_ID, userId)
                .apply();
    }

    public static void saveProfile(Context context, User user) {
        if (user == null) return;

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_USER_ID, user.getId())
                .putString(KEY_USER_NAME, user.getFullName() == null ? "" : user.getFullName())
                .putString(KEY_USER_EMAIL, user.getEmail() == null ? "" : user.getEmail())
                .putString(KEY_AVATAR_URL, user.getAvatarUrl() == null ? "" : user.getAvatarUrl())
                .apply();
    }

    public static String getToken(Context context) {
        return getPrefs(context).getString(KEY_TOKEN, "");
    }

    public static boolean hasToken(Context context) {
        String token = getToken(context);
        return token != null && !token.trim().isEmpty();
    }

    public static void clearSession(Context context) {
        getPrefs(context).edit().clear().apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
