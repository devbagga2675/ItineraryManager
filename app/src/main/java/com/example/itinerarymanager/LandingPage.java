package com.example.itinerarymanager;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class LandingPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
        FloatingActionButton fabCreateTrip = findViewById(R.id.fab);

        fabCreateTrip.setOnClickListener(view -> {
            Intent createTripIntent = new Intent(LandingPage.this, TripCreationActivity.class);
            startActivity(createTripIntent);
        });

        // Set up the BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomAppBar);


        // Set default selected item (e.g., Home)
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // Set up item selected listener using the new API
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            // Determine which fragment to show
            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else if (item.getItemId() == R.id.nav_explore) {
                selectedFragment = new ExploreFragment();
            }

            // Replace the current fragment with the selected fragment
            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, selectedFragment)
                        .commit();
            }
            return true; // Indicate the item was selected
        });

        // Load the default fragment (Home)
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, new HomeFragment())
                    .commit();
        }
    }
}
