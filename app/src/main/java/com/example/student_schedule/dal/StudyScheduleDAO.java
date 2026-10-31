package com.example.student_schedule.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.student_schedule.models.StudySchedule;

import java.util.ArrayList;
import java.util.List;

public class StudyScheduleDAO {

    private static final String SELECT_WITH_SUBJECT =
            "SELECT s." + DBHelper.COL_SCHEDULE_ID + " AS " + DBHelper.COL_SCHEDULE_ID + ", "
                    + "s." + DBHelper.COL_SCHEDULE_STUDENT_ID + " AS "
                    + DBHelper.COL_SCHEDULE_STUDENT_ID + ", "
                    + "s." + DBHelper.COL_SCHEDULE_SUBJECT_ID + " AS "
                    + DBHelper.COL_SCHEDULE_SUBJECT_ID + ", "
                    + "s." + DBHelper.COL_STUDY_DATE + " AS " + DBHelper.COL_STUDY_DATE + ", "
                    + "s." + DBHelper.COL_START_TIME + " AS " + DBHelper.COL_START_TIME + ", "
                    + "s." + DBHelper.COL_END_TIME + " AS " + DBHelper.COL_END_TIME + ", "
                    + "s." + DBHelper.COL_LOCATION + " AS " + DBHelper.COL_LOCATION + ", "
                    + "s." + DBHelper.COL_STATUS + " AS " + DBHelper.COL_STATUS + ", "
                    + "sub." + DBHelper.COL_SUBJECT_NAME + " AS subjectName "
                    + "FROM " + DBHelper.TABLE_STUDY_SCHEDULE + " s "
                    + "LEFT JOIN " + DBHelper.TABLE_SUBJECT + " sub ON sub."
                    + DBHelper.COL_SUBJECT_ID + " = s." + DBHelper.COL_SCHEDULE_SUBJECT_ID + " ";

    private static final String ORDER_BY =
            " ORDER BY s." + DBHelper.COL_STUDY_DATE + " ASC, s." + DBHelper.COL_START_TIME + " ASC";

    private static final String COLUMN_SUBJECT_NAME = "subjectName";

    private final DBHelper dbHelper;

    public StudyScheduleDAO(Context context) {
        this(DBHelper.getInstance(context));
    }

    public StudyScheduleDAO(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Inserts a schedule unless it overlaps an existing non-cancelled session
     * of the same student on the same date.
     *
     * @return the new row id, or {@code -1} on conflict (or constraint failure).
     */
    public long insert(StudySchedule schedule) {
        String status = DBHelper.normalizeStatus(schedule.getStatus());
        if (!StudySchedule.STATUS_CANCELLED.equals(status)
                && hasConflict(schedule.getStudentId(), schedule.getStudyDate(),
                schedule.getStartTime(), schedule.getEndTime(), 0)) {
            return -1;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toValues(schedule);
        values.put(DBHelper.COL_STATUS, status);
        long id = db.insert(DBHelper.TABLE_STUDY_SCHEDULE, null, values);
        if (id != -1) {
            schedule.setId((int) id);
            schedule.setStatus(status);
        }
        return id;
    }

    /**
     * Updates a schedule unless it overlaps another non-cancelled session.
     *
     * @return the number of updated rows, or {@code -1} on conflict.
     */
    public int update(StudySchedule schedule) {
        String status = DBHelper.normalizeStatus(schedule.getStatus());
        if (!StudySchedule.STATUS_CANCELLED.equals(status)
                && hasConflict(schedule.getStudentId(), schedule.getStudyDate(),
                schedule.getStartTime(), schedule.getEndTime(), schedule.getId())) {
            return -1;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toValues(schedule);
        values.put(DBHelper.COL_STATUS, status);
        String where = DBHelper.COL_SCHEDULE_ID + " = ?";
        String[] args = {String.valueOf(schedule.getId())};
        int rows = db.update(DBHelper.TABLE_STUDY_SCHEDULE, values, where, args);
        if (rows > 0) {
            schedule.setStatus(status);
        }
        return rows;
    }

    public int delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = DBHelper.COL_SCHEDULE_ID + " = ?";
        String[] args = {String.valueOf(id)};
        return db.delete(DBHelper.TABLE_STUDY_SCHEDULE, where, args);
    }

    public StudySchedule findById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(SELECT_WITH_SUBJECT + "WHERE s."
                + DBHelper.COL_SCHEDULE_ID + " = ?", new String[]{String.valueOf(id)})) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    public List<StudySchedule> getByStudent(int studentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(SELECT_WITH_SUBJECT + "WHERE s."
                + DBHelper.COL_SCHEDULE_STUDENT_ID + " = ?" + ORDER_BY,
                new String[]{String.valueOf(studentId)})) {
            return readAll(cursor);
        }
    }

    /**
     * Searches a student's schedules by subject code, subject name or location.
     */
    public List<StudySchedule> search(int studentId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getByStudent(studentId);
        }
        String like = "%" + keyword.trim() + "%";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = SELECT_WITH_SUBJECT + "WHERE s." + DBHelper.COL_SCHEDULE_STUDENT_ID
                + " = ? AND (sub." + DBHelper.COL_SUBJECT_CODE + " LIKE ? OR sub."
                + DBHelper.COL_SUBJECT_NAME + " LIKE ? OR s." + DBHelper.COL_LOCATION
                + " LIKE ?)" + ORDER_BY;
        try (Cursor cursor = db.rawQuery(sql, new String[]{
                String.valueOf(studentId), like, like, like})) {
            return readAll(cursor);
        }
    }

    /**
     * Returns schedules whose date falls between {@code fromDate} and
     * {@code toDate} (both inclusive, {@code yyyy-MM-dd}).
     */
    public List<StudySchedule> getByDateRange(int studentId, String fromDate, String toDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = SELECT_WITH_SUBJECT + "WHERE s." + DBHelper.COL_SCHEDULE_STUDENT_ID
                + " = ? AND s." + DBHelper.COL_STUDY_DATE + " BETWEEN ? AND ?" + ORDER_BY;
        try (Cursor cursor = db.rawQuery(sql, new String[]{
                String.valueOf(studentId), fromDate, toDate})) {
            return readAll(cursor);
        }
    }

    /**
     * Returns every non-cancelled session dated on or after {@code fromDate}
     * (inclusive, {@code yyyy-MM-dd}), across all students. Used to restore
     * reminder alarms after a reboot.
     */
    public List<StudySchedule> getAllFromDate(String fromDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = SELECT_WITH_SUBJECT + "WHERE s." + DBHelper.COL_STUDY_DATE
                + " >= ? AND s." + DBHelper.COL_STATUS + " <> ?" + ORDER_BY;
        try (Cursor cursor = db.rawQuery(sql, new String[]{
                fromDate, StudySchedule.STATUS_CANCELLED})) {
            return readAll(cursor);
        }
    }

    public int updateStatus(int id, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_STATUS, DBHelper.normalizeStatus(status));
        String where = DBHelper.COL_SCHEDULE_ID + " = ?";
        String[] args = {String.valueOf(id)};
        return db.update(DBHelper.TABLE_STUDY_SCHEDULE, values, where, args);
    }

    /**
     * Tests whether a session overlaps an existing non-cancelled session of the
     * same student on the same date.
     *
     * @param excludeId id to ignore (the row being updated), or {@code 0}
     */
    public boolean hasConflict(int studentId, String studyDate, String startTime,
                               String endTime, int excludeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DBHelper.COL_SCHEDULE_STUDENT_ID + " = ? AND "
                + DBHelper.COL_STUDY_DATE + " = ? AND "
                + DBHelper.COL_STATUS + " <> ? AND "
                + DBHelper.COL_START_TIME + " < ? AND "
                + DBHelper.COL_END_TIME + " > ?";
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(studentId));
        args.add(studyDate);
        args.add(StudySchedule.STATUS_CANCELLED);
        args.add(endTime);
        args.add(startTime);
        if (excludeId > 0) {
            selection += " AND " + DBHelper.COL_SCHEDULE_ID + " <> ?";
            args.add(String.valueOf(excludeId));
        }
        try (Cursor cursor = db.query(DBHelper.TABLE_STUDY_SCHEDULE,
                new String[]{"COUNT(*)"}, selection, args.toArray(new String[0]),
                null, null, null)) {
            return cursor.moveToFirst() && cursor.getInt(0) > 0;
        }
    }

    private List<StudySchedule> readAll(Cursor cursor) {
        List<StudySchedule> schedules = new ArrayList<>();
        while (cursor.moveToNext()) {
            schedules.add(fromCursor(cursor));
        }
        return schedules;
    }

    private ContentValues toValues(StudySchedule schedule) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_SCHEDULE_STUDENT_ID, schedule.getStudentId());
        values.put(DBHelper.COL_SCHEDULE_SUBJECT_ID, schedule.getSubjectId());
        values.put(DBHelper.COL_STUDY_DATE, schedule.getStudyDate());
        values.put(DBHelper.COL_START_TIME, schedule.getStartTime());
        values.put(DBHelper.COL_END_TIME, schedule.getEndTime());
        values.put(DBHelper.COL_LOCATION, schedule.getLocation());
        return values;
    }

    private StudySchedule fromCursor(Cursor cursor) {
        StudySchedule schedule = new StudySchedule();
        schedule.setId(cursor.getInt(
                cursor.getColumnIndexOrThrow(DBHelper.COL_SCHEDULE_ID)));
        schedule.setStudentId(cursor.getInt(
                cursor.getColumnIndexOrThrow(DBHelper.COL_SCHEDULE_STUDENT_ID)));
        schedule.setSubjectId(cursor.getInt(
                cursor.getColumnIndexOrThrow(DBHelper.COL_SCHEDULE_SUBJECT_ID)));
        schedule.setStudyDate(cursor.getString(
                cursor.getColumnIndexOrThrow(DBHelper.COL_STUDY_DATE)));
        schedule.setStartTime(cursor.getString(
                cursor.getColumnIndexOrThrow(DBHelper.COL_START_TIME)));
        schedule.setEndTime(cursor.getString(
                cursor.getColumnIndexOrThrow(DBHelper.COL_END_TIME)));
        schedule.setLocation(cursor.getString(
                cursor.getColumnIndexOrThrow(DBHelper.COL_LOCATION)));
        schedule.setStatus(cursor.getString(
                cursor.getColumnIndexOrThrow(DBHelper.COL_STATUS)));
        schedule.setSubjectName(cursor.getString(
                cursor.getColumnIndexOrThrow(COLUMN_SUBJECT_NAME)));
        return schedule;
    }
}
