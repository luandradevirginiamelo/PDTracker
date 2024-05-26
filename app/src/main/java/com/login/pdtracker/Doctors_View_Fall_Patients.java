package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Doctors_View_Fall_Patients extends AppCompatActivity {

    private Spinner spinner;
    private List<String> timeList;
    private Map<String, String> timestampToDocID;
    private String selectedUserID;
    private Button bt_bdb;  // Add this line

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_abnormalities_patients);

        spinner = findViewById(R.id.spinner_users);

        // Retrieve the selected user's ID from the intent
        selectedUserID = getIntent().getStringExtra("selectedUserID");

        timeList = new ArrayList<>();
        timestampToDocID = new HashMap<>();

        // Use the selectedUserID to fetch and display relevant health data from Firestore
        fetchHealthData(selectedUserID);

        findViewById(R.id.View).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected timestamp from the spinner
                String selectedTimestamp = timeList.get(spinner.getSelectedItemPosition());
                // Get the corresponding document ID
                String documentID = timestampToDocID.get(selectedTimestamp);

                // Pass both selectedUserID and documentID to Doctor_Abnormal_Send class
                Intent intent = new Intent(Doctors_View_Fall_Patients.this, Doctor_Fall_Send.class);
                intent.putExtra("selectedUserID", selectedUserID);
                intent.putExtra("documentID", documentID);
                startActivity(intent);
            }
        });

        // Find the button by its ID and set an OnClickListener
        bt_bdb = findViewById(R.id.bt_bdb);  // Add this line
        bt_bdb.setOnClickListener(new View.OnClickListener() {  // Add this block
            @Override
            public void onClick(View v) {
                // Go back to Doctors_Dashboard_Screen activity
                Intent intent = new Intent(Doctors_View_Fall_Patients.this, Doctors_Dashboard_Screen.class);
                startActivity(intent);
            }
        });
    }

    private void fetchHealthData(String selectedUserID) {
        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch data from Firestore based on the selected user's ID
        db.collection("Patient_Fall_Detected")
                .whereEqualTo("PatientID", selectedUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        timeList.clear(); // Clear existing data
                        timestampToDocID.clear(); // Clear existing mapping

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Timestamp timestamp = document.getTimestamp("Time");
                            if (timestamp != null) {
                                // Convert timestamp to string
                                String time = timestamp.toDate().toString();
                                // Store timestamp and corresponding document ID
                                String documentID = document.getId();
                                timeList.add(time);
                                timestampToDocID.put(time, documentID);
                            }
                        }


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
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeList);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }
}
