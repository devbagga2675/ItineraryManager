package com.example.itinerarymanager;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Get reference to the logout button
        Button logout = rootView.findViewById(R.id.logout);

        // Set an onClickListener on the logout button
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out the user
                mAuth.signOut();

                // Redirect to MainActivity (Login Screen or wherever you want)
                Intent i = new Intent(getActivity(), MainActivity.class);
                startActivity(i);
                getActivity().finish(); // Optional: to close the current fragment/activity
            }
        });

        return rootView; // Return the inflated view
    }
}
