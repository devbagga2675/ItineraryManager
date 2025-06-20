package com.example.itinerarymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class
ItineraryActivity extends AppCompatActivity {
    private Button addActivityButton;
    private Button btnDocuments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itinerary);


        // Initialize the button
        addActivityButton = findViewById(R.id.addActivityButton);
//        btnDocuments = findViewById(R.id.addDocumentButton);

        // Set click listener for the button
        addActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent to navigate to AddActivity
                Intent intent = new Intent(ItineraryActivity.this, Add_Activity.class);
                startActivity(intent);
            }
        });
        btnDocuments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent to navigate to AddActivity
                Intent intent = new Intent(ItineraryActivity.this, documents.class);
                startActivity(intent);
        }


    });
}
}
