package com.example.itinerarymanager;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ExpensesFragment extends Fragment {

    private TextView accommodationExpense, transportationExpense, foodDrinksExpense, shoppingExpense, outingsExpense;
    private FirebaseFirestore db;
    private String tripId;  // To hold the tripId passed from the Activity

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);

        // Initialize TextViews from XML
        accommodationExpense = view.findViewById(R.id.accommodationExpense);
        transportationExpense = view.findViewById(R.id.transportationExpense);
        foodDrinksExpense = view.findViewById(R.id.foodDrinksExpense);
        shoppingExpense = view.findViewById(R.id.shoppingExpense);
        outingsExpense = view.findViewById(R.id.outingsExpense);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get the tripId from arguments
        if (getArguments() != null) {
            tripId = (String) getArguments().getSerializable("tripId"); // Retrieve tripId
        }

        // Fetch the expenses from Firestore using the tripId
        if (tripId != null) {
            fetchExpenses();
        } else {
            Log.e("ExpensesFragment", "tripId is null");
        }

        return view;
    }

    private void fetchExpenses() {
        db.collection("trips").document(tripId) // Assuming you are passing the trip ID here
                .collection("days") // Get the days for the specific trip
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Iterate through each day document
                        for (QueryDocumentSnapshot dayDoc : task.getResult()) {
                            String dayId = dayDoc.getId();

                            // Fetch expenses for each day
                            fetchDayExpenses(dayId);
                        }
                    } else {
                        Log.e("ExpensesFragment", "Error fetching days: ", task.getException());
                    }
                });
    }

    private void fetchDayExpenses(String dayId) {
        // Reference to the activities sub-collection or directly fetching the fields
        db.collection("trips").document(tripId)
                .collection("days").document(dayId)
                .collection("activities")  // If activities are stored within days collection
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        double accommodationTotal = 0;
                        double transportationTotal = 0;
                        double foodDrinksTotal = 0;
                        double shoppingTotal = 0;
                        double outingsTotal = 0;

                        // Iterate over each activity and sum expenses based on category
                        for (QueryDocumentSnapshot activityDoc : task.getResult()) {
                            String category = activityDoc.getString("category"); // "Accommodation", "Transportation", etc.
                            String expenseStr = activityDoc.getString("expense"); // "String" type

                            // If expense is null or empty, treat it as 0
                            double expense = (expenseStr == null || expenseStr.isEmpty()) ? 0 : Double.parseDouble(expenseStr);

                            // Check the category and add the expense to the correct total
                            if (category != null) {
                                switch (category) {
                                    case "Accommodation":
                                        accommodationTotal += expense;
                                        Toast.makeText(requireContext(), "Accommodation Total: " + accommodationTotal, Toast.LENGTH_SHORT).show();
                                        break;
                                    case "Transportation":
                                        transportationTotal += expense;
                                        break;
                                    case "Food and Drinks":
                                        foodDrinksTotal += expense;
                                        break;
                                    case "Shopping":
                                        shoppingTotal += expense;
                                        break;
                                    case "Outings":
                                        outingsTotal += expense;
                                        break;
                                }
                            }
                        }

                        // Update the UI with the total expenses for each category
                        updateExpenseTextViews(accommodationTotal, transportationTotal, foodDrinksTotal, shoppingTotal, outingsTotal);
                    } else {
                        Log.e("ExpensesFragment", "Error fetching activities: ", task.getException());
                    }
                });
    }

    private void updateExpenseTextViews(double accommodationTotal, double transportationTotal, double foodDrinksTotal, double shoppingTotal, double outingsTotal) {
        // Run on the main thread
        requireActivity().runOnUiThread(() -> {
            // Set the corresponding expense values to the TextViews
            accommodationExpense.setText(String.format("%.2f",accommodationTotal));
            transportationExpense.setText(String.format("%.2f",transportationTotal));
            foodDrinksExpense.setText(String.format("%.2f", foodDrinksTotal));
            shoppingExpense.setText(String.format("%.2f", shoppingTotal));
            outingsExpense.setText(String.format("%.2f", outingsTotal));
        });
    }

}
