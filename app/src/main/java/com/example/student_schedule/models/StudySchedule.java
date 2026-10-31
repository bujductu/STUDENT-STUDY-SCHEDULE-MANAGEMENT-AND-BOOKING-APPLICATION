package com.example.student_schedule.models;

public class StudySchedule {

    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_CONFIRMED = "Confirmed";
    public static final String STATUS_CANCELLED = "Cancelled";

    private int id;
    private int studentId;
    private int subjectId;
    private String studyDate;
    private String startTime;
    private String endTime;
    private String location;
    private String status;

    private String subjectName;

    public StudySchedule() {
    }

    public StudySchedule(int studentId, int subjectId, String studyDate,
                         String startTime, String endTime, String location, String status) {
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.studyDate = studyDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.status = status;
    }

    public StudySchedule(int id, int studentId, int subjectId, String studyDate,
                         String startTime, String endTime, String location, String status) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.studyDate = studyDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(String studyDate) {
        this.studyDate = studyDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    @Override
    public String toString() {
        String subject = subjectName != null ? subjectName : "Subject #" + subjectId;
        return studyDate + " " + startTime + "-" + endTime + " | " + subject
                + " | " + location + " | " + status;
    }
}
