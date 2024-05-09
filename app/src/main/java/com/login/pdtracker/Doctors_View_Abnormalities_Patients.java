package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Doctors_View_Abnormalities_Patients extends AppCompatActivity {

    private Spinner spinnerNotes;
    private AppCompatButton bt_bdb;
    private FirebaseFirestore db;
    private String selectedUserID; // Variable to store selected user ID
    private List<String> noteIDs = new ArrayList<>(); // List to store note document IDs

    private AppCompatButton bUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_abnormalities_patients);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        IniciarComponentes();

        // Button to go back to dashboard
        bt_bdb = findViewById(R.id.bt_bdb);
        bt_bdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to Doctors_Dashboard_Screen activity
                Intent intent = new Intent(Doctors_View_Abnormalities_Patients.this, Doctors_Dashboard_Screen.class);
                startActivity(intent);
            }
        });

        // Get the selected user ID passed from the previous activity
        selectedUserID = getIntent().getStringExtra("selectedUserID");

        // Fetch notes for the selected patient ID
        fetchNotesForPatient(selectedUserID);

        // Update button click listener
        bUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedNoteIndex = spinnerNotes.getSelectedItemPosition();
                String selectedNoteID = noteIDs.get(selectedNoteIndex); // Get the selected note ID

                // Navigate to Doctor_Emergency_Note_Edition activity along with the selected note ID
                Intent intent = new Intent(Doctors_View_Abnormalities_Patients.this, Doctor_Abnormal_Send.class);
                intent.putExtra("selectedDocumentId", selectedNoteID);
                startActivity(intent);
            }
        });
    }

    private void fetchNotesForPatient(String patientID) {
        db.collection("Patient_Abnormal_Codition_Detected") // Change Firestore collection name
                .whereEqualTo("PatientID", patientID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> noteDates = new ArrayList<>();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy 'at' HH:mm:ss z", Locale.getDefault());
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Get the document ID and add it to the list
                                String noteID = document.getId();
                                noteIDs.add(noteID);

                                // Get the date of the note and format it
                                Date noteDate = document.getDate("DetectionTime");
                                if (noteDate != null) {
                                    String formattedDate = dateFormat.format(noteDate);
                                    noteDates.add(formattedDate);
                                }
                            }
                            // Populate the spinner with formatted note dates
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    Doctors_View_Abnormalities_Patients.this,
                                    android.R.layout.simple_spinner_item,
                                    noteDates
                            );
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerNotes.setAdapter(adapter);
                        } else {
                            // Handle errors
                        }
                    }
                });
    }

    private void IniciarComponentes() {
        spinnerNotes = findViewById(R.id.spinner_users);
        bUpdate = findViewById(R.id.Edit);
        bt_bdb = findViewById(R.id.bt_bdb2);
    }
}
