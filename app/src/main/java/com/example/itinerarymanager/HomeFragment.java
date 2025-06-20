package com.example.itinerarymanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private LinearLayout tripsContainer;
    private final Set<String> renderedTripIds = new HashSet<>(); // To track rendered trips

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated called");

        tripsContainer = view.findViewById(R.id.tripsContainer);

        // Load trips initially
        loadTripsIfUserSignedIn();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");

        // Reload trips when fragment resumes
        loadTripsIfUserSignedIn();
    }

    private void loadTripsIfUserSignedIn() {
        // Get the current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            // Log the user's email for debugging
            Log.d(TAG, "Current user email: " + userEmail);

            // Load trips for the current user
            loadTripsFromFirestore(userEmail);
        } else {
            Log.w(TAG, "No user is currently signed in.");
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadTripsFromFirestore(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Clear trips if necessary (only when explicitly reloading all)
        if (renderedTripIds.isEmpty()) {
            tripsContainer.removeAllViews();
        }

        db.collection("trips")
                .whereArrayContains("members", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String tripId = document.getId();

                            // Avoid duplicate rendering
                            if (!renderedTripIds.contains(tripId)) {
                                Map<String, Object> trip = document.getData();
                                String tripName = Objects.requireNonNull(trip.get("tripName")).toString();
                                String status = Objects.requireNonNull(trip.get("status")).toString();

                                addTripCard(tripName, status, tripId);
                                renderedTripIds.add(tripId); // Mark trip as rendered
                            }
                        }
                    } else {
                        // Show a placeholder text if no trips are found
                        if (tripsContainer.getChildCount() == 0) {
                            TextView emptyTextView = new TextView(requireContext());
                            emptyTextView.setText("Nothing to show here");
                            tripsContainer.addView(emptyTextView);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching trips: ", e));
    }

    private void addTripCard(String tripName, String status, String id) {
        // Inflate the trip card layout
        View tripCardView = getLayoutInflater().inflate(R.layout.trip_card, tripsContainer, false);

        // Find the relevant views
        TextView tripNameTextView = tripCardView.findViewById(R.id.tripNameTextView);
        TextView tripStatusTextView = tripCardView.findViewById(R.id.tripStatusTextView);

        // Set the trip name and status
        tripNameTextView.setText(tripName);
        tripStatusTextView.setText(status);

        // Style the status background based on the status
        if (status.equals("Planned")) {
            tripStatusTextView.setBackgroundResource(R.drawable.status_background_planned);
        } else if (status.equals("Completed")) {
            tripStatusTextView.setBackgroundResource(R.drawable.status_background_completed);
        }

        // Set click listener to navigate to TripDetailsActivity
        tripCardView.setOnClickListener(v -> {
            Intent viewTripIntent = new Intent(requireContext(), TripDetailsActivity.class);
            viewTripIntent.putExtra("tripId", id);
            startActivity(viewTripIntent);
        });

        // Add the card to the container
        tripsContainer.addView(tripCardView);
    }
}
