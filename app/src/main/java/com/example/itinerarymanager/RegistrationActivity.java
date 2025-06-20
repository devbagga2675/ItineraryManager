package com.example.itinerarymanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, phoneNumberInput, passwordInput;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView loginlink;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Assign views
        nameInput = findViewById(R.id.nameinput);
        emailInput = findViewById(R.id.emailinput);
        phoneNumberInput = findViewById(R.id.phonenumberinput);
        passwordInput = findViewById(R.id.passwordinput);
        signUpButton = findViewById(R.id.signupbutton);
        loginlink = findViewById(R.id.loginlink);

        // Apply insets for EdgeToEdge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up the sign-up button listener
        signUpButton.setOnClickListener(v -> registerUser());


        loginlink.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity2.class));
        });
    }

    private void registerUser() {
        String username = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phoneNumber = phoneNumberInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate input fields
        if (username.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Call method to add user details to Firestore
                            addUserDetailsToFirestore(firebaseUser.getUid(), username, email, phoneNumber);
                        }
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void addUserDetailsToFirestore(String userId, String username, String email, String phoneNumber) {
        Map<String, Object> user = new HashMap<>();
        user.put("createdAt", System.currentTimeMillis());
        user.put("email", email);
        user.put("phonenumber", phoneNumber);
        user.put("username", username);

        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegistrationActivity.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                    // Redirect to another activity if needed
                    startActivity(new Intent(RegistrationActivity.this, LoginActivity2.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w("RegistrationActivity", "Error adding document", e);
                    Toast.makeText(RegistrationActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                });
    }
}
