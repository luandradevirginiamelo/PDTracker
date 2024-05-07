package com.login.pdtracker;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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

import java.util.ArrayList;
import java.util.List;

public class Doctors_View_Abnormalities extends AppCompatActivity {

    private Spinner spinnerUsers;
    private FirebaseFirestore db;
    private List<String> userNamesWithDate = new ArrayList<>(); // Initialize the list here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_abnormalities_detected);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        spinnerUsers = findViewById(R.id.spinner_users);

        // Button to go back to dashboard
        AppCompatButton bt_bdb = findViewById(R.id.bt_bdb2);
        bt_bdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to Doctors_Dashboard_Screen activity
                finish();
            }
        });

        // Initialize spinner adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUsers.setAdapter(adapter);

        // Fetch users
        fetchUsers();
    }

    private void fetchUsers() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userUID = currentUser.getUid();

            db.collection("Doctor")
                    .document(userUID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    String doctorID = document.getString("DoctorID");
                                    if (doctorID != null) {
                                        fetchUsersByDoctorID(doctorID);
                                    }
                                }
                            } else {
                                // Handle errors
                            }
                        }
                    });
        }
    }

    private void fetchUsersByDoctorID(String doctorID) {
        db.collection("AbnormalHeartRateDetected")
                .whereEqualTo("DoctorID", doctorID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            userNamesWithDate.clear(); // Clear the list before adding new user names with date
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String patientName = document.getString("PatientName");
                                String detectionTime = document.getTimestamp("DetectionTime").toDate().toString();
                                String formattedDate = formatDate(detectionTime);
                                userNamesWithDate.add(patientName + " - " + formattedDate);
                            }
                            // Update spinner adapter data
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerUsers.getAdapter();
                            adapter.clear(); // Clear previous data
                            adapter.addAll(userNamesWithDate); // Add new data
                            adapter.notifyDataSetChanged(); // Notify spinner to refresh its UI
                        } else {
                            // Handle errors
                        }
                    }
                });
    }

    private String formatDate(String dateString) {
        // Your date formatting logic here
        return dateString; // Placeholder, replace with actual formatting
    }
}
