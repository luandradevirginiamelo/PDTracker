package com.login.pdtracker;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "Firestore";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get a Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new collection "patientProfiles" and a document with auto-generated ID
        // You can also specify a custom document ID by passing it as an argument to the document() method
        db.collection("patientProfiles")
                .add(new HashMap<>()) // You can add initial data to the document if needed
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);

                        EdgeToEdge.enable(this);
                        setContentView(R.layout.activity_main);
                        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                            return insets;
                        });

                    }
                }


