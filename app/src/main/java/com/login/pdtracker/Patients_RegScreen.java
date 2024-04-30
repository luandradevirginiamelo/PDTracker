package com.login.pdtracker;

import android.graphics.Color;
import android.os.Bundle;
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
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import android.os.Handler;
import java.util.Locale;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

public class Patients_RegScreen extends AppCompatActivity {
    private EditText edit_name, edit_surname, edit_Mobile, edit_Patient_Doctors_Name, edit_Patient_Doctors_Surname, edit_Patient_Caregiver_Name, edit_DOB, edit_email, edit_Password2, edit_Re_type;
    private Button bt_register;

    private TextView textViewError2;
    private boolean isMobileValid = false; // To track the validity of the mobile number

    String[] errorMessages = {"Please complete all required fields to proceed", "Registration Successful!", "Invalid Irish mobile number format informe 083, 085, 086, 087, or 089"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_register);

        iniciarComponentes();

        // TextWatcher for real-time password strength validation
        edit_Password2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isPasswordStrong(s.toString())) {
                    edit_Password2.setError("For a strong password, use at least 8 characters, including both uppercase and lowercase letters, as well as special characters.");
                } else {
                    edit_Password2.setError(null); // Clear error
                }
            }
        });

        // TextWatcher for real-time retype password matching validation
        edit_Re_type.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = edit_Password2.getText().toString();
                if (!password.equals(s.toString())) {
                    edit_Re_type.setError("Passwords do not match.");
                } else {
                    edit_Re_type.setError(null); // Clear error if they match
                }
            }
        });
        edit_Mobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String mobile = s.toString();
                if (mobile.length() < 10 || !isValidIrishPrefix(mobile.substring(0, 10))) {
                   // "Invalid Irish mobile number format informe 083, 085, 086, 087, or 089"
                    isMobileValid = false;
                } else {
                    isMobileValid = true;
                }
            }
        });

        bt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edit_name.getText().toString();
                String surname = edit_surname.getText().toString();
                String mobile = edit_Mobile.getText().toString();
                String patient_D_Name = edit_Patient_Doctors_Name.getText().toString();
                String patient_D_Surname = edit_Patient_Doctors_Surname.getText().toString();
                String patient_C_Name = edit_Patient_Caregiver_Name.getText().toString();
                String username = edit_email.getText().toString();
                String password = edit_Password2.getText().toString();
                String retype = edit_Re_type.getText().toString();
                String dobString = edit_DOB.getText().toString();

                // First check if all fields are filled
                if (name.isEmpty() || surname.isEmpty() || mobile.isEmpty() || patient_D_Name.isEmpty() || patient_D_Surname.isEmpty() || patient_C_Name.isEmpty() || dobString.isEmpty() || username.isEmpty() || password.isEmpty() || retype.isEmpty()) {
                    Snackbar.make(v, errorMessages[0], Snackbar.LENGTH_SHORT).show();
                    return; // End method here if any field is empty
                }

                // Check if the passwords match
                if (!password.equals(retype)) {
                    Snackbar.make(v, "Passwords do not match.", Snackbar.LENGTH_LONG).show();
                    return; // Stop execution if the passwords are not the same
                }

                // Check for strong password
                if (!isPasswordStrong(password)) {
                    Snackbar.make(v, "A Strong Password should be minimum of 8 in length, containing a mixture of upper and lower case letters, and special characters.", Snackbar.LENGTH_LONG).show();
                    return; // Stop execution if the password is not strong
                }

                if (!isAgeValid(dobString)) {
                    edit_DOB.setError("You must be at least 18 years old to register.");
                    return; // Stop registration if not valid
                } else {
                    edit_DOB.setError(null); // Clear error if age is valid
                }


                // Check the validity of the mobile number
                if (!isMobileValid) {
                    return;
                }

                // All validations passed, proceed with registration
                cadastrarUsuario(v, username, password);
            }
        });
    }

    private boolean isPasswordStrong(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean hasUpper = false, hasLower = false, hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }

        return hasUpper && hasLower && hasSpecial;
    }

    private void setupMobileValidation() {
        edit_Mobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String mobile = s.toString();
                if (mobile.length() < 10 || !isValidIrishPrefix(mobile)) {
                    edit_Mobile.setError("Invalid Irish mobile number format. Must be 10 digits and start with 083, 085, 086, 087, or 089");
                    isMobileValid = false;
                } else {
                    edit_Mobile.setError(null); // Clear error
                    isMobileValid = true;
                }
            }
        });
    }

    private boolean isValidIrishPrefix(String number) {
        if (number.length() >= 3) {
            String prefix = number.substring(0, 3);
            return prefix.equals("083") || prefix.equals("085") || prefix.equals("086") || prefix.equals("087") || prefix.equals("089");
        }
        return false;
    }

    // Metod shows the format date
    public void showDatePickerDialog(View v) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                monthOfYear = monthOfYear + 1; // Adjust month to 1-based index
                String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", monthOfYear, dayOfMonth, year);
                edit_DOB.setText(formattedDate);

                // Check age right after the date is set
                if (!isAgeValid(formattedDate)) {
                    textViewError2.setText("You must be at least 18 years old to register.");
                    textViewError2.setVisibility(View.VISIBLE);
                } else {
                    textViewError2.setVisibility(View.GONE);
                }
            }
        }, year, month, day);
        datePickerDialog.show();
    }


    private void cadastrarUsuario(View v, String email, String password) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    saveUserData();
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

    private void saveUserData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("name", edit_name.getText().toString());
        user.put("surname", edit_surname.getText().toString());
        user.put("mobile", edit_Mobile.getText().toString());
        user.put("doctor_name", edit_Patient_Doctors_Name.getText().toString());
        user.put("doctor_surname", edit_Patient_Doctors_Surname.getText().toString());
        user.put("caregiver_name", edit_Patient_Caregiver_Name.getText().toString());
        user.put("date_of_birth", edit_DOB.getText().toString());
        user.put("username", edit_email.getText().toString());



        DocumentReference documentReference = db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        documentReference.set(user)
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
    }

    private void iniciarComponentes() {
        edit_name = findViewById(R.id.edit_name);
        edit_surname = findViewById(R.id.edit_surname);
        edit_Mobile = findViewById(R.id.edit_Mobile);
        edit_Patient_Doctors_Name = findViewById(R.id.edit_Patient_Doctors_Name);
        edit_Patient_Doctors_Surname = findViewById(R.id.edit_Patient_Doctors_Surname);
        edit_Patient_Caregiver_Name = findViewById(R.id.edit_Patient_Caregiver_Name);
        edit_email = findViewById(R.id.edit_email);
        edit_Password2 = findViewById(R.id.edit_Password2);
        edit_Re_type = findViewById(R.id.edit_Re_type);
        edit_DOB = findViewById(R.id.edit_DOB);
        bt_register = findViewById(R.id.bt_register);
        textViewError2 = findViewById(R.id.textViewError2);

        setupEmailValidation();
        setupDOBValidation();
        setupMobileValidation();
    }

    private void setupDOBValidation() {
        edit_DOB.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                edit_DOB.setError(null); // Clear any existing error message
                validateDOB(s.toString()); // Validate new DOB immediately
            }
        });
    }

    private void validateDOB(String dob) {
        if (!isAgeValid(dob)) {
            edit_DOB.setError("You must be at least 18 years old to register.");
            textViewError2.setText("You must be at least 18 years old to register.");
            textViewError2.setVisibility(View.VISIBLE);
        } else {
            textViewError2.setVisibility(View.GONE);
        }
    }

    private Date parseDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isAgeValid(String dobString) {
        Date dob = parseDate(dobString);
        if (dob == null) {
            return false;
        }
        int age = calculateAge(dob);
        return age >= 18;
    }

    private int calculateAge(Date birthDate) {
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);
        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }


    private void setupEmailValidation() {
        final Handler handler = new Handler();
        final Runnable emailValidationRunnable = new Runnable() {
            @Override
            public void run() {
                String email = edit_email.getText().toString();
                if (!isValidEmail(email)) {
                    edit_email.setError("Invalid email address. Must be a valid domain like @gmail.com, @hotmail.com, etc.");
                } else {
                    edit_email.setError(null); // Limpa o erro
                }
            }
        };

           edit_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacks(emailValidationRunnable); // Cancela qualquer runnable existente
                handler.postDelayed(emailValidationRunnable, 800); // Aguarda 800 milissegundos antes de validar
            }
        });
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[\\w-\\.]+@((gmail.com)|(hotmail.com)|(yahoo.com)|(outlook.com)|(live.com))$";
        return email.matches(emailRegex);
    }
}

