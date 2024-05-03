package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.login.pdtracker.R;

import java.util.ArrayList;
import java.util.List;

public class Doctor_Emergency_Notes extends AppCompatActivity {

    private Spinner spinnerUsers;
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

        // Fetch users from Firestore where DoctorID = 1
        fetchUsers();
    }


    private void fetchUsers() {
        db.collection("users")
                .whereEqualTo("DoctorID", 1) // Filter users where DoctorID is 1
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

        bCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!userIDs.isEmpty()) { // Check if the list is not empty
                    // Get the selected user's UID from the list based on the selected item position of the spinner
                    String selectedUserID = userIDs.get(spinnerUsers.getSelectedItemPosition());

                    // Start the next activity and pass the selected user's UID as an extra
                    Intent intent = new Intent(Doctor_Emergency_Notes.this, Doctors_Create_Note.class);
                    intent.putExtra("selectedUserID", selectedUserID);
                    startActivity(intent);
                } else {
                    // Handle error case appropriately
                }
            }
        });

        bUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!userIDs.isEmpty()) { // Check if the list is not empty
                    // Get the selected user's UID from the list based on the selected item position of the spinner
                    String selectedUserID = userIDs.get(spinnerUsers.getSelectedItemPosition());

                    // Start the next activity and pass the selected user's UID as an extra
                    Intent intent = new Intent(Doctor_Emergency_Notes.this, Doctors_Update_Note.class);
                    intent.putExtra("selectedUserID", selectedUserID);
                    startActivity(intent);
                } else {
                    // Handle error case appropriately
                }
            }
        });
    }

    private void IniciarComponentes() {
        spinnerUsers = findViewById(R.id.spinner_users);
        bCreate = findViewById(R.id.Create);
        bUpdate = findViewById(R.id.Update);
    }
}