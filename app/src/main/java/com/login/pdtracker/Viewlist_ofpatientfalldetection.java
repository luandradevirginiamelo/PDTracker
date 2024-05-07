package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.login.pdtracker.R;

import java.util.ArrayList;
import java.util.List;

public class Viewlist_ofpatientfalldetection extends AppCompatActivity {

    private Spinner spinnerUsers;
    private FirebaseFirestore db;
    private List<String> patientNamesWithDate = new ArrayList<>(); // Initialize the list here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewlist_ofpatientfalldetection);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        spinnerUsers = findViewById(R.id.spinner_users);

        // Fetch patients with fall detection from Firestore
        fetchPatientsWithFallDetection();

        // Button to go back to dashboard
        AppCompatButton bt_bdb = findViewById(R.id.bt_bdb);
        bt_bdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to Doctors_Dashboard_Screen activity
                Intent intent = new Intent(Viewlist_ofpatientfalldetection.this, Doctors_Dashboard_Screen.class);
                startActivity(intent);
            }
        });
    }

    private void fetchPatientsWithFallDetection() {
        db.collection("Doctor")
                .whereEqualTo("userAuthID", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String doctorID = document.getString("DoctorID");
                                if (doctorID != null) {
                                    fetchPatientsByDoctorID(doctorID);
                                }
                            }
                        } else {
                            // Handle errors
                            Toast.makeText(Viewlist_ofpatientfalldetection.this, "Failed to fetch patients", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void fetchPatientsByDoctorID(String doctorID) {
        db.collection("AbnormalHeartRateDetected")
                .whereEqualTo("DoctorID", doctorID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            patientNamesWithDate.clear(); // Clear the list before adding new patient names with date
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String patientName = document.getString("PatientName");
                                String fallDetectionTime = document.getTimestamp("FallDetectionTime").toDate().toString();
                                String formattedDate = formatDate(fallDetectionTime);
                                patientNamesWithDate.add(patientName + " - " + formattedDate);
                            }
                            // Update spinner adapter data
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    Viewlist_ofpatientfalldetection.this,
                                    android.R.layout.simple_spinner_item,
                                    patientNamesWithDate
                            );
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerUsers.setAdapter(adapter);
                        } else {
                            // Handle errors
                            Toast.makeText(Viewlist_ofpatientfalldetection.this, "Failed to fetch patients", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private String formatDate(String dateString) {
        // Your date formatting logic here
        return dateString; // Placeholder, replace with actual formatting
    }
}
