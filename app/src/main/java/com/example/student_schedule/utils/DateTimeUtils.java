package com.example.student_schedule.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Date and time helpers shared by the booking, schedule and calendar screens.
 *
 * <p>Dates are stored as local {@code yyyy-MM-dd} strings and times as
 * zero-padded {@code HH:mm} strings. {@link com.google.android.material.datepicker.MaterialDatePicker}
 * works with UTC midnight timestamps, so conversions to and from the picker
 * explicitly use UTC while every other conversion uses the device time zone.</p>
 */
public final class DateTimeUtils {

    public static final String PATTERN_DATE = "yyyy-MM-dd";
    public static final String PATTERN_TIME = "HH:mm";
    public static final String PATTERN_DISPLAY_DATE = "EEE, dd MMM yyyy";
    public static final String PATTERN_LONG_DATE = "EEEE, dd MMM yyyy";
    public static final String PATTERN_MONTH_YEAR = "MMMM yyyy";
    public static final String PATTERN_DAY_MONTH = "dd MMM";
    public static final String PATTERN_DAY_MONTH_YEAR = "dd MMM yyyy";

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private DateTimeUtils() {
    }

    /**
     * @return today's date in the device time zone as {@code yyyy-MM-dd}.
     */
    public static String todayLocal() {
        return formatter(PATTERN_DATE, TimeZone.getDefault()).format(new Date());
    }

    public static String utcMillisToDateString(long millis) {
        return formatter(PATTERN_DATE, UTC).format(new Date(millis));
    }

    public static long dateStringToUtcMillis(String date) {
        Date parsed = parse(date, PATTERN_DATE, UTC);
        return parsed == null ? 0L : parsed.getTime();
    }

    /**
     * Formats a stored {@code yyyy-MM-dd} date for display, or returns the raw
     * value when it cannot be parsed.
     */
    public static String formatLocalDate(String date, String pattern) {
        if (date == null) {
            return "";
        }
        Date parsed = parse(date, PATTERN_DATE, TimeZone.getDefault());
        if (parsed == null) {
            return date;
        }
        return formatter(pattern, TimeZone.getDefault()).format(parsed);
    }

    public static String formatCalendar(Calendar calendar, String pattern) {
        return formatter(pattern, TimeZone.getDefault()).format(calendar.getTime());
    }

    /**
     * Parses a stored date into a local {@link Calendar} set to midnight. The
     * current date is used when the value cannot be parsed.
     */
    public static Calendar toCalendar(String date) {
        Calendar calendar = Calendar.getInstance();
        Date parsed = parse(date, PATTERN_DATE, TimeZone.getDefault());
        if (parsed != null) {
            calendar.setTime(parsed);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static String toDateString(Calendar calendar) {
        return formatter(PATTERN_DATE, TimeZone.getDefault()).format(calendar.getTime());
    }

    /**
     * @return the local start-of-session timestamp, or {@code -1} when the
     * arguments cannot be parsed.
     */
    public static long startMillis(String date, String time) {
        if (date == null || time == null) {
            return -1L;
        }
        Date parsed = parse(date + " " + time, PATTERN_DATE + " " + PATTERN_TIME,
                TimeZone.getDefault());
        return parsed == null ? -1L : parsed.getTime();
    }

    public static String formatTime(int hour, int minute) {
        return String.format(Locale.US, "%02d:%02d", hour, minute);
    }

    /**
     * @return the current time in the device time zone as {@code HH:mm}.
     */
    public static String currentTimeLocal() {
        return formatter(PATTERN_TIME, TimeZone.getDefault()).format(new Date());
    }

    private static Date parse(String value, String pattern, TimeZone timeZone) {
        if (value == null) {
            return null;
        }
        try {
            SimpleDateFormat format = formatter(pattern, timeZone);
            format.setLenient(false);
            return format.parse(value);
        } catch (ParseException exception) {
            return null;
        }
    }

    private static SimpleDateFormat formatter(String pattern, TimeZone timeZone) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
        format.setTimeZone(timeZone);
        return format;
    }
}
