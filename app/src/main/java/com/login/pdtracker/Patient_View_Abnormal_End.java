package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Patient_View_Abnormal_End extends AppCompatActivity {

    private TextView patientNameTextView, doctorNameTextView, instructionsTextView, dateTextView;
    private String selectedUserID, documentID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_view_abnormality_end);

        // Initialize views
        patientNameTextView = findViewById(R.id.PatientName);
        doctorNameTextView = findViewById(R.id.DoctorName);
        instructionsTextView = findViewById(R.id.Instructions);
        dateTextView = findViewById(R.id.Date);

        // Retrieve the selected user's ID and document ID from the intent
        selectedUserID = getIntent().getStringExtra("selectedUserID");
        documentID = getIntent().getStringExtra("documentID");

        // Fetch and display relevant health data from Firestore
        fetchPatientName(selectedUserID);
        fetchHealthData(selectedUserID, documentID);

        findViewById(R.id.bt_bdb).setOnClickListener(v -> {
            Intent intent = new Intent(Patient_View_Abnormal_End.this, Patients_Home.class);
            startActivity(intent);
            finish();
        });
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

    private void updatePatientNameUI(String patientName) {
        // Update the PatientName TextView with the fetched patient name
        TextView tvPatientName = findViewById(R.id.PatientName);
        tvPatientName.setText("Patient Name: " + patientName);
    }

    private void fetchHealthData(String selectedUserID, String documentID) {
        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch data from Firestore based on the provided documentID
        db.collection("DoctorsNoteForPatientEmergency")
                .document(documentID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Retrieve data from the document
                            String patientName = documentSnapshot.getString("PatientName");
                            String doctorID = documentSnapshot.getString("DoctorID");
                            String instructions = documentSnapshot.getString("NotesText");
                            Timestamp timestamp = documentSnapshot.getTimestamp("NotesDate");

                            // Fetch doctor details based on doctorID
                            fetchDoctorDetails(doctorID, patientName, instructions, timestamp);
                        } else {
                            Log.e("Firestore", "Document does not exist");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error fetching document", e);
                    }
                });
    }

    private void fetchDoctorDetails(String doctorID, String patientName, String instructions, Timestamp timestamp) {
        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch doctor details from the Doctor collection
        db.collection("Doctor")
                .document(doctorID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Retrieve doctor name and surname
                            String doctorName = documentSnapshot.getString("DoctorName");
                            String doctorSurname = documentSnapshot.getString("DoctorSurname");

                            // Set data to the respective TextViews
                            if (doctorName != null && doctorSurname != null) {
                                doctorNameTextView.setText("Doctor Name: " + doctorName + " " + doctorSurname);
                            }
                            if (patientName != null) {
                                patientNameTextView.setText("Patient Name: " + patientName);
                            }
                            if (instructions != null) {
                                instructionsTextView.setText("Instructions for Action: " + instructions);
                            }
                            if (timestamp != null) {
                                dateTextView.setText("Date: " + timestamp.toDate().toString());
                            }
                        } else {
                            Log.e("Firestore", "Doctor document does not exist");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error fetching doctor document", e);
                    }
                });
    }
}
