package com.example.student_schedule.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.student_schedule.R;
import com.example.student_schedule.dal.DBHelper;
import com.example.student_schedule.dal.StudyScheduleDAO;
import com.example.student_schedule.dal.SubjectDAO;
import com.example.student_schedule.databinding.ActivityBookingFormBinding;
import com.example.student_schedule.models.StudySchedule;
import com.example.student_schedule.models.Subject;
import com.example.student_schedule.utils.DateTimeUtils;
import com.example.student_schedule.utils.NotificationHelper;
import com.example.student_schedule.utils.ReminderScheduler;
import com.example.student_schedule.utils.SessionManager;
import com.example.student_schedule.utils.StatusUtils;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Add/Edit form for a study session of the signed-in student.
 */
public class BookingFormActivity extends AppCompatActivity {

    public static final String EXTRA_SCHEDULE_ID = "extra_schedule_id";

    private static final String DATE_PICKER_TAG = "booking_date_picker";
    private static final String START_PICKER_TAG = "booking_start_time_picker";
    private static final String END_PICKER_TAG = "booking_end_time_picker";

    private ActivityBookingFormBinding binding;
    private StudyScheduleDAO scheduleDAO;
    private final List<Subject> subjects = new ArrayList<>();

    private int studentId;
    private int scheduleId;
    private boolean isEdit;
    private String selectedDate;
    private String selectedStart;
    private String selectedEnd;
    private String currentStatus = StudySchedule.STATUS_PENDING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(this);
        if (!session.isStudent() || session.getStudentId() <= 0) {
            finish();
            return;
        }
        studentId = session.getStudentId();

        EdgeToEdge.enable(this);
        binding = ActivityBookingFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        scheduleDAO = new StudyScheduleDAO(this);
        scheduleId = getIntent().getIntExtra(EXTRA_SCHEDULE_ID, 0);
        isEdit = scheduleId > 0;
        setTitle(isEdit ? R.string.title_edit_session : R.string.title_book_session);

        setupSubjectDropdown();
        setupDateField();
        setupTimeFields();

        if (isEdit) {
            loadExistingSchedule();
        } else {
            selectedDate = DateTimeUtils.todayLocal();
            updateDateText();
        }
        updateStatusText();

        binding.btnSave.setOnClickListener(view -> save());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupSubjectDropdown() {
        subjects.clear();
        subjects.addAll(new SubjectDAO(this).getAll());

        List<String> labels = new ArrayList<>();
        for (Subject subject : subjects) {
            labels.add(subject.toString());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, labels);
        binding.actSubject.setAdapter(adapter);
        binding.actSubject.setOnClickListener(view -> {
            if (!subjects.isEmpty()) {
                binding.actSubject.showDropDown();
            }
        });

        if (subjects.isEmpty()) {
            binding.tilSubject.setError(getString(R.string.error_no_subjects));
            binding.actSubject.setEnabled(false);
            binding.btnSave.setEnabled(false);
        }
    }

    private void setupDateField() {
        View.OnClickListener listener = view -> showDatePicker();
        binding.edtDate.setOnClickListener(listener);
        binding.tilDate.setOnClickListener(listener);
        binding.tilDate.setEndIconOnClickListener(listener);
    }

    private void setupTimeFields() {
        View.OnClickListener startListener = view -> showTimePicker(true);
        binding.edtStart.setOnClickListener(startListener);
        binding.tilStart.setOnClickListener(startListener);
        binding.tilStart.setEndIconOnClickListener(startListener);

        View.OnClickListener endListener = view -> showTimePicker(false);
        binding.edtEnd.setOnClickListener(endListener);
        binding.tilEnd.setOnClickListener(endListener);
        binding.tilEnd.setEndIconOnClickListener(endListener);
    }

    private void loadExistingSchedule() {
        StudySchedule schedule = scheduleDAO.findById(scheduleId);
        if (schedule == null || schedule.getStudentId() != studentId) {
            Toast.makeText(this, R.string.error_schedule_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        selectedDate = schedule.getStudyDate();
        selectedStart = schedule.getStartTime();
        selectedEnd = schedule.getEndTime();
        currentStatus = DBHelper.normalizeStatus(schedule.getStatus());

        binding.actSubject.setText(subjectLabel(schedule.getSubjectId()), false);
        binding.edtLocation.setText(schedule.getLocation() == null ? "" : schedule.getLocation());
        updateDateText();
        updateTimeText();
    }

    private String subjectLabel(int subjectId) {
        for (Subject subject : subjects) {
            if (subject.getId() == subjectId) {
                return subject.toString();
            }
        }
        return getString(R.string.subject_fallback, subjectId);
    }

    private String subjectName(int subjectId) {
        for (Subject subject : subjects) {
            if (subject.getId() == subjectId) {
                return subject.getSubjectName();
            }
        }
        return null;
    }

    private void showDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.hint_date);

        CalendarConstraints.Builder constraints = new CalendarConstraints.Builder();
        if (!isEdit) {
            constraints.setValidator(DateValidatorPointForward.now());
        }
        builder.setCalendarConstraints(constraints.build());

        if (isEdit && selectedDate != null) {
            long selection = DateTimeUtils.dateStringToUtcMillis(selectedDate);
            if (selection > 0) {
                builder.setSelection(selection);
            }
        }

        MaterialDatePicker<Long> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            selectedDate = DateTimeUtils.utcMillisToDateString(selection);
            binding.tilDate.setError(null);
            updateDateText();
        });
        picker.show(getSupportFragmentManager(), DATE_PICKER_TAG);
    }

    private void showTimePicker(boolean startTime) {
        String existing = startTime ? selectedStart : selectedEnd;
        int hour = startTime ? 9 : 10;
        int minute = 0;
        if (existing != null && existing.length() == 5 && existing.charAt(2) == ':') {
            hour = parseTimePart(existing.substring(0, 2), hour);
            minute = parseTimePart(existing.substring(3, 5), minute);
        }

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(startTime
                        ? R.string.title_pick_start_time
                        : R.string.title_pick_end_time)
                .build();

        picker.addOnPositiveButtonClickListener(view -> {
            String value = DateTimeUtils.formatTime(picker.getHour(), picker.getMinute());
            if (startTime) {
                selectedStart = value;
                binding.tilStart.setError(null);
            } else {
                selectedEnd = value;
                binding.tilEnd.setError(null);
            }
            updateTimeText();
        });
        picker.show(getSupportFragmentManager(),
                startTime ? START_PICKER_TAG : END_PICKER_TAG);
    }

    private static int parseTimePart(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private void updateDateText() {
        binding.edtDate.setText(selectedDate == null ? "" : DateTimeUtils.formatLocalDate(
                selectedDate, DateTimeUtils.PATTERN_DISPLAY_DATE));
    }

    private void updateTimeText() {
        binding.edtStart.setText(selectedStart == null ? "" : selectedStart);
        binding.edtEnd.setText(selectedEnd == null ? "" : selectedEnd);
    }

    private void updateStatusText() {
        binding.txtStatus.setText(getString(R.string.status_label,
                StatusUtils.label(this, currentStatus)));
    }

    private void save() {
        clearErrors();

        if (subjects.isEmpty()) {
            Toast.makeText(this, R.string.error_no_subjects, Toast.LENGTH_LONG).show();
            return;
        }

        int subjectId = resolveSubjectId();
        if (subjectId <= 0) {
            binding.tilSubject.setError(getString(R.string.error_subject_required));
            binding.actSubject.requestFocus();
            return;
        }
        if (selectedDate == null) {
            binding.tilDate.setError(getString(R.string.error_date_required));
            return;
        }
        if (selectedStart == null) {
            binding.tilStart.setError(getString(R.string.error_start_required));
            return;
        }
        if (selectedEnd == null) {
            binding.tilEnd.setError(getString(R.string.error_end_required));
            return;
        }

        String location = textOf();
        if (location.isEmpty()) {
            binding.tilLocation.setError(getString(R.string.error_location_required));
            binding.edtLocation.requestFocus();
            return;
        }
        if (selectedEnd.compareTo(selectedStart) <= 0) {
            binding.tilEnd.setError(getString(R.string.error_end_before_start));
            return;
        }
        if (!isEdit && selectedDate.compareTo(DateTimeUtils.todayLocal()) < 0) {
            binding.tilDate.setError(getString(R.string.error_date_in_past));
            return;
        }
        if (!isEdit && selectedDate.equals(DateTimeUtils.todayLocal())
                && selectedStart.compareTo(DateTimeUtils.currentTimeLocal()) <= 0) {
            binding.tilStart.setError(getString(R.string.error_start_in_past));
            return;
        }

        boolean cancelled = StudySchedule.STATUS_CANCELLED.equals(currentStatus);
        int excludeId = isEdit ? scheduleId : 0;
        if (!cancelled && scheduleDAO.hasConflict(studentId, selectedDate, selectedStart,
                selectedEnd, excludeId)) {
            showConflict();
            return;
        }

        StudySchedule schedule = new StudySchedule();
        if (isEdit) {
            schedule.setId(scheduleId);
        }
        schedule.setStudentId(studentId);
        schedule.setSubjectId(subjectId);
        schedule.setSubjectName(subjectName(subjectId));
        schedule.setStudyDate(selectedDate);
        schedule.setStartTime(selectedStart);
        schedule.setEndTime(selectedEnd);
        schedule.setLocation(location);
        schedule.setStatus(currentStatus);

        if (isEdit) {
            int rows = scheduleDAO.update(schedule);
            if (rows == -1) {
                showConflict();
                return;
            }
            if (rows <= 0) {
                Toast.makeText(this, R.string.error_save_failed, Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            long id = scheduleDAO.insert(schedule);
            if (id == -1) {
                showConflict();
                return;
            }
            if (id <= 0) {
                Toast.makeText(this, R.string.error_save_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            schedule.setId((int) id);
        }

        int messageId = isEdit
                ? R.string.study_session_updated
                : R.string.study_session_booked;
        String message = getString(messageId);

        if (cancelled) {
            ReminderScheduler.cancel(this, schedule.getId());
        } else {
            ReminderScheduler.schedule(this, schedule);
            NotificationHelper.notifyScheduleChanged(this, schedule, message);
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private int resolveSubjectId() {
        String text = binding.actSubject.getText() == null
                ? "" : binding.actSubject.getText().toString().trim();
        for (Subject subject : subjects) {
            if (subject.toString().equals(text)) {
                return subject.getId();
            }
        }
        return 0;
    }

    private String textOf() {
        return binding.edtLocation.getText() == null
                ? "" : binding.edtLocation.getText().toString().trim();
    }

    private void showConflict() {
        Toast.makeText(this, R.string.error_time_conflict, Toast.LENGTH_LONG).show();
    }

    private void clearErrors() {
        binding.tilSubject.setError(null);
        binding.tilDate.setError(null);
        binding.tilStart.setError(null);
        binding.tilEnd.setError(null);
        binding.tilLocation.setError(null);
    }
}
