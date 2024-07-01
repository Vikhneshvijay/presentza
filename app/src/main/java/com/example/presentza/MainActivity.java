package com.example.presentza;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String SELECTED_ROLE = "selected_role"; // Key for selected role

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example: Click listeners for role selection
        findViewById(R.id.container_login_student).setOnClickListener(v -> {
            Log.d(TAG, "Student login clicked");
            startLoginActivity("Student");
        });

        findViewById(R.id.container_login_teacher).setOnClickListener(v -> {
            Log.d(TAG, "Teacher login clicked");
            startLoginActivity("Teacher");
        });

        findViewById(R.id.container_login_parent).setOnClickListener(v -> {
            Log.d(TAG, "Parent login clicked");
            startLoginActivity("Parent");
        });
    }

    private void startLoginActivity(String selectedRole) {
        Intent intent;
        switch (selectedRole) {
            case "Student":
                intent = new Intent(MainActivity.this, Login_student.class);
                break;
            case "Teacher":
                intent = new Intent(MainActivity.this, login_teacher.class);
                break;
            case "Parent":
                intent = new Intent(MainActivity.this, login_parent.class);
                break;
            default:
                return;
        }
        intent.putExtra(SELECTED_ROLE, selectedRole); // Pass selected role to login activity
        startActivity(intent);
    }
}

