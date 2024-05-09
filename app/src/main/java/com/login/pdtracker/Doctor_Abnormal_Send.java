package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
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
            selectedDocumentId = intent.getStringExtra("selectedDocumentId");
            selectedNoteID = intent.getStringExtra("selectedNoteID");
        }

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize TextViews and EditText
        textViewPatientName = findViewById(R.id.textViewPatientName);
        textViewPatientMobile = findViewById(R.id.textViewPatientMobile);
        textViewCareGiverName = findViewById(R.id.textViewCareGiverName);
        textViewCaregiverNo = findViewById(R.id.textViewCaregiverNo);
        editTextNotification = findViewById(R.id.editText);

        // Fetch data from Firestore
        fetchPatientDetails();
        //fetchCareGiverDetails(caregiverDocumentID);
    }

    private void fetchPatientDetails() {
        db.collection("Patient_Abnormal_Codition_Detected")
                .document(selectedDocumentId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            textViewPatientName.setText("Patient Name: " + document.getString("PatientName"));
                            patientID = document.getString("PatientID");
                                                                                                                                                                           patientSmartWatchDevice = document.getString("PatientSmartWatchDevice"); // Assuming this is the correct field name
                            doctorID = document.getString("DoctorID");
                              String caregiverDocumentID = document.getString("CaregiverID"); // Get the document ID of the caregiver
                            fetchCareGiverDetails(document.getString("CaregiverID")); // Fetch caregiver details using the document ID
                        }
                    }
                });
    }

    private void fetchCareGiverDetails(String caregiverDocumentID) {
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
                            caregiverID = document.getString("IDcaregiver");
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
