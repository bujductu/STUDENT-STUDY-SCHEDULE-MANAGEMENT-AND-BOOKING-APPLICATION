package com.example.student_schedule.models;

public class Student {

    private int id;
    private String studentCode;
    private String fullname;
    private String email;
    private String phone;
    private int accountId;

    public Student() {
    }

    public Student(String studentCode, String fullname, String email, String phone, int accountId) {
        this.studentCode = studentCode;
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
        this.accountId = accountId;
    }

    public Student(int id, String studentCode, String fullname, String email, String phone, int accountId) {
        this.id = id;
        this.studentCode = studentCode;
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
        this.accountId = accountId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    @Override
    public String toString() {
        return studentCode + " - " + fullname;
    }
}
