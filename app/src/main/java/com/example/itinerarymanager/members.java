package com.example.itinerarymanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class members extends AppCompatActivity {

    private LinearLayout membersContainer;
    private String tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);

        membersContainer = findViewById(R.id.members_container);

        // Get the tripId passed via Intent
        tripId = getIntent().getStringExtra("tripId");

        if (tripId != null) {
            fetchMembersData(tripId);
        } else {
            Toast.makeText(this, "No Trip ID found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchMembersData(String tripId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch the trip document using the tripId
        db.collection("trips").document(tripId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get the members array from the trip document
                        List<String> members = (List<String>) task.getResult().get("members");

                        if (members != null) {
                            // Iterate over the members array and fetch the corresponding details for each member
                            for (String email : members) {
                                fetchMemberDetailsByEmail(email);
                            }
                        } else {
                            Toast.makeText(members.this, "No members found in this trip.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(members.this, "Error getting trip data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchMemberDetailsByEmail(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch the member details (name, phone number, etc.) by email
        db.collection("users").whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String phoneNumber = document.getString("phoneNumber");
                            String username = document.getString("username");

                            // Create the card for this member and add it to the container
                            addMemberCard(phoneNumber, username);
                        }
                    } else {
                        Toast.makeText(members.this, "Error fetching member details.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addMemberCard(String phoneNumber, String username) {
        // Inflate the member card layout dynamically
        LinearLayout memberCard = (LinearLayout) getLayoutInflater().inflate(R.layout.member_card, membersContainer, false);

        // Set the username
        TextView usernameTextView = memberCard.findViewById(R.id.member_username);

        // Set the fetched username
        usernameTextView.setText(username != null ? username : "No Username");

        // Handle the call button click
        Button callButton = memberCard.findViewById(R.id.call_button);
        callButton.setOnClickListener(v -> onCallButtonClick(phoneNumber));

        // Add the card to the container
        membersContainer.addView(memberCard);
    }

    private void onCallButtonClick(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        } else {
            Toast.makeText(this, "Phone number is not available", Toast.LENGTH_SHORT).show();
        }
    }
}
