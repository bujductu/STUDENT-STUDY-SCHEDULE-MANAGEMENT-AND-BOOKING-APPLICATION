package com.example.student_schedule.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.student_schedule.models.Account;

/**
 * Stores the signed-in account in SharedPreferences so the session survives
 * app restarts until the user logs out.
 *
 * <p>Only identity information is stored here; the password stays in the
 * SQLite Account table.</p>
 */
public class SessionManager {

    private static final String PREFS_NAME = "student_schedule_session";

    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_ACCOUNT_ID = "account_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_STUDENT_ID = "student_id";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        this.preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(Account account, int studentId) {
        preferences.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putInt(KEY_ACCOUNT_ID, account.getId())
                .putString(KEY_USERNAME, account.getUsername())
                .putString(KEY_ROLE, account.getRole())
                .putInt(KEY_STUDENT_ID, studentId)
                .apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_LOGGED_IN, false)
                && getAccountId() > 0;
    }

    public int getAccountId() {
        return preferences.getInt(KEY_ACCOUNT_ID, -1);
    }

    public String getUsername() {
        return preferences.getString(KEY_USERNAME, "");
    }

    public String getRole() {
        return preferences.getString(KEY_ROLE, "");
    }

    /**
     * @return the linked Student row id, or {@code -1} for administrators.
     */
    public int getStudentId() {
        return preferences.getInt(KEY_STUDENT_ID, -1);
    }

    public boolean isAdmin() {
        return Account.ROLE_ADMIN.equalsIgnoreCase(getRole());
    }

    public boolean isStudent() {
        return Account.ROLE_STUDENT.equalsIgnoreCase(getRole());
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
