package com.example.presentza;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;


import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;



import com.harrywhewell.scrolldatepicker.DayScrollDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.joda.time.Days;
import org.joda.time.LocalDate;

public class student_dashboard extends AppCompatActivity {

    private static final String TAG = "student_dashboard";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String selectedDate;

    private final List<Lecture> lectures = new ArrayList<>();

    private DayScrollDatePicker dayScrollDatePicker;

    private LectureAdapter lectureAdapter;


    private TextView usernameTextView;



    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dayScrollDatePicker = findViewById(R.id.dayScrollDatePicker);
        usernameTextView = findViewById(R.id.Username);

        setDatePickerDate();

        RecyclerView lectureRecyclerView = findViewById(R.id.lectureRecyclerView);

        fetchAndDisplayFirstName();
        lectureRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        LectureAdapter lectureAdapter = new LectureAdapter(lectures, this::showAttendancePopup);
        lectureRecyclerView.setAdapter(lectureAdapter);

        dayScrollDatePicker.getSelectedDate(date -> {
            if (date != null) {
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                selectedDate = format.format(date);
                fetchLecturesForSelectedDate(selectedDate, lectureAdapter);
            } else {
                Toast.makeText(student_dashboard.this, "Please select a date first", Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void fetchLecturesForSelectedDate(String selectedDate, LectureAdapter lectureAdapter) {
        db.collection("lectures")
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Lecture> fetchedLectures = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            Lecture lecture = document.toObject(Lecture.class);
                            fetchedLectures.add(lecture);
                        }
                        lectureAdapter.updateLectures(fetchedLectures);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(student_dashboard.this, "Failed to fetch lectures for the selected date.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void setDatePickerDate() {


        // Ensure correct range is being set for the DayScrollDatePicker
        dayScrollDatePicker.setStartDate(1,1,2024);
        dayScrollDatePicker.setEndDate(1,1,2025);

        dayScrollDatePicker.post(this::scrollToToday);

    }




    private void fetchAndDisplayFirstName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Log.d(TAG, "Fetching details for user ID: " + userId);

            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String firstName = document.getString("firstName");
                                if (firstName != null) {
                                    Log.d(TAG, "First name found: " + firstName);
                                    updateUIWithFirstName(firstName);
                                } else {
                                    Log.d(TAG, "First name is null");
                                    Toast.makeText(student_dashboard.this, "First name not found in document.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.d(TAG, "No such document");
                                Toast.makeText(student_dashboard.this, "User details not found.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                            Toast.makeText(student_dashboard.this, "Failed to fetch user details.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.d(TAG, "No logged in user");
            Toast.makeText(student_dashboard.this, "No user is logged in.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private void scrollToToday() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = new LocalDate(2024, 1, 1);
        int position = Days.daysBetween(startDate, today).getDays();

        RecyclerView mDayRecyclerView = findViewById(com.harrywhewell.scrolldatepicker.R.id.date_picker_scroll_day_recycler_view);
        LinearLayoutManager layoutManager = (LinearLayoutManager) mDayRecyclerView.getLayoutManager();
        if (layoutManager != null) {
            layoutManager.scrollToPosition(position);
        }
    }


    @SuppressLint("SetTextI18n")
    private void updateUIWithFirstName(String firstName) {
        if (firstName != null && !firstName.isEmpty()) {
            usernameTextView.setText("Hello, " + firstName);
        } else {
            Log.d(TAG, "First name is null or empty");
        }
    }

    private void showAttendancePopup(Lecture lecture) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.otp_view, null);
        dialogBuilder.setView(dialogView);

        EditText otpEditText1 = dialogView.findViewById(R.id.otpet1);
        EditText otpEditText2 = dialogView.findViewById(R.id.otpet2);
        EditText otpEditText3 = dialogView.findViewById(R.id.otpet3);
        EditText otpEditText4 = dialogView.findViewById(R.id.otpet4);
        Button submitButton = dialogView.findViewById(R.id.submit_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        final AlertDialog alertDialog = dialogBuilder.create();

        submitButton.setOnClickListener(v -> {
            String enteredOtp = otpEditText1.getText().toString().trim() +
                    otpEditText2.getText().toString().trim() +
                    otpEditText3.getText().toString().trim() +
                    otpEditText4.getText().toString().trim();

            if (enteredOtp.equals(lecture.getOtp())) {
                markAttendance(lecture);
                alertDialog.dismiss();
            } else {
                Toast.makeText(this, "Invalid OTP, please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }
    private void markAttendance(Lecture lecture) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String attendanceId = lecture.getLectureName() + "_" + userId;
            // Create an attendance object
            Attendance attendance = new Attendance(userId, lecture.getLectureName(), lecture.getDate(), true);

            db.collection("attendance").document(attendanceId)
                    .set(attendance)
                    .addOnSuccessListener(aVoid -> Toast.makeText(student_dashboard.this, "Attendance marked successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(student_dashboard.this, "Failed to mark attendance", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(student_dashboard.this, "No user is logged in.", Toast.LENGTH_SHORT).show();
        }
    }
}


