package com.example.itinerarymanager;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.itinerarymanager.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Redirect to login screen if no user is signed in
            startActivity(new Intent(this, LoginActivity2.class));
            finish();
        }
        else {
            // Redirect to home screen if user is already signed in
            startActivity(new Intent(this, LandingPage.class));
            finish();
        }
        // Start LoginActivity2
//        Intent intent = new Intent(MainActivity.this, LoginActivity2.class);
//        startActivity(intent);
//        finish();
    }
}
