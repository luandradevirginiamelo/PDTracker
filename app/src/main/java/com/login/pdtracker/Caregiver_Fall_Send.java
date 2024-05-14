package com.login.pdtracker;
import android.Manifest;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Caregiver_Fall_Send extends AppCompatActivity {

    private TextView textViewPatientName;
    private TextView textViewPatientMobile;
    private TextView textViewCareGiverName;
    private TextView textViewCaregiverNo;
    private TextView textViewFallDetectedText;
    private TextView abnormalDetectedText;
    private String longitudeFormated;

    private String latitudeFormated;

    private TextView textViewLongitude;
    private FirebaseFirestore db;
    private String selectedDocumentId;
    private String selectedNoteID;
    private String patientID;
    private String PatientName;
    private String patientNumber;
    private String patientSmartWatchDevice;
    private String doctorID;
    private String caregiverID;

    private static final String SEND_SMS = "android.permission.SEND_SMS";


    private static final int SMS_PERMISSION_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caregiver_fall_create);


        Intent intent = getIntent();
        if (intent != null) {
            selectedDocumentId = intent.getStringExtra("selectedUserID");
            selectedNoteID = intent.getStringExtra("documentID");
        }

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize TextViews
        textViewPatientName = findViewById(R.id.textViewPatientName);
        textViewPatientMobile = findViewById(R.id.textViewPatientMobile);
        textViewCareGiverName = findViewById(R.id.textViewCareGiverName);
        textViewCaregiverNo = findViewById(R.id.textViewCaregiverNo);
        textViewFallDetectedText = findViewById(R.id.FallDetectedText);
        abnormalDetectedText = findViewById(R.id.AbnormalDetectedText);


        findViewById(R.id.SendNotificationParamedic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmergencySMS();
            }
        });

        findViewById(R.id.CallPatient).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numbersOnly = textViewPatientMobile.getText().toString().replaceAll("[^0-9]", "");
                callPhoneNumber(numbersOnly);
            }
        });

        findViewById(R.id.CallDoctor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numbersOnly = textViewCaregiverNo.getText().toString().replaceAll("[^0-9]", "");
                callPhoneNumber(numbersOnly);
            }
        });

        findViewById(R.id.CallParamedic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParamedicsNumber(selectedDocumentId, new OnParamedicsNumberListener() {
                    @Override
                    public void onParamedicsNumberRetrieved(String paramedicsNumber) {
                        // Call the paramedics using the retrieved number
                        callPhoneNumber(paramedicsNumber);
                    }

                    @Override
                    public void onError(Exception e) {
                        // Handle error
                        Log.e("Doctor_Fall_Send", "Error fetching paramedics number: ", e);
                    }
                });
            }
        });

        // Fetch patient details and then fetch caregiver details
        fetchPatientDetails();
        fetchAbnormalities();
        fetchFallDetectionTime();
    }

    private void getParamedicsNumber(String userID, OnParamedicsNumberListener listener) {
        db.collection("DoctorsNoteForPatientEmergency")
                .whereEqualTo("PatientID", selectedDocumentId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Access the document here
                                String paramedicsNumber = document.getString("ParamedicsNumber");
                                // Call the paramedics using the retrieved number
                                if (listener != null) {
                                    listener.onParamedicsNumberRetrieved(paramedicsNumber);
                                }
                                return; // Exit after the first match
                            }
                        } else {
                            Log.e("Doctor_Fall_Send", "Error fetching paramedics number: ", task.getException());
                            if (listener != null) {
                                listener.onError(task.getException());
                            }
                        }
                    }
                });
    }

    // Define a callback interface
    interface OnParamedicsNumberListener {
        void onParamedicsNumberRetrieved(String paramedicsNumber);
        void onError(Exception e);
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
                            PatientName = document.getString("name")+document.getString("surname");
                            patientNumber = document.getString("mobile");

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
                                                    // Fetch threshold heart and blood pressure from patient_profile
                                                    String thresholdHeart = patientProfileDoc.getString("threshold heart");
                                                    String thresholdBloodPressure = patientProfileDoc.getString("threshold blood pressure");

                                                    // Check for heart rate abnormalities
                                                    int maxHeartRate = healthDataDoc.getLong("Max Heart Rate Morning").intValue();
                                                    int minHeartRate = healthDataDoc.getLong("Min Heart Rate Morning").intValue();
                                                    final String heartRateAbnormality;
                                                    if (maxHeartRate > Integer.parseInt(thresholdHeart) || minHeartRate < Integer.parseInt(thresholdHeart)) {
                                                        heartRateAbnormality = "Heart Rate Abnormality: " + Math.max(maxHeartRate, minHeartRate) +"BPM";
                                                    } else {
                                                        heartRateAbnormality = null;
                                                    }

                                                    // Check for blood pressure abnormalities
                                                    String bloodPressure = healthDataDoc.getString("Blood Pressure");
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

    private void fetchFallDetectionTime() {
        db.collection("Patient_Fall_Detected")
                .document(selectedNoteID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot fallDetectionDoc = task.getResult();
                            Double latitude = fallDetectionDoc.getDouble("Latitude");
                            Double longitude = fallDetectionDoc.getDouble("Longitude");
                             latitudeFormated = "Latitude: " + latitude;
                            longitudeFormated = "Longitude: " + longitude;

                            Timestamp fallTimestamp = fallDetectionDoc.getTimestamp("Time");
                            if (fallTimestamp != null) {
                                Date fallDate = fallTimestamp.toDate();
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                String formattedDate = sdf.format(fallDate);
                                textViewFallDetectedText.setText(formattedDate);
                            } else {
                                Log.e("Doctor_Fall_Send", "Fall detection time is null");
                            }
                        } else {
                            Log.e("Doctor_Fall_Send", "Error fetching fall detection time");
                        }
                    }
                });
    }

    private void callPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
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

    private void sendEmergencySMS() {
        // Check for SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Request SMS permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, send SMS
            sendSMS();
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setData(Uri.parse("sms:" + phoneNumber));
        smsIntent.putExtra("sms_body", message);
        startActivity(smsIntent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, send SMS
                sendSMS("0830707014", "Emergency Alert! A Parkinson patient has fallen at Latitude: "+latitudeFormated+" and longitude "+longitudeFormated+". Name: "+PatientName+" Contact: "+patientNumber);
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void sendSMS(){
        sendSMS("0830707014", "Emergency Alert! A Parkinson patient has fallen at Latitude: "+latitudeFormated+" and longitude "+longitudeFormated+". Name: "+PatientName+" Contact: "+patientNumber);
    }

}
