package com.example.student_schedule.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.student_schedule.dal.StudyScheduleDAO;
import com.example.student_schedule.models.StudySchedule;
import com.example.student_schedule.utils.DateTimeUtils;
import com.example.student_schedule.utils.ReminderScheduler;

import java.util.List;

/**
 * Restores the session reminder alarms after a device reboot. AlarmManager
 * alarms do not survive reboots, so every upcoming session is rescheduled.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        List<StudySchedule> schedules =
                new StudyScheduleDAO(context).getAllFromDate(DateTimeUtils.todayLocal());
        for (StudySchedule schedule : schedules) {
            ReminderScheduler.schedule(context, schedule);
        }
    }
}
