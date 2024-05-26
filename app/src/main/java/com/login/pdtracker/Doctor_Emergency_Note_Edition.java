package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Doctor_Emergency_Note_Edition extends AppCompatActivity {

    private TextView textViewNotesDate;
    private TextView textViewNotesText;
    private EditText editNoteText;
    private EditText editNextOfKin;
    private EditText editTextPhone;
    private AppCompatButton Edit;

    private AppCompatButton bt_bdb;
    private String selectedDocumentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_update_notes_edition);

        Intent intent = getIntent();
        if (intent != null) {
            selectedDocumentId = intent.getStringExtra("selectedDocumentID");
        }

        textViewNotesDate = findViewById(R.id.textViewNotesDate);
        textViewNotesText = findViewById(R.id.textViewNotesText);
        editNoteText = findViewById(R.id.editNoteText);
        editNextOfKin = findViewById(R.id.editNextOfKin);
        editTextPhone = findViewById(R.id.editTextPhone);

        fetchDocumentFromFirestore(selectedDocumentId);

        Edit = findViewById(R.id.Edit);
        Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChangesToFirestore();
            }
        });

        bt_bdb = findViewById(R.id.bt_bdb);
        bt_bdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to Doctors_Dashboard_Screen activity
                Intent intent = new Intent(Doctor_Emergency_Note_Edition.this, Doctors_Dashboard_Screen.class);
                startActivity(intent);
            }
        });
    }

    private void fetchDocumentFromFirestore(String documentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("DoctorsNoteForPatientEmergency")
                .document(documentId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            editNoteText.setText(documentSnapshot.getString("NotesText"));
                            editNextOfKin.setText(documentSnapshot.getString("PatientNextOfKinName"));
                            editTextPhone.setText(documentSnapshot.getString("PatientNextOfKinMobileNumber"));
                            textViewNotesDate.setText("Notes Date: " + formatDate(documentSnapshot.getDate("NotesDate")));
                            textViewNotesText.setText("Notes Text: " + documentSnapshot.getString("NotesText"));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Doctor_Emergency_Note_Edition.this, "Error fetching document", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveChangesToFirestore() {
        String updatedNoteText = editNoteText.getText().toString();
        String updatedNextOfKin = editNextOfKin.getText().toString();
        String updatedPhone = editTextPhone.getText().toString();

        updateFirestoreDocument("NotesText", updatedNoteText);
        updateFirestoreDocument("PatientNextOfKinName", updatedNextOfKin);
        updateFirestoreDocument("PatientNextOfKinMobileNumber", updatedPhone);
    }

    private void updateFirestoreDocument(String field, String value) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("DoctorsNoteForPatientEmergency")
                .document(selectedDocumentId)
                .update(field, value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Doctor_Emergency_Note_Edition.this, "Document updated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Doctor_Emergency_Note_Edition.this, "Error updating document", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy 'at' HH:mm:ss z", Locale.getDefault());
        return dateFormat.format(date);
    }
}
