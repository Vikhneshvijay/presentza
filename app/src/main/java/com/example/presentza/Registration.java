package com.example.presentza;

import android.os.Bundle;


import android.widget.EditText;

import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Registration extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, mobileNumberEditText, emailEditText, uniqueIdEditText, passwordEditText, confirmPasswordEditText;
    private RadioGroup roleRadioGroup;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        firstNameEditText = findViewById(R.id.First_Name);
        lastNameEditText = findViewById(R.id.Last_Name);
        mobileNumberEditText = findViewById(R.id.Mobile_number);
        emailEditText = findViewById(R.id.Email);
        uniqueIdEditText = findViewById(R.id.Unique_ID);
        passwordEditText = findViewById(R.id.passwordtext);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        roleRadioGroup = findViewById(R.id.role);

        TextView registerButton = findViewById(R.id.registerbutton);
        TextView alreadyHaveAccountTextView = findViewById(R.id.already);


        registerButton.setOnClickListener(v -> registerUser());
        alreadyHaveAccountTextView.setOnClickListener(v -> {
            Intent intent = new Intent(Registration.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void registerUser() {
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String mobileNumber = mobileNumberEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String uniqueId = uniqueIdEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();
        int selectedRoleId = roleRadioGroup.getCheckedRadioButtonId();

        if (selectedRoleId == -1) {
            Toast.makeText(this, "Please select a role!", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRoleRadioButton = findViewById(selectedRoleId);
        String selectedRole = selectedRoleRadioButton.getText().toString();

        if (validateFields(firstName, lastName, mobileNumber, email, uniqueId, password, confirmPassword, selectedRole )) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            addUserToFirestore(user.getUid(), firstName, lastName, mobileNumber, email, uniqueId, selectedRole);
                            Toast.makeText(Registration.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Registration.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            // Add additional user data to Firebase Realtime Database or Firestore
                        } else {
                            Toast.makeText(Registration.this, "Registration failed!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

    private boolean validateFields(String firstName, String lastName, String mobileNumber, String email, String uniqueId, String password, String confirmPassword, String selectedRole) {
        if (firstName.isEmpty() || lastName.isEmpty() || mobileNumber.isEmpty() || email.isEmpty() || uniqueId.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || selectedRole.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addUserToFirestore(String uid, String firstName, String lastName, String mobileNumber, String email, String uniqueId, String selectedRole) {
        boolean isApproved = !selectedRole.equals("Teacher");
        User user = new User(firstName, lastName, mobileNumber, email, uniqueId, selectedRole, isApproved);
        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    // User added successfully
                })
                .addOnFailureListener(e -> {
                    // Failed to add user
                });
    }

    @SuppressWarnings("unused")
    public static class User {
        private String firstName;
        private String lastName;
        private String mobileNumber;
        private String email;
        private String uniqueId;

        private boolean isApproved;
        private String role;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String firstName, String lastName, String mobileNumber, String email, String uniqueId, String role, boolean isApproved) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.mobileNumber = mobileNumber;
            this.email = email;
            this.uniqueId = uniqueId;
            this.role = role;
            this.isApproved = isApproved;
        }

        // Getters
        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getMobileNumber() {
            return mobileNumber;
        }

        public String getEmail() {
            return email;
        }

        public String getUniqueId() {
            return uniqueId;
        }

        public String getRole() {
            return role;
        }

        // Setters
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public void setMobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public boolean isApproved() {
            return isApproved;
        }

        public void setApproved(boolean approved) {
            isApproved = approved;
        }
    }
}