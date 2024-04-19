package com.login.pdtracker;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Patients_RegScreen extends AppCompatActivity {
    private EditText edit_name, edit_surname, edit_Mobile, edit_Patient_Doctors_Name, edit_Patient_Doctors_Surname, edit_Patient_Caregiver_Name, edit_Patient_Caregiver_Surname, edit_Username, edit_Password2, edit_Re_type;
    private Button bt_register;
    private TextView textViewError; // TextView for displaying error messages
    private boolean isMobileValid = false; // To track the validity of the mobile number

    String[] errorMessages = {"Please complete all required fields to proceed", "Registration Successful!", "Invalid Irish mobile number format informe 083, 085, 086, 087, or 089"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_register);

        iniciarComponentes();

        edit_Mobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 3 && !isValidIrishPrefix(s.toString().substring(0, 3))) {
                    textViewError.setText(errorMessages[2]);
                    textViewError.setVisibility(View.VISIBLE);
                    isMobileValid = false;
                } else {
                    textViewError.setVisibility(View.GONE);
                    isMobileValid = true;
                }
            }
        });

        bt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edit_name.getText().toString();
                String surname = edit_surname.getText().toString();
                String mobile = edit_Mobile.getText().toString();
                String patient_D_Name = edit_Patient_Doctors_Name.getText().toString();
                String patient_D_Surname = edit_Patient_Doctors_Surname.getText().toString();
                String patient_C_Name = edit_Patient_Caregiver_Name.getText().toString();
                String p_C_Surname = edit_Patient_Caregiver_Surname.getText().toString();
                String username = edit_Username.getText().toString();
                String password = edit_Password2.getText().toString();
                String retype = edit_Re_type.getText().toString();

                if (name.isEmpty() || surname.isEmpty() || mobile.isEmpty() || patient_D_Name.isEmpty() || patient_D_Surname.isEmpty() || patient_C_Name.isEmpty() || p_C_Surname.isEmpty() || username.isEmpty() || password.isEmpty() || retype.isEmpty()) {
                    Snackbar.make(v, errorMessages[0], Snackbar.LENGTH_SHORT).show();
                } else if (!isMobileValid) {
                    Snackbar.make(v, errorMessages[2], Snackbar.LENGTH_LONG).show();
                } else {
                    cadastrarUsuario(v, username, password);
                }
            }
        });
    }

    private boolean isValidIrishPrefix(String number) {
        return number.startsWith("083") || number.startsWith("085") || number.startsWith("086") || number.startsWith("087") || number.startsWith("089");
    }

    private void cadastrarUsuario(View v, String email, String password) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    saveUserData();
                    Snackbar snackbar = Snackbar.make(v, errorMessages[1], Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                } else {
                    String error = "Registration failed due to an error";
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        error = e.getMessage();
                    }

                    Snackbar snackbar = Snackbar.make(v, error, Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(Color.RED);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }
            }
        });
    }

    private void saveUserData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("name", edit_name.getText().toString());
        user.put("surname", edit_surname.getText().toString());
        user.put("mobile", edit_Mobile.getText().toString());

        DocumentReference documentReference = db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        documentReference.set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "User data successfully saved.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error saving user data", e);
                    }
                });
    }

    private void iniciarComponentes() {
        edit_name = findViewById(R.id.edit_name);
        edit_surname = findViewById(R.id.edit_surname);
        edit_Mobile = findViewById(R.id.edit_Mobile);
        edit_Patient_Doctors_Name = findViewById(R.id.edit_Patient_Doctors_Name);
        edit_Patient_Doctors_Surname = findViewById(R.id.edit_Patient_Doctors_Surname);
        edit_Patient_Caregiver_Name = findViewById(R.id.edit_Patient_Caregiver_Name);
        edit_Patient_Caregiver_Surname = findViewById(R.id.edit_Patient_Caregiver_Surname);
        edit_Username = findViewById(R.id.edit_Username);
        edit_Password2 = findViewById(R.id.edit_Password2);
        edit_Re_type = findViewById(R.id.edit_Re_type);
        bt_register = findViewById(R.id.bt_register);
        textViewError = findViewById(R.id.textViewError);  // Ensure this ID is in your XML
    }
}
