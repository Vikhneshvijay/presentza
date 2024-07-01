package com.example.presentza;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class login_teacher extends AppCompatActivity {

    private static final String TAG = "Login_teacher";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailEditText;
    private EditText passwordEditText;
    private String selectedRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_teacher);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        selectedRole = getIntent().getStringExtra(MainActivity.SELECTED_ROLE);

        TextView registerTextView = findViewById(R.id.register);

        emailEditText = findViewById(R.id.Unique_ID);
        passwordEditText = findViewById(R.id.First_Name);
        LinearLayout loginButton = findViewById(R.id.login_student);

        loginButton.setOnClickListener(v -> signInWithEmailAndPassword());

        registerTextView.setOnClickListener(v -> navigateToRegistration());
    }

    private void signInWithEmailAndPassword() {
        final String email = emailEditText.getText().toString();
        final String password = passwordEditText.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        checkRoleAndProceed();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(login_teacher.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkRoleAndProceed() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference docRef = db.collection("users").document(userId);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String userRole = document.getString("role");
                        if (selectedRole != null && selectedRole.equalsIgnoreCase(userRole)) {
                            Intent intent = new Intent(login_teacher.this, teacher_dashboard.class);
                            startActivity(intent);
                            finish();
                        } else {
                            mAuth.signOut();
                            Toast.makeText(login_teacher.this, "Role does not match", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(login_teacher.this, "No such document", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(login_teacher.this, "Failed to get document", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(login_teacher.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToRegistration() {
        Intent intent = new Intent(login_teacher.this, Registration.class);
        startActivity(intent);
        finish();
    }
}
