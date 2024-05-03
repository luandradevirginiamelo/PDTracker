package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Doctors_View_Daily_Readings extends AppCompatActivity {

    private Spinner spinnerUsers;
    private FirebaseFirestore db;
    private List<String> userIDs = new ArrayList<>(); // Initialize the list here

    interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_view_daily_readings);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize spinner
        spinnerUsers = findViewById(R.id.spinner_users);

        AppCompatButton bt_view = findViewById(R.id.View);
        bt_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the next activity
                Intent intent = new Intent(Doctors_View_Daily_Readings.this, Doctors_View_Daily_Reads_By_Patient.class);
                startActivity(intent);
            }
        });

        // Button to go back to dashboard
        AppCompatButton bt_bdb = findViewById(R.id.bt_bdb);
        bt_bdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to Doctors_Dashboard_Screen activity
                Intent intent = new Intent(Doctors_View_Daily_Readings.this, Doctors_Dashboard_Screen.class);
                startActivity(intent);
            }
        });

        // Fetch users from Firestore where DoctorID matches logged doctor's documentID
        getLoggedDoctorDocumentID(new Callback<String>() {
            @Override
            public void onSuccess(String doctorDocumentID) {
                fetchUsers(doctorDocumentID);
            }

            @Override
            public void onError(String message) {
                // Handle errors
                Log.e("Error", message);
            }
        });
    }

    private void fetchUsers(String doctorDocumentID) {
        db.collection("users")
                .whereEqualTo("DoctorID", 1) // Filter users where DoctorID matches logged doctor's documentID
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
                                    Doctors_View_Daily_Readings.this,
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
    }

    // Dummy method to get the logged doctor's documentID
    private void getLoggedDoctorDocumentID(Callback<String> callback) {
        // Get the UID of the logged-in user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String doctorUID = currentUser.getUid();

            // Query the Doctor collection to find the documentID where userAuthID == doctorUID
            FirebaseFirestore.getInstance().collection("Doctor")
                    .whereEqualTo("userAuthID", doctorUID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                // Retrieve the first document's ID (assuming there's only one matching document)
                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                String doctorDocumentID = documentSnapshot.getId();
                                // Use the retrieved doctor documentID
                                callback.onSuccess(doctorDocumentID);
                            } else {
                                // Handle the case where no matching document is found
                                String message = "Doctor is not logged in";
                                callback.onError(message);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failures
                            String message = "Doctor is not logged in";
                            callback.onError(message);
                        }
                    });
        } else {
            // Handle the case where the current user is null
            String message = "Current user is null";
            callback.onError(message);
        }
    }
}


