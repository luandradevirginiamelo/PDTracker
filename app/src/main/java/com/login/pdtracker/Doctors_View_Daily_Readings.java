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
                // Get the selected user ID from the spinner
                String selectedUserID = userIDs.get(spinnerUsers.getSelectedItemPosition());
                // Start the next activity and pass the selected user ID
                Intent intent = new Intent(Doctors_View_Daily_Readings.this, Doctors_View_Daily_Reads_By_Patient.class);
                intent.putExtra("selectedUserID", selectedUserID);
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
        fetchUsers();
    }

    private void fetchUsers() {
        // Get the UID of the logged-in user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String doctorUID = currentUser.getUid();

            // Query the Doctor collection to find the DoctorID where userAuthID == doctorUID
            FirebaseFirestore.getInstance().collection("Doctor")
                    .whereEqualTo("userAuthID", doctorUID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                // Retrieve the first document's DoctorID (assuming there's only one matching document)
                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                String doctorID = documentSnapshot.getString("DoctorID");
                                if (doctorID != null) {
                                    fetchUsersByDoctorID(doctorID);
                                } else {
                                    // Handle the case where DoctorID is null
                                    String message = "DoctorID is null";
                                    Log.e("Error", message);
                                }
                            } else {
                                // Handle the case where no matching document is found
                                String message = "Doctor is not logged in";
                                Log.e("Error", message);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failures
                            String message = "Failed to fetch Doctor";
                            Log.e("Error", message, e);
                        }
                    });
        } else {
            // Handle the case where the current user is null
            String message = "Current user is null";
            Log.e("Error", message);
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
                                    Doctors_View_Daily_Readings.this,
                                    android.R.layout.simple_spinner_item,
                                    userNames
                            );
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerUsers.setAdapter(adapter);
                        } else {
                            // Handle errors
                            Log.e("Error", "Error fetching users: ", task.getException());
                        }
                    }
                });
    }
}
