package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Map;
import java.util.HashMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;


public class Create_Update_PProfile extends AppCompatActivity {

    private String buttonMode;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_update_patient_profile);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Set up Edge to Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Determine mode from intent
        buttonMode = getIntent().getStringExtra("mode");
        if ("Create".equals(buttonMode)) {
            setCreateMode();
        } else {
            setUpdateMode();  // You need to implement this method to set up the UI for updating
        }

        // Set up the back to dashboard button
        findViewById(R.id.bt_bdb).setOnClickListener(v -> {
            Intent intent = new Intent(Create_Update_PProfile.this, Patients_Home.class);
            startActivity(intent);
            finish();
        });

        // Set up the save button
        findViewById(R.id.bt_save).setOnClickListener(v -> saveOrUpdatePatientProfile());
    }

    private void setUpdateMode() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            // Buscar o documento do perfil do paciente
            db.collection("patient_profiles").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    // Configurar os campos com os dados existentes
                    ((EditText) findViewById(R.id.Patient_Hight)).setText(document.getString("height"));
                    ((EditText) findViewById(R.id.Patient_weight)).setText(document.getString("weight"));
                    ((EditText) findViewById(R.id.Patient_heart)).setText(document.getString("heart"));
                    ((EditText) findViewById(R.id.Normal_OVS)).setText(document.getString("normalOVS"));
                    ((EditText) findViewById(R.id.Treshold)).setText(document.getString("threshold"));
                    ((EditText) findViewById(R.id.Smart_details)).setText(document.getString("smartDetails"));
                    ((EditText) findViewById(R.id.Smart_model)).setText(document.getString("smartModel"));
                    ((EditText) findViewById(R.id.Smart_DEVIDEID)).setText(document.getString("smartDeviceId"));
                } else {
                    Toast.makeText(Create_Update_PProfile.this, "Erro ao carregar dados: " + (task.getException() != null ? task.getException().getMessage() : "Desconhecido"), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void setCreateMode() {
        buttonMode = "Create";
        // Show all EditText fields
        // Note: Ensure that these IDs correspond to your layout
        findViewById(R.id.Patient_Hight).setVisibility(View.VISIBLE);
        findViewById(R.id.Patient_weight).setVisibility(View.VISIBLE);
        findViewById(R.id.Patient_heart).setVisibility(View.VISIBLE);
        findViewById(R.id.Normal_OVS).setVisibility(View.VISIBLE);
        findViewById(R.id.Treshold).setVisibility(View.VISIBLE);
        findViewById(R.id.Smart_details).setVisibility(View.VISIBLE);
        findViewById(R.id.Smart_model).setVisibility(View.VISIBLE);
        findViewById(R.id.Smart_DEVIDEID).setVisibility(View.VISIBLE);

        // Hide all update buttons
        // Note: Adjust these IDs based on your actual UI elements
        findViewById(R.id.Update).setVisibility(View.GONE);
        findViewById(R.id.Update2).setVisibility(View.GONE);
        findViewById(R.id.Update3).setVisibility(View.GONE);
        findViewById(R.id.Update4).setVisibility(View.GONE);
        findViewById(R.id.Update5).setVisibility(View.GONE);
        findViewById(R.id.Update6).setVisibility(View.GONE);
        findViewById(R.id.Update7).setVisibility(View.GONE);
        findViewById(R.id.Update8).setVisibility(View.GONE);
    }

    private void saveOrUpdatePatientProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Map<String, Object> updates = new HashMap<>();

            // Verificar cada campo e adicionar ao mapa de updates apenas se não estiver vazio
            updateField(updates, "height", ((EditText) findViewById(R.id.Patient_Hight)).getText().toString());
            updateField(updates, "weight", ((EditText) findViewById(R.id.Patient_weight)).getText().toString());
            updateField(updates, "heart", ((EditText) findViewById(R.id.Patient_heart)).getText().toString());
            updateField(updates, "normalOVS", ((EditText) findViewById(R.id.Normal_OVS)).getText().toString());
            updateField(updates, "threshold", ((EditText) findViewById(R.id.Treshold)).getText().toString());
            updateField(updates, "smartDetails", ((EditText) findViewById(R.id.Smart_details)).getText().toString());
            updateField(updates, "smartModel", ((EditText) findViewById(R.id.Smart_model)).getText().toString());
            updateField(updates, "smartDeviceId", ((EditText) findViewById(R.id.Smart_DEVIDEID)).getText().toString());

            if (!updates.isEmpty()) {
                // Atualizar o documento com apenas os campos que mudaram
                db.collection("patient_profiles").document(userId).update(updates)
                        .addOnSuccessListener(aVoid -> Toast.makeText(Create_Update_PProfile.this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(Create_Update_PProfile.this, "Erro ao atualizar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } else {
                Toast.makeText(this, "Nenhuma alteração detectada.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateField(Map<String, Object> updates, String fieldKey, String fieldValue) {
        if (fieldValue != null && !fieldValue.isEmpty()) {
            updates.put(fieldKey, fieldValue);
        }
    }
}
