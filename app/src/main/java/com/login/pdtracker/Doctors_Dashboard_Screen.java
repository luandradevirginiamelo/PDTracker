package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

public class Doctors_Dashboard_Screen extends AppCompatActivity {

    private TextView welcome_user;
    private ImageButton bt_logoutButton;
    private AppCompatButton b1; // Reference to the update profile button
    private AppCompatButton b2;
    private AppCompatButton b3;
    private AppCompatButton b4;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctors_dashboard_screen);

        // Configure edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        IniciarComponentes();

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Doctors_Dashboard_Screen.this, Doctor_Emergency_Notes.class);
                startActivity(intent);
            }
        });

        // Set click listener for the update profile button
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Doctors_Dashboard_Screen.this, Doctors_View_Daily_Readings.class);
                startActivity(intent);
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Doctors_Dashboard_Screen.this, Doctors_View_Abnormalities.class);
                startActivity(intent);
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Doctors_Dashboard_Screen.this, Viewlist_ofpatientfalldetection.class);
                startActivity(intent);
            }
        });

        // Set click listener for the logout button
        bt_logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(Doctors_Dashboard_Screen.this, Doctors_LoginScreen.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userID = currentUser.getUid();
            db.collection("Doctor")
                    .whereEqualTo("UserAuthID", userID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String doctorName = document.getString("DoctorName");
                                    String doctorSurname = document.getString("DoctorSurname");
                                    String message = doctorName + " " + doctorSurname;
                                    welcome_user.setText("Welcome, Dr. "+message);
                                }
                            } else {
                                // Handle errors
                            }
                        }
                    });
        } else {
            // Se não houver usuário logado, redirecionar para a tela de login
            Intent intent = new Intent(Doctors_Dashboard_Screen.this, Doctors_LoginScreen.class);
            startActivity(intent);
            finish();
        }
    }

    private void IniciarComponentes() {
        welcome_user = findViewById(R.id.welcome_user);
        bt_logoutButton = findViewById(R.id.bt_logoutButton);
        b1 = findViewById(R.id.b1);
        b2 = findViewById(R.id.b2);
        b3 = findViewById(R.id.b3);
        b4 = findViewById(R.id.b4);
        // Initialize the update profile button
    }
}