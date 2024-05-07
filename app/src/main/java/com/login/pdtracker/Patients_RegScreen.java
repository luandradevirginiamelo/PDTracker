package com.login.pdtracker;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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
    private String codeVerifier;
    private String codeChallenge;

    private String fitbitCode; // Variable to store the Fitbit authorization code
    private String fitbitState; // Variable to store the Fitbit state

    String state = generateSecureRandomString();
    String[] errorMessages = {"Please complete all required fields to proceed", "Registration Successful!", "Invalid Irish mobile number format informe 083, 085, 086, 087, or 089"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_register);
        // Generate code verifier
        codeVerifier = generateCodeVerifier();

        // Generate code challenge
        codeChallenge = generateCodeChallenge(codeVerifier);
        iniciarComponentes();
        Button authorizeButton = findViewById(R.id.btn_fitbit_authorize);

        authorizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFitbitAuthorizationUrl();
            }
        });

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
                    edit_Password2.setError("A strong password should be of minimum 8 characters in length, and a mixture of upper- and lower-case letters, digits, and special characters.");
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
                String caregiverName = edit_Patient_Caregiver_Name.getText().toString();
                String doctorName = edit_Patient_Doctors_Name.getText().toString();
                String doctorSurname = edit_Patient_Doctors_Surname.getText().toString();


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
                    Snackbar.make(v, "A strong password should be of minimum 8 characters in length, and a mixture of upper- and lower-case letters, digits, and special characters.", Snackbar.LENGTH_LONG).show();
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
                cadastrarUsuario(v, edit_email.getText().toString(), edit_Password2.getText().toString(), caregiverName, doctorName, doctorSurname);
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
                    edit_Mobile.setError("A valid Irish mobile number should start with 083, 086, 087, 089, and be of total 10 digits in length.");
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

    // Method to extract access token from URL fragment
    private String extractAccessTokenFromUrl(String url) {
        // Extract access token from URL fragment
        Uri uri = Uri.parse(url);
        return uri.getFragment().replace("access_token=", "");
    }

    // Método atualizado com parâmetro adicional para o nome do cuidador
    private void cadastrarUsuario(View v, String email, String password, String caregiverName, String doctorName, String doctorSurname) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Obtém o ID do usuário recém-criado
                    String patientId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    // Chama saveUserData passando o ID do paciente, o nome do cuidador e os dados do médico
                    saveUserData(patientId, caregiverName, doctorName, doctorSurname);

                    Snackbar snackbar = Snackbar.make(v, "Registration successful!", Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                } else {
                    String error = "Registration failed: " + task.getException().getMessage();
                    Snackbar snackbar = Snackbar.make(v, error, Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(Color.RED);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }
            }
        });
    }


    private void linkCaregiverToPatient(String caregiverName, String patientId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Buscar o cuidador pelo nome na coleção "Caregiver"
        db.collection("Caregiver") // Assume que "Caregiver" é uma coleção direta
                .whereEqualTo("name", caregiverName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            // Assume-se que só haja um cuidador com esse nome; pegamos o ID do primeiro documento
                            String caregiverId = querySnapshot.getDocuments().get(0).getId();

                            // Atualiza o documento do paciente com o ID do cuidador
                            db.collection("users") // Substitua "users" se a coleção tiver um nome diferente
                                    .document(patientId)
                                    .update("IDcaregiver", caregiverId)
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Caregiver ID linked successfully to patient."))
                                    .addOnFailureListener(e -> Log.e("Firestore", "Error linking caregiver ID", e));
                        } else {
                            Log.d("Firestore", "No caregiver found with that name.");
                        }
                    } else {
                        Log.e("Firestore", "Error searching for caregiver", task.getException());
                    }
                });
    }

    private void linkDoctorToPatient(String doctorName, String doctorSurname, String patientId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Buscar o médico pelo nome e sobrenome na coleção "Doctor"
        db.collection("Doctor")
                .whereEqualTo("DoctorName", doctorName)
                .whereEqualTo("DoctorSurname", doctorSurname)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            // Assume-se que só haja um médico com esse nome e sobrenome; pegamos o ID do primeiro documento
                            String doctorId = querySnapshot.getDocuments().get(0).getId();

                            // Atualiza o documento do paciente com o ID do médico
                            db.collection("users") // Substitua "users" se a coleção tiver um nome diferente
                                    .document(patientId)
                                    .update("DoctorID", doctorId)
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Doctor ID linked successfully to patient."))
                                    .addOnFailureListener(e -> Log.e("Firestore", "Error linking doctor ID", e));
                        } else {
                            Log.d("Firestore", "No doctor found with that name and surname.");
                        }
                    } else {
                        Log.e("Firestore", "Error searching for doctor", task.getException());
                    }
                });
    }


    private void saveUserData(String patientId, String caregiverName, String doctorName, String doctorSurname) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("name", edit_name.getText().toString());
        user.put("surname", edit_surname.getText().toString());
        user.put("mobile", edit_Mobile.getText().toString());
        user.put("doctor_name", doctorName); // Usa o nome do médico passado como parâmetro
        user.put("doctor_surname", doctorSurname); // Usa o sobrenome do médico passado como parâmetro
        user.put("caregiver_name", caregiverName);
        user.put("date_of_birth", edit_DOB.getText().toString());
        user.put("username", edit_email.getText().toString());

        DocumentReference newUserDocRef = db.collection("users").document(patientId);
        newUserDocRef.set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User data successfully saved.");
                    linkCaregiverToPatient(caregiverName, patientId); // Continua a vincular o cuidador
                    linkDoctorToPatient(doctorName, doctorSurname, patientId); // Adiciona chamada para vincular o médico
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error saving user data", e);
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
                    edit_email.setError("Incorrect email address. A valid email should contain valid email domain, like gmail.com, hotmail.com, yahoo.com, etc., and has the @ sign before it.");
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

    public static String generateSecureRandomString() {
        String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        String NUMBER = "0123456789";
        String SPECIAL_CHARS = "!@#$%&*()_+-=[]|,./?><";

        String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL_CHARS;
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            // 0-62 (exclusive), random returns 0-61
            int randomCharIndex = random.nextInt(PASSWORD_ALLOW_BASE.length());
            // append a character at random location
            sb.append(PASSWORD_ALLOW_BASE.charAt(randomCharIndex));
        }
        return sb.toString();
    }

    public static String generateSHA256Base64(String codeVerifier) {
        try {
            // Calculate SHA-256 hash of the codeVerifier
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(codeVerifier.getBytes());

            // Encode the hash bytes to base64
            return android.util.Base64.encodeToString(hashBytes, android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            // Handle NoSuchAlgorithmException
            e.printStackTrace();
            return null;
        }
    }
    // Method to generate a random string of length between 43 to 128 characters
    private String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifierBytes = new byte[64]; // Adjust length as needed (minimum 43 characters)
        secureRandom.nextBytes(codeVerifierBytes);
        return android.util.Base64.encodeToString(codeVerifierBytes, android.util.Base64.URL_SAFE | android.util.Base64.NO_PADDING);

    }

    // Method to generate code challenge from code verifier
    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] codeVerifierBytes = codeVerifier.getBytes();
            byte[] codeChallengeBytes = digest.digest(codeVerifierBytes);
            return android.util.Base64.encodeToString(codeVerifierBytes, android.util.Base64.URL_SAFE | android.util.Base64.NO_PADDING);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            // Extract data from the URL response
            String code = data.getQueryParameter("code");
            String state = data.getQueryParameter("state");

            // Save the data to Firebase or perform any other necessary action
            saveDataToFirebase(code, state);
        }
    }

    private void saveDataToFirebase(String code, String state) {
        // Save the data to Firebase Firestore or Realtime Database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("code", code);
        data.put("state", state);

        db.collection("fitbit_auth_responses")
                .add(data)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
    }

    // Method to extract refresh token from URL
    private String extractRefreshTokenFromUrl(String url) {
        Uri uri = Uri.parse(url);
        return uri.getQueryParameter("refresh_token");
    }

    // Method to extract user ID from URL
    private String extractUserIdFromUrl(String url) {
        Uri uri = Uri.parse(url);
        return uri.getQueryParameter("user_id");
    }

    private void openFitbitAuthorizationUrl() {
        if (fitbitPermissionGranted()) {
            String authorizationUrl = getIntent().getData().toString();
            handleFitbitAuthorizationResponse(authorizationUrl);
            return;
        }

        String fitbitAuthorizationUrl = "https://www.fitbit.com/oauth2/authorize?" +
                "response_type=code" +
                "&client_id=23RWTJ" +
                "&scope=activity+nutrition+heartrate+location+profile+settings+sleep+social+weight" +
                "&code_challenge=" + codeChallenge +
                "&code_challenge_method=S256" +
                "&redirect_uri=https://Localhost" +
                "&state=" + state;

        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("https://Localhost") || url.contains("transferpage")) {
                    handleFitbitAuthorizationResponse(url);
                    finish(); // Close WebView after handling response
                    return true;
                }
                return false;
            }
        });



        webView.loadUrl(fitbitAuthorizationUrl);

        setContentView(R.layout.activity_form_register);
    }

    private void handleFitbitAuthorizationResponse(String url) {
        if (url.contains("transferpage")) {
            String code = extractCodeFromTransferpageUrl(url);
            String state = extractStateFromTransferpageUrl(url);
            // Save data to Firebase or perform other actions
            saveDataToFirebase(code, state);
            return;

        }

        // Existing code to handle localhost URL (code and state extraction)
        String code = getCodeFromUrl(url);
        String returnedState = getStateFromUrl(url);

        if (!state.equals(returnedState)) {
            Log.e(TAG, "Returned state does not match the expected state.");
            return;
        }

        //saveUserData(code, state);
    }



    private String getCodeFromUrl(String url) {
        Uri uri = Uri.parse(url);
        return uri.getQueryParameter("code");
    }

    private String getStateFromUrl(String url) {
        Uri uri = Uri.parse(url);
        return uri.getQueryParameter("state");
    }

    private boolean fitbitPermissionGranted() {
        Uri data = getIntent().getData();
        return data != null && data.toString().startsWith("https://Localhost");
    }

    private String extractCodeFromTransferpageUrl(String url) {
        Uri uri = Uri.parse(url);
        String query = uri.getQueryParameter("code");
        return query != null ? query : "";
    }

    private String extractStateFromTransferpageUrl(String url) {
        Uri uri = Uri.parse(url);
        String query = uri.getQueryParameter("state");
        return query != null ? query : "";
    }
}
