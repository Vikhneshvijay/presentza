package com.example.presentza;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.annotation.SuppressLint;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import androidx.recyclerview.widget.LinearLayoutManager;



import com.harrywhewell.scrolldatepicker.DayScrollDatePicker;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import org.joda.time.Days;
import org.joda.time.LocalDate;

public class teacher_dashboard extends AppCompatActivity {

    private static final String TAG = "teacher_dashboard";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TableLayout attendanceTableLayout;

    private String selectedDate;

    private DayScrollDatePicker dayScrollDatePicker;
    private TextView usernameTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        dayScrollDatePicker = findViewById(R.id.dayScrollDatePicker);

        attendanceTableLayout = findViewById(R.id.attendanceTableLayout);

        usernameTextView = findViewById(R.id.Username);

        CardView addLectureButton = findViewById(R.id.add_lecture);

        CardView showattendancebutton = findViewById(R.id.show_attendance);


        setDatePickerDate();

        fetchAndDisplayFirstName();



        dayScrollDatePicker.getSelectedDate(date -> {
            if (date != null) {
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                selectedDate = format.format(date);
            } else {
                Toast.makeText(teacher_dashboard.this, "Please select a date first", Toast.LENGTH_SHORT).show();
            }
        });

        showattendancebutton.setOnClickListener(v -> {
            if (selectedDate != null) {
                fetchAttendanceForSelectedDate(selectedDate);
            } else {
                Toast.makeText(teacher_dashboard.this, "Please select a date first", Toast.LENGTH_SHORT).show();
            }
        });

        addLectureButton.setOnClickListener(v -> {
            if (selectedDate != null) {
                showAddLectureDialog(selectedDate);
            } else {
                Toast.makeText(teacher_dashboard.this, "Please select a date first", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void setDatePickerDate() {


        // Ensure correct range is being set for the DayScrollDatePicker
        dayScrollDatePicker.setStartDate(1,1,2024);
        dayScrollDatePicker.setEndDate(1,1,2025);

        dayScrollDatePicker.post(this::scrollToToday);

    }
    private void fetchUsername(String userId, final OnUsernameFetchedListener listener) {
        Log.d(TAG, "Fetching firstname and lastname for user ID: " + userId);

        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String firstname = document.getString("firstName");
                            String lastname = document.getString("lastName");
                            listener.onUsernameFetched(firstname, lastname);
                            Log.d(TAG, "Fetched firstName: " + firstname + " and lastName: " + lastname);
                        } else {
                            Log.d(TAG, "No such document for user ID: " + userId);
                            listener.onUsernameFetched(null, null);
                        }
                    } else {
                        Log.d(TAG, "Failed to fetch firstname and lastname for user ID: " + userId, task.getException());
                        listener.onUsernameFetched(null, null);
                    }
                });
    }

    private interface OnUsernameFetchedListener {
        void onUsernameFetched(String firstName, String lastName);
    }


    private void fetchAttendanceForSelectedDate(String selectedDate) {
        db.collection("attendance")
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Attendance> attendanceList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            Attendance attendance = document.toObject(Attendance.class);
                            assert attendance != null;
                            Log.d(TAG, "Fetched attendance for user ID: " + attendance.getUserId());
                            attendanceList.add(attendance);
                        }
                        fetchUsernamesForAttendance(attendanceList);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(teacher_dashboard.this, "Failed to fetch attendance for the selected date.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUsernamesForAttendance(List<Attendance> attendanceList) {
        Log.d(TAG, "Fetching firstnames and lastnames for attendance list");

        List<AttendanceWithUsername> attendanceWithUsernameList = new ArrayList<>();
        for (Attendance attendance : attendanceList) {
            fetchUsername(attendance.getUserId(), (firstName, lastName) -> {
                if (firstName != null && lastName != null) {
                    Log.d(TAG, "Fetched firstname: " + firstName + " and lastname: " + lastName + " for user ID: " + attendance.getUserId());
                    attendanceWithUsernameList.add(new AttendanceWithUsername(firstName, lastName, attendance.isPresent()));
                    if (attendanceWithUsernameList.size() == attendanceList.size()) {
                        displayAttendance(attendanceWithUsernameList);
                    }
                } else {
                    Log.d(TAG, "Firstname or lastname not found for user ID: " + attendance.getUserId());
                }
            });
        }
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
                                    Toast.makeText(teacher_dashboard.this, "First name not found in document.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.d(TAG, "No such document");
                                Toast.makeText(teacher_dashboard.this, "User details not found.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                            Toast.makeText(teacher_dashboard.this, "Failed to fetch user details.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.d(TAG, "No logged in user");
            Toast.makeText(teacher_dashboard.this, "No user is logged in.",
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

    private void showAddLectureDialog(String selectedDate){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_lecture, null);
        dialogBuilder.setView(dialogView);

        EditText editTextLectureName = dialogView.findViewById(R.id.editText);
        EditText editTextOTP1 = dialogView.findViewById(R.id.otpet1);
        EditText editTextOTP2 = dialogView.findViewById(R.id.otpet2);
        EditText editTextOTP3 = dialogView.findViewById(R.id.otpet3);
        EditText editTextOTP4 = dialogView.findViewById(R.id.otpet4);

        TextView generateOTPButton = dialogView.findViewById(R.id.generateotp);
        Button addButton = dialogView.findViewById(R.id.addbutton);
        Button cancelButton = dialogView.findViewById(R.id.cancelbutton);

        final AlertDialog alertDialog = dialogBuilder.create();

        generateOTPButton.setOnClickListener(v -> generateOTP(editTextOTP1, editTextOTP2, editTextOTP3, editTextOTP4));

        addButton.setOnClickListener(v -> {
            String lectureName = editTextLectureName.getText().toString().trim();
            String otp1 = editTextOTP1.getText().toString().trim();
            String otp2 = editTextOTP2.getText().toString().trim();
            String otp3 = editTextOTP3.getText().toString().trim();
            String otp4 = editTextOTP4.getText().toString().trim();

            // Combine OTP parts into a single OTP string
            String otp = otp1 + otp2 + otp3 + otp4;

            // Validate input fields
            if (lectureName.isEmpty() || otp.length() != 4) {
                Toast.makeText(teacher_dashboard.this, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a lecture object with the data
            Lecture lecture = new Lecture(lectureName, otp, selectedDate);

            // Add lecture data to Firestore
            db.collection("lectures")
                    .add(lecture)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(teacher_dashboard.this, "Lecture added successfully", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(teacher_dashboard.this, "Failed to add lecture", Toast.LENGTH_SHORT).show());
        });

        cancelButton.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    private void generateOTP(EditText editTextOTP1, EditText editTextOTP2, EditText editTextOTP3, EditText editTextOTP4) {
        // Generate random 4-digit OTP
        String otp = "";
        for (int i = 0; i < 4; i++) {
            otp += String.valueOf((int) (Math.random() * 10));
        }

        // Set generated OTP to EditTexts
        editTextOTP1.setText(String.valueOf(otp.charAt(0)));
        editTextOTP2.setText(String.valueOf(otp.charAt(1)));
        editTextOTP3.setText(String.valueOf(otp.charAt(2)));
        editTextOTP4.setText(String.valueOf(otp.charAt(3)));
    }

    private void displayAttendance(List<AttendanceWithUsername> attendanceList) {
        Log.d(TAG, "Displaying attendance");
        if (attendanceTableLayout != null) {
            attendanceTableLayout.removeAllViews();


            TableRow headerRow = new TableRow(this);
            headerRow.setBackgroundColor(Color.LTGRAY);
            addCellToRow(headerRow, "Username", true);
            addCellToRow(headerRow, "Status", true);
            attendanceTableLayout.addView(headerRow);


            for (AttendanceWithUsername attendance : attendanceList) {
                TableRow row = new TableRow(this);
                addCellToRow(row, attendance.getFullname(), false);
                TextView statusTextView = new TextView(this);
                statusTextView.setText(attendance.isPresent() ? "Present" : "Absent");
                statusTextView.setTextColor(Color.BLACK);


                statusTextView.setBackgroundColor(attendance.isPresent() ? Color.GREEN : Color.RED);
                statusTextView.setGravity(Gravity.CENTER);
                statusTextView.setPadding(8, 8, 8, 8);

                row.addView(statusTextView);
                attendanceTableLayout.addView(row);
            }

        }else {
            Log.d(TAG, "attendanceTableLayout is null");
        }
    }

    private void addCellToRow(TableRow row, String text, boolean isHeader) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setWidth(500);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(8, 8, 8, 8);

        if (isHeader) {
            textView.setBackgroundColor(Color.DKGRAY);
            textView.setTextColor(Color.WHITE);
        }
        else {
            textView.setTextColor(Color.BLACK);
        }
        row.addView(textView);
    }
}





