package com.example.student_schedule.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.student_schedule.dal.StudyScheduleDAO;
import com.example.student_schedule.models.StudySchedule;
import com.example.student_schedule.utils.DateTimeUtils;
import com.example.student_schedule.utils.NotificationHelper;

/**
 * Receives the reminder alarm and posts the notification when the session still
 * exists, has not been cancelled and has not started yet.
 */
public class ReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_SCHEDULE_ID = "extra_reminder_schedule_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        int scheduleId = intent.getIntExtra(EXTRA_SCHEDULE_ID, 0);
        if (scheduleId <= 0) {
            return;
        }
        StudySchedule schedule = new StudyScheduleDAO(context).findById(scheduleId);
        if (schedule == null
                || StudySchedule.STATUS_CANCELLED.equalsIgnoreCase(schedule.getStatus())) {
            return;
        }
        long startMillis = DateTimeUtils.startMillis(schedule.getStudyDate(),
                schedule.getStartTime());
        if (startMillis <= System.currentTimeMillis()) {
            return;
        }
        NotificationHelper.showReminder(context, schedule);
    }
}
