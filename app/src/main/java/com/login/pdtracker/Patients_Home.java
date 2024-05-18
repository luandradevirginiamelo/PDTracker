package com.login.pdtracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class Patients_Home extends AppCompatActivity {

    private TextView welcome_user;
    private ImageButton bt_logoutButton;
    private AppCompatButton b2; // Update profile button
    private AppCompatButton b4; // Doctor's notifications button
    private AppCompatButton b3; // Daily data reading button
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        IniciarComponentes();

        b2.setOnClickListener(v -> checkUserProfile());
        bt_logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(Patients_Home.this, Patient_LoginScreen.class);
            startActivity(intent);
            finish();
        });

        b4.setOnClickListener(v -> {
            Intent intent = new Intent(Patients_Home.this, Patient_View_Abnormalities_Patients.class);
            startActivity(intent);
        });

        b3.setOnClickListener(v -> {
            Intent intent = new Intent(Patients_Home.this, Dailydata_reading.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userID = currentUser.getUid();
            DocumentReference documentReference = db.collection("users").document(userID);
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("name");
                        welcome_user.setText("Welcome, " + userName);
                    }
                }
            });
        } else {
            Intent intent = new Intent(Patients_Home.this, Patient_LoginScreen.class);
            startActivity(intent);
            finish();
        }
    }

    private void IniciarComponentes() {
        welcome_user = findViewById(R.id.welcome_user);
        bt_logoutButton = findViewById(R.id.bt_logoutButton);
        b2 = findViewById(R.id.b2);
        b4 = findViewById(R.id.b4);
        b3 = findViewById(R.id.b3);
    }

    private void checkUserProfile() {
        DocumentReference profileRef = db.collection("patient_profiles").document(userID);
        profileRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    openProfileActivity("Update");
                } else {
                    openProfileActivity("Create");
                }
            } else {
                Log.d("TAG", "Failed with: ", task.getException());
            }
        });
    }

    private void openProfileActivity(String mode) {
        Intent intent = new Intent(Patients_Home.this, Create_Update_PProfile.class);
        intent.putExtra("mode", mode);
        startActivity(intent);
    }
}
