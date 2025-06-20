package com.example.itinerarymanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TripDetailsActivity extends AppCompatActivity {

    private static final String TAG = "TripDetailsActivity";

    private ImageButton tripMembersTextView;
    private TextView tripNameTextView;
    private TextView tripDatesTextView;
    private TextView tripLocationTextView;
    private ImageButton menuButton;
    private ImageButton backButton;
    private FrameLayout fragmentContainer;
    private TextView itineraryNav, expensesNav;

    // Declare variables to store trip details
    private Date startDate;
    private Date endDate;
    private String tripName;
    private String location;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        // Initialize views
        tripMembersTextView = findViewById(R.id.trip_members);
        tripNameTextView = findViewById(R.id.trip_name);
        tripDatesTextView = findViewById(R.id.trip_dates);
        tripLocationTextView = findViewById(R.id.trip_location);
        menuButton = findViewById(R.id.menu_button);
        backButton = findViewById(R.id.back_button);
        fragmentContainer = findViewById(R.id.fragment_container);
        itineraryNav = findViewById(R.id.trip_itinerary);
        expensesNav = findViewById(R.id.trip_expenses);

        // Set up the menu button listener
        setupMenuButton();

        // Get document ID from the intent
        String documentId = getIntent().getStringExtra("tripId");
        if (documentId != null) {
            fetchTripDetails(documentId);
        } else {
            Log.e(TAG, "No document ID provided.");
        }

        // Handle trip members button click
        tripMembersTextView.setOnClickListener(view -> {
            Intent membersIntent = new Intent(TripDetailsActivity.this, members.class);
            membersIntent.putExtra("tripId", documentId);
            startActivity(membersIntent);
        });

        // Handle back button click
        backButton.setOnClickListener(view -> finish());

        // Handle navigation menu clicks
        itineraryNav.setOnClickListener(v -> {
            // Check if the ItineraryFragment is already in the fragment manager
            ItineraryFragment existingFragment = (ItineraryFragment) getSupportFragmentManager().findFragmentByTag(ItineraryFragment.class.getSimpleName());

            if (existingFragment == null) {
                // Create a new instance and pass data
                ItineraryFragment itineraryFragment = new ItineraryFragment();
                Bundle bundle = new Bundle();

                // Pass startDate, endDate, and tripId
                bundle.putSerializable("startDate", startDate);
                bundle.putSerializable("endDate", endDate);
                bundle.putSerializable("tripId", documentId); // Pass the tripId
                itineraryFragment.setArguments(bundle);
                loadFragment(itineraryFragment);
            } else {
                // If the fragment already exists, just show it
                getSupportFragmentManager().beginTransaction()
                        .show(existingFragment)
                        .commit();
            }
        });

        expensesNav.setOnClickListener(v -> {
            // Create an ExpensesFragment and pass the tripId
            ExpensesFragment expensesFragment = new ExpensesFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("tripId", documentId); // Pass the tripId to the ExpensesFragment
            expensesFragment.setArguments(bundle);
            loadFragment(expensesFragment);
        });
    }

    // Fetch trip details from Firestore
    private void fetchTripDetails(String documentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("trips").document(documentId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        tripName = document.getString("tripName");
                        location = document.getString("location");

                        Timestamp startTimestamp = document.getTimestamp("startDate");
                        Timestamp endTimestamp = document.getTimestamp("endDate");

                        startDate = startTimestamp != null ? startTimestamp.toDate() : null;
                        endDate = endTimestamp != null ? endTimestamp.toDate() : null;

                        // Set trip details to views
                        tripNameTextView.setText(tripName);
                        tripLocationTextView.setText(location != null ? location : "No location provided");

                        if (startDate != null && endDate != null) {
                            tripDatesTextView.setText("From: " + sdf.format(startDate) + " To: " + sdf.format(endDate));

                            // Check if the ItineraryFragment is already in the fragment manager
                            ItineraryFragment existingFragment = (ItineraryFragment) getSupportFragmentManager().findFragmentByTag(ItineraryFragment.class.getSimpleName());

                            if (existingFragment == null) {
                                // Create and load the fragment if it's not already loaded
                                ItineraryFragment itineraryFragment = new ItineraryFragment();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("startDate", startDate);
                                bundle.putSerializable("endDate", endDate);
                                bundle.putSerializable("tripId", documentId); // Pass the tripId
                                itineraryFragment.setArguments(bundle);
                                loadFragment(itineraryFragment);
                            }
                        } else {
                            Log.e(TAG, "Start or End date is null");
                        }
                    } else {
                        Log.e(TAG, "Error fetching trip details: ", task.getException());
                    }
                });
    }

    // Load a fragment dynamically
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);  // Optional, if you want back navigation
        transaction.commit();
    }

    // Set up the menu button for additional options (Edit, Delete, etc.)
    private void setupMenuButton() {
        menuButton.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(TripDetailsActivity.this, menuButton);
            popupMenu.getMenuInflater().inflate(R.menu.trip_details_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                int id = menuItem.getItemId();
                if (id == R.id.edit_trip) {
                    // Handle Edit Trip
                } else if (id == R.id.delete_trip) {
                    deleteTrip();
                    return true;
                } else if (id == R.id.share_trip) {
                    shareTripDetails();
                    return true;
                } else if (id == R.id.trip_documents) {
                    Intent documentsIntent = new Intent(TripDetailsActivity.this, documents.class);
                    startActivity(documentsIntent);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
    }

    // Handle deleting the trip
    private void deleteTrip() {
        Log.d(TAG, "Trip deleted.");
        // Implement trip deletion logic here
    }

    // Handle sharing trip details
    private void shareTripDetails() {
        Log.d(TAG, "Trip details shared.");
        // Implement trip sharing logic here
    }
}
