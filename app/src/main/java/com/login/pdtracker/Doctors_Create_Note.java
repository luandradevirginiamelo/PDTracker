package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Doctors_Create_Note extends AppCompatActivity {

    private EditText SpecCondition, Allergies, NextOfKin, NextOfKinMobile, DoctorsNotes,
            ParamedicsNumber;

    private FirebaseFirestore db;
    private String selectedUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.window_doctor_emergency_note_one);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Receive the selectedUserID passed from the previous activity
        Intent intent = getIntent();
        if (intent != null) {
            selectedUserID = intent.getStringExtra("selectedUserID");
            if (selectedUserID == null) {
                // Handle null case appropriately
                Toast.makeText(this, "Error: Selected user ID is null", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity if selectedUserID is null
                return;
            }
        } else {
            // Handle null intent case appropriately
            Toast.makeText(this, "Error: Intent is null", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if intent is null
            return;
        }

        // Initialize EditText fields
        SpecCondition = findViewById(R.id.SpecCondition);
        Allergies = findViewById(R.id.Allergies);
        NextOfKin = findViewById(R.id.NextOfKin);
        NextOfKinMobile = findViewById(R.id.NextOfKinMobile);
        DoctorsNotes = findViewById(R.id.DoctorsNotes);
        ParamedicsNumber = findViewById(R.id.ParamedicsNumber);

        // Initialize button and set onClickListener
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        // Setup field validation
        setupMobileValidation();
        setupParamedicsValidation();
    }

    private void setupMobileValidation() {
        NextOfKinMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String mobile = s.toString();
                if (!isValidIrishMobile(mobile)) {
                    NextOfKinMobile.setError("A valid Irish mobile number should start with 083, 086, 087, 089, and be of total 10 digits in length.");
                } else {
                    NextOfKinMobile.setError(null);
                }
            }
        });
    }

    private void setupParamedicsValidation() {
        ParamedicsNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String mobile = s.toString();
                if (!isValidIrishMobile(mobile)) {
                    ParamedicsNumber.setError("A valid Irish mobile number should start with 083, 086, 087, 089, and be of total 10 digits in length.");
                } else {
                    ParamedicsNumber.setError(null);
                }
            }
        });
    }

    private boolean isValidIrishMobile(String mobile) {
        return Pattern.matches("^(083|085|086|087|089)\\d{7}$", mobile);
    }

    private boolean isValidParamedicsNumber(String number) {
        return Pattern.matches("^(083|085|086|087|089)\\d{7}$", number);
    }

    private void saveNote() {
        // Get data from EditText fields
        String specCondition = SpecCondition.getText().toString();
        String allergies = Allergies.getText().toString();
        String nextOfKin = NextOfKin.getText().toString();
        String nextOfKinMobile = NextOfKinMobile.getText().toString();
        String doctorsNotes = DoctorsNotes.getText().toString();
        String paramedicsNumber = ParamedicsNumber.getText().toString();

        // Create a reference to the user document in Firestore
        DocumentReference userRef = db.collection("users").document(selectedUserID);

        // Fetch the DoctorID asynchronously
        fetchDoctorID(selectedUserID).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String doctorID) {
                if (doctorID != null) {
                    // Create a new document with the collected data
                    Map<String, Object> note = new HashMap<>();
                    note.put("DoctorID", doctorID); // Populate DoctorID with the fetched value
                    note.put("NotesDate", FieldValue.serverTimestamp());
                    note.put("NotesText", doctorsNotes);
                    note.put("PatientAllergies", allergies);
                    note.put("PatientID", selectedUserID);
                    note.put("PatientNextOfKinMobileNumber", nextOfKinMobile);
                    note.put("PatientNextOfKinName", nextOfKin);
                    note.put("PatientSpecificMedicalCondition", specCondition);
                    note.put("ParamedicsNumber", paramedicsNumber); // Add ParamedicsNumber to the note

                    // Add the document to Firestore
                    db.collection("DoctorsNoteForPatientEmergency")
                            .add(note)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    // Document successfully added
                                    sendNotificationToPatient(selectedUserID);
                                    Toast.makeText(Doctors_Create_Note.this, "Note saved successfully", Toast.LENGTH_SHORT).show();
                                    finish(); // Close the activity after saving
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle errors
                                    Toast.makeText(Doctors_Create_Note.this, "Failed to save note", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // DoctorID is null, handle appropriately
                    Toast.makeText(Doctors_Create_Note.this, "DoctorID not found", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle errors
                Toast.makeText(Doctors_Create_Note.this, "Failed to fetch DoctorID", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void sendNotificationToPatient(String patientID) {
        // Get the token of the patient from Firestore using patientID
        db.collection("UserTokens").document(patientID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String patientToken = documentSnapshot.getString("token");
                            if (patientToken != null) {
                                // Construct notification payload
                                Map<String, String> data = new HashMap<>();
                                data.put("title", "New Note Added");
                                data.put("body", "A new note has been added by your doctor.");
                                data.put("patientID", patientID);

                                // Send FCM notification
                                sendFCMNotification(patientToken, data);
                            }
                        }
                    }
                });
    }

    private void sendFCMNotification(String token, Map<String, String> data) {
        // Construct notification message
        RemoteMessage.Builder builder = new RemoteMessage.Builder(token);
        builder.setData(data);
        RemoteMessage message = builder.build();

        // Send notification
        FirebaseMessaging.getInstance().send(message);
    }

    private Task<String> fetchDoctorID(String selectedUserID) {
        // Initialize a Task object to handle the asynchronous operation
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();

        // Fetch data from Firestore based on the selected user's ID
        FirebaseFirestore.getInstance().collection("users")
                .document(selectedUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Extract DoctorID from document
                            String doctorID = documentSnapshot.getString("DoctorID");

                            // Resolve the Task with the retrieved DoctorID
                            taskCompletionSource.setResult(doctorID);
                        } else {
                            // If the document does not exist, resolve with null
                            taskCompletionSource.setResult(null);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure by resolving with null and logging the error
                        taskCompletionSource.setResult(null);
                        Log.e("Firestore", "Error fetching patient profile document", e);
                    }
                });

        // Return the Task object's future result
        return taskCompletionSource.getTask();
    }
}
