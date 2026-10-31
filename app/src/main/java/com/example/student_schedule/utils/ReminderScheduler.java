package com.example.student_schedule.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.student_schedule.models.StudySchedule;
import com.example.student_schedule.receivers.ReminderReceiver;

/**
 * Schedules a one-shot alarm 30 minutes before each study session. The alarm
 * fires {@link ReminderReceiver}, which posts the reminder notification.
 */
public final class ReminderScheduler {

    /** How long before the session starts the reminder fires. */
    public static final long REMINDER_OFFSET_MILLIS = 30L * 60L * 1000L;

    private ReminderScheduler() {
    }

    public static void schedule(Context context, StudySchedule schedule) {
        if (schedule == null || schedule.getId() <= 0) {
            return;
        }
        if (StudySchedule.STATUS_CANCELLED.equalsIgnoreCase(schedule.getStatus())) {
            return;
        }
        long startMillis = DateTimeUtils.startMillis(schedule.getStudyDate(),
                schedule.getStartTime());
        if (startMillis <= 0) {
            return;
        }
        long triggerAt = startMillis - REMINDER_OFFSET_MILLIS;
        if (triggerAt <= System.currentTimeMillis()) {
            return;
        }
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt,
                pendingIntent(context, schedule.getId()));
    }

    public static void cancel(Context context, int scheduleId) {
        if (scheduleId <= 0) {
            return;
        }
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = pendingIntent(context, scheduleId);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        pendingIntent.cancel();
    }

    private static PendingIntent pendingIntent(Context context, int scheduleId) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(ReminderReceiver.EXTRA_SCHEDULE_ID, scheduleId);
        return PendingIntent.getBroadcast(context, scheduleId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
