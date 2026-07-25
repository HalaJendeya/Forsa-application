package com.example.forsa.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFERENCES_NAME =
            "forsa_session";

    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_REMEMBER_ME = "remember_me";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );
    }

    public void saveSession(long userId, boolean rememberMe) {
        preferences.edit()
                .putLong(KEY_USER_ID, userId)
                .putBoolean(KEY_REMEMBER_ME, rememberMe)
                .apply();
    }

    public long getUserId() {
        return preferences.getLong(KEY_USER_ID, -1);
    }

    public boolean shouldRememberUser() {
        return preferences.getBoolean(
                KEY_REMEMBER_ME,
                false
        );
    }

    public void clearSession() {
        preferences.edit().clear().apply();
    }
}