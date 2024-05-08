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

public class Dailydata_reading extends AppCompatActivity {

    private String smartwatchID;
    private TextView dataDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dailydata_reading);

        Button syncButton = findViewById(R.id.btn_sync);
        dataDisplay = findViewById(R.id.data_display);

        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchSmartwatchIDAndSyncData();
            }
        });
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
                                smartwatchID = document.getString("smartID");
                                if (smartwatchID != null && !smartwatchID.isEmpty()) {
                                    syncHealthData(smartwatchID);
                                } else {
                                    dataDisplay.setText("No smartwatch ID found.");
                                }
                            } else {
                                dataDisplay.setText("No document found.");
                            }
                        } else {
                            Log.e("Firestore Error", "Failed to fetch smartwatch ID", task.getException());
                            dataDisplay.setText("Failed to fetch smartwatch ID.");
                        }
                    });
        } else {
            dataDisplay.setText("User not found.");
        }
    }

    private void syncHealthData(String smartwatchID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("health_data").document(smartwatchID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().getData() != null) {
                            Map<String, Object> healthData = task.getResult().getData();
                            dataDisplay.setText(formatData(healthData));
                        } else {
                            dataDisplay.setText("No health data found for the smartwatch ID.");
                        }
                    } else {
                        Log.e("Firestore Error", "Failed to fetch health data", task.getException());
                        dataDisplay.setText("Failed to fetch health data.");
                    }
                });
    }

    private String formatData(Map<String, Object> data) {
        StringBuilder formattedData = new StringBuilder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            formattedData.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue().toString())
                    .append("\n");  // Add a new line for each data entry
        }
        return formattedData.toString();
    }
}
