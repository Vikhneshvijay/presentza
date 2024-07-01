package com.example.presentza;

public class AttendanceWithUsername {
    private final String firstname;
    private final String lastname;
    private final boolean present;

    public AttendanceWithUsername(String firstname, String lastname, boolean present) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.present = present;
    }

    public String getFullname() {
        return firstname + " " + lastname;
    }

    public boolean isPresent() {
        return present;
    }
}