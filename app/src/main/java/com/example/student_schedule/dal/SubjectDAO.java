package com.example.student_schedule.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.student_schedule.models.Subject;

import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {

    private static final String ORDER_BY = DBHelper.COL_SUBJECT_CODE + " ASC";

    private final DBHelper dbHelper;

    public SubjectDAO(Context context) {
        this(DBHelper.getInstance(context));
    }

    public SubjectDAO(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insert(Subject subject) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(DBHelper.TABLE_SUBJECT, null, toValues(subject));
        if (id != -1) {
            subject.setId((int) id);
        }
        return id;
    }

    public int update(Subject subject) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = DBHelper.COL_SUBJECT_ID + " = ?";
        String[] args = {String.valueOf(subject.getId())};
        return db.update(DBHelper.TABLE_SUBJECT, toValues(subject), where, args);
    }

    public int delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = DBHelper.COL_SUBJECT_ID + " = ?";
        String[] args = {String.valueOf(id)};
        return db.delete(DBHelper.TABLE_SUBJECT, where, args);
    }

    public Subject findById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DBHelper.COL_SUBJECT_ID + " = ?";
        String[] args = {String.valueOf(id)};
        try (Cursor cursor = db.query(DBHelper.TABLE_SUBJECT, null, selection, args,
                null, null, null)) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    public List<Subject> getAll() {
        return query(null, null);
    }

    public List<Subject> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAll();
        }
        String like = "%" + keyword.trim() + "%";
        String selection = DBHelper.COL_SUBJECT_CODE + " LIKE ? OR "
                + DBHelper.COL_SUBJECT_NAME + " LIKE ? OR "
                + DBHelper.COL_SUBJECT_DESCRIPTION + " LIKE ?";
        String[] args = {like, like, like};
        return query(selection, args);
    }

    public boolean subjectCodeExists(String subjectCode, int excludeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DBHelper.COL_SUBJECT_CODE + " = ? COLLATE NOCASE AND "
                + DBHelper.COL_SUBJECT_ID + " <> ?";
        String[] args = {subjectCode, String.valueOf(excludeId)};
        try (Cursor cursor = db.query(DBHelper.TABLE_SUBJECT, new String[]{"COUNT(*)"},
                selection, args, null, null, null)) {
            return cursor.moveToFirst() && cursor.getInt(0) > 0;
        }
    }

    private List<Subject> query(String selection, String[] args) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Subject> subjects = new ArrayList<>();
        try (Cursor cursor = db.query(DBHelper.TABLE_SUBJECT, null, selection, args,
                null, null, ORDER_BY)) {
            while (cursor.moveToNext()) {
                subjects.add(fromCursor(cursor));
            }
        }
        return subjects;
    }

    private ContentValues toValues(Subject subject) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_SUBJECT_CODE, subject.getSubjectCode());
        values.put(DBHelper.COL_SUBJECT_NAME, subject.getSubjectName());
        values.put(DBHelper.COL_SUBJECT_DESCRIPTION, subject.getDescription());
        values.put(DBHelper.COL_SUBJECT_CREDITS, subject.getCredits());
        return values;
    }

    private Subject fromCursor(Cursor cursor) {
        Subject subject = new Subject();
        subject.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_SUBJECT_ID)));
        subject.setSubjectCode(cursor.getString(
                cursor.getColumnIndexOrThrow(DBHelper.COL_SUBJECT_CODE)));
        subject.setSubjectName(cursor.getString(
                cursor.getColumnIndexOrThrow(DBHelper.COL_SUBJECT_NAME)));
        subject.setDescription(cursor.getString(
                cursor.getColumnIndexOrThrow(DBHelper.COL_SUBJECT_DESCRIPTION)));
        subject.setCredits(cursor.getInt(
                cursor.getColumnIndexOrThrow(DBHelper.COL_SUBJECT_CREDITS)));
        return subject;
    }
}
