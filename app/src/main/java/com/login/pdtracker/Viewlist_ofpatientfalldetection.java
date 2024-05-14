package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class Viewlist_ofpatientfalldetection extends AppCompatActivity {

    private Spinner spinnerUsers;
    private FirebaseFirestore db;
    private List<String> userNamesWithDate = new ArrayList<>(); // Initialize the list here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewlist_ofpatientfalldetection);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        spinnerUsers = findViewById(R.id.spinner_users);

        // Button to go back to dashboard
        AppCompatButton bt_bdb = findViewById(R.id.bt_bdb);
        AppCompatButton btView = findViewById(R.id.View);
        bt_bdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to Doctors_Dashboard_Screen activity
                finish();
            }
        });

        btView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected item position from the spinner
                int selectedItemPosition = spinnerUsers.getSelectedItemPosition();

                // Check if a valid item is selected
                if (selectedItemPosition != Spinner.INVALID_POSITION) {
                    // Get the document ID associated with the selected item
                    String selectedDocumentID = getSelectedDocumentID(selectedItemPosition);

                    String[] names = selectedDocumentID.split(" ");

                    String firstName = names[0];
                    String lastName = names[1];

                    FirebaseFirestore.getInstance().collection("users")
                            .whereEqualTo("name", firstName)
                            .whereEqualTo("surname", lastName)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                        String userId = documentSnapshot.getId();
                                        Intent intent = new Intent(Viewlist_ofpatientfalldetection.this, Doctors_View_Fall_Patients.class);
                                        intent.putExtra("selectedUserID", userId);
                                        startActivity(intent);
                                    } else {
                                        // Handle case where no user with the given name and surname is found
                                        Log.e("Firestore", "User not found with name: " + firstName + " and surname: " + lastName);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("Firestore", "Error fetching user data", e);
                                }
                            });
                }
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
                    .whereEqualTo("userAuthID", userUID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QuerySnapshot querySnapshot = task.getResult();
                                if (!querySnapshot.isEmpty()) {
                                    DocumentSnapshot document = querySnapshot.getDocuments().get(0);
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
                            userNamesWithDate.clear(); // Clear the list before adding new user names with date
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String patientName = document.getString("name");
                                String patientSurname = document.getString("surname");
                                String fullName = patientName + " " + patientSurname;
                                userNamesWithDate.add(fullName);
                            }
                            // Update spinner adapter data
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerUsers.getAdapter();
                            adapter.clear();
                            adapter.addAll(userNamesWithDate);
                            adapter.notifyDataSetChanged();
                        } else {
                            // Handle errors
                        }
                    }
                });
    }

    private String getSelectedDocumentID(int position) {
        // Ensure position is within bounds of the list
        if (position >= 0 && position < userNamesWithDate.size()) {
            // Retrieve the document ID from the list at the selected position
            return userNamesWithDate.get(position);
        } else {
            return null; // Return null if position is invalid
        }
    }
}
