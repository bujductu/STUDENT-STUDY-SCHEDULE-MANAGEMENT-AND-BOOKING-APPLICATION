package com.example.student_schedule.models;

/**
 * One cell of the calendar grid. The {@code date} is a stored
 * {@code yyyy-MM-dd} string so it can be compared with schedule rows.
 */
public class CalendarDay {

    private String date;
    private int dayOfMonth;
    private boolean inCurrentMonth;
    private boolean selected;
    private boolean today;
    private int sessionCount;

    public CalendarDay() {
    }

    public CalendarDay(String date, int dayOfMonth, boolean inCurrentMonth) {
        this.date = date;
        this.dayOfMonth = dayOfMonth;
        this.inCurrentMonth = inCurrentMonth;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public boolean isInCurrentMonth() {
        return inCurrentMonth;
    }

    public void setInCurrentMonth(boolean inCurrentMonth) {
        this.inCurrentMonth = inCurrentMonth;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isToday() {
        return today;
    }

    public void setToday(boolean today) {
        this.today = today;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
    }
}
