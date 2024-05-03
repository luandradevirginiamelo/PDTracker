package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.activity.EdgeToEdge;

public class Home_App extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_app);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button patientButton = findViewById(R.id.Patienthome);
        patientButton.setOnClickListener(v -> {
            Intent intent = new Intent(Home_App.this, Patient_LoginScreen.class);
            startActivity(intent);
        });

        Button Buttondoctor = findViewById(R.id.b3D);
        Buttondoctor.setOnClickListener(v -> {
            Intent intent = new Intent(Home_App.this, Doctors_LoginScreen.class);
            startActivity(intent);
        });

        Button caregiverButton = findViewById(R.id.b4C);
        caregiverButton.setOnClickListener(v -> {
            Intent intent = new Intent(Home_App.this, Caregiver_LoginScreen.class);
            startActivity(intent);
        });
    }
}

