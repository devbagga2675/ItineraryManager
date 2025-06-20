package com.example.itinerarymanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripCreationActivity extends AppCompatActivity {

    private EditText tripNameInput, startDateInput, endDateInput, memberEmailInput;
    private Button createTripButton, cancelButton, addMemberButton;
    private LinearLayout membersContainer;
    private FirebaseFirestore db;
    private List<String> membersList; // List to hold valid email addresses of members
    private String currentUserEmail; // Holds the email of the current user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_creation_activity);

        // Initialize Firestore and views
        db = FirebaseFirestore.getInstance();
        tripNameInput = findViewById(R.id.tripNameInput);
        startDateInput = findViewById(R.id.startDateInput);
        endDateInput = findViewById(R.id.endDateInput);
        memberEmailInput = findViewById(R.id.memberInput);
        createTripButton = findViewById(R.id.createTripButton);
        cancelButton = findViewById(R.id.cancelButton);
        addMemberButton = findViewById(R.id.addMemberButton);
        membersContainer = findViewById(R.id.membersContainer);
        membersList = new ArrayList<>();

        // Get the current user's email from Firebase Auth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserEmail = currentUser.getEmail();
            if (currentUserEmail != null) {
                membersList.add(currentUserEmail); // Add current user as a default member
                renderMemberCard(currentUserEmail); // Show the current user in the member list
            }
        }

        // Set click listeners
        createTripButton.setOnClickListener(v -> createTrip());
        cancelButton.setOnClickListener(v -> {
            resetFields();
            finish();
        });
        addMemberButton.setOnClickListener(v -> addMember());
    }

    private void addMember() {
        String email = memberEmailInput.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter a member's email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate if the user exists in the Firestore "users" collection
        CollectionReference usersRef = db.collection("users");
        usersRef.whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().isEmpty()) {
                    // User exists, add email to the list
                    if (!membersList.contains(email)) { // Avoid duplicates
                        membersList.add(email);
                        renderMemberCard(email);
                        memberEmailInput.setText(""); // Clear input field
                    } else {
                        Toast.makeText(this, "This member is already added", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "User not found in the system", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Error checking user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderMemberCard(String email) {
        // Create a new layout for the member card
        LinearLayout memberCard = new LinearLayout(this);
        memberCard.setOrientation(LinearLayout.HORIZONTAL);
        memberCard.setPadding(16, 16, 16, 16);

        // Text view for the email
        TextView emailText = new TextView(this);
        emailText.setText(email);
        emailText.setTextSize(16);
        emailText.setPadding(0, 0, 16, 0);
        memberCard.addView(emailText);

        // Remove button
        Button removeButton = new Button(this);
        removeButton.setText("Remove");
        removeButton.setOnClickListener(v -> {
            if (!email.equals(currentUserEmail)) { // Prevent removing the current user
                membersList.remove(email); // Remove from the list
                membersContainer.removeView(memberCard); // Remove the card from the UI
            } else {
                Toast.makeText(this, "You cannot remove yourself from the trip", Toast.LENGTH_SHORT).show();
            }
        });
        memberCard.addView(removeButton);

        // Add the member card to the container
        membersContainer.addView(memberCard);
    }

    private void createTrip() {
        String tripName = tripNameInput.getText().toString().trim();
        String startDateString = startDateInput.getText().toString().trim();
        String endDateString = endDateInput.getText().toString().trim();

        if (tripName.isEmpty() || startDateString.isEmpty() || endDateString.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            Date startDate = sdf.parse(startDateString);
            Date endDate = sdf.parse(endDateString);

            assert endDate != null;
            if (endDate.before(startDate)) {
                Toast.makeText(this, "End date cannot be before start date", Toast.LENGTH_SHORT).show();
                return;
            }

            Timestamp startTimestamp = new Timestamp(startDate);
            Timestamp endTimestamp = new Timestamp(endDate);

            // Create trip data
            Map<String, Object> tripData = new HashMap<>();
            tripData.put("tripName", tripName);
            tripData.put("startDate", startTimestamp);
            tripData.put("endDate", endTimestamp);
            tripData.put("members", membersList); // Add members list
            tripData.put("status", "planned");

            // Save trip to Firestore
            db.collection("trips")
                    .add(tripData)
                    .addOnSuccessListener(documentReference -> {
                        String tripId = documentReference.getId();
                        createDaysSubcollection(tripId, startDate, endDate); // Create the days subcollection
                        Toast.makeText(this, "Trip created successfully!", Toast.LENGTH_SHORT).show();
                        resetFields();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error creating trip: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid date format. Please use YYYY-MM-DD.", Toast.LENGTH_SHORT).show();
        }
    }

    private void createDaysSubcollection(String tripId, Date startDate, Date endDate) {
        CollectionReference daysCollection = db.collection("trips").document(tripId).collection("days");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        while (!calendar.getTime().after(endDate)) {
            Date currentDay = calendar.getTime();
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("dayDate", new Timestamp(currentDay)); // Add dayDate value

            daysCollection.add(dayData)
                    .addOnSuccessListener(documentReference -> {
                        // Optionally, you can add an empty "activities" subcollection here
                        CollectionReference activitiesCollection = documentReference.collection("activities");
                        // No activities yet, keep it empty or add a placeholder
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error creating day: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            // Move to the next day
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void resetFields() {
        tripNameInput.setText("");
        startDateInput.setText("");
        endDateInput.setText("");
        memberEmailInput.setText("");
        membersList.clear();
        membersContainer.removeAllViews();

        // Re-add the current user as the default member
        if (currentUserEmail != null) {
            membersList.add(currentUserEmail);
            renderMemberCard(currentUserEmail);
        }
    }
}
