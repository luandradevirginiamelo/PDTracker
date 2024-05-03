package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Create_Update_PProfile extends AppCompatActivity {

    private String buttonMode;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_update_patient_profile);
        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buttonMode = getIntent().getStringExtra("mode");
        Log.d("ProfileMode", "Current mode: " + buttonMode);
        if ("Create".equals(buttonMode)) {
            setCreateMode();
        } else {
            setUpdateMode();
        }

        findViewById(R.id.bt_bdb).setOnClickListener(v -> {
            Intent intent = new Intent(Create_Update_PProfile.this, Patients_Home.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.bt_save).setOnClickListener(v -> saveOrUpdatePatientProfile());
    }

    private void setUpdateMode() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("patient_profiles").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    populateFields(document);
                } else {
                    Log.e("FirestoreError", "Error loading document", task.getException());
                    Toast.makeText(this, "Erro ao carregar dados: " + (task.getException() != null ? task.getException().getMessage() : "Desconhecido"), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void populateFields(DocumentSnapshot document) {
        ((EditText) findViewById(R.id.Patient_Hight)).setText(document.getString("height"));
        ((EditText) findViewById(R.id.Patient_weight)).setText(document.getString("weight"));
        ((EditText) findViewById(R.id.Patient_heart)).setText(document.getString("heart"));
        ((EditText) findViewById(R.id.threshold_hr)).setText(document.getString("threshold heart"));
        ((EditText) findViewById(R.id.Nbloodpre)).setText(document.getString("normal blood pressure"));
        ((EditText) findViewById(R.id.threshold_bloodpre)).setText(document.getString("threshold blood pressure"));
        ((EditText) findViewById(R.id.Smartw_ID)).setText(document.getString("smartID"));
        ((EditText) findViewById(R.id.Smart_name)).setText(document.getString("smartname"));
        ((EditText) findViewById(R.id.Smartwatch_model)).setText(document.getString("smartDevicemodel"));
    }

    private void setCreateMode() {
        buttonMode = "Create";
        clearFields();
    }

    private void clearFields() {
        ((EditText) findViewById(R.id.Patient_Hight)).setText("");
        ((EditText) findViewById(R.id.Patient_weight)).setText("");
        ((EditText) findViewById(R.id.Patient_heart)).setText("");
        ((EditText) findViewById(R.id.threshold_hr)).setText("");
        ((EditText) findViewById(R.id.Nbloodpre)).setText("");
        ((EditText) findViewById(R.id.threshold_bloodpre)).setText("");
        ((EditText) findViewById(R.id.Smartw_ID)).setText("");
        ((EditText) findViewById(R.id.Smart_name)).setText("");
        ((EditText) findViewById(R.id.Smartwatch_model)).setText("");
    }

    private void saveOrUpdatePatientProfile() {
        // Validate weight
        if (!validatePatientWeight()) {
            Toast.makeText(this, "Please correct the weight value.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate heart rate
        if (!validateHeartRate()) {
            Toast.makeText(this, "Please correct the heart rate value.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateThresholdHeartRate()) {
            Toast.makeText(this, "Please correct the Threshold heart value.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validateNormalBloodPressure()) {
            Toast.makeText(this, "Please correct the Normal Blood Pressure value should be between 60-120.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validateThresholdBloodPressure()) {
            Toast.makeText(this, "Threshold Blood Pressure should be between 40-200.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Map<String, Object> updates = new HashMap<>();
            collectFieldData(updates);

            if ("Create".equals(buttonMode) && !updates.isEmpty()) {
                db.collection("patient_profiles").document(userId).set(updates)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("ProfileCreation", "Profile created successfully");
                            Toast.makeText(this, "Profile created successfully!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreError", "Error creating profile", e);
                            Toast.makeText(this, "Error creating profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else if (!updates.isEmpty()) {
                db.collection("patient_profiles").document(userId).update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("ProfileUpdate", "Profile updated successfully");
                            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreError", "Error updating profile", e);
                            Toast.makeText(this, "Error updating profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                Toast.makeText(this, "No alteration detected.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "User not found, try login again.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validatePatientWeight() {
        EditText weightEditText = findViewById(R.id.Patient_weight);
        String weightStr = weightEditText.getText().toString();
        if (!weightStr.isEmpty()) {
            try {
                double weight = Double.parseDouble(weightStr);
                if (weight < 40.0 || weight > 150.0) {
                    weightEditText.setError("Weight must be between 40kg and 150kg");
                    return false;
                }
            } catch (NumberFormatException e) {
                weightEditText.setError("Invalid number");
                return false;
            }
        }
        return true;
    }
    private boolean validateHeartRate() {
        EditText heartRateEditText = findViewById(R.id.Patient_heart);
        String heartRateStr = heartRateEditText.getText().toString();
        if (!heartRateStr.isEmpty()) {
            try {
                int heartRate = Integer.parseInt(heartRateStr);
                if (heartRate < 60 || heartRate > 100) {
                    heartRateEditText.setError("Heart rate must be between 60 and 100 bpm");
                    return false;
                }
            } catch (NumberFormatException e) {
                heartRateEditText.setError("Invalid number");
                return false;
            }
        }
        return true;
    }
    private boolean validateThresholdHeartRate() {
        EditText thresholdHrEditText = findViewById(R.id.threshold_hr);
        String thresholdHrStr = thresholdHrEditText.getText().toString();
        if (!thresholdHrStr.isEmpty()) {
            try {
                int thresholdHr = Integer.parseInt(thresholdHrStr);
                if (thresholdHr < 180) {
                    thresholdHrEditText.setError("Threshould heart exceed 180 bpm");
                    return false;
                }
            } catch (NumberFormatException e) {
                thresholdHrEditText.setError("Invalid number");
                return false;
            }
        }
        return true;
    }
    private boolean validateNormalBloodPressure() {
        EditText nbpEditText = findViewById(R.id.Nbloodpre);
        String nbpStr = nbpEditText.getText().toString();
        if (!nbpStr.isEmpty()) {
            try {
                String[] nbpParts = nbpStr.split("-");
                if (nbpParts.length == 2) {
                    int nbpLow = Integer.parseInt(nbpParts[0]);
                    int nbpHigh = Integer.parseInt(nbpParts[1]);
                    if (nbpLow < 60 || nbpHigh > 120) {
                        nbpEditText.setError("Normal Blood Pressure should be between 60-120.");
                        return false;
                    }
                } else {
                    nbpEditText.setError("Please enter the Normal Blood Pressure in the format XX-XXX.");
                    return false;
                }
            } catch (NumberFormatException e) {
                nbpEditText.setError("Invalid number");
                return false;
            }
        }
        return true;
    }

    private boolean validateThresholdBloodPressure() {
        EditText tbpEditText = findViewById(R.id.threshold_bloodpre);
        String tbpStr = tbpEditText.getText().toString();
        if (!tbpStr.isEmpty()) {
            try {
                String[] tbpParts = tbpStr.split("-");
                if (tbpParts.length == 2) {
                    int tbpLow = Integer.parseInt(tbpParts[0]);
                    int tbpHigh = Integer.parseInt(tbpParts[1]);
                    if (tbpLow < 40 || tbpHigh > 200) {
                        tbpEditText.setError("Threshold Blood Pressure should be between 40-200.");
                        return false;
                    }
                } else {
                    tbpEditText.setError("Please enter the Threshold Blood Pressure in the format XX-XXX.");
                    return false;
                }
            } catch (NumberFormatException e) {
                tbpEditText.setError("Invalid number");
                return false;
            }
        }
        return true;
    }

    private void collectFieldData(Map<String, Object> updates) {
        updateField(updates, "height", ((EditText) findViewById(R.id.Patient_Hight)).getText().toString());
        updateField(updates, "weight", ((EditText) findViewById(R.id.Patient_weight)).getText().toString());
        updateField(updates, "heart", ((EditText) findViewById(R.id.Patient_heart)).getText().toString());
        updateField(updates, "threshold heart", ((EditText) findViewById(R.id.threshold_hr)).getText().toString());
        updateField(updates, "normal blood pressure", ((EditText) findViewById(R.id.Nbloodpre)).getText().toString());
        updateField(updates, "threshold blood pressure", ((EditText) findViewById(R.id.threshold_bloodpre)).getText().toString());
        updateField(updates, "smartID", ((EditText) findViewById(R.id.Smartw_ID)).getText().toString());
        updateField(updates, "smartname", ((EditText) findViewById(R.id.Smart_name)).getText().toString());
        updateField(updates, "smartDevicemodel", ((EditText) findViewById(R.id.Smartwatch_model)).getText().toString());
    }

    private void updateField(Map<String, Object> updates, String fieldKey, String fieldValue) {
        if (fieldValue != null && !fieldValue.isEmpty()) {
            updates.put(fieldKey, fieldValue);
        }
    }
}
