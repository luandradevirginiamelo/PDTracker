package com.login.pdtracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

public class Patients_RegScreen extends AppCompatActivity {
    private EditText edit_name, edit_surname, edit_Mobile, edit_Patient_Doctors_Name, edit_Patient_Doctors_Surname, edit_Patient_Caregiver_Name, edit_Patient_Caregiver_Surname, edit_Username, edit_Password2, edit_Re_type;
    private Button bt_register;

    String[] errorMessages = {"Please complete all required fields to proceed", "Registration Successful!"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_register);

        // Initialize components
        IniciarComponentes();

        // Set click listener for register button
        bt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve values from EditText fields
                String Name = edit_name.getText().toString();
                String Surname = edit_surname.getText().toString();
                String Mobile = edit_Mobile.getText().toString();
                String Patient_D_Name = edit_Patient_Doctors_Name.getText().toString();
                String Patient_D_Surname = edit_Patient_Doctors_Surname.getText().toString();
                String Patient_C_Name = edit_Patient_Caregiver_Name.getText().toString();
                String P_C_Surname = edit_Patient_Caregiver_Surname.getText().toString();
                String edit_Pass = edit_Password2.getText().toString();
                String edit_REtype = edit_Re_type.getText().toString();

                // Check for empty fields
                if (Name.isEmpty() || Surname.isEmpty() || Mobile.isEmpty() || Patient_D_Name.isEmpty() || Patient_D_Surname.isEmpty() || Patient_C_Name.isEmpty() || P_C_Surname.isEmpty() || edit_Pass.isEmpty() || edit_REtype.isEmpty()) {
                    Snackbar.make(v, errorMessages[0], Snackbar.LENGTH_SHORT).show();
                } else {
                    // All fields are filled, proceed with registration process
                    // Implement your registration logic here
                    Snackbar.make(v, errorMessages[1], Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void IniciarComponentes() {
        edit_name = findViewById(R.id.edit_name);
        edit_surname = findViewById(R.id.edit_surname);
        edit_Mobile = findViewById(R.id.edit_Mobile);
        edit_Patient_Doctors_Name = findViewById(R.id.edit_Patient_Doctors_Name);
        edit_Patient_Doctors_Surname = findViewById(R.id.edit_Patient_Doctors_Surname);
        edit_Patient_Caregiver_Name = findViewById(R.id.edit_Patient_Caregiver_Name);
        edit_Patient_Caregiver_Surname = findViewById(R.id.edit_Patient_Caregiver_Surname);
        edit_Password2 = findViewById(R.id.edit_Password2);
        edit_Re_type = findViewById(R.id.edit_Re_type);
        bt_register = findViewById(R.id.bt_register);
    }
}
