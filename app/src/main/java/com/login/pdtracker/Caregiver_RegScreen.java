package com.login.pdtracker;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class Caregiver_RegScreen extends AppCompatActivity {
    private EditText caregiver_Mobile, caregiver_name_surname, caregiver_email, caregiver_password2, caregiver_retypepassword;
    private Button caregiver_register;
    private TextView textView_Error; // TextView for displaying error messages

    private boolean isMobileValid = false; // To track the validity of the mobile number

    String[] errorMessages = {
            "Please complete all required fields to proceed.",
            "Registration Successful!",
            "A valid Irish mobile number should start with 083, 086, 087, 089, and be of total 10 digits in length."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_register_caregiver);
        initializeComponents();
    }


    private void registerUser(View v, String email, String password) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    saveUserData(v);
                    Snackbar snackbar = Snackbar.make(v, errorMessages[1], Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                } else {
                    String error = "Registration failed due to an error";
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        error = e.getMessage();
                    }

                    Snackbar snackbar = Snackbar.make(v, error, Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(Color.RED);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }
            }
        });
    }

    private void saveUserData(View v) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String fcmToken = task.getResult();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            Map<String, Object> user = new HashMap<>();
                            user.put("name", caregiver_name_surname.getText().toString());
                            user.put("mobile", caregiver_Mobile.getText().toString());
                            user.put("email", caregiver_email.getText().toString());
                            user.put("password", caregiver_password2.getText().toString());
                            user.put("retype", caregiver_retypepassword.getText().toString());
                            user.put("token", fcmToken);

                            db.collection("Caregiver").document(firebaseAuth.getCurrentUser().getUid())
                                    .set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("Firestore", "User data successfully saved.");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("Firestore", "Error saving user data", e);
                                        }
                                    });
                        } else {
                            Snackbar.make(v, "Failed to retrieve FCM token", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void initializeComponents() {
        caregiver_name_surname = findViewById(R.id.caregiver_name_surname);
        caregiver_Mobile = findViewById(R.id.caregiver_Mobile);
        caregiver_email = findViewById(R.id.caregiver_email);
        caregiver_password2 = findViewById(R.id.caregiver_password2);
        caregiver_retypepassword = findViewById(R.id.caregiver_retypepassword);
        caregiver_register = findViewById(R.id.caregiver_register);
        textView_Error = findViewById(R.id.textView_Error);

        if (caregiver_Mobile == null) {
            throw new RuntimeException("Make sure the caregiver_Mobile EditText is correctly referenced from XML");
        }

        setupFieldValidations();
    }

    private void setupFieldValidations() {
        setupMobileValidation();
        setupEmailValidation();
        setupPasswordValidation();

        caregiver_register.setOnClickListener(v -> attemptRegistration(v));
    }

    private void setupMobileValidation() {
        caregiver_Mobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String mobile = s.toString();
                if (!isValidIrishMobile(mobile)) {
                    caregiver_Mobile.setError(errorMessages[2]);
                    isMobileValid = false;
                } else {
                    caregiver_Mobile.setError(null);
                    isMobileValid = true;
                }
            }
        });
    }

    private boolean isValidIrishMobile(String mobile) {
        return mobile.matches("^(083|085|086|087|089)\\d{7}$");
    }

    private void setupEmailValidation() {
        caregiver_email.addTextChangedListener(new TextWatcher() {
            final Handler handler = new Handler();
            final Runnable emailValidationRunnable = () -> {
                String email = caregiver_email.getText().toString();
                if (!isValidEmail(email)) {
                    caregiver_email.setError("Incorrect email address. A valid email should contain valid email domain, like gmail.com, hotmail.com, yahoo.com, etc., and has the @ sign before it.");
                } else {
                    caregiver_email.setError(null);
                }
            };

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacks(emailValidationRunnable);
                handler.postDelayed(emailValidationRunnable, 800);
            }
        });
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w-\\.]+@((gmail.com)|(hotmail.com)|(yahoo.com)|(outlook.com)|(live.com))$");
    }

    private void setupPasswordValidation() {
        caregiver_password2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isPasswordStrong(s.toString())) {
                    caregiver_password2.setError("A strong password should be of minimum 8 characters in length, and a mixture of upper- and lower-case letters, digits, and special characters.");
                } else {
                    caregiver_password2.setError(null);
                }
            }
        });

        caregiver_retypepassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String password = caregiver_password2.getText().toString();
                if (!password.equals(s.toString())) {
                    caregiver_retypepassword.setError("Passwords do not match.");
                } else {
                    caregiver_retypepassword.setError(null);
                }
            }
        });
    }

    private boolean isPasswordStrong(String password) {
        return password.length() >= 8 && password.matches(".*[A-Z].*") && password.matches(".*[a-z].*") && password.matches(".*[\\W_].*");
    }

    private void attemptRegistration(View v) {
        String name = caregiver_name_surname.getText().toString();
        String mobile = caregiver_Mobile.getText().toString();
        String email = caregiver_email.getText().toString();
        String password = caregiver_password2.getText().toString();
        String retype = caregiver_retypepassword.getText().toString();

        if (name.isEmpty() || email.isEmpty() || mobile.isEmpty() || password.isEmpty() || retype.isEmpty()) {
            Snackbar.make(v, errorMessages[0], Snackbar.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(retype)) {
            Snackbar.make(v, "Passwords do not match.", Snackbar.LENGTH_LONG).show();
            return;
        }

        if (!isPasswordStrong(password)) {
            Snackbar.make(v, "A strong password should be of minimum 8 characters in length, and a mixture of upper- and lower-case letters, digits, and special characters.", Snackbar.LENGTH_LONG).show();
            return;
        }

        if (!isMobileValid) {
            Snackbar.make(v, errorMessages[2], Snackbar.LENGTH_LONG).show();
            return;
        }

        registerUser(v, email, password);
    }

}
