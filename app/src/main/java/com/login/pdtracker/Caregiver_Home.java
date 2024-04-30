package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class Caregiver_Home extends AppCompatActivity {

    private TextView welcome_user;
    private ImageButton bt_logoutButton;
    private AppCompatButton b2c; // Reference to the update profile button
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caregiver_home);

        // Configure edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        IniciarComponentes();

        // Set click listener for the update profile button
        b2c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Caregiver_Home.this, Caregiver_Home.class);
                startActivity(intent);
            }
        });

        // Set click listener for the logout button
        bt_logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(Caregiver_Home.this, Caregiver_LoginScreen.class);
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
            DocumentReference documentReference = db.collection("Caregiver").document(userID);
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                    if (documentSnapshot != null) {
                        String userName = documentSnapshot.getString("name");
                        welcome_user.setText("Welcome, " + userName);
                    }
                }
            });
        } else {
            // Se não houver usuário logado, redirecionar para a tela de login
            Intent intent = new Intent(Caregiver_Home.this, Caregiver_LoginScreen.class);
            startActivity(intent);
            finish();
        }
    }

    private void IniciarComponentes() {
        welcome_user = findViewById(R.id.welcome_user);
        bt_logoutButton = findViewById(R.id.bt_logoutButton);
        b2c = findViewById(R.id.b2c); // Initialize the update profile button
    }
}