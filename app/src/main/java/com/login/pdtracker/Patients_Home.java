package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class Patients_Home extends AppCompatActivity {

    private TextView welcome_user;
    private ImageButton bt_logoutButton;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_home);

        // Configuração para garantir o layout de edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        IniciarComponentes();

        bt_logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(Patients_Home.this, Patient_LoginScreen.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference documentReference = db.collection("users").document(userID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot != null) {
                    String userName = documentSnapshot.getString("name");
                    welcome_user.setText("Welcome, " + userName);
                }
            }
        });
    }

    private void IniciarComponentes() {
        welcome_user = findViewById(R.id.welcome_user);
        bt_logoutButton = findViewById(R.id.bt_logoutButton);
    }
}
