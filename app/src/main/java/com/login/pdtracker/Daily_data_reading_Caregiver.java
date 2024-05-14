package com.login.pdtracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class Daily_data_reading_Caregiver extends AppCompatActivity {

    private static final String PREFS_NAME = "PDTrackerPrefs";
    private static final String KEY_SMARTWATCH_ID = "smartwatchId";

    private TextView textViewBloodPressure, textViewBloodPressureAbnormality, textViewFallDetected,
            textViewHeartRateAbnormality, textViewCoordinates, textViewOxygenLevels, textViewSleepQuality;
    private TextView textViewMaxHeartRateAfternoon, textViewMaxHeartRateMorning,
            textViewMinHeartRateAfternoon, textViewMinHeartRateMorning;
    private EditText editTextSmartwID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_data_reading_caregiver);

        initializeUIComponents();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editTextSmartwID.setText(prefs.getString(KEY_SMARTWATCH_ID, ""));

        Button syncButton = findViewById(R.id.btn_sync);
        syncButton.setOnClickListener(v -> {
            String smartwatchID = editTextSmartwID.getText().toString().trim();
            if (!smartwatchID.isEmpty()) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(KEY_SMARTWATCH_ID, smartwatchID);
                editor.apply();
                syncHealthData(smartwatchID);
            } else {
                Toast.makeText(this, "Please enter a Smartwatch ID.", Toast.LENGTH_SHORT).show();
            }
        });

        Button backButton = findViewById(R.id.bt_bdb);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, Caregiver_Home.class);
            startActivity(intent);
            finish();
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
        editTextSmartwID = findViewById(R.id.Smartw_ID);
    }

    private void syncHealthData(String smartwatchID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("health_data").document(smartwatchID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            updateUI(document.getData());
                        } else {
                            textViewBloodPressure.setText("No health data found for the Smartwatch ID.");
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
}


