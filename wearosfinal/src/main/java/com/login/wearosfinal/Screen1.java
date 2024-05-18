package com.login.wearosfinal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.login.wear_ospdtracker.R;

import java.util.HashMap;
import java.util.Map;

public class Screen1 extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FirebaseFirestore db;
    private FusedLocationProviderClient locationClient;
    private static String documentId = ""; // Document ID to be used for all updates
    private Map<String, Object> profileToUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen1);
        db = FirebaseFirestore.getInstance();
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationPermission();
        setupButtons();
        showDialogForDocumentId();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    private void setupButtons() {
        Button btnHighHeartRate = findViewById(R.id.button_high_heart_rate);
        Button btnLowHeartRate = findViewById(R.id.button_low_heart_rate);
        Button btnHighBloodPressure = findViewById(R.id.button_high_blood_pressure);
        Button btnLowBloodPressure = findViewById(R.id.button_low_blood_pressure);
        Button btnHighOxygen = findViewById(R.id.button_high_oxygen);
        Button btnLowOxygen = findViewById(R.id.button_low_oxygen);
        Button btnPoorSleep = findViewById(R.id.button_poor_sleep);
        Button btnFallDetected = findViewById(R.id.button_fall_detected);

        btnHighHeartRate.setOnClickListener(view -> {
            profileToUpdate = createHighHeartRateProfile();
            requestNewLocation();
        });
        btnLowHeartRate.setOnClickListener(view -> {
            profileToUpdate = createLowHeartRateProfile();
            requestNewLocation();
        });
        btnHighBloodPressure.setOnClickListener(view -> {
            profileToUpdate = createHighBloodPressureProfile();
            requestNewLocation();
        });
        btnLowBloodPressure.setOnClickListener(view -> {
            profileToUpdate = createLowBloodPressureProfile();
            requestNewLocation();
        });
        btnHighOxygen.setOnClickListener(view -> {
            profileToUpdate = createHighOxygenProfile();
            requestNewLocation();
        });
        btnLowOxygen.setOnClickListener(view -> {
            profileToUpdate = createLowOxygenProfile();
            requestNewLocation();
        });
        btnPoorSleep.setOnClickListener(view -> {
            profileToUpdate = createPoorSleepProfile();
            requestNewLocation();
        });
        btnFallDetected.setOnClickListener(view -> {
            profileToUpdate = createFallDetectedProfile();
            requestNewLocation();
        });
    }

    private Map<String, Object> createHighHeartRateProfile() {
        Map<String, Object> data = new HashMap<>();
        data.put("Min Heart Rate Morning", 130);  // Minimum morning heart rate
        data.put("Max Heart Rate Morning", 160);  // Maximum morning heart rate
        data.put("Min Heart Rate Afternoon", 140);  // Minimum afternoon heart rate
        data.put("Max Heart Rate Afternoon", 170);  // Maximum afternoon heart rate
        data.put("Blood Pressure", "120/80");  // Normal blood pressure
        data.put("Oxygen Levels", 98);  // Normal oxygen levels
        data.put("Sleep Quality", "Good");
        data.put("Heart Rate Abnormality", "Y"); // Heart Rate Abnormality indicator
        data.put("Blood Pressure Abnormality", "N"); // Blood Pressure Abnormality indicator
        return data;
    }

    private Map<String, Object> createLowHeartRateProfile() {
        Map<String, Object> data = new HashMap<>();
        data.put("Min Heart Rate Morning", 40);  // Minimum morning heart rate
        data.put("Max Heart Rate Morning", 70);  // Maximum morning heart rate
        data.put("Min Heart Rate Afternoon", 50);  // Minimum afternoon heart rate
        data.put("Max Heart Rate Afternoon", 80);  // Maximum afternoon heart rate
        data.put("Blood Pressure", "120/80");  // Normal blood pressure
        data.put("Oxygen Levels", 98);  // Normal oxygen levels
        data.put("Sleep Quality", "Good");
        data.put("Heart Rate Abnormality", "Y"); // Heart Rate Abnormality indicator
        data.put("Blood Pressure Abnormality", "N"); // Blood Pressure Abnormality indicator
        return data;
    }

    private Map<String, Object> createHighBloodPressureProfile() {
        Map<String, Object> data = new HashMap<>();
        data.put("Min Heart Rate Morning", 60);  // Minimum morning heart rate
        data.put("Max Heart Rate Morning", 90);  // Maximum morning heart rate
        data.put("Min Heart Rate Afternoon", 70);  // Minimum afternoon heart rate
        data.put("Max Heart Rate Afternoon", 100);  // Maximum afternoon heart rate
        data.put("Blood Pressure", "180/120");  // High blood pressure
        data.put("Oxygen Levels", 90);  // Slightly low oxygen
        data.put("Sleep Quality", "Poor");
        data.put("Heart Rate Abnormality", "Y"); // Heart Rate Abnormality indicator
        data.put("Blood Pressure Abnormality", "Y"); // Blood Pressure Abnormality indicator
        return data;
    }

    private Map<String, Object> createLowBloodPressureProfile() {
        Map<String, Object> data = new HashMap<>();
        data.put("Min Heart Rate Morning", 60);  // Minimum morning heart rate
        data.put("Max Heart Rate Morning", 90);  // Maximum morning heart rate
        data.put("Min Heart Rate Afternoon", 70);  // Minimum afternoon heart rate
        data.put("Max Heart Rate Afternoon", 100);  // Maximum afternoon heart rate
        data.put("Blood Pressure", "90/60");  // Low blood pressure
        data.put("Oxygen Levels", 98);  // Normal oxygen levels
        data.put("Sleep Quality", "Good");
        data.put("Heart Rate Abnormality", "Y"); // Heart Rate Abnormality indicator
        data.put("Blood Pressure Abnormality", "Y"); // Blood Pressure Abnormality indicator
        return data;
    }

    private Map<String, Object> createHighOxygenProfile() {
        Map<String, Object> data = new HashMap<>();
        data.put("Min Heart Rate Morning", 60);  // Minimum morning heart rate
        data.put("Max Heart Rate Morning", 90);  // Maximum morning heart rate
        data.put("Min Heart Rate Afternoon", 70);  // Minimum afternoon heart rate
        data.put("Max Heart Rate Afternoon", 100);  // Maximum afternoon heart rate
        data.put("Blood Pressure", "120/80");  // Normal blood pressure
        data.put("Oxygen Levels", 100);  // High oxygen levels
        data.put("Sleep Quality", "Excellent");
        data.put("Heart Rate Abnormality", "Y"); // Heart Rate Abnormality indicator
        data.put("Blood Pressure Abnormality", "N"); // Blood Pressure Abnormality indicator
        return data;
    }

    private Map<String, Object> createLowOxygenProfile() {
        Map<String, Object> data = new HashMap<>();
        data.put("Min Heart Rate Morning", 60);  // Minimum morning heart rate
        data.put("Max Heart Rate Morning", 90);  // Maximum morning heart rate
        data.put("Min Heart Rate Afternoon", 70);  // Minimum afternoon heart rate
        data.put("Max Heart Rate Afternoon", 100);  // Maximum afternoon heart rate
        data.put("Blood Pressure", "120/80");  // Normal blood pressure
        data.put("Oxygen Levels", 85);  // Low oxygen levels
        data.put("Sleep Quality", "Poor");
        data.put("Heart Rate Abnormality", "Y"); // Heart Rate Abnormality indicator
        data.put("Blood Pressure Abnormality", "N"); // Blood Pressure Abnormality indicator
        return data;
    }

    private Map<String, Object> createPoorSleepProfile() {
        Map<String, Object> data = new HashMap<>();
        data.put("Min Heart Rate Morning", 60);  // Minimum morning heart rate
        data.put("Max Heart Rate Morning", 90);  // Maximum morning heart rate
        data.put("Min Heart Rate Afternoon", 70);  // Minimum afternoon heart rate
        data.put("Max Heart Rate Afternoon", 100);  // Maximum afternoon heart rate
        data.put("Blood Pressure", "120/80");  // Normal blood pressure
        data.put("Oxygen Levels", 98);  // Normal oxygen levels
        data.put("Sleep Quality", "Very Poor");
        data.put("Heart Rate Abnormality", "Y"); // Heart Rate Abnormality indicator
        data.put("Blood Pressure Abnormality", "N"); // Blood Pressure Abnormality indicator
        return data;
    }

    private Map<String, Object> createFallDetectedProfile() {
        Map<String, Object> data = new HashMap<>();
        data.put("Min Heart Rate Morning", 60);  // Minimum morning heart rate
        data.put("Max Heart Rate Morning", 190);  // Maximum morning heart rate
        data.put("Min Heart Rate Afternoon", 70);  // Minimum afternoon heart rate
        data.put("Max Heart Rate Afternoon", 200);  // Maximum afternoon heart rate
        data.put("Blood Pressure", "189/80");  // Normal blood pressure
        data.put("Oxygen Levels", 98);  // Normal oxygen levels
        data.put("Sleep Quality", "Good");
        data.put("Fall Detected", "Yes");
        data.put("Heart Rate Abnormality", "Y"); // Heart Rate Abnormality indicator
        data.put("Blood Pressure Abnormality", "Y"); // Blood Pressure Abnormality indicator
        return data;
    }

    private void requestNewLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Permissions", "Location permission not granted");
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    profileToUpdate.put("Latitude", location.getLatitude());
                    profileToUpdate.put("Longitude", location.getLongitude());
                    sendHealthProfile();
                    locationClient.removeLocationUpdates(this); // Stop updates after getting the location
                } else {
                    Log.e("Location Error", "No location available");
                }
            }
        };

        locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void showDialogForDocumentId() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Document ID");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            documentId = input.getText().toString().trim();
            if (documentId.isEmpty()) {
                Log.e("Document ID Error", "Document ID cannot be empty");
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void sendHealthProfile() {
        if (documentId.isEmpty()) {
            Log.e("Document ID Error", "Document ID not set");
            return;
        }

        db.collection("health_data").document(documentId)
                .set(profileToUpdate) // This will overwrite the document each time
                .addOnSuccessListener(aVoid -> Log.d("Firestore Success", "Document successfully updated!"))
                .addOnFailureListener(e -> Log.e("Firestore Error", "Error updating document", e));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
            } else {
                // Permission was denied
            }
        }
    }
}