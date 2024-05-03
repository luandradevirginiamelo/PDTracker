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

public class Doctors_LoginScreen extends AppCompatActivity {

    private EditText edit_email, edit_Password;
    private Button bt_login;
    String[] errorMessages = {"Please complete all required fields to proceed", "Login Successfully!"};
    private ProgressBar progressbar;
    private TextView textForgotten;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctors_login_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the button
        bt_login = findViewById(R.id.bt_DoctorsLogin);

        // Initialize components after setting up the layout
        IniciarComponentes();

        textForgotten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Doctors_LoginScreen.this, Doctors_Dashboard_Screen.class);
                startActivity(intent);
            }
        });

        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edit_email.getText().toString();
                String password = edit_Password.getText().toString();

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
        String email = edit_email.getText().toString();
        String password = edit_Password.getText().toString();

        if (!isEmailValid(email)) {
            // Se o email não estiver em um formato válido, exibir mensagem de erro
            Snackbar snackbar = Snackbar.make(bt_login, "Invalid email format. Please enter a valid email address.", Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(Color.WHITE);
            snackbar.setTextColor(Color.RED);
            snackbar.show();
            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressbar.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DoctorDashboard();
                        }
                    }, 3000);
                } else {
                    // Capturando exceção específica para email não cadastrado
                    if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                        Snackbar snackbar = Snackbar.make(bt_login, "Email not registered. Please register first.", Snackbar.LENGTH_SHORT);
                        snackbar.setBackgroundTint(Color.WHITE);
                        snackbar.setTextColor(Color.RED);
                        snackbar.show();
                    } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        // Se a senha estiver incorreta, exibir mensagem de erro
                        Snackbar snackbar = Snackbar.make(bt_login, "Wrong password. Please try again.", Snackbar.LENGTH_SHORT);
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

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser userlogon = FirebaseAuth.getInstance().getCurrentUser();

        if (userlogon != null) {
            DoctorDashboard();
        }
    }

    private void DoctorDashboard() {
        Intent intent = new Intent(Doctors_LoginScreen.this, Doctors_Dashboard_Screen.class);
        startActivity(intent);

    }

    private void IniciarComponentes() {
        textForgotten = findViewById(R.id.Forgotten);
        edit_email = findViewById(R.id.edit_email);
        edit_Password = findViewById(R.id.edit_Password);
        progressbar = findViewById(R.id.progressbar);
    }
}