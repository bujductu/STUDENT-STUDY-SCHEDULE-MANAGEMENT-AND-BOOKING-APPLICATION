package com.example.student_schedule.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.student_schedule.models.Account;
import com.example.student_schedule.models.StudySchedule;

/**
 * Creates and owns the SQLite database for the study schedule application.
 *
 * <p>Times are stored as zero-padded {@code HH:mm} strings and dates as
 * {@code yyyy-MM-dd} strings so that lexicographic comparison equals
 * chronological comparison.</p>
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "StudentSchedule.db";
    public static final int DB_VERSION = 1;

    // ---------------------------------------------------------------------
    // Account
    // ---------------------------------------------------------------------
    public static final String TABLE_ACCOUNT = "Account";
    public static final String COL_ACCOUNT_ID = "id";
    public static final String COL_ACCOUNT_USERNAME = "username";
    public static final String COL_ACCOUNT_PASSWORD = "password";
    public static final String COL_ACCOUNT_ROLE = "role";

    // ---------------------------------------------------------------------
    // Student
    // ---------------------------------------------------------------------
    public static final String TABLE_STUDENT = "Student";
    public static final String COL_STUDENT_ID = "id";
    public static final String COL_STUDENT_CODE = "studentCode";
    public static final String COL_STUDENT_FULLNAME = "fullname";
    public static final String COL_STUDENT_EMAIL = "email";
    public static final String COL_STUDENT_PHONE = "phone";
    public static final String COL_STUDENT_ACCOUNT_ID = "accountId";

    // ---------------------------------------------------------------------
    // Subject
    // ---------------------------------------------------------------------
    public static final String TABLE_SUBJECT = "Subject";
    public static final String COL_SUBJECT_ID = "id";
    public static final String COL_SUBJECT_CODE = "subjectCode";
    public static final String COL_SUBJECT_NAME = "subjectName";
    public static final String COL_SUBJECT_DESCRIPTION = "description";
    public static final String COL_SUBJECT_CREDITS = "credits";

    // ---------------------------------------------------------------------
    // StudySchedule
    // ---------------------------------------------------------------------
    public static final String TABLE_STUDY_SCHEDULE = "StudySchedule";
    public static final String COL_SCHEDULE_ID = "id";
    public static final String COL_SCHEDULE_STUDENT_ID = "studentId";
    public static final String COL_SCHEDULE_SUBJECT_ID = "subjectId";
    public static final String COL_STUDY_DATE = "studyDate";
    public static final String COL_START_TIME = "startTime";
    public static final String COL_END_TIME = "endTime";
    public static final String COL_LOCATION = "location";
    public static final String COL_STATUS = "status";

    // ---------------------------------------------------------------------
    // Seed data
    // ---------------------------------------------------------------------
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    public static final String DEFAULT_STUDENT_USERNAME = "student1";
    public static final String DEFAULT_STUDENT_PASSWORD = "123456";

    private static final String CREATE_TABLE_ACCOUNT =
            "CREATE TABLE " + TABLE_ACCOUNT + " ("
                    + COL_ACCOUNT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_ACCOUNT_USERNAME + " TEXT NOT NULL UNIQUE, "
                    + COL_ACCOUNT_PASSWORD + " TEXT NOT NULL, "
                    + COL_ACCOUNT_ROLE + " TEXT NOT NULL "
                    + "CHECK(" + COL_ACCOUNT_ROLE + " IN ('ADMIN','STUDENT'))"
                    + ")";

    private static final String CREATE_TABLE_STUDENT =
            "CREATE TABLE " + TABLE_STUDENT + " ("
                    + COL_STUDENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_STUDENT_CODE + " TEXT NOT NULL UNIQUE, "
                    + COL_STUDENT_FULLNAME + " TEXT NOT NULL, "
                    + COL_STUDENT_EMAIL + " TEXT, "
                    + COL_STUDENT_PHONE + " TEXT, "
                    + COL_STUDENT_ACCOUNT_ID + " INTEGER NOT NULL, "
                    + "FOREIGN KEY(" + COL_STUDENT_ACCOUNT_ID + ") REFERENCES "
                    + TABLE_ACCOUNT + "(" + COL_ACCOUNT_ID + ") ON DELETE CASCADE"
                    + ")";

    private static final String CREATE_TABLE_SUBJECT =
            "CREATE TABLE " + TABLE_SUBJECT + " ("
                    + COL_SUBJECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_SUBJECT_CODE + " TEXT NOT NULL UNIQUE, "
                    + COL_SUBJECT_NAME + " TEXT NOT NULL, "
                    + COL_SUBJECT_DESCRIPTION + " TEXT, "
                    + COL_SUBJECT_CREDITS + " INTEGER NOT NULL DEFAULT 0"
                    + ")";

    private static final String CREATE_TABLE_STUDY_SCHEDULE =
            "CREATE TABLE " + TABLE_STUDY_SCHEDULE + " ("
                    + COL_SCHEDULE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_SCHEDULE_STUDENT_ID + " INTEGER NOT NULL, "
                    + COL_SCHEDULE_SUBJECT_ID + " INTEGER NOT NULL, "
                    + COL_STUDY_DATE + " TEXT NOT NULL, "
                    + COL_START_TIME + " TEXT NOT NULL, "
                    + COL_END_TIME + " TEXT NOT NULL, "
                    + COL_LOCATION + " TEXT, "
                    + COL_STATUS + " TEXT NOT NULL DEFAULT 'Pending' "
                    + "CHECK(" + COL_STATUS + " IN ('Pending','Confirmed','Cancelled')), "
                    + "FOREIGN KEY(" + COL_SCHEDULE_STUDENT_ID + ") REFERENCES "
                    + TABLE_STUDENT + "(" + COL_STUDENT_ID + ") ON DELETE CASCADE, "
                    + "FOREIGN KEY(" + COL_SCHEDULE_SUBJECT_ID + ") REFERENCES "
                    + TABLE_SUBJECT + "(" + COL_SUBJECT_ID + ") ON DELETE CASCADE"
                    + ")";

    private static final String CREATE_INDEX_SCHEDULE_STUDENT_DATE =
            "CREATE INDEX idx_schedule_student_date ON " + TABLE_STUDY_SCHEDULE
                    + "(" + COL_SCHEDULE_STUDENT_ID + ", " + COL_STUDY_DATE + ")";

    private static volatile DBHelper instance;

    public static DBHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DBHelper.class) {
                if (instance == null) {
                    instance = new DBHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // SQLite disables foreign keys by default on every connection.
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ACCOUNT);
        db.execSQL(CREATE_TABLE_STUDENT);
        db.execSQL(CREATE_TABLE_SUBJECT);
        db.execSQL(CREATE_TABLE_STUDY_SCHEDULE);
        db.execSQL(CREATE_INDEX_SCHEDULE_STUDENT_DATE);

        seedData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Development friendly: recreate everything on a schema bump.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDY_SCHEDULE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBJECT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);
        onCreate(db);
    }

    private void seedData(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            insertAccount(db, DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD, Account.ROLE_ADMIN);
            long studentAccountId = insertAccount(
                    db, DEFAULT_STUDENT_USERNAME, DEFAULT_STUDENT_PASSWORD, Account.ROLE_STUDENT);

            ContentValues student = new ContentValues();
            student.put(COL_STUDENT_CODE, "SV001");
            student.put(COL_STUDENT_FULLNAME, "Nguyen Van A");
            student.put(COL_STUDENT_EMAIL, "nguyenvana@example.com");
            student.put(COL_STUDENT_PHONE, "0901234567");
            student.put(COL_STUDENT_ACCOUNT_ID, studentAccountId);
            db.insertOrThrow(TABLE_STUDENT, null, student);

            insertSubject(db, "PRJ301", "Java Web Application Development",
                    "Build web applications with Java Servlets and JSP.", 3);
            insertSubject(db, "MAD101", "Discrete Mathematics",
                    "Logic, sets, relations, graphs and combinatorics.", 3);
            insertSubject(db, "DBI202", "Database Systems",
                    "Relational model, SQL and database design.", 3);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private long insertAccount(SQLiteDatabase db, String username, String password, String role) {
        ContentValues values = new ContentValues();
        values.put(COL_ACCOUNT_USERNAME, username);
        values.put(COL_ACCOUNT_PASSWORD, password);
        values.put(COL_ACCOUNT_ROLE, role);
        return db.insertOrThrow(TABLE_ACCOUNT, null, values);
    }

    private void insertSubject(SQLiteDatabase db, String code, String name,
                               String description, int credits) {
        ContentValues values = new ContentValues();
        values.put(COL_SUBJECT_CODE, code);
        values.put(COL_SUBJECT_NAME, name);
        values.put(COL_SUBJECT_DESCRIPTION, description);
        values.put(COL_SUBJECT_CREDITS, credits);
        db.insertOrThrow(TABLE_SUBJECT, null, values);
    }

    /**
     * Normalizes a status coming from the UI. Unknown or empty values fall back
     * to {@link StudySchedule#STATUS_PENDING}.
     */
    public static String normalizeStatus(String status) {
        if (status == null) {
            return StudySchedule.STATUS_PENDING;
        }
        String trimmed = status.trim();
        if (StudySchedule.STATUS_CONFIRMED.equalsIgnoreCase(trimmed)) {
            return StudySchedule.STATUS_CONFIRMED;
        }
        if (StudySchedule.STATUS_CANCELLED.equalsIgnoreCase(trimmed)) {
            return StudySchedule.STATUS_CANCELLED;
        }
        return StudySchedule.STATUS_PENDING;
    }
}
