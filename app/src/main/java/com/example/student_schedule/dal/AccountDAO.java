package com.example.student_schedule.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.student_schedule.models.Account;

/**
 * Data access for the Account table.
 */
public class AccountDAO {

    private final DBHelper dbHelper;

    public AccountDAO(Context context) {
        this(DBHelper.getInstance(context));
    }

    public AccountDAO(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insert(Account account) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_ACCOUNT_USERNAME, account.getUsername());
        values.put(DBHelper.COL_ACCOUNT_PASSWORD, account.getPassword());
        values.put(DBHelper.COL_ACCOUNT_ROLE, account.getRole());
        long id = db.insert(DBHelper.TABLE_ACCOUNT, null, values);
        if (id != -1) {
            account.setId((int) id);
        }
        return id;
    }

    public Account findById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DBHelper.COL_ACCOUNT_ID + " = ?";
        String[] args = {String.valueOf(id)};
        try (Cursor cursor = db.query(DBHelper.TABLE_ACCOUNT, null, selection, args,
                null, null, null)) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    public Account findByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DBHelper.COL_ACCOUNT_USERNAME + " = ? COLLATE NOCASE";
        String[] args = {username};
        try (Cursor cursor = db.query(DBHelper.TABLE_ACCOUNT, null, selection, args,
                null, null, null)) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    /**
     * @return the matching account, or {@code null} if the credentials are wrong.
     */
    public Account login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DBHelper.COL_ACCOUNT_USERNAME + " = ? COLLATE NOCASE AND "
                + DBHelper.COL_ACCOUNT_PASSWORD + " = ?";
        String[] args = {username.trim(), password};
        try (Cursor cursor = db.query(DBHelper.TABLE_ACCOUNT, null, selection, args,
                null, null, null)) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    public boolean usernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DBHelper.COL_ACCOUNT_USERNAME + " = ? COLLATE NOCASE";
        String[] args = {username};
        try (Cursor cursor = db.query(DBHelper.TABLE_ACCOUNT,
                new String[]{"COUNT(*)"}, selection, args, null, null, null)) {
            return cursor.moveToFirst() && cursor.getInt(0) > 0;
        }
    }

    public int update(Account account) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_ACCOUNT_USERNAME, account.getUsername());
        values.put(DBHelper.COL_ACCOUNT_PASSWORD, account.getPassword());
        values.put(DBHelper.COL_ACCOUNT_ROLE, account.getRole());
        String where = DBHelper.COL_ACCOUNT_ID + " = ?";
        String[] args = {String.valueOf(account.getId())};
        return db.update(DBHelper.TABLE_ACCOUNT, values, where, args);
    }

    public int delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = DBHelper.COL_ACCOUNT_ID + " = ?";
        String[] args = {String.valueOf(id)};
        return db.delete(DBHelper.TABLE_ACCOUNT, where, args);
    }

    private Account fromCursor(Cursor cursor) {
        Account account = new Account();
        account.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_ACCOUNT_ID)));
        account.setUsername(cursor.getString(
                cursor.getColumnIndexOrThrow(DBHelper.COL_ACCOUNT_USERNAME)));
        account.setPassword(cursor.getString(
                cursor.getColumnIndexOrThrow(DBHelper.COL_ACCOUNT_PASSWORD)));
        account.setRole(cursor.getString(
                cursor.getColumnIndexOrThrow(DBHelper.COL_ACCOUNT_ROLE)));
        return account;
    }
}
