package com.login.pdtracker;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.Calendar;

public class Dailydata_reading extends AppCompatActivity {

    private TextView textViewBloodPressure, textViewBloodPressureAbnormality, textViewFallDetected,
            textViewHeartRateAbnormality, textViewCoordinates, textViewOxygenLevels, textViewSleepQuality;

    private TextView textViewMaxHeartRateAfternoon, textViewMaxHeartRateMorning,
            textViewMinHeartRateAfternoon, textViewMinHeartRateMorning;

      @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dailydata_reading);

        initializeUIComponents();

        Button syncButton = findViewById(R.id.btn_sync);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchSmartwatchIDAndSyncData();
            }
        });
    }

    private void initializeUIComponents() {
        textViewBloodPressure = findViewById(R.id.textView_bloodPressure);
        textViewBloodPressureAbnormality = findViewById(R.id.textView_bloodPressureAbnormality);
        textViewFallDetected = findViewById(R.id.textView_fallDetected);
        textViewHeartRateAbnormality = findViewById(R.id.textView_heartRateAbnormality);
        textViewCoordinates = findViewById(R.id.textView_coordinates);
        textViewOxygenLevels = findViewById(R.id.textView_oxygenLevels);
        textViewSleepQuality = findViewById(R.id.textView_sleepQuality);
        textViewMaxHeartRateAfternoon = findViewById(R.id.textView_maxHeartRateAfternoon);
        textViewMaxHeartRateMorning = findViewById(R.id.textView_maxHeartRateMorning);
        textViewMinHeartRateAfternoon = findViewById(R.id.textView_minHeartRateAfternoon);
        textViewMinHeartRateMorning = findViewById(R.id.textView_minHeartRateMorning);
    }

    private void fetchSmartwatchIDAndSyncData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = user.getUid();
            db.collection("patient_profiles").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String smartwatchID = document.getString("smartID");
                                if (smartwatchID != null && !smartwatchID.isEmpty()) {
                                    syncHealthData(smartwatchID);
                                } else {
                                    textViewBloodPressure.setText("No smartwatch ID found.");
                                }
                            } else {
                                textViewBloodPressure.setText("No document found.");
                            }
                        } else {
                            Log.e("Firestore Error", "Failed to fetch smartwatch ID", task.getException());
                            textViewBloodPressure.setText("Failed to fetch smartwatch ID.");
                        }
                    });
        } else {
            textViewBloodPressure.setText("User not found.");
        }
    }

    private void syncHealthData(String smartwatchID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("health_data").document(smartwatchID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().getData() != null) {
                            updateUI(task.getResult().getData());
                        } else {
                            textViewBloodPressure.setText("No health data found for the smartwatch ID.");
                        }
                    } else {
                        Log.e("Firestore Error", "Failed to fetch health data", task.getException());
                        textViewBloodPressure.setText("Failed to fetch health data.");
                    }
                });
    }

    private void updateUI(Map<String, Object> data) {
        textViewBloodPressure.setText("Blood Pressure: " + data.get("Blood Pressure"));
        textViewBloodPressureAbnormality.setText("Blood Pressure Abnormality: " + (data.get("Blood Pressure Abnormality").equals("Y") ? "Yes" : "No"));
        textViewFallDetected.setText("Fall Detected: " + data.get("Fall Detected"));
        textViewHeartRateAbnormality.setText("Heart Rate Abnormality: " + (data.get("Heart Rate Abnormality").equals("Y") ? "Yes" : "No"));
        textViewCoordinates.setText("Coordinates: " + data.get("Latitude") + ", " + data.get("Longitude"));
        textViewOxygenLevels.setText("Oxygen Levels: " + data.get("Oxygen Levels"));
        textViewSleepQuality.setText("Sleep Quality: " + data.get("Sleep Quality"));
        textViewMaxHeartRateAfternoon.setText("Max Heart Rate Afternoon: " + data.get("Max Heart Rate Afternoon"));
        textViewMaxHeartRateMorning.setText("Max Heart Rate Morning: " + data.get("Max Heart Rate Morning"));
        textViewMinHeartRateAfternoon.setText("Min Heart Rate Afternoon: " + data.get("Min Heart Rate Afternoon"));
        textViewMinHeartRateMorning.setText("Min Heart Rate Morning: " + data.get("Min Heart Rate Morning"));

    }



    private void updateHeartRateVisibility() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        // Determine if it's before noon (morning) or after (afternoon)
        boolean isMorning = hour < 12;

        // Update visibility based on time of day
        textViewMaxHeartRateMorning.setVisibility(isMorning ? View.VISIBLE : View.GONE);
        textViewMinHeartRateMorning.setVisibility(isMorning ? View.VISIBLE : View.GONE);
        textViewMaxHeartRateAfternoon.setVisibility(isMorning ? View.GONE : View.VISIBLE);
        textViewMinHeartRateAfternoon.setVisibility(isMorning ? View.GONE : View.VISIBLE);

        // If it's not the correct time, clear the text
        if (isMorning) {
            textViewMaxHeartRateAfternoon.setText("");
            textViewMinHeartRateAfternoon.setText("");
        } else {
            textViewMaxHeartRateMorning.setText("");
            textViewMinHeartRateMorning.setText("");
        }
    }
}
