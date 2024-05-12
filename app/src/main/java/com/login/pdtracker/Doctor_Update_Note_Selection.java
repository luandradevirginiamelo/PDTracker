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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Doctor_Update_Note_Selection extends AppCompatActivity {

    private Spinner spinnerNotes;
    private AppCompatButton bt_bdb;
    private FirebaseFirestore db;
    private String selectedUserID;
    private String selectedDocumentID;
    private AppCompatButton bEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_update_notes_selection);

        db = FirebaseFirestore.getInstance();
        IniciarComponentes();

        bt_bdb = findViewById(R.id.bt_bdb);
        bt_bdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Doctor_Update_Note_Selection.this, Doctors_Dashboard_Screen.class);
                startActivity(intent);
            }
        });

        selectedUserID = getIntent().getStringExtra("selectedUserID");
        fetchNotesForPatient(selectedUserID);

        bEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Doctor_Update_Note_Selection.this, Doctor_Emergency_Note_Edition.class);
                intent.putExtra("selectedDocumentID", selectedDocumentID);
                startActivity(intent);
            }
        });
    }

    private void fetchNotesForPatient(String patientID) {
        db.collection("DoctorsNoteForPatientEmergency")
                .whereEqualTo("PatientID", patientID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> noteDates = new ArrayList<>();
                            List<String> documentIDs = new ArrayList<>();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy 'at' HH:mm:ss z", Locale.getDefault());
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Date noteDate = document.getDate("NotesDate");
                                if (noteDate != null) {
                                    String formattedDate = dateFormat.format(noteDate);
                                    noteDates.add(formattedDate);
                                }
                                documentIDs.add(document.getId());
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    Doctor_Update_Note_Selection.this,
                                    android.R.layout.simple_spinner_item,
                                    noteDates
                            );
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerNotes.setAdapter(adapter);

                            spinnerNotes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedDocumentID = documentIDs.get(position);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });
                        } else {
                            // Handle errors
                        }
                    }
                });
    }

    private void IniciarComponentes() {
        spinnerNotes = findViewById(R.id.spinner_notes);
        bEdit = findViewById(R.id.Edit);
        bt_bdb = findViewById(R.id.bt_bdb2);
    }
}
