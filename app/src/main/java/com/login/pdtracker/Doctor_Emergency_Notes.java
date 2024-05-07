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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.login.pdtracker.R;

import java.util.ArrayList;
import java.util.List;

public class Doctor_Emergency_Notes extends AppCompatActivity {

    private Spinner spinnerUsers;

    private AppCompatButton bt_bdb;
    private FirebaseFirestore db;
    private List<String> userIDs = new ArrayList<>(); // Initialize the list here

    private AppCompatButton bCreate;
    private AppCompatButton bUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_emergency_notes);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        IniciarComponentes();

        // Button to go back to dashboard
        AppCompatButton bt_bdb = findViewById(R.id.bt_bdb);
        bt_bdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to Doctors_Dashboard_Screen activity
                Intent intent = new Intent(Doctor_Emergency_Notes.this, Doctors_Dashboard_Screen.class);
                startActivity(intent);
            }
        });

        // Fetch users from Firestore where DoctorID matches logged doctor's documentID
        fetchUsers();

    }



    private void fetchUsers() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userUID = currentUser.getUid();

            db.collection("Doctor")
                    .whereEqualTo("userAuthID", userUID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
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
        db.collection("users")
                .whereEqualTo("DoctorID", doctorID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            userIDs.clear(); // Clear the list before adding new user IDs
                            List<String> userNames = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String userID = document.getId(); // Get the user ID
                                userIDs.add(userID); // Add user ID to the list
                                String name = document.getString("name") + " " + document.getString("surname");
                                userNames.add(name);
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    Doctor_Emergency_Notes.this,
                                    android.R.layout.simple_spinner_item,
                                    userNames
                            );
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerUsers.setAdapter(adapter);
                        } else {
                            // Handle errors
                        }
                    }
                });
        spinnerUsers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Pass the selected user's ID to the next activity
                String selectedUserID = userIDs.get(position);
                bCreate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Go to Doctors_Create_Note activity
                        Intent intent = new Intent(Doctor_Emergency_Notes.this, Doctors_Create_Note.class);
                        intent.putExtra("selectedUserID", selectedUserID);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void IniciarComponentes() {
        spinnerUsers = findViewById(R.id.spinner_users);
        bCreate = findViewById(R.id.Create);
        bUpdate = findViewById(R.id.Update);
        bt_bdb = findViewById(R.id.bt_bdb2);
    }
}
