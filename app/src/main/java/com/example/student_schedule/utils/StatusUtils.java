package com.example.student_schedule.utils;

import android.content.Context;

import com.example.student_schedule.R;
import com.example.student_schedule.models.StudySchedule;

/**
 * Maps the canonical status values stored in the database to localized labels
 * for display.
 */
public final class StatusUtils {

    private StatusUtils() {
    }

    public static int labelRes(String status) {
        if (StudySchedule.STATUS_CONFIRMED.equalsIgnoreCase(status)) {
            return R.string.status_confirmed;
        }
        if (StudySchedule.STATUS_CANCELLED.equalsIgnoreCase(status)) {
            return R.string.status_cancelled;
        }
        return R.string.status_pending;
    }

    public static String label(Context context, String status) {
        return context.getString(labelRes(status));
    }
}
