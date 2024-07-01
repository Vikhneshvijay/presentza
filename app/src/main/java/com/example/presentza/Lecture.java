package com.example.presentza;

public class Lecture {
    private String date;
    private String lectureName;
    private String otp;

    // Default constructor required for calls to DataSnapshot.getValue(Lecture.class)
    public Lecture() {
    }


    public Lecture(String lectureName, String otp, String date) {
        this.date = date;
        this.lectureName = lectureName;
        this.otp = otp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLectureName() {
        return lectureName;
    }

    public void setLectureName(String lectureName) {
        this.lectureName = lectureName;
    }
    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}



