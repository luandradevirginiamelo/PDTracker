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

public class Reset_PasswordScreen extends AppCompatActivity {

    private EditText emailField;
    private Button sendResetPassLinkButton;
    private Button btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password_screen);

        // Setup the layout to handle Window Insets (for status and navigation bars)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        emailField = findViewById(R.id.emailField); // Make sure the ID matches in your XML file
        sendResetPassLinkButton = findViewById(R.id.Send_Reset_Pass_Link); // Make sure the ID matches in your XML file
        btnBackToLogin = findViewById(R.id.btnBackToLogin); // This is the button to go back to login

        // Set up the listener for the reset password button
        sendResetPassLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPasswordResetEmail();
            }
        });

        // Listener for the back to login button
        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Finishes the current activity and returns to the previous activity in the stack
            }
        });
    }

    private void sendPasswordResetEmail() {
        String email = emailField.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(Reset_PasswordScreen.this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Reset_PasswordScreen.this, "Reset password email sent.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Reset_PasswordScreen.this, "Failed to send reset email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

