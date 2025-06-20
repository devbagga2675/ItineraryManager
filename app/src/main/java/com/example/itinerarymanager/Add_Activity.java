package com.example.itinerarymanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class Add_Activity extends Activity {

    private EditText activityNameInput, activityStartTimeInput, activityEndTimeInput, activityDescriptionInput, activityLocationInput, activityExpenseInput;
    private Spinner dayNumberSpinner, activityCategorySpinner;
    private Button createActivityButton, cancelButton, filePickerButton;

    private FirebaseFirestore db;
    private String selectedFileUrl;
    private String tripId;

    private static final int PICK_FILE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_activity);

        // Initialize views
        activityNameInput = findViewById(R.id.activityNameInput);
        activityStartTimeInput = findViewById(R.id.activityStartTimeInput);
        activityEndTimeInput = findViewById(R.id.activityEndTimeInput);
        activityDescriptionInput = findViewById(R.id.activityDescriptionInput);
        activityLocationInput = findViewById(R.id.activityLocationInput);
        activityExpenseInput = findViewById(R.id.activityExpenseInput);
        dayNumberSpinner = findViewById(R.id.dayNumberSpinner);
        activityCategorySpinner = findViewById(R.id.activityCategorySpinner);
        createActivityButton = findViewById(R.id.createActivityButton);
        cancelButton = findViewById(R.id.cancelButton);
        filePickerButton = findViewById(R.id.filePickerButton);

        db = FirebaseFirestore.getInstance();

        // Get tripId passed from previous activity
        tripId = getIntent().getStringExtra("tripId");
        Log.d("Add_Activity", "Received tripId: " + tripId);

        // Set up the Day Numbers Spinner (Fetch days dynamically)
        fetchDaysForTrip();

        // Set up the Activity Category Spinner
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this, R.array.activity_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activityCategorySpinner.setAdapter(categoryAdapter);

        // Handle the file picker button click
        filePickerButton.setOnClickListener(v -> openFileManager());

        // Handle the create activity button click
        createActivityButton.setOnClickListener(v -> createActivity());

        // Handle the cancel button click
        cancelButton.setOnClickListener(v -> finish());
    }

    private void fetchDaysForTrip() {
        // Get the days subcollection of the trip document
        db.collection("trips").document(tripId).collection("days")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Populate the spinner with the number of days
                        int totalDays = queryDocumentSnapshots.size();
                        String[] daysArray = new String[totalDays];

                        int i = 0;
                        for (DocumentSnapshot document : queryDocumentSnapshots) {  // Use explicit type
                            daysArray[i] = "Day " + (i + 1); // Assuming document represents each day
                            i++;
                        }

                        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, daysArray);
                        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        dayNumberSpinner.setAdapter(dayAdapter);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load days", Toast.LENGTH_SHORT).show());
    }


    private void openFileManager() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Allow any file type
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                selectedFileUrl = data.getData().toString(); // Get the URI of the selected file
                Toast.makeText(this, "File selected: " + selectedFileUrl, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createActivity() {
        // Get the input values
        String activityName = activityNameInput.getText().toString();
        String startTime = activityStartTimeInput.getText().toString();
        String endTime = activityEndTimeInput.getText().toString();
        String description = activityDescriptionInput.getText().toString();
        String location = activityLocationInput.getText().toString();
        String expense = activityExpenseInput.getText().toString();
        String dayNumber = dayNumberSpinner.getSelectedItem().toString(); // Get the selected day
        String category = activityCategorySpinner.getSelectedItem().toString();

        if (activityName.isEmpty()) {
            Toast.makeText(this, "Please enter activity name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find the corresponding day document
        int dayIndex = Integer.parseInt(dayNumber.replace("Day ", "")) - 1;

        // Get the day document from Firestore based on dayIndex
        db.collection("trips").document(tripId).collection("days")
                .orderBy("dayDate") // Order days by their 'dayDate' field
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > dayIndex) {
                        // Now get the correct day document based on dayIndex
                        DocumentReference dayDocRef = queryDocumentSnapshots.getDocuments().get(dayIndex).getReference();

                        // Create a new activity object to be saved in Firestore
                        Map<String, Object> activity = new HashMap<>();
                        activity.put("activityName", activityName);
                        activity.put("startTime", startTime);
                        activity.put("endTime", endTime);
                        activity.put("description", description);
                        activity.put("location", location);
                        activity.put("expense", expense);
                        activity.put("category", category);
                        activity.put("fileUrl", selectedFileUrl);  // Store the file URL

                        // Save the activity to the corresponding day's activities subcollection
                        dayDocRef.collection("activities")
                                .add(activity)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "Activity created successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error creating activity", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch day document", Toast.LENGTH_SHORT).show());
    }
}
