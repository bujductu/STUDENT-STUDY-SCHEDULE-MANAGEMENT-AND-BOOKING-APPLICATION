package com.example.student_schedule.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.student_schedule.R;
import com.example.student_schedule.adapters.CalendarDayAdapter;
import com.example.student_schedule.adapters.ScheduleAdapter;
import com.example.student_schedule.dal.DBHelper;
import com.example.student_schedule.dal.StudyScheduleDAO;
import com.example.student_schedule.databinding.ActivityCalendarBinding;
import com.example.student_schedule.models.CalendarDay;
import com.example.student_schedule.models.StudySchedule;
import com.example.student_schedule.utils.DateTimeUtils;
import com.example.student_schedule.utils.NotificationHelper;
import com.example.student_schedule.utils.ReminderScheduler;
import com.example.student_schedule.utils.SessionManager;
import com.example.student_schedule.utils.StatusUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Day / Week / Month calendar for the signed-in student. The grid highlights
 * days with sessions and the list underneath shows the selected day.
 */
public class CalendarActivity extends AppCompatActivity
        implements ScheduleAdapter.OnScheduleActionListener, CalendarDayAdapter.OnDayClickListener {

    private static final int MODE_DAY = 0;
    private static final int MODE_WEEK = 1;
    private static final int MODE_MONTH = 2;

    private ActivityCalendarBinding binding;
    private StudyScheduleDAO scheduleDAO;
    private ScheduleAdapter scheduleAdapter;
    private CalendarDayAdapter dayAdapter;

    private int studentId;
    private int mode = MODE_MONTH;
    private String today;
    private Calendar selectedDate;
    private Calendar displayedPeriod;

    private List<StudySchedule> periodSchedules = new ArrayList<>();
    private final Map<String, Integer> sessionCounts = new HashMap<>();

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
        binding = ActivityCalendarBinding.inflate(getLayoutInflater());
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

        NotificationHelper.ensureChannel(this);
        scheduleDAO = new StudyScheduleDAO(this);
        today = DateTimeUtils.todayLocal();
        selectedDate = DateTimeUtils.toCalendar(today);
        displayedPeriod = (Calendar) selectedDate.clone();

        dayAdapter = new CalendarDayAdapter(this);
        scheduleAdapter = new ScheduleAdapter(this);

        binding.recyclerDays.setLayoutManager(new GridLayoutManager(this, 7));
        binding.recyclerDays.setAdapter(dayAdapter);
        binding.recyclerDays.setNestedScrollingEnabled(false);
        binding.recyclerSchedules.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSchedules.setAdapter(scheduleAdapter);

        binding.toggleMode.check(R.id.btnMonth);
        binding.toggleMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.btnDay) {
                mode = MODE_DAY;
            } else if (checkedId == R.id.btnWeek) {
                mode = MODE_WEEK;
            } else {
                mode = MODE_MONTH;
            }
            displayedPeriod = (Calendar) selectedDate.clone();
            refresh();
        });

        binding.btnPrev.setOnClickListener(view -> shiftPeriod(-1));
        binding.btnNext.setOnClickListener(view -> shiftPeriod(1));
        binding.fabAdd.setOnClickListener(view ->
                startActivity(new Intent(this, BookingFormActivity.class)));

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scheduleDAO != null) {
            refresh();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void shiftPeriod(int delta) {
        if (mode == MODE_DAY) {
            selectedDate.add(Calendar.DAY_OF_MONTH, delta);
        } else if (mode == MODE_WEEK) {
            selectedDate.add(Calendar.WEEK_OF_YEAR, delta);
        } else {
            selectedDate.add(Calendar.MONTH, delta);
        }
        displayedPeriod = (Calendar) selectedDate.clone();
        refresh();
    }

    private void refresh() {
        String fromDate;
        String toDate;
        List<CalendarDay> cells = new ArrayList<>();

        if (mode == MODE_DAY) {
            fromDate = DateTimeUtils.toDateString(selectedDate);
            toDate = fromDate;
            binding.recyclerDays.setVisibility(View.GONE);
            binding.layoutWeekdays.setVisibility(View.GONE);
            binding.txtPeriod.setText(DateTimeUtils.formatCalendar(selectedDate,
                    DateTimeUtils.PATTERN_LONG_DATE));
        } else if (mode == MODE_WEEK) {
            Calendar monday = mondayOf(displayedPeriod);
            Calendar sunday = (Calendar) monday.clone();
            sunday.add(Calendar.DAY_OF_MONTH, 6);
            fromDate = DateTimeUtils.toDateString(monday);
            toDate = DateTimeUtils.toDateString(sunday);
            for (int index = 0; index < 7; index++) {
                Calendar cell = (Calendar) monday.clone();
                cell.add(Calendar.DAY_OF_MONTH, index);
                cells.add(createDay(cell, true));
            }
            binding.recyclerDays.setVisibility(View.VISIBLE);
            binding.layoutWeekdays.setVisibility(View.VISIBLE);
            binding.txtPeriod.setText(getString(R.string.calendar_week_period,
                    DateTimeUtils.formatCalendar(monday, DateTimeUtils.PATTERN_DAY_MONTH),
                    DateTimeUtils.formatCalendar(sunday, DateTimeUtils.PATTERN_DAY_MONTH_YEAR)));
        } else {
            Calendar firstOfMonth = (Calendar) displayedPeriod.clone();
            firstOfMonth.set(Calendar.DAY_OF_MONTH, 1);
            Calendar start = (Calendar) firstOfMonth.clone();
            start.add(Calendar.DAY_OF_MONTH, -mondayOffset(firstOfMonth));
            Calendar end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_MONTH, 41);
            fromDate = DateTimeUtils.toDateString(start);
            toDate = DateTimeUtils.toDateString(end);

            for (int index = 0; index < 42; index++) {
                Calendar cell = (Calendar) start.clone();
                cell.add(Calendar.DAY_OF_MONTH, index);
                boolean inMonth = cell.get(Calendar.YEAR) == firstOfMonth.get(Calendar.YEAR)
                        && cell.get(Calendar.MONTH) == firstOfMonth.get(Calendar.MONTH);
                cells.add(createDay(cell, inMonth));
            }
            binding.recyclerDays.setVisibility(View.VISIBLE);
            binding.layoutWeekdays.setVisibility(View.VISIBLE);
            binding.txtPeriod.setText(DateTimeUtils.formatCalendar(displayedPeriod,
                    DateTimeUtils.PATTERN_MONTH_YEAR));
        }

        periodSchedules = scheduleDAO.getByDateRange(studentId, fromDate, toDate);
        sessionCounts.clear();
        for (StudySchedule schedule : periodSchedules) {
            Integer current = sessionCounts.get(schedule.getStudyDate());
            sessionCounts.put(schedule.getStudyDate(), current == null ? 1 : current + 1);
        }

        String selectedKey = DateTimeUtils.toDateString(selectedDate);
        for (CalendarDay day : cells) {
            day.setSelected(day.getDate().equals(selectedKey));
            day.setToday(day.getDate().equals(today));
            Integer count = sessionCounts.get(day.getDate());
            day.setSessionCount(count == null ? 0 : count);
        }
        dayAdapter.submitList(cells);

        updateScheduleList();
    }

    private void updateScheduleList() {
        String selectedKey = DateTimeUtils.toDateString(selectedDate);
        List<StudySchedule> daySchedules = new ArrayList<>();
        for (StudySchedule schedule : periodSchedules) {
            if (selectedKey.equals(schedule.getStudyDate())) {
                daySchedules.add(schedule);
            }
        }
        scheduleAdapter.submitList(daySchedules);
        binding.txtEmpty.setVisibility(daySchedules.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private CalendarDay createDay(Calendar calendar, boolean inCurrentMonth) {
        CalendarDay day = new CalendarDay();
        day.setDate(DateTimeUtils.toDateString(calendar));
        day.setDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
        day.setInCurrentMonth(inCurrentMonth);
        return day;
    }

    private static Calendar mondayOf(Calendar day) {
        Calendar monday = (Calendar) day.clone();
        monday.add(Calendar.DAY_OF_MONTH, -mondayOffset(day));
        return monday;
    }

    /**
     * Monday-first offset: 0 for Monday, 6 for Sunday.
     */
    private static int mondayOffset(Calendar day) {
        return (day.get(Calendar.DAY_OF_WEEK) + 5) % 7;
    }

    @Override
    public void onDayClick(CalendarDay day) {
        if (day == null || day.getDate() == null) {
            return;
        }
        selectedDate = DateTimeUtils.toCalendar(day.getDate());
        refresh();
    }

    @Override
    public void onEdit(StudySchedule schedule) {
        Intent intent = new Intent(this, BookingFormActivity.class);
        intent.putExtra(BookingFormActivity.EXTRA_SCHEDULE_ID, schedule.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(StudySchedule schedule) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_schedule_title)
                .setMessage(R.string.delete_schedule_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete,
                        (dialog, which) -> deleteSchedule(schedule))
                .show();
    }

    private void deleteSchedule(StudySchedule schedule) {
        int rows = scheduleDAO.delete(schedule.getId());
        if (rows > 0) {
            ReminderScheduler.cancel(this, schedule.getId());
            Toast.makeText(this, R.string.schedule_deleted, Toast.LENGTH_SHORT).show();
            refresh();
        } else {
            Toast.makeText(this, R.string.error_delete_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStatusClick(StudySchedule schedule) {
        final String[] statuses = {
                StudySchedule.STATUS_PENDING,
                StudySchedule.STATUS_CONFIRMED,
                StudySchedule.STATUS_CANCELLED
        };
        String[] labels = new String[statuses.length];
        for (int index = 0; index < statuses.length; index++) {
            labels[index] = StatusUtils.label(this, statuses[index]);
        }
        String current = DBHelper.normalizeStatus(schedule.getStatus());
        int checked = 0;
        for (int index = 0; index < statuses.length; index++) {
            if (statuses[index].equalsIgnoreCase(current)) {
                checked = index;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.status_dialog_title)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    dialog.dismiss();
                    applyStatus(schedule, statuses[which]);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void applyStatus(StudySchedule schedule, String status) {
        int rows = scheduleDAO.updateStatus(schedule.getId(), status);
        if (rows <= 0) {
            Toast.makeText(this, R.string.error_save_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        schedule.setStatus(status);
        if (StudySchedule.STATUS_CANCELLED.equals(status)) {
            ReminderScheduler.cancel(this, schedule.getId());
        } else {
            ReminderScheduler.schedule(this, schedule);
        }
        NotificationHelper.notifyScheduleChanged(this, schedule,
                getString(R.string.status_changed, StatusUtils.label(this, status)));
        refresh();
    }
}
