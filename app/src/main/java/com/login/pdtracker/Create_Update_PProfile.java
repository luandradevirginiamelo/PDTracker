package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class Create_Update_PProfile extends AppCompatActivity {

    private String buttonMode;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_update_patient_profile);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Setting up Edge to Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up the back to dashboard button
        findViewById(R.id.bt_bdb).setOnClickListener(v -> {
            Intent intent = new Intent(Create_Update_PProfile.this, Patients_Home.class);
            startActivity(intent);
            finish();
        });

        // Set up the create patient profile button
        findViewById(R.id.bt_CPP).setOnClickListener(v -> setCreateMode());

        // Set up the save button
        findViewById(R.id.bt_save).setOnClickListener(v -> saveOrUpdatePatientProfile());
    }

    private void setCreateMode() {
        buttonMode = "Create";
        // Show all EditText fields
        // Note: Ensure that these IDs correspond to your layout
        findViewById(R.id.Patient_Hight).setVisibility(View.VISIBLE);
        findViewById(R.id.Patient_weight).setVisibility(View.VISIBLE);
        findViewById(R.id.Patient_heart).setVisibility(View.VISIBLE);
        findViewById(R.id.Normal_OVS).setVisibility(View.VISIBLE);
        findViewById(R.id.Treshold).setVisibility(View.VISIBLE);
        findViewById(R.id.Smart_details).setVisibility(View.VISIBLE);
        findViewById(R.id.Smart_model).setVisibility(View.VISIBLE);
        findViewById(R.id.Smart_DEVIDEID).setVisibility(View.VISIBLE);

        // Hide all update buttons
        // Note: Adjust these IDs based on your actual UI elements
        findViewById(R.id.Update).setVisibility(View.GONE);
        findViewById(R.id.Update2).setVisibility(View.GONE);
        findViewById(R.id.Update3).setVisibility(View.GONE);
        findViewById(R.id.Update4).setVisibility(View.GONE);
        findViewById(R.id.Update5).setVisibility(View.GONE);
        findViewById(R.id.Update6).setVisibility(View.GONE);
        findViewById(R.id.Update7).setVisibility(View.GONE);
        findViewById(R.id.Update8).setVisibility(View.GONE);
    }

    private void saveOrUpdatePatientProfile() {
        // Retrieving EditText fields
        EditText editHeight = findViewById(R.id.Patient_Hight);
        EditText editWeight = findViewById(R.id.Patient_weight);
        EditText editHeart = findViewById(R.id.Patient_heart);
        EditText editNormalOVS = findViewById(R.id.Normal_OVS);
        EditText editThreshold = findViewById(R.id.Treshold);
        EditText editSmartDetails = findViewById(R.id.Smart_details);
        EditText editSmartModel = findViewById(R.id.Smart_model);
        EditText editSmartDeviceId = findViewById(R.id.Smart_DEVIDEID);

        // Extracting the string values from EditTexts
        String height = editHeight.getText().toString();
        String weight = editWeight.getText().toString();
        String heart = editHeart.getText().toString();
        String normalOVS = editNormalOVS.getText().toString();
        String threshold = editThreshold.getText().toString();
        String smartDetails = editSmartDetails.getText().toString();
        String smartModel = editSmartModel.getText().toString();
        String smartDeviceId = editSmartDeviceId.getText().toString();

         class PatientProfile {
            private String height;
            private String weight;
            private String heart;
            private String normalOVS;
            private String threshold;
            private String smartDetails;
            private String smartModel;
            private String smartDeviceId;

            // Construtor vazio necessário para Firestore
            public PatientProfile() {
            }
            public PatientProfile(String height, String weight, String heart, String normalOVS, String threshold, String smartDetails, String smartModel, String smartDeviceId) {
                this.height = height;
                this.weight = weight;
                this.heart = heart;
                this.normalOVS = normalOVS;
                this.threshold = threshold;
                this.smartDetails = smartDetails;
                this.smartModel = smartModel;
                this.smartDeviceId = smartDeviceId;
            }

            // Getters e Setters
            public String getHeight() { return height; }
            public void setHeight(String height) { this.height = height; }
            public String getWeight() { return weight; }
            public void setWeight(String weight) { this.weight = weight; }
            public String getHeart() { return heart; }
            public void setHeart(String heart) { this.heart = heart; }
            public String getNormalOVS() { return normalOVS; }
            public void setNormalOVS(String normalOVS) { this.normalOVS = normalOVS; }
            public String getThreshold() { return threshold; }
            public void setThreshold(String threshold) { this.threshold = threshold; }
            public String getSmartDetails() { return smartDetails; }
            public void setSmartDetails(String smartDetails) { this.smartDetails = smartDetails; }
            public String getSmartModel() { return smartModel; }
            public void setSmartModel(String smartModel) { this.smartModel = smartModel; }
            public String getSmartDeviceId() { return smartDeviceId; }
            public void setSmartDeviceId(String smartDeviceId) { this.smartDeviceId = smartDeviceId; }
        }
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            PatientProfile profile = new PatientProfile(height, weight, heart, normalOVS, threshold, smartDetails, smartModel, smartDeviceId);
            if ("Create".equals(buttonMode)) {
                // Create new patient profile
                db.collection("patient_profiles").document(userId).set(profile)
                        .addOnSuccessListener(aVoid -> Toast.makeText(Create_Update_PProfile.this, "Profile created successfully!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(Create_Update_PProfile.this, "Error creating profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } else {
                // Update existing patient profile
                db.collection("patient_profiles").document(userId).update(
                                "height", height,
                                "weight", weight,
                                "heart", heart,
                                "normalOVS", normalOVS,
                                "threshold", threshold,
                                "smartDetails", smartDetails,
                                "smartModel", smartModel,
                                "smartDeviceId", smartDeviceId)
                        .addOnSuccessListener(aVoid -> Toast.makeText(Create_Update_PProfile.this, "Profile updated successfully.!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(Create_Update_PProfile.this, "Error updating default Android profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }
    }
}

