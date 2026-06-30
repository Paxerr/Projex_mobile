package com.example.projex_mobile.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class PrefKeyHelper {

    private PrefKeyHelper() {
    }

    public static String userScopedKey(Context context, String baseKey) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        String identity = getPrefValue(prefs, "user_id");
        if (identity.isEmpty()) identity = getPrefValue(prefs, "id");
        if (identity.isEmpty()) identity = getPrefValue(prefs, "email");
        if (identity.isEmpty()) identity = getPrefValue(prefs, "user_email");
        if (identity.isEmpty()) identity = getPrefValue(prefs, "username");
        if (identity.isEmpty()) identity = getPrefValue(prefs, "token");

        if (identity.isEmpty()) {
            return baseKey + "_guest";
        }

        return baseKey + "_" + identity.hashCode();
    }

    private static String getPrefValue(SharedPreferences prefs, String key) {
        Object value = prefs.getAll().get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }
}