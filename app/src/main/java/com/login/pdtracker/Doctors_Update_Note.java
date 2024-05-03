package com.login.pdtracker;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Doctors_Update_Note extends AppCompatActivity {

    private EditText SpecCondition, Allergies, NextOfKin, NextOfKinMobile, DoctorsNotes, ParamedicsNumber;

    private Spinner spinnerUsers;
    private FirebaseFirestore db;
    private String selectedUserID;
    private boolean isMobileValid2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.window_doctor_emergency_note_two);

        db = FirebaseFirestore.getInstance();

        SpecCondition = findViewById(R.id.SpecCondition);
        Allergies = findViewById(R.id.Allergies);
        NextOfKin = findViewById(R.id.NextOfKin);
        NextOfKinMobile = findViewById(R.id.NextOfKinMobile); // Corrigir a inicialização do NextOfKinMobile2 aqui
        DoctorsNotes = findViewById(R.id.DoctorsNotes);
        ParamedicsNumber = findViewById(R.id.ParamedicsNumber);

        spinnerUsers = findViewById(R.id.spinner_users);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        fetchNotesForUser();

        setupFieldValidations();
    }

    private void setupFieldValidations() {
        setupMobileValidation();
        setupMobileValidation2();
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
                    isMobileValid2 = false;
                } else {
                    NextOfKinMobile.setError(null);
                    isMobileValid2 = true;
                }
            }
        });
    }

    private void setupMobileValidation2() {
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
                    isMobileValid2 = false;
                } else {
                    ParamedicsNumber.setError(null);
                    isMobileValid2 = true;
                }
            }
        });
    }

    private boolean isValidIrishMobile(String mobile) {
        return mobile.matches("^(083|085|086|087|089)\\d{7}$");
    }

    private void fetchNotesForUser() {
        db.collection("DoctorsNoteForPatientEmergency")
                .whereEqualTo("PatientID", selectedUserID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> notesList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String noteText = document.getString("NotesText");
                                notesList.add(noteText);
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    Doctors_Update_Note.this,
                                    android.R.layout.simple_spinner_item,
                                    notesList
                            );
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerUsers.setAdapter(adapter);
                            spinnerUsers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {}
                            });
                        } else {
                            Toast.makeText(Doctors_Update_Note.this, "Failed to fetch notes", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveNote() {
        NextOfKinMobile = findViewById(R.id.NextOfKinMobile); // Aqui também
        ParamedicsNumber = findViewById(R.id.ParamedicsNumber);
        String specCondition = SpecCondition.getText().toString();
        String allergies = Allergies.getText().toString();
        String nextOfKin = NextOfKin.getText().toString();
        String nextOfKinMobile = NextOfKinMobile.getText().toString();
        String doctorsNotes = DoctorsNotes.getText().toString();
        String paramedicsNumber = ParamedicsNumber.getText().toString();


        DocumentReference noteRef = db.collection("DoctorsNoteForPatientEmergency").document(selectedUserID);

        Map<String, Object> note = new HashMap<>();
        note.put("NotesText", doctorsNotes);
        note.put("PatientAllergies", allergies);
        note.put("PatientNextOfKinMobileNumber", nextOfKinMobile);
        note.put("PatientNextOfKinName", nextOfKin);
        note.put("PatientSpecificMedicalCondition", specCondition);

        noteRef.update(note)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Doctors_Update_Note.this, "Note updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Doctors_Update_Note.this, "Failed to update note", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}