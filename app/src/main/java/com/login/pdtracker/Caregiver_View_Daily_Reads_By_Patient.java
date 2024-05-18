package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.login.pdtracker.R;

public class Caregiver_View_Daily_Reads_By_Patient extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caregiver_view_daily_readings_by_patient);

        // Retrieve the selected user's ID from the intent
        String selectedUserID = getIntent().getStringExtra("selectedUserID");

        // Use the selectedUserID to fetch and display relevant health data from Firestore
        fetchHealthData(selectedUserID);

        AppCompatButton bt_bdb = findViewById(R.id.bt_bdb);
        bt_bdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Caregiver_View_Daily_Reads_By_Patient.this, Caregiver_Home.class);
                startActivity(intent);
            }
        });
    }

    private void fetchHealthData(String selectedUserID) {
        // Fetch data from Firestore based on the selected user's ID
        FirebaseFirestore.getInstance().collection("patient_profiles")
                .document(selectedUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Extract smartID from patient_profiles document
                            String smartID = documentSnapshot.getString("smartID");
                            if (smartID != null) {
                                // Fetch health data based on smartID
                                FirebaseFirestore.getInstance().collection("health_data")
                                        .document(smartID)
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot document) {
                                                if (document.exists()) {
                                                    // Update UI with the fetched health data
                                                    updateUI(document, selectedUserID);
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("Firestore", "Error fetching health data document", e);
                                            }
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error fetching patient profile document", e);
                    }
                });
    }

    private void updateUI(DocumentSnapshot document, String selectedUserID) {
        // Update UI elements with the fetched health data
        fetchPatientName(selectedUserID);
        fetchPatientLimits(selectedUserID);
        TextView tvBloodPressure = findViewById(R.id.BloodPressure);
        TextView tvMinHRMorning = findViewById(R.id.MinHRMorning);
        TextView tvMaxHRMorning = findViewById(R.id.MaxHRMorning);
        TextView tvMinHRPM = findViewById(R.id.MinHRPM);
        TextView tvMaxHRPM = findViewById(R.id.MaxHRPM);
        TextView tvOxygenLevels = findViewById(R.id.OxygenLevels);
        TextView tvSleep = findViewById(R.id.Sleep);
        TextView tvAbnormalActivity = findViewById(R.id.AbnormalActivity);

        String BPabnormality = document.getString("Blood Pressure Abnormality");
        String HRabnormality = document.getString("Heart Rate Abnormality");
        String abnormalities;

        if(BPabnormality == "N" && HRabnormality == "N"){
            abnormalities = "NO";
        }
        else if (BPabnormality == "Y" && HRabnormality == "N"){
            abnormalities = "Abnormal Blood Pressure";
        }
        else if (HRabnormality == "Y" && BPabnormality == "N"){
            abnormalities = "Abnormal Heart Rate";
        }
        else {
            abnormalities = "Abnormal Heart Rate and Abnormal Blood Pressure";
        };

        // Set text for each TextView if the corresponding attribute exists in the document
        if (document.contains("Blood Pressure")) {
            tvBloodPressure.setText("Blood Pressure: " + document.getString("Blood Pressure"));
        }
        if (document.contains("Min Heart Rate Morning")) {
            tvMinHRMorning.setText("Lower Bound Heart Rate (AM): " + document.getLong("Min Heart Rate Morning"));
        }
        if (document.contains("Max Heart Rate Morning")) {
            tvMaxHRMorning.setText("Higher Bound Heart Rate (AM): " + document.getLong("Max Heart Rate Morning"));
        }
        if (document.contains("Min Heart Rate Afternoon")) {
            tvMinHRPM.setText("Lower Bound Heart Rate (PM): " + document.getLong("Min Heart Rate Afternoon"));
        }
        if (document.contains("Max Heart Rate Afternoon")) {
            tvMaxHRPM.setText("Higher Bound Heart Rate (PM): " + document.getLong("Max Heart Rate Afternoon"));
        }
        if (document.contains("Oxygen Levels")) {
            tvOxygenLevels.setText("Oxygen Levels: " + document.getLong("Oxygen Levels"));
        }
        if (document.contains("Sleep Quality")) {
            tvSleep.setText("Sleep: " + document.getString("Sleep Quality"));
        }

        tvAbnormalActivity.setText("Abnormal Activity Detected: " + abnormalities);

    }

    private void fetchPatientName(String selectedUserID) {
        // Fetch data from Firestore based on the selected user's ID
        FirebaseFirestore.getInstance().collection("users")
                .document(selectedUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Extract patient name and surname from document
                            String patientName = documentSnapshot.getString("name");
                            String patientSurname = documentSnapshot.getString("surname");
                            String username = patientName + " " + patientSurname;

                            // Update UI with the fetched patient name
                            updatePatientNameUI(username);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error fetching patient profile document", e);
                    }
                });
    }

    private void fetchPatientLimits(String selectedUserID) {
        // Fetch data from Firestore based on the selected user's ID
        FirebaseFirestore.getInstance().collection("patient_profiles")
                .document(selectedUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Extract patient name and surname from document
                            String normalBP = documentSnapshot.getString("normal blood pressure");
                            String limitBP = documentSnapshot.getString("threshold blood pressure");
                            String normalHR = documentSnapshot.getString("heart");
                            String limitHR = documentSnapshot.getString("threshold heart");


                            // Update UI with the fetched patient name
                            updatePatientLimitsUI(normalBP, limitBP, normalHR, limitHR);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error fetching patient profile document", e);
                    }
                });
    }

    private void updatePatientNameUI(String patientName) {
        // Update the PatientName TextView with the fetched patient name
        TextView tvPatientName = findViewById(R.id.PatientName);
        tvPatientName.setText("Patient Name: " + patientName);
    }

    private void updatePatientLimitsUI(String normalBP, String limitBP, String normalHR, String limitHR){

        TextView tvBDNormal = findViewById(R.id.BDNormal);
        tvBDNormal.setText("Normal Blood Pressure: " + normalBP);

        TextView tvBDLimits = findViewById(R.id.BDLimits);
        tvBDLimits.setText("Blood Pressure Limits: " + limitBP);

        TextView tvHRNormal = findViewById(R.id.HRNormal);
        tvHRNormal.setText("Normal Heart Rate: " + normalHR);

        TextView tvHRLimits = findViewById(R.id.HRLimits);
        tvHRLimits.setText("Limit Heart Rate: " + limitHR);
    }

}
