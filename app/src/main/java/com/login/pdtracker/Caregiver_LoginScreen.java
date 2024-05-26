package com.login.pdtracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class Caregiver_LoginScreen extends AppCompatActivity {

    private TextView textRegister;
    private EditText caregiver_email, caregiver_password;
    private Button caregiver_login;

    private AppCompatButton bt_bm;
    String[] errorMessages = {"Please complete all required fields to proceed", "Login Successfully!"};
    private ProgressBar progressbar;
    private TextView textForgotten;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_caregiver_login_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bt_bm = findViewById(R.id.bt_bm);
        bt_bm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Caregiver_LoginScreen.this, Home_App.class);
                startActivity(intent);
            }
        });

        // Initialize the button
        caregiver_login = findViewById(R.id.caregiver_login);

        // Initialize components after setting up the layout
        IniciarComponentes();

        textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Caregiver_LoginScreen.this, Caregiver_RegScreen.class);
                startActivity(intent);
            }
        });

        textForgotten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Caregiver_LoginScreen.this, Caregiver_Home.class);
                startActivity(intent);

            }
        });

        caregiver_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = caregiver_email.getText().toString();
                String password = caregiver_password.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Snackbar snackbar = Snackbar.make(v, errorMessages[0], Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                } else {
                    AutenticarUsuario();
                }
            }
        });
    }
    private void AutenticarUsuario() {
        String email = caregiver_email.getText().toString();
        String password = caregiver_password.getText().toString();

        if (!isEmailValid(email)) {
            // Se o email não estiver em um formato válido, exibir mensagem de erro
            Snackbar snackbar = Snackbar.make(caregiver_login, "Invalid email format. Please enter a valid email address.", Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(Color.WHITE);
            snackbar.setTextColor(Color.RED);
            snackbar.show();
            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(new OnCompleteListener<String>() {
                                    @Override
                                    public void onComplete(@NonNull Task<String> task) {
                                        if (task.isSuccessful() && task.getResult() != null) {
                                            String fcmToken = task.getResult();
                                            // Store FCM token in Firestore
                                            storeTokenInFirestore(user.getUid(), fcmToken);
                                        }
                                    }
                                });
                    }
                    progressbar.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PatientsDashboard();
                        }
                    }, 3000);
                } else {
                    // Capturando exceção específica para email não cadastrado
                    if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                        Snackbar snackbar = Snackbar.make(caregiver_login, "Email not registered. Please register first.", Snackbar.LENGTH_SHORT);
                        snackbar.setBackgroundTint(Color.WHITE);
                        snackbar.setTextColor(Color.RED);
                        snackbar.show();
                    } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        // Se a senha estiver incorreta, exibir mensagem de erro
                        Snackbar snackbar = Snackbar.make(caregiver_login, "Wrong password. Please try again.", Snackbar.LENGTH_SHORT);
                        snackbar.setBackgroundTint(Color.WHITE);
                        snackbar.setTextColor(Color.RED);
                        snackbar.show();
                    }
                }
            }
        });
    }

    private boolean isEmailValid(String email) {
        // Esta é uma validação básica para verificar o formato do email
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void storeTokenInFirestore(String userId, String fcmToken) {
        // Create a reference to the Firestore collection "UserTokens"
        CollectionReference userTokensRef = FirebaseFirestore.getInstance().collection("UserTokens");
        // Create a document with user ID as document ID
        userTokensRef.document(userId)
                .set(new HashMap<String, Object>() {{
                    put("token", fcmToken);
                }})
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Token stored successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to store token
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser userlogon = FirebaseAuth.getInstance().getCurrentUser();

        if (userlogon !=null){
            PatientsDashboard();
        }
    }

    private void PatientsDashboard() {
        Intent intent = new Intent(Caregiver_LoginScreen.this, Caregiver_Home.class);
        startActivity(intent);
        finish();
    }

    private void IniciarComponentes() {
        textRegister = findViewById(R.id.caregiver_Register);
        textForgotten = findViewById(R.id.caregiver_forgotten);
        caregiver_email = findViewById(R.id.caregiver_email);
        caregiver_password = findViewById(R.id.caregiver_password);
        progressbar = findViewById(R.id.progressbar);
        caregiver_login = findViewById(R.id.caregiver_login);
    }
}