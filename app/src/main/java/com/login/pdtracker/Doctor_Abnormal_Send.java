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

    private TextView abnormalDetectedText;

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
        abnormalDetectedText = findViewById(R.id.AbnormalDetectedText);

        // Fetch patient details and then fetch caregiver details
        fetchPatientDetails();
        fetchAbnormalities();
        findViewById(R.id.SendNotification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification();
            }
        });
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

    private void fetchAbnormalities() {
        db.collection("patient_profiles")
                .document(selectedDocumentId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot patientProfileDoc = task.getResult();
                            String smartID = patientProfileDoc.getString("smartID");
                            if (smartID != null) {
                                db.collection("health_data")
                                        .document(smartID)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful() && task.getResult() != null) {
                                                    DocumentSnapshot healthDataDoc = task.getResult();
                                                    String maxHeartRateMorning = String.valueOf(healthDataDoc.getLong("Max Heart Rate Morning"));
                                                    String maxHeartRateAfternoon = String.valueOf(healthDataDoc.getLong("Max Heart Rate Afternoon"));
                                                    String minHeartRateMorning = String.valueOf(healthDataDoc.getLong("Min Heart Rate Morning"));
                                                    String minHeartRateAfternoon = String.valueOf(healthDataDoc.getLong("Min Heart Rate Afternoon"));
                                                    String bloodPressure = healthDataDoc.getString("Blood Pressure");

                                                    // Fetch threshold heart and blood pressure from patient_profile
                                                    String thresholdHeart = patientProfileDoc.getString("threshold heart");
                                                    String thresholdBloodPressure = patientProfileDoc.getString("threshold blood pressure");

                                                    // Check for heart rate abnormalities
                                                    int maxHeartRate = Math.max(Integer.parseInt(maxHeartRateMorning), Integer.parseInt(maxHeartRateAfternoon));
                                                    int minHeartRate = Math.min(Integer.parseInt(minHeartRateMorning), Integer.parseInt(minHeartRateAfternoon));
                                                    final String heartRateAbnormality;
                                                    if (maxHeartRate > Integer.parseInt(thresholdHeart) || minHeartRate < Integer.parseInt(thresholdHeart)) {
                                                        heartRateAbnormality = "Heart Rate Abnormality: " + Math.max(maxHeartRate, minHeartRate) +"BPM";
                                                    } else {
                                                        heartRateAbnormality = null;
                                                    }

                                                    // Check for blood pressure abnormalities
                                                    String[] bloodPressureValues = bloodPressure.split("/");
                                                    int systolicBP = Integer.parseInt(bloodPressureValues[0]);
                                                    int diastolicBP = Integer.parseInt(bloodPressureValues[1]);
                                                    final String bloodPressureAbnormality;

                                                    String[] bloodPressureLimitsValues = thresholdBloodPressure.split("/");
                                                    int LimitsystolicBP = Integer.parseInt(bloodPressureLimitsValues[0]);
                                                    int LimitdiastolicBP = Integer.parseInt(bloodPressureLimitsValues[1]);


                                                    if (systolicBP > LimitsystolicBP || diastolicBP > LimitdiastolicBP) {
                                                        bloodPressureAbnormality = "Blood Pressure Alert: " + bloodPressure;
                                                    } else {
                                                        bloodPressureAbnormality = null;
                                                    }

                                                    // Update the TextViews with detected abnormalities
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            TextView abnormalDetectedText = findViewById(R.id.AbnormalDetectedText);
                                                            if (heartRateAbnormality != null && bloodPressureAbnormality != null) {
                                                                // Concatenate both abnormalities if both are not null
                                                                int maxHeartRate = Integer.parseInt(maxHeartRateMorning);
                                                                int minHeartRate = Integer.parseInt(minHeartRateMorning);
                                                                abnormalDetectedText.setText("Heart Rate peak " + Math.max(maxHeartRate, minHeartRate) + " BPMs registered and " + bloodPressureAbnormality);
                                                            } else if (heartRateAbnormality != null) {
                                                                // Set text for heart rate abnormality
                                                                abnormalDetectedText.setText(heartRateAbnormality);
                                                            } else if (bloodPressureAbnormality != null) {
                                                                // Set text for blood pressure abnormality
                                                                abnormalDetectedText.setText(bloodPressureAbnormality);
                                                            } else {
                                                                // Set text if no abnormalities detected
                                                                abnormalDetectedText.setText("No abnormalities detected");
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    Log.e("Doctor_Abnormal_Send", "Error fetching health data");
                                                }
                                            }
                                        });
                            } else {
                                Log.e("Doctor_Abnormal_Send", "Smart ID is null");
                            }
                        } else {
                            Log.e("Doctor_Abnormal_Send", "Error fetching patient profile");
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

    public void sendNotification() {
        String notificationText = editTextNotification.getText().toString();
        if (!notificationText.isEmpty()) {
            // Create a map to store the data
            Map<String, Object> notificationData = new HashMap<>();

            // Add the values from TextViews
            notificationData.put("Patient Name", textViewPatientName.getText().toString());
            notificationData.put("Patient Mobile", textViewPatientMobile.getText().toString());
            notificationData.put("Caregiver Name", textViewCareGiverName.getText().toString());
            notificationData.put("Caregiver Mobile", textViewCaregiverNo.getText().toString());

            // Check if abnormalDetectedText is not null before accessing its text
            if (abnormalDetectedText != null) {
                notificationData.put("Abnormality Detected", abnormalDetectedText.getText().toString());
            } else {
                notificationData.put("Abnormality Detected", "");
            }

            // Add the PatientID
            notificationData.put("PatientID", selectedDocumentId);

            // Add the notification text
            notificationData.put("TextNotification", notificationText);

            // Add timestamp
            notificationData.put("Notification", Timestamp.now());

            // Add the DoctorID and CaregiverID
            notificationData.put("DoctorID", doctorID);
            notificationData.put("Caregiver", caregiverID);

            // Add the notification data to the Firestore collection
            db.collection("Notification_For_Patient_Abnormal_Detected")
                    .document()
                    .set(notificationData)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Doctor_Abnormal_Send.this, "Notification sent successfully", Toast.LENGTH_SHORT).show();

                                // Navigate to the doctor's dashboard screen
                                Intent intent = new Intent(Doctor_Abnormal_Send.this, Doctors_Dashboard_Screen.class);
                                startActivity(intent);
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
