package com.example.presentza;

public class Attendance {
    private String userId;
    private String lectureName;
    private String date;
    private boolean present;

    public Attendance() {
        // Default constructor required for calls to DataSnapshot.getValue(Attendance.class)
    }

    public Attendance(String userId, String lectureName, String date, boolean present) {
        this.userId = userId;
        this.lectureName = lectureName;
        this.date = date;
        this.present = present;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLectureName() {
        return lectureName;
    }

    public void setLectureName(String lectureName) {
        this.lectureName = lectureName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }
}

