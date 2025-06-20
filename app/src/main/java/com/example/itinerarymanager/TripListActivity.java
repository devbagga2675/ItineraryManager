package com.example.itinerarymanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.itinerarymanager.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Map;
import java.util.Objects;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;

public class TripListActivity extends AppCompatActivity {

    private static final int TRIP_CREATION_REQUEST_CODE = 1;
    private LinearLayout tripsContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_list_activity);


        String userEmail = getIntent().getStringExtra("email");

        tripsContainer = findViewById(R.id.tripsContainer);
        Button addTripButton = findViewById(R.id.addTripButton);


        loadTripsFromFirestore(userEmail);

        addTripButton.setOnClickListener(v -> {

            Intent intent = new Intent(TripListActivity.this, TripCreationActivity.class);
            intent.putExtra("email", userEmail); // Pass the user's email
            startActivityForResult(intent, TRIP_CREATION_REQUEST_CODE);
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TRIP_CREATION_REQUEST_CODE && resultCode == RESULT_OK) {

            String userEmail = getIntent().getStringExtra("email");
            loadTripsFromFirestore(userEmail);
        }
    }


    @SuppressLint("SetTextI18n")
    private void loadTripsFromFirestore(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        tripsContainer.removeAllViews();

        db.collection("trips")
                .whereArrayContains("members", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> trip = document.getData();
                            String tripName = Objects.requireNonNull(trip.get("tripName")).toString();
                            String location = trip.get("location") != null ? trip.get("location").toString() : "No location specified";
                            String status = Objects.requireNonNull(trip.get("status")).toString();

                            Timestamp startTimestamp = (Timestamp) trip.get("startDate");
                            Timestamp endTimestamp = (Timestamp) trip.get("endDate");

                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            String startDate = sdf.format(startTimestamp.toDate());
                            String endDate = sdf.format(endTimestamp.toDate());

                            addTripCard(tripName, startDate, endDate, location, status);
                        }
                    } else {
                        TextView emptyTextView = new TextView(this);
                        emptyTextView.setText("Nothing to show here");
                        tripsContainer.addView(emptyTextView);
                    }
                });
    }



    @SuppressLint("SetTextI18n")
    private void addTripCard(String tripName, String startDate, String endDate, String location, String status) {

        View tripCardView = getLayoutInflater().inflate(R.layout.trip_card, tripsContainer, false);


        TextView tripNameTextView = tripCardView.findViewById(R.id.tripNameTextView);
//        TextView tripDateTextView = tripCardView.findViewById(R.id.tripDateTextView);
//        TextView tripLocationTextView = tripCardView.findViewById(R.id.tripLocationTextView);
        TextView tripStatusTextView = tripCardView.findViewById(R.id.tripStatusTextView);
//        Button viewTripButton = tripCardView.findViewById(R.id.viewTripButton);


        tripNameTextView.setText(tripName);
//        tripDateTextView.setText("From: " + startDate + " To: " + endDate);
//        tripLocationTextView.setText(location);


        tripStatusTextView.setText(status);
        if (status.equals("Planned")) {
            tripStatusTextView.setTextColor(Integer.parseInt("#76A154"));
        } else if (status.equals("Completed")) {
            tripStatusTextView.setTextColor(Integer.parseInt("#4D80FF"));
        }


//        viewTripButton.setOnClickListener(v -> {
//
//            Intent intent = new Intent(TripListActivity.this, TripDetailsActivity.class);
//            intent.putExtra("tripName", tripName);
//            intent.putExtra("startDate", startDate);
//            intent.putExtra("endDate", endDate);
//            intent.putExtra("location", location);
//            intent.putExtra("status", status);
//            startActivity(intent);
//        });


        tripsContainer.addView(tripCardView);
    }

}


