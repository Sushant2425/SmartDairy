//package com.ss.smartdairy;
//
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import com.google.firebase.FirebaseException;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.PhoneAuthCredential;
//import com.google.firebase.auth.PhoneAuthOptions;
//import com.google.firebase.auth.PhoneAuthProvider;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import java.util.HashMap;
//import java.util.concurrent.TimeUnit;
//
//public class LoginActivity extends AppCompatActivity {
//
//    private EditText etPhone;
//    private Button btnSendOTP;
//    private FirebaseAuth mAuth;
//    private ProgressDialog progressDialog;
//
//    private String verificationId;
//    private DatabaseReference userRef;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // Auto-login check
//        mAuth = FirebaseAuth.getInstance();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            startActivity(new Intent(this, DashboardActivity.class));
//            finish();
//            return;
//        }
//
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_login);
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        etPhone = findViewById(R.id.etPhone);
//        btnSendOTP = findViewById(R.id.btnSendOTP);
//        userRef = FirebaseDatabase.getInstance().getReference("Dairy").child("User");
//
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setCancelable(false);
//        progressDialog.setMessage("Sending OTP...");
//
//        btnSendOTP.setOnClickListener(v -> {
//            String phoneNumber = etPhone.getText().toString().trim();
//            if (phoneNumber.isEmpty() || phoneNumber.length() != 10) {
//                etPhone.setError("Enter a valid 10-digit number");
//                return;
//            }
//            sendOtp("+91" + phoneNumber);
//        });
//    }
//
//    private void sendOtp(String phoneNumber) {
//        progressDialog.show();
//
//        PhoneAuthOptions options =
//                PhoneAuthOptions.newBuilder(mAuth)
//                        .setPhoneNumber(phoneNumber)
//                        .setTimeout(60L, TimeUnit.SECONDS)
//                        .setActivity(this)
//                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//                            @Override
//                            public void onVerificationCompleted(PhoneAuthCredential credential) {
//                                progressDialog.dismiss();
//                                signInWithCredential(credential, phoneNumber);
//                            }
//
//                            @Override
//                            public void onVerificationFailed(FirebaseException e) {
//                                progressDialog.dismiss();
//                                Toast.makeText(LoginActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                            }
//
//                            @Override
//                            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
//                                progressDialog.dismiss();
//                                LoginActivity.this.verificationId = verificationId;
//
//                                Intent intent = new Intent(LoginActivity.this, OtpActivity.class);
//                                intent.putExtra("verificationId", verificationId);
//                                intent.putExtra("phone", phoneNumber);
//                                startActivity(intent);
//                            }
//                        })
//                        .build();
//        PhoneAuthProvider.verifyPhoneNumber(options);
//    }
//
//    private void signInWithCredential(PhoneAuthCredential credential, String phoneNumber) {
//        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                saveUserToFirebase(phoneNumber);
//                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(this, DashboardActivity.class));
//                finish();
//            } else {
//                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void saveUserToFirebase(String phone) {
//        HashMap<String, Object> userData = new HashMap<>();
//        userData.put("phone", phone);
//        userData.put("timestamp", System.currentTimeMillis());
//
//        userRef.child(phone).child("DairyInfo").updateChildren(userData)
//                .addOnFailureListener(e ->
//                        Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
//                );
//    }
//}
//

//package com.ss.smartdairy;
//
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import com.google.firebase.FirebaseException;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.PhoneAuthCredential;
//import com.google.firebase.auth.PhoneAuthOptions;
//import com.google.firebase.auth.PhoneAuthProvider;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.HashMap;
//import java.util.concurrent.TimeUnit;
//
//public class LoginActivity extends AppCompatActivity {
//
//    private EditText etPhone;
//    private Button btnSendOTP;
//    private FirebaseAuth mAuth;
//    private ProgressDialog progressDialog;
//
//    private String verificationId;
//    private DatabaseReference userRef;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // Initialize Firebase Auth and Database
//        mAuth = FirebaseAuth.getInstance();
//        userRef = FirebaseDatabase.getInstance().getReference("Dairy").child("User");
//
//        // Auto-login check
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            String phone = currentUser.getPhoneNumber();
//            if (phone != null && phone.startsWith("+91")) {
//                String phoneKey = phone.substring(3); // Strip +91
//                checkAndRecreateUserData(phoneKey);
//            } else {
//                Toast.makeText(this, "Invalid phone number in auth session", Toast.LENGTH_SHORT).show();
//                mAuth.signOut(); // Clear invalid session
//                setupUI();
//            }
//            return;
//        }
//
//        setupUI();
//    }
//
//    private void setupUI() {
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_login);
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        etPhone = findViewById(R.id.etPhone);
//        btnSendOTP = findViewById(R.id.btnSendOTP);
//
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setCancelable(false);
//        progressDialog.setMessage("Sending OTP...");
//
//        btnSendOTP.setOnClickListener(v -> {
//            String phoneNumber = etPhone.getText().toString().trim();
//            if (phoneNumber.isEmpty() || phoneNumber.length() != 10 || !phoneNumber.matches("\\d+")) {
//                etPhone.setError("Enter a valid 10-digit number");
//                return;
//            }
//            sendOtp("+91" + phoneNumber, phoneNumber);
//        });
//    }
//
//    private void checkAndRecreateUserData(String phoneKey) {
//        userRef.child(phoneKey).child("DairyInfo").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (!dataSnapshot.exists()) {
//                    // Data doesn't exist, recreate it
//                    saveUserToFirebase(phoneKey);
//                }
//                // Proceed to DashboardActivity
//                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
//                finish();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//                mAuth.signOut(); // Clear session on error
//                setupUI();
//            }
//        });
//    }
//
//    private void sendOtp(String fullPhoneNumber, String phoneKey) {
//        progressDialog.show();
//
//        PhoneAuthOptions options =
//                PhoneAuthOptions.newBuilder(mAuth)
//                        .setPhoneNumber(fullPhoneNumber)
//                        .setTimeout(60L, TimeUnit.SECONDS)
//                        .setActivity(this)
//                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//                            @Override
//                            public void onVerificationCompleted(PhoneAuthCredential credential) {
//                                progressDialog.dismiss();
//                                signInWithCredential(credential, phoneKey);
//                            }
//
//                            @Override
//                            public void onVerificationFailed(FirebaseException e) {
//                                progressDialog.dismiss();
//                                Toast.makeText(LoginActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                            }
//
//                            @Override
//                            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
//                                progressDialog.dismiss();
//                                LoginActivity.this.verificationId = verificationId;
//
//                                Intent intent = new Intent(LoginActivity.this, OtpActivity.class);
//                                intent.putExtra("verificationId", verificationId);
//                                intent.putExtra("phone", phoneKey);
//                                startActivity(intent);
//                            }
//                        })
//                        .build();
//        PhoneAuthProvider.verifyPhoneNumber(options);
//    }
//
//    private void signInWithCredential(PhoneAuthCredential credential, String phoneKey) {
//        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                // Save user data after successful login
//                saveUserToFirebase(phoneKey);
//                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(this, DashboardActivity.class));
//                finish();
//            } else {
//                Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void saveUserToFirebase(String phoneKey) {
//        HashMap<String, Object> userData = new HashMap<>();
//        userData.put("phone", "+91" + phoneKey);
//        userData.put("timestamp", System.currentTimeMillis());
//
//        userRef.child(phoneKey).child("DairyInfo").setValue(userData)
//                .addOnSuccessListener(unused -> {
//                    // Data saved successfully
//                })
//                .addOnFailureListener(e ->
//                        Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
//                );
//    }
//}



package com.ss.smartdairy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText etPhone;
    private Button btnSendOTP;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    private String verificationId;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Dairy").child("User");

        // Auto-login check
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String phone = currentUser.getPhoneNumber();
            if (phone != null && phone.startsWith("+91") && phone.length() == 13) {
                String phoneKey = phone.substring(3); // Strip +91
                Log.d(TAG, "Auto-login detected for phone: " + phoneKey);
                checkAndRecreateUserData(phoneKey);
            } else {
                Log.w(TAG, "Invalid phone number in auth session: " + phone);
                Toast.makeText(this, "Invalid auth session. Please log in again.", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                setupUI();
            }
            return;
        }

        setupUI();
    }

    private void setupUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etPhone = findViewById(R.id.etPhone);
        btnSendOTP = findViewById(R.id.btnSendOTP);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Sending OTP...");

        btnSendOTP.setOnClickListener(v -> {
            String phoneNumber = etPhone.getText().toString().trim();
            if (phoneNumber.isEmpty() || phoneNumber.length() != 10 || !phoneNumber.matches("\\d+")) {
                etPhone.setError("Enter a valid 10-digit number");
                Log.w(TAG, "Invalid phone number input: " + phoneNumber);
                return;
            }
            Log.d(TAG, "Sending OTP for phone: " + phoneNumber);
            sendOtp("+91" + phoneNumber, phoneNumber);
        });
    }

    private void checkAndRecreateUserData(String phoneKey) {
        Log.d(TAG, "Checking data existence for phone: " + phoneKey);
        userRef.child(phoneKey).child("DairyInfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.w(TAG, "Data missing for phone: " + phoneKey + ". Recreating...");
                    saveUserToFirebase(phoneKey);
                } else {
                    Log.d(TAG, "Data exists for phone: " + phoneKey);
                }
                // Proceed to DashboardActivity
                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error checking data for phone: " + phoneKey, databaseError.toException());
                Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                setupUI();
            }
        });
    }

    private void sendOtp(String fullPhoneNumber, String phoneKey) {
        progressDialog.show();
        Log.d(TAG, "Initiating OTP for: " + fullPhoneNumber);

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(fullPhoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                            @Override
                            public void onVerificationCompleted(PhoneAuthCredential credential) {
                                progressDialog.dismiss();
                                Log.d(TAG, "Verification completed for phone: " + phoneKey);
                                signInWithCredential(credential, phoneKey);
                            }

                            @Override
                            public void onVerificationFailed(FirebaseException e) {
                                progressDialog.dismiss();
                                Log.e(TAG, "Verification failed for phone: " + phoneKey, e);
                                Toast.makeText(LoginActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                                progressDialog.dismiss();
                                Log.d(TAG, "OTP sent for phone: " + phoneKey + ", verificationId: " + verificationId);
                                LoginActivity.this.verificationId = verificationId;

                                Intent intent = new Intent(LoginActivity.this, OtpActivity.class);
                                intent.putExtra("verificationId", verificationId);
                                intent.putExtra("phone", phoneKey);
                                startActivity(intent);
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithCredential(PhoneAuthCredential credential, String phoneKey) {
        Log.d(TAG, "Signing in with credential for phone: " + phoneKey);
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Login successful for phone: " + phoneKey);
                saveUserToFirebase(phoneKey);
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            } else {
                Log.e(TAG, "Login failed for phone: " + phoneKey, task.getException());
                Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserToFirebase(String phoneKey) {
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("phone", "+91" + phoneKey);
        userData.put("timestamp", System.currentTimeMillis());

        Log.d(TAG, "Saving user data for phone: " + phoneKey);
        userRef.child(phoneKey).child("DairyInfo").setValue(userData)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Successfully saved user data for phone: " + phoneKey);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save user data for phone: " + phoneKey, e);
                    Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
