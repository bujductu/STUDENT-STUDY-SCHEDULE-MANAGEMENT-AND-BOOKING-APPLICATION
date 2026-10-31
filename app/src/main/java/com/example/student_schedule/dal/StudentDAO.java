package com.example.student_schedule.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.student_schedule.models.Student;

import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the Student table.
 */
public class StudentDAO {

    private static final String ORDER_BY = DBHelper.COL_STUDENT_CODE + " ASC";

    private final DBHelper dbHelper;

    public StudentDAO(Context context) {
        this(DBHelper.getInstance(context));
    }

    public StudentDAO(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insert(Student student) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(DBHelper.TABLE_STUDENT, null, toValues(student));
        if (id != -1) {
            student.setId((int) id);
        }
        return id;
    }

    public int update(Student student) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = DBHelper.COL_STUDENT_ID + " = ?";
        String[] args = {String.valueOf(student.getId())};
        return db.update(DBHelper.TABLE_STUDENT, toValues(student), where, args);
    }

    public int delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = DBHelper.COL_STUDENT_ID + " = ?";
        String[] args = {String.valueOf(id)};
        return db.delete(DBHelper.TABLE_STUDENT, where, args);
    }

    public Student findById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DBHelper.COL_STUDENT_ID + " = ?";
        String[] args = {String.valueOf(id)};
        try (Cursor cursor = db.query(DBHelper.TABLE_STUDENT, null, selection, args,
                null, null, null)) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    public Student findByAccountId(int accountId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DBHelper.COL_STUDENT_ACCOUNT_ID + " = ?";
        String[] args = {String.valueOf(accountId)};
        try (Cursor cursor = db.query(DBHelper.TABLE_STUDENT, null, selection, args,
                null, null, null)) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    public List<Student> getAll() {
        return query(null, null, ORDER_BY);
    }

    /**
     * Searches student code, full name, email and phone.
     */
    public List<Student> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAll();
        }
        String like = "%" + keyword.trim() + "%";
        String selection = DBHelper.COL_STUDENT_CODE + " LIKE ? OR "
                + DBHelper.COL_STUDENT_FULLNAME + " LIKE ? OR "
                + DBHelper.COL_STUDENT_EMAIL + " LIKE ? OR "
                + DBHelper.COL_STUDENT_PHONE + " LIKE ?";
        String[] args = {like, like, like, like};
        return query(selection, args, ORDER_BY);
    }

    public boolean studentCodeExists(String studentCode, int excludeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DBHelper.COL_STUDENT_CODE + " = ? COLLATE NOCASE AND "
                + DBHelper.COL_STUDENT_ID + " <> ?";
        String[] args = {studentCode, String.valueOf(excludeId)};
        try (Cursor cursor = db.query(DBHelper.TABLE_STUDENT, new String[]{"COUNT(*)"},
                selection, args, null, null, null)) {
            return cursor.moveToFirst() && cursor.getInt(0) > 0;
        }
    }

    private List<Student> query(String selection, String[] args, String orderBy) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Student> students = new ArrayList<>();
        try (Cursor cursor = db.query(DBHelper.TABLE_STUDENT, null, selection, args,
                null, null, orderBy)) {
            while (cursor.moveToNext()) {
                students.add(fromCursor(cursor));
            }
        }
        return students;
    }

    private ContentValues toValues(Student student) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_STUDENT_CODE, student.getStudentCode());
        values.put(DBHelper.COL_STUDENT_FULLNAME, student.getFullname());
        values.put(DBHelper.COL_STUDENT_EMAIL, student.getEmail());
        values.put(DBHelper.COL_STUDENT_PHONE, student.getPhone());
        values.put(DBHelper.COL_STUDENT_ACCOUNT_ID, student.getAccountId());
        return values;
    }

    private Student fromCursor(Cursor cursor) {
        Student student = new Student();
        student.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_ID)));
        student.setStudentCode(cursor.getString(
                cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_CODE)));
        student.setFullname(cursor.getString(
                cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_FULLNAME)));
        student.setEmail(cursor.getString(
                cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_EMAIL)));
        student.setPhone(cursor.getString(
                cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_PHONE)));
        student.setAccountId(cursor.getInt(
                cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_ACCOUNT_ID)));
        return student;
    }
}
