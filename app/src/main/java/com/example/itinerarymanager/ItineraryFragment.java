package com.example.itinerarymanager;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItineraryFragment extends Fragment {

    private static final String TAG = "ItineraryFragment";
    private LinearLayout daysContainer;
    private LinearLayout activitiesContainer;
    private ExtendedFloatingActionButton addActivityButton;
    private String tripId;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_itinerary, container, false);
        daysContainer = rootView.findViewById(R.id.days_container);
        activitiesContainer = rootView.findViewById(R.id.activities_container);
        addActivityButton = rootView.findViewById(R.id.addActivityButton);

        // Set listener for the floating action button (FAB)
        addActivityButton.setOnClickListener(v -> {
            Intent addActivityIntent = new Intent(requireActivity(), Add_Activity.class);
            addActivityIntent.putExtra("tripId", tripId);
            startActivity(addActivityIntent);
        });

        // Retrieve trip data passed to the fragment
        Bundle arguments = getArguments();
        if (arguments != null) {
            Date startDate = (Date) arguments.getSerializable("startDate");
            Date endDate = (Date) arguments.getSerializable("endDate");
            tripId = (String) arguments.getSerializable("tripId");

            if (tripId != null) {
                Toast.makeText(getActivity(), "Trip ID: " + tripId, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "No Trip ID", Toast.LENGTH_SHORT).show();
            }

            // Add days dynamically between start and end date
            if (startDate != null && endDate != null) {
                addDays(startDate, endDate);
            } else {
                Log.e(TAG, "Start or End date is null in ItineraryFragment");
            }
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        activitiesContainer.removeAllViews(); // Clear activities
        fetchActivitiesDayWise(); // Fetch activities again
    }

    private void addDays(Date startDate, Date endDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        int dayCount = 1;

        Log.d(TAG, "Adding days from " + sdf.format(startDate) + " to " + sdf.format(endDate));

        while (!calendar.getTime().after(endDate)) {
            View dayCardView = getLayoutInflater().inflate(R.layout.day_card, daysContainer, false);
            TextView dayNameTextView = dayCardView.findViewById(R.id.day_title);
            dayNameTextView.setText("Day " + dayCount);

            daysContainer.addView(dayCardView);
            calendar.add(Calendar.DATE, 1);
            dayCount++;
        }
    }

    private void fetchActivitiesDayWise() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        activitiesContainer.removeAllViews(); // Clear existing activities

        db.collection("trips").document(tripId)
                .collection("days")
                .orderBy("dayDate")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot dayDoc : task.getResult()) {
                            String dayId = dayDoc.getId();
                            LinearLayout dayActivitiesLayout = new LinearLayout(getActivity());
                            dayActivitiesLayout.setOrientation(LinearLayout.VERTICAL);
                            dayActivitiesLayout.setPadding(0, 10, 0, 10);

                            TextView dateTextView = new TextView(getActivity());
                            String dateText = sdf.format(dayDoc.getDate("dayDate"));
                            dateTextView.setText(dateText.substring(0, 5));
                            dateTextView.setTextSize(40);
                            dateTextView.setTextColor(Color.parseColor("#6A4C3C"));
                            dayActivitiesLayout.addView(dateTextView);

                            db.collection("trips").document(tripId)
                                    .collection("days").document(dayId)
                                    .collection("activities")
                                    .orderBy("startTime")
                                    .get()
                                    .addOnCompleteListener(activityTask -> {
                                        if (activityTask.isSuccessful()) {
                                            List<DocumentSnapshot> activities = activityTask.getResult().getDocuments();
                                            if (activities.isEmpty()) {
                                                TextView noActivitiesTextView = new TextView(getActivity());
                                                noActivitiesTextView.setText("No activities yet");
                                                noActivitiesTextView.setTextSize(16);
                                                noActivitiesTextView.setTextColor(Color.GRAY);
                                                dayActivitiesLayout.addView(noActivitiesTextView);
                                            } else {
                                                for (DocumentSnapshot activityDoc : activities) {
                                                    View activityCardView = getLayoutInflater().inflate(R.layout.activity_card, dayActivitiesLayout, false);
                                                    populateActivityCard(activityDoc, activityCardView);
                                                    dayActivitiesLayout.addView(activityCardView);
                                                }
                                            }
                                        } else {
                                            Log.e(TAG, "Error fetching activities: ", activityTask.getException());
                                        }
                                    });

                            activitiesContainer.addView(dayActivitiesLayout);
                        }
                    } else {
                        Log.e(TAG, "Error fetching days: ", task.getException());
                    }
                });
    }

    private void populateActivityCard(DocumentSnapshot activityDoc, View activityCardView) {
        String activityName = activityDoc.getString("activityName");
        String location = activityDoc.getString("location");
        String startTime = activityDoc.getString("startTime");
        String expense = activityDoc.getString("expense");
        String fileUrl = activityDoc.getString("fileUrl");

        TextView activityNameTextView = activityCardView.findViewById(R.id.activityName);
        activityNameTextView.setText(activityName != null ? activityName : "No name provided");

        TextView locationTextView = activityCardView.findViewById(R.id.activity_location);
        locationTextView.setText(location != null ? location : "No location");

        TextView timeTextView = activityCardView.findViewById(R.id.activity_time);
        timeTextView.setText(startTime != null ? startTime : "No start time");

        TextView expenseTextView = activityCardView.findViewById(R.id.activity_status);
        expenseTextView.setText("Expense: " + (expense != null ? expense : "0"));

        TextView docsTextView = activityCardView.findViewById(R.id.activity_docs);
        if (fileUrl != null && !fileUrl.isEmpty()) {
            docsTextView.setVisibility(View.VISIBLE);
            docsTextView.setOnClickListener(v -> openLocalFile(fileUrl));
        } else {
            docsTextView.setVisibility(View.GONE);
        }
    }

    private void openLocalFile(String fileUrl) {
        try {
            Uri fileUri = Uri.parse(fileUrl);
            String mimeType = requireContext().getContentResolver().getType(fileUri);

            // Fallback to getMimeType() if ContentResolver fails
            if (mimeType == null || mimeType.isEmpty()) {
                String filePath = fileUri.getPath();
                if (filePath != null) {
                    mimeType = getMimeType(filePath);
                }
            }

            if (mimeType == null || mimeType.isEmpty()) {
                Toast.makeText(requireContext(), "Unsupported file type.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
            openFileIntent.setDataAndType(fileUri, mimeType);
            openFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(openFileIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening file: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Unable to open file.", Toast.LENGTH_SHORT).show();
        }
    }



    private String getMimeType(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase(Locale.getDefault());
        return android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
}
