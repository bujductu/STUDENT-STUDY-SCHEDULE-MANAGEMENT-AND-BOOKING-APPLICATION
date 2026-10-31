package com.example.student_schedule.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.student_schedule.R;
import com.example.student_schedule.adapters.ScheduleAdapter;
import com.example.student_schedule.dal.DBHelper;
import com.example.student_schedule.dal.StudyScheduleDAO;
import com.example.student_schedule.databinding.ActivityScheduleListBinding;
import com.example.student_schedule.models.StudySchedule;
import com.example.student_schedule.utils.NotificationHelper;
import com.example.student_schedule.utils.ReminderScheduler;
import com.example.student_schedule.utils.SessionManager;
import com.example.student_schedule.utils.StatusUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Student screen listing all of their study sessions with search, status
 * filtering and inline edit/delete/status actions.
 */
public class ScheduleListActivity extends AppCompatActivity
        implements ScheduleAdapter.OnScheduleActionListener {

    private ActivityScheduleListBinding binding;
    private ScheduleAdapter adapter;
    private StudyScheduleDAO scheduleDAO;
    private int studentId;

    private String currentQuery = "";
    private String currentStatusFilter;

    private final TextWatcher searchWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            currentQuery = s.toString().trim();
            loadSchedules();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

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
        binding = ActivityScheduleListBinding.inflate(getLayoutInflater());
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
        adapter = new ScheduleAdapter(this);

        binding.recyclerSchedules.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSchedules.setAdapter(adapter);
        binding.edtSearch.addTextChangedListener(searchWatcher);

        binding.chipAll.setChecked(true);
        binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            currentStatusFilter = statusFilterFor(checkedIds.isEmpty() ? 0 : checkedIds.get(0));
            loadSchedules();
        });

        binding.fabAdd.setOnClickListener(view ->
                startActivity(new Intent(this, BookingFormActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scheduleDAO != null) {
            loadSchedules();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private String statusFilterFor(int checkedChipId) {
        if (checkedChipId == R.id.chipPending) {
            return StudySchedule.STATUS_PENDING;
        }
        if (checkedChipId == R.id.chipConfirmed) {
            return StudySchedule.STATUS_CONFIRMED;
        }
        if (checkedChipId == R.id.chipCancelled) {
            return StudySchedule.STATUS_CANCELLED;
        }
        return null;
    }

    private void loadSchedules() {
        List<StudySchedule> schedules = scheduleDAO.search(studentId, currentQuery);
        List<StudySchedule> visible = new ArrayList<>();
        for (StudySchedule schedule : schedules) {
            if (currentStatusFilter == null
                    || currentStatusFilter.equalsIgnoreCase(
                    DBHelper.normalizeStatus(schedule.getStatus()))) {
                visible.add(schedule);
            }
        }
        adapter.submitList(visible);

        boolean empty = visible.isEmpty();
        binding.txtEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.txtEmpty.setText(currentQuery.isEmpty() && currentStatusFilter == null
                ? R.string.empty_schedules
                : R.string.empty_schedules_filtered);
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
            loadSchedules();
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
        loadSchedules();
    }
}
