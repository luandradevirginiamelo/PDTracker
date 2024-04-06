package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Patient_LoginScreen extends AppCompatActivity {

    private TextView textRegister;
    private TextView textForgotten;

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

        // Iniciar componentes após configurar o layout
        IniciarComponentes();

        textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Patient_LoginScreen.this, Patients_RegScreen.class);
                startActivity(intent);
            }
        });

        textForgotten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Coloque o código para a ação de clique do "esqueceu a senha" aqui
                // Por exemplo, pode abrir uma nova Activity para redefinir a senha
            }
        });
    }

    private void IniciarComponentes() {
        textRegister = findViewById(R.id.Register);
        textForgotten = findViewById(R.id.Forgotten);
    }
}
