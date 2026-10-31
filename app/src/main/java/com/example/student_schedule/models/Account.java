package com.example.student_schedule.models;

public class Account {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_STUDENT = "STUDENT";

    private int id;
    private String username;
    private String password;
    private String role;

    public Account() {
    }

    public Account(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Account(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(role);
    }

    public boolean isStudent() {
        return ROLE_STUDENT.equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
