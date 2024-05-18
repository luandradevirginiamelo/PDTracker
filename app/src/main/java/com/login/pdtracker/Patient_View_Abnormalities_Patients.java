package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Patient_View_Abnormalities_Patients extends AppCompatActivity {

    private Spinner spinner;
    private List<String> timeList;
    private Map<String, String> timestampToDocID;
    private String selectedUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abnormal_conditions_patient);

        spinner = findViewById(R.id.spinner_users);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            selectedUserID = currentUser.getUid();
            Log.d("UserID", "Current user ID: " + selectedUserID);
        } else {
            Log.e("UserID", "No user is currently logged in.");
            return;
        }

        timeList = new ArrayList<>();
        timestampToDocID = new HashMap<>();

        fetchHealthData(selectedUserID);

        findViewById(R.id.bt_Create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timeList.isEmpty()) {
                    Log.e("Spinner Error", "timeList is empty, nothing to select.");
                    return;
                }
                // Get the selected timestamp from the spinner
                String selectedTimestamp = timeList.get(spinner.getSelectedItemPosition());
                // Get the corresponding document ID
                String documentID = timestampToDocID.get(selectedTimestamp);

                // Pass both selectedUserID and documentID to Caregiver_View_Abnormal_End class
                Intent intent = new Intent(Patient_View_Abnormalities_Patients.this, Patient_View_Abnormal_End.class);
                intent.putExtra("selectedUserID", selectedUserID);
                intent.putExtra("documentID", documentID);
                startActivity(intent);
            }
        });

        findViewById(R.id.bt_bdb).setOnClickListener(v -> {
            Intent intent = new Intent(Patient_View_Abnormalities_Patients.this, Patients_Home.class);
            startActivity(intent);
            finish();
        });
    }

    private void fetchHealthData(String selectedUserID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("DoctorsNoteForPatientEmergency")
                .whereEqualTo("PatientID", selectedUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        timeList.clear(); // Clear existing data
                        timestampToDocID.clear(); // Clear existing mapping

                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d("Firestore", "No documents found for the user.");
                        } else {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Timestamp timestamp = document.getTimestamp("NotesDate");
                                if (timestamp != null) {
                                    // Convert timestamp to string
                                    String time = timestamp.toDate().toString();
                                    // Store timestamp and corresponding document ID
                                    String documentID = document.getId();
                                    timeList.add(time);
                                    timestampToDocID.put(time, documentID);
                                } else {
                                    Log.w("Firestore", "Document with ID " + document.getId() + " has no timestamp.");
                                }
                            }
                            Log.d("Firestore", "Data fetched successfully.");
                        }

                        // Populate the spinner with timeList
                        populateSpinner(timeList);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error fetching patient abnormal condition detected documents", e);
                    }
                });
    }

    private void populateSpinner(List<String> timeList) {
        if (timeList.isEmpty()) {
            Log.w("Spinner", "No data available to populate the spinner.");
            return;
        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeList);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }
}
