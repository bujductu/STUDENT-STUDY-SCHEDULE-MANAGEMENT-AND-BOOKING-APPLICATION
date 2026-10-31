package com.example.student_schedule.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.student_schedule.R;
import com.example.student_schedule.activities.ScheduleListActivity;
import com.example.student_schedule.models.StudySchedule;

/**
 * Builds the app's local notifications: study reminders fired by
 * {@link ReminderScheduler} and informational updates posted when a session is
 * created, edited or has its status changed.
 */
public final class NotificationHelper {

    public static final String CHANNEL_ID = "study_reminders";

    private static final int CHANGED_NOTIFICATION_OFFSET = 10000;

    private NotificationHelper() {
    }

    /**
     * Creates the notification channel (API 26+) if it does not exist yet.
     * Safe to call repeatedly.
     */
    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                context.getString(R.string.notification_channel_study_reminders),
                NotificationManager.IMPORTANCE_DEFAULT);
        manager.createNotificationChannel(channel);
    }

    public static void showReminder(Context context, StudySchedule schedule) {
        if (schedule == null || !canNotify(context)) {
            return;
        }
        ensureChannel(context);

        String text = context.getString(R.string.notification_reminder_text,
                subjectName(context, schedule), schedule.getStartTime(),
                safe(schedule.getLocation()));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(context.getString(R.string.notification_reminder_title))
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(openScheduleList(context))
                .setAutoCancel(true);

        notifySafely(context, schedule.getId(), builder);
    }

    public static void notifyScheduleChanged(Context context, StudySchedule schedule,
                                             String message) {
        if (schedule == null || !canNotify(context)) {
            return;
        }
        ensureChannel(context);

        String text = context.getString(R.string.notification_changed_text,
                message == null ? "" : message,
                subjectName(context, schedule),
                DateTimeUtils.formatLocalDate(schedule.getStudyDate(),
                        DateTimeUtils.PATTERN_DISPLAY_DATE));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(context.getString(R.string.notification_changed_title))
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(openScheduleList(context))
                .setAutoCancel(true);

        notifySafely(context, CHANGED_NOTIFICATION_OFFSET + schedule.getId(), builder);
    }

    private static void notifySafely(Context context, int id,
                                     NotificationCompat.Builder builder) {
        try {
            NotificationManagerCompat.from(context).notify(id, builder.build());
        } catch (SecurityException exception) {
            // The user revoked the notification permission in the meantime.
        }
    }

    private static PendingIntent openScheduleList(Context context) {
        Intent intent = new Intent(context, ScheduleListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static boolean canNotify(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    private static String subjectName(Context context, StudySchedule schedule) {
        String name = schedule.getSubjectName();
        if (name == null || name.trim().isEmpty()) {
            return context.getString(R.string.subject_fallback, schedule.getSubjectId());
        }
        return name;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
