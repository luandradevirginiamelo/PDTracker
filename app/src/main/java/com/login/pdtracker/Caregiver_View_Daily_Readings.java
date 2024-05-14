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

public class Caregiver_View_Daily_Readings extends AppCompatActivity {

    private Spinner spinnerUsers;
    private FirebaseFirestore db;
    private List<String> userIDs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caregiver_view_daily_readings);

        db = FirebaseFirestore.getInstance();

        spinnerUsers = findViewById(R.id.spinner_users);

        AppCompatButton bt_view = findViewById(R.id.View);
        bt_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected user's position in the spinner
                int selectedPosition = spinnerUsers.getSelectedItemPosition();

                // Check if a user is selected
                if (selectedPosition != AdapterView.INVALID_POSITION) {
                    // Get the corresponding user ID from the list
                    String selectedUserID = userIDs.get(selectedPosition);

                    // Pass the selected user's document ID to the next activity
                    Intent intent = new Intent(Caregiver_View_Daily_Readings.this, Caregiver_View_Daily_Reads_By_Patient.class);
                    intent.putExtra("selectedUserID", selectedUserID);
                    startActivity(intent);
                } else {
                    // Handle case when no user is selected
                    Log.e("Error", "No user selected");
                }
            }
        });

        AppCompatButton bt_bdb = findViewById(R.id.bt_bdb);
        bt_bdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Caregiver_View_Daily_Readings.this, Doctors_Dashboard_Screen.class);
                startActivity(intent);
            }
        });

        getLoggedCaregiverDocumentID(new Callback<String>() {
            @Override
            public void onSuccess(String caregiverDocumentID) {
                // Fetch users every time the activity is created
                fetchUsers(caregiverDocumentID);
            }

            @Override
            public void onError(String message) {
                Log.e("Error", message);
            }
        });
    }

    private void fetchUsers(String caregiverDocumentID) {
        db.collection("users")
                .whereEqualTo("IDcaregiver", caregiverDocumentID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            userIDs.clear();
                            List<String> userNames = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String userID = document.getId();
                                userIDs.add(userID);
                                String name = document.getString("name") + " " + document.getString("surname");
                                userNames.add(name);
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    Caregiver_View_Daily_Readings.this,
                                    android.R.layout.simple_spinner_item,
                                    userNames
                            );
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerUsers.setAdapter(adapter);
                        } else {
                            Log.e("Fetch Users", "Error getting users", task.getException());
                        }
                    }
                });
    }

    private void getLoggedCaregiverDocumentID(Callback<String> callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String caregiverUID = currentUser.getUid();
            FirebaseFirestore.getInstance().collection("Caregiver")
                    .document(caregiverUID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String caregiverDocumentID = documentSnapshot.getId();
                                callback.onSuccess(caregiverDocumentID);
                            } else {
                                String message = "Caregiver is not logged in";
                                callback.onError(message);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String message = "Caregiver is not logged in";
                            callback.onError(message);
                        }
                    });
        } else {
            String message = "Current user is null";
            callback.onError(message);
        }
    }

    interface Callback<T> {
        void onSuccess(T result);

        void onError(String message);
    }
}
