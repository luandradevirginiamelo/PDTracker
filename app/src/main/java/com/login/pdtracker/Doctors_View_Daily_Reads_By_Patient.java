package com.login.pdtracker;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;


import androidx.annotation.NonNull;

public class Doctors_View_Daily_Reads_By_Patient extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_view_daily_readings_by_patient);

        // Retrieve the selected user's ID from the intent
        String selectedUserID = getIntent().getStringExtra("selectedUserID");

        // Use the selectedUserID to fetch and display relevant data from Firestore
        fetchPatientData(selectedUserID);
    }

    private void fetchPatientData(String selectedUserID) {
        // Fetch data from Firestore based on the selected user's ID
        FirebaseFirestore.getInstance().collection("Daily_Health_Data_Readings")
                .whereEqualTo("PatientID", selectedUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Iterate through the documents and process the data
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Extract data from the document
                            String patientName = document.getString("PatientName");
                            int morningHR = document.getLong("morningHR").intValue();
                            int afternoonHR = document.getLong("afternoonHR").intValue();
                            boolean isMorningMoodOk = document.getBoolean("IsMorningMoodOk");
                            boolean isEveningMoodOk = document.getBoolean("IsEveningMoodOk");
                            boolean abnormalHRDetected = document.getBoolean("AbnormalHRdetected");
                            boolean abnormalMoodDetected = document.getBoolean("AbnormalMoodDetected");
                            boolean abnormalSleepDetected = document.getBoolean("AbnormalSleepDetected");
                            boolean abnormalActivityDetected = document.getBoolean("AbnormalActivityDetected");

                            // Update UI with the fetched data
                            updateUI(patientName, morningHR, afternoonHR, isMorningMoodOk, isEveningMoodOk, abnormalHRDetected, abnormalMoodDetected, abnormalSleepDetected, abnormalActivityDetected);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failures
                        Log.e("Firestore", "Error fetching documents", e);
                    }
                });
    }


    private void updateUI(String patientName, int morningHR, int afternoonHR, boolean isMorningMoodOk, boolean isEveningMoodOk, boolean abnormalHRDetected, boolean abnormalMoodDetected, boolean abnormalSleepDetected, boolean abnormalActivityDetected) {
        // Update UI elements with the fetched data
        TextView tvPatientName = findViewById(R.id.PatientName);
        TextView tvMorningHR = findViewById(R.id.MorningHR);
        TextView tvAfternoonHR = findViewById(R.id.AfternoonHR);
        TextView tvMorningMood = findViewById(R.id.MorningMood);
        TextView tvEveningMood = findViewById(R.id.EveningMood);
        TextView tvAbnormalHR = findViewById(R.id.AbnormalHR);
        TextView tvAbnormalMood = findViewById(R.id.AbnormalMood);
        TextView tvAbnormalSleep = findViewById(R.id.AbnormalSleep);
        TextView tvAbnormalActivity = findViewById(R.id.AbnormalActivity);

        tvPatientName.setText(patientName);
        tvMorningHR.setText("Morning Heart Rate: " + morningHR + " bpm");
        tvAfternoonHR.setText("Afternoon Heart Rate: " + afternoonHR + " bpm");
        tvMorningMood.setText("Morning Mood: " + (isMorningMoodOk ? "OK" : "Abnormal"));
        tvEveningMood.setText("Evening Mood: " + (isEveningMoodOk ? "OK" : "Abnormal"));
        tvAbnormalHR.setText("Abnormal Heart Rate Detected: " + (abnormalHRDetected ? "Yes" : "No"));
        tvAbnormalMood.setText("Abnormal Mood Detected: " + (abnormalMoodDetected ? "Yes" : "No"));
        tvAbnormalSleep.setText("Abnormal Sleep Detected: " + (abnormalSleepDetected ? "Yes" : "No"));
        tvAbnormalActivity.setText("Abnormal Activity Detected: " + (abnormalActivityDetected ? "Yes" : "No"));
    }
}
