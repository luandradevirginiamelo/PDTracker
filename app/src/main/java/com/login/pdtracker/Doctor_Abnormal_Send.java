package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.Timestamp;


import java.util.HashMap;
import java.util.Map;

public class Doctor_Abnormal_Send extends AppCompatActivity {

    private TextView textViewPatientName;
    private TextView textViewPatientMobile;
    private TextView textViewCareGiverName;
    private TextView textViewCaregiverNo;
    private EditText editTextNotification;
    private FirebaseFirestore db;
    private String selectedDocumentId;
    private String selectedNoteID;
    private String patientID;
    private String patientSmartWatchDevice;
    private String doctorID;
    private String caregiverID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_abnormalities_create);

        Intent intent = getIntent();
        if (intent != null) {
            selectedDocumentId = intent.getStringExtra("selectedUserID");
            selectedNoteID = intent.getStringExtra("documentID");
        }

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize TextViews and EditText
        textViewPatientName = findViewById(R.id.textViewPatientName);
        textViewPatientMobile = findViewById(R.id.textViewPatientMobile);
        textViewCareGiverName = findViewById(R.id.textViewCareGiverName);
        textViewCaregiverNo = findViewById(R.id.textViewCaregiverNo);
        editTextNotification = findViewById(R.id.Notification);

        // Fetch patient details and then fetch caregiver details
        fetchPatientDetails();
    }

    private void fetchPatientDetails() {
        db.collection("users")
                .document(selectedDocumentId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            textViewPatientName.setText("Patient Name: " + document.getString("name")+ " "+ document.getString("surname") );
                            textViewPatientMobile.setText("Patient Mobile: " + document.getString("mobile"));
                            patientID = document.getString("PatientID");
                            patientSmartWatchDevice = document.getString("SmartWatchDevice");
                            doctorID = document.getString("DoctorID");
                            caregiverID = document.getString("IDcaregiver");

                            // Check if caregiverID is not null before fetching caregiver details
                            if (caregiverID != null) {
                                fetchCareGiverDetails();
                            } else {
                                Log.e("Doctor_Abnormal_Send", "CaregiverID is null");
                            }
                        } else {
                            Log.e("Doctor_Abnormal_Send", "Error fetching patient details");
                        }
                    }
                });
    }

    private void fetchCareGiverDetails() {
        db.collection("Caregiver")
                .document(caregiverID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            textViewCareGiverName.setText("Caregiver Name: " + document.getString("name"));
                            textViewCaregiverNo.setText("Caregiver Mobile: " + document.getString("mobile"));
                        }
                    }
                });
    }

    public void sendNotification(View view) {
        String notificationText = editTextNotification.getText().toString();
        if (!notificationText.isEmpty()) {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("PatientID", patientID);
            notificationData.put("PatientSmartWatchDevice", patientSmartWatchDevice);
            notificationData.put("TextNotification", notificationText);
            notificationData.put("Notification", Timestamp.now());
            notificationData.put("DoctorID", doctorID);
            notificationData.put("Caregiver", caregiverID);

            // Add the notification to the Firestore collection
            db.collection("AbnormalitiesDetectedNotes")
                    .document()
                    .set(notificationData)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Doctor_Abnormal_Send.this, "Notification sent successfully", Toast.LENGTH_SHORT).show();

                                finish();
                            } else {
                                Toast.makeText(Doctor_Abnormal_Send.this, "Failed to send notification", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Please enter notification text", Toast.LENGTH_SHORT).show();
        }
    }
}
