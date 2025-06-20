package com.example.itinerarymanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class LoginActivity2 extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button nextButton;
    private TextView signupLink;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login2);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Link UI elements
        emailField = findViewById(R.id.editTextTextEmailAddress);
        passwordField = findViewById(R.id.editTextTextPassword);
        nextButton = findViewById(R.id.nextButton);
        signupLink = findViewById(R.id.signuplink);

        // Set up the sign-up link to open the RegistrationActivity
        signupLink.setOnClickListener(v -> {
            Intent signupIntent = new Intent(LoginActivity2.this, RegistrationActivity.class);
            startActivity(signupIntent);
        });

        // Set up the login button to sign in with Firebase Auth
        nextButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity2.this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Authenticate with Firebase Auth
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Sign in success
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity2.this, "Login successful", Toast.LENGTH_SHORT).show();

                            // Start TripListActivity and pass the user's email
                            Intent intent = new Intent(LoginActivity2.this, LandingPage.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            finish();
                        } else {
                            // Sign in fails
                            Log.w("LoginActivity2", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity2.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
