package com.login.pdtracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class Patient_LoginScreen extends AppCompatActivity {

    private static final String TAG = "Patient_LoginScreen";
    private TextView textRegister, textForgotten;
    private EditText edit_email, edit_senha;
    private Button bt_login;
    private ProgressBar progressbar;
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initComponents();
        setupButtons();
    }

    private void initComponents() {
        textRegister = findViewById(R.id.Register);
        textForgotten = findViewById(R.id.Forgotten);
        edit_email = findViewById(R.id.edit_email);
        edit_senha = findViewById(R.id.edit_senha);
        bt_login = findViewById(R.id.bt_login);
        progressbar = findViewById(R.id.progressbar);
    }

    private void setupButtons() {
        textRegister.setOnClickListener(v -> {
            Intent intent = new Intent(Patient_LoginScreen.this, Patients_RegScreen.class);
            startActivity(intent);
        });

        textForgotten.setOnClickListener(v -> {
            Intent intent = new Intent(Patient_LoginScreen.this, Reset_PasswordScreen.class);
            startActivity(intent);
        });

        bt_login.setOnClickListener(v -> {
            String email = edit_email.getText().toString();
            String password = edit_senha.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(v, "Please complete all required fields to proceed", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.WHITE)
                        .setTextColor(Color.BLACK)
                        .show();
            } else {
                authenticateUser(email, password);
            }
        });
    }

    private void authenticateUser(String email, String password) {
        if (!isEmailValid(email)) {
            Snackbar.make(bt_login, "Invalid email format. Please enter a valid email address.", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.WHITE)
                    .setTextColor(Color.RED)
                    .show();
            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        updateFCMTokenAndNavigate(user);
                    }
                } else {
                    handleLoginFailure(task.getException());
                }
            }
        });
    }

    private void updateFCMTokenAndNavigate(FirebaseUser user) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                return;
            }
            String token = task.getResult();
            Log.d(TAG, "FCM Token: " + token);

            db.collection("users").document(user.getUid())
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Token updated successfully.");
                        progressbar.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(this::goToPatientDashboard, 3000);
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating token", e));
        });
    }

    private void handleLoginFailure(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            Snackbar.make(bt_login, "Email not registered. Please register first.", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.WHITE)
                    .setTextColor(Color.RED)
                    .show();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            Snackbar.make(bt_login, "Wrong password. Please try again.", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.WHITE)
                    .setTextColor(Color.RED)
                    .show();
        }
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void goToPatientDashboard() {
        Intent intent = new Intent(Patient_LoginScreen.this, Patients_Home.class);
        startActivity(intent);
        finish();
    }
}
