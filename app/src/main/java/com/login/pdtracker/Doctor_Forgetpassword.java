package com.login.pdtracker;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class Doctor_Forgetpassword extends AppCompatActivity {

    private EditText emailField3;
    private Button Send_Reset_Pass_Link3;
    private Button btnBackToLogin3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_forgetpassword);

        // Setup the layout to handle Window Insets (for status and navigation bars)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        emailField3 = findViewById(R.id.emailField3); // Make sure the ID matches in your XML file
        Send_Reset_Pass_Link3 = findViewById(R.id.Send_Reset_Pass_Link3); // Make sure the ID matches in your XML file
        btnBackToLogin3 = findViewById(R.id.btnBackToLogin3); // This is the button to go back to login

        // Set up the listener for the reset password button
        Send_Reset_Pass_Link3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPasswordResetEmail();
            }
        });

        // Listener for the back to login button
        btnBackToLogin3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Finishes the current activity and returns to the previous activity in the stack
            }
        });
    }

    private void sendPasswordResetEmail() {
        String email = emailField3.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(Doctor_Forgetpassword.this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Doctor_Forgetpassword.this, "Reset password email sent.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Doctor_Forgetpassword.this, "Failed to send reset email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}