//// OtpActivity.java
//package com.ss.smartdairy;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.PhoneAuthCredential;
//import com.google.firebase.auth.PhoneAuthProvider;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//public class OtpActivity extends AppCompatActivity {
//
//    private EditText etOtp;
//    private Button btnVerify;
//    private String verificationId;
//    private FirebaseAuth mAuth;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_otp);
//
//        etOtp = findViewById(R.id.etOtp);
//        btnVerify = findViewById(R.id.btnVerify);
//
//        mAuth = FirebaseAuth.getInstance();
//        verificationId = getIntent().getStringExtra("verificationId");
//
//        btnVerify.setOnClickListener(v -> {
//            String otp = etOtp.getText().toString().trim();
//            if (otp.isEmpty() || otp.length() != 6) {
//                etOtp.setError("Enter 6-digit OTP");
//                return;
//            }
//            verifyCode(otp);
//        });
//    }
//
//    private void verifyCode(String code) {
//        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
//        signInWithCredential(credential);
//    }
//
////    private void signInWithCredential(PhoneAuthCredential credential) {
////        mAuth.signInWithCredential(credential)
////                .addOnCompleteListener(task -> {
//////                    if (task.isSuccessful()) {
//////                        Toast.makeText(OtpActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
//////                        // Redirect to home or dashboard
//////                        Intent intent = new Intent(OtpActivity.this, MainActivity.class);
//////                        startActivity(intent);
//////                        finish();
//////                    } else {
//////                        Toast.makeText(OtpActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
//////                    }
//////                });
////
////
////                    if (task.isSuccessful()) {
////                        // 🔍 Check SharedPreferences
////                        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
////                        boolean isFirstLaunch = prefs.getBoolean("isFirstLaunch", true);
////
////                        if (isFirstLaunch) {
////                            // 👉 First time: open Data Entry
//////                            startActivity(new Intent(OtpActivity.this, DataEntryActivity.class));
////                            Intent intent = new Intent(OtpActivity.this, DataEntryActivity.class);
////                            intent.putExtra("phone", getIntent().getStringExtra("phone")); // pass from login
////                            startActivity(intent);
////
////                        } else {
////                            // 👉 Already done: go to MainActivity
////                            startActivity(new Intent(OtpActivity.this, DashboardActivity.class));
////                        }
////
////                        finish();
////                    } else {
////                        Toast.makeText(OtpActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
////                    }
////                });
////    }
//
//private void signInWithCredential(PhoneAuthCredential credential) {
//    mAuth.signInWithCredential(credential)
//            .addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    String rawPhone = getIntent().getStringExtra("phone");
//                    String mobile = rawPhone.replaceAll("[^0-9]", "");
//
//                    DatabaseReference userRef = FirebaseDatabase.getInstance()
//                            .getReference("Dairy").child("Users").child(mobile);
//
//                    userRef.get().addOnSuccessListener(snapshot -> {
//                        if (snapshot.exists()) {
//                            // 👉 User already exists → Go to Dashboard
//                            startActivity(new Intent(OtpActivity.this, DashboardActivity.class));
//                        } else {
//                            // 👉 New user → Go to Data Entry
//                            Intent intent = new Intent(OtpActivity.this, DataEntryActivity.class);
//                            intent.putExtra("phone", mobile);
//                            startActivity(intent);
//                        }
//                        finish();
//                    }).addOnFailureListener(e -> {
//                        Toast.makeText(this, "Failed to check user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        finish();
//                    });
//
//                } else {
//                    Toast.makeText(OtpActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
//                }
//            });
//}
//
//}

////package com.ss.smartdairy;
////
////import android.content.Intent;
////import android.os.Bundle;
////import android.widget.Button;
////import android.widget.EditText;
////import android.widget.Toast;
////
////import androidx.appcompat.app.AppCompatActivity;
////
////import com.google.firebase.auth.FirebaseAuth;
////import com.google.firebase.auth.PhoneAuthCredential;
////import com.google.firebase.auth.PhoneAuthProvider;
////import com.google.firebase.database.DataSnapshot;
////import com.google.firebase.database.DatabaseReference;
////import com.google.firebase.database.FirebaseDatabase;
////
////public class OtpActivity extends AppCompatActivity {
////
////    private EditText etOtp;
////    private Button btnVerify;
////    private String verificationId;
////    private FirebaseAuth mAuth;
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_otp);
////
////        etOtp = findViewById(R.id.etOtp);
////        btnVerify = findViewById(R.id.btnVerify);
////
////        mAuth = FirebaseAuth.getInstance();
////        verificationId = getIntent().getStringExtra("verificationId");
////
////        btnVerify.setOnClickListener(v -> {
////            String otp = etOtp.getText().toString().trim();
////            if (otp.isEmpty() || otp.length() != 6) {
////                etOtp.setError("Enter 6-digit OTP");
////                return;
////            }
////            verifyCode(otp);
////        });
////    }
////
////    private void verifyCode(String code) {
////        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
////        signInWithCredential(credential);
////    }
////
////    private void signInWithCredential(PhoneAuthCredential credential) {
////        mAuth.signInWithCredential(credential)
////                .addOnCompleteListener(task -> {
////                    if (task.isSuccessful()) {
////                        String rawPhone = getIntent().getStringExtra("phone");
////                        String mobile = rawPhone.replaceAll("[^0-9]", "");
////
////
////
////                        DatabaseReference userRef = FirebaseDatabase.getInstance()
////                                .getReference("Dairy").child("Users").child(mobile);
////
////                        userRef.get().addOnSuccessListener(snapshot -> {
////                            if (snapshot.exists()) {
////                                // ✅ Registered user
////                                startActivity(new Intent(OtpActivity.this, DashboardActivity.class));
////                            } else {
////                                // 🔔 First time user
////                                Intent intent = new Intent(OtpActivity.this, DataEntryActivity.class);
////                                intent.putExtra("phone", mobile);
////                                startActivity(intent);
////                            }
////                            finish();
////                        }).addOnFailureListener(e -> {
////                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
////                            finish();
////                        });
////
////                    } else {
////                        Toast.makeText(OtpActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
////                    }
////                });
////    }
////}
////package com.ss.smartdairy;
////
////import android.content.Intent;
////import android.os.Bundle;
////import android.widget.Button;
////import android.widget.EditText;
////import android.widget.Toast;
////
////import androidx.appcompat.app.AppCompatActivity;
////
////import com.google.firebase.auth.FirebaseAuth;
////import com.google.firebase.auth.PhoneAuthCredential;
////import com.google.firebase.auth.PhoneAuthProvider;
////import com.google.firebase.database.DatabaseReference;
////import com.google.firebase.database.FirebaseDatabase;
////
////public class OtpActivity extends AppCompatActivity {
////
////    private EditText etOtp;
////    private Button btnVerify;
////    private String verificationId;
////    private FirebaseAuth mAuth;
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_otp);
////
////        etOtp = findViewById(R.id.etOtp);
////        btnVerify = findViewById(R.id.btnVerify);
////
////        mAuth = FirebaseAuth.getInstance();
////        verificationId = getIntent().getStringExtra("verificationId");
////
////        btnVerify.setOnClickListener(v -> {
////            String otp = etOtp.getText().toString().trim();
////            if (otp.isEmpty() || otp.length() != 6) {
////                etOtp.setError("Enter 6-digit OTP");
////                return;
////            }
////            verifyCode(otp);
////        });
////    }
////
////    private void verifyCode(String code) {
////        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
////        signInWithCredential(credential);
////    }
////
////    private void signInWithCredential(PhoneAuthCredential credential) {
////        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
////            if (task.isSuccessful()) {
////                String phone = getIntent().getStringExtra("phone");
////                String cleanPhone = phone.replaceAll("[^0-9]", "");  // ✅ clean the phone number
////
////                // ✅ Correct Firebase path to check dairyInfo
////                DatabaseReference dairyInfoRef = FirebaseDatabase.getInstance()
////                        .getReference("Dairy")
////                        .child("Users")
////                        .child(cleanPhone)
////                        .child("dairyInfo");
////
////                dairyInfoRef.get().addOnSuccessListener(snapshot -> {
////                    if (snapshot.exists()) {
////                        // 👉 DairyInfo already saved
////                        startActivity(new Intent(OtpActivity.this, DashboardActivity.class));
////                    } else {
////                        // 👉 First time user
////                        Intent intent = new Intent(OtpActivity.this, DataEntryActivity.class);
////                        intent.putExtra("phone", cleanPhone);
////                        startActivity(intent);
////                    }
////                    finish();
////                }).addOnFailureListener(e -> {
////                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
////                });
////
////            } else {
////                Toast.makeText(OtpActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
////            }
////        });
////    }
////}
//
//
//
//
//
//
//
////
////package com.ss.smartdairy;
////
////import android.content.Intent;
////import android.os.Bundle;
////import android.util.Log;
////import android.view.View;
////import android.widget.Button;
////import android.widget.EditText;
////import android.widget.ProgressBar;
////import android.widget.Toast;
////
////import androidx.appcompat.app.AppCompatActivity;
////
////import com.google.firebase.FirebaseException;
////import com.google.firebase.FirebaseTooManyRequestsException;
////import com.google.firebase.auth.FirebaseAuth;
////import com.google.firebase.auth.PhoneAuthCredential;
////import com.google.firebase.auth.PhoneAuthOptions;
////import com.google.firebase.auth.PhoneAuthProvider;
////import com.google.firebase.database.DatabaseReference;
////import com.google.firebase.database.FirebaseDatabase;
////
////import java.util.concurrent.TimeUnit;
////
////public class OtpActivity extends AppCompatActivity {
////
////    private EditText etOtp;
////    private Button btnVerify, btnResendOtp;
////    private ProgressBar progressBar;
////    private String verificationId;
////    private FirebaseAuth mAuth;
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_otp);
////
////        etOtp = findViewById(R.id.etOtp);
////        btnVerify = findViewById(R.id.btnVerify);
////        btnResendOtp = findViewById(R.id.btnResendOtp);
////        progressBar = findViewById(R.id.progressBar);
////
////        mAuth = FirebaseAuth.getInstance();
////        verificationId = getIntent().getStringExtra("verificationId");
////        String phoneNumber = getIntent().getStringExtra("phone");
////
////        btnVerify.setOnClickListener(v -> {
////            String otp = etOtp.getText().toString().trim();
////            if (otp.isEmpty() || otp.length() != 6) {
////                etOtp.setError("Enter 6-digit OTP");
////                return;
////            }
////            verifyCode(otp);
////        });
////
////        btnResendOtp.setOnClickListener(v -> resendOtp(phoneNumber));
////    }
////
////    private void verifyCode(String code) {
////        if (progressBar == null) {
////            Log.e("OtpActivity", "ProgressBar is null");
////            Toast.makeText(this, "UI error. Please try again.", Toast.LENGTH_SHORT).show();
////            return;
////        }
////        progressBar.setVisibility(View.VISIBLE);
////        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
////        signInWithCredential(credential);
////    }
////
////    private void signInWithCredential(PhoneAuthCredential credential) {
////        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
////            if (progressBar != null) {
////                progressBar.setVisibility(View.GONE);
////            }
////            if (task.isSuccessful()) {
////                if (mAuth.getCurrentUser() == null) {
////                    Log.e("OtpActivity", "User is not authenticated");
////                    Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show();
////                    startActivity(new Intent(this, LoginActivity.class));
////                    finish();
////                    return;
////                }
////                String uid = mAuth.getCurrentUser().getUid();
////                String phone = getIntent().getStringExtra("phone");
////                String cleanPhone = phone.replaceAll("[^0-9]", "");
////                DatabaseReference dairyInfoRef = FirebaseDatabase.getInstance()
////                        .getReference("Dairy")
////                        .child("Users")
////                        .child(uid)
////                        .child("dairyInfo");
////
////                dairyInfoRef.get().addOnSuccessListener(snapshot -> {
////                    Log.d("OtpActivity", "Snapshot exists: " + snapshot.exists());
////                    Log.d("OtpActivity", "Snapshot data: " + snapshot.getValue());
////                    if (snapshot.exists()) {
////                        Log.d("OtpActivity", "Navigating to DashboardActivity");
////                        startActivity(new Intent(OtpActivity.this, DashboardActivity.class));
////                    } else {
////                        Log.d("OtpActivity", "Navigating to DataEntryActivity");
////                        Intent intent = new Intent(OtpActivity.this, DataEntryActivity.class);
////                        intent.putExtra("phone", cleanPhone);
////                        startActivity(intent);
////                    }
////                    finish();
////                }).addOnFailureListener(e -> {
////                    Log.e("OtpActivity", "Failed to fetch dairyInfo: " + e.getMessage());
////                    Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
////                });
////            } else {
////                Toast.makeText(OtpActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
////            }
////        });
////    }
////
////    private void resendOtp(String phoneNumber) {
////        if (progressBar == null) {
////            Log.e("OtpActivity", "ProgressBar is null");
////            Toast.makeText(this, "UI error. Please try again.", Toast.LENGTH_SHORT).show();
////            return;
////        }
////        progressBar.setVisibility(View.VISIBLE);
////        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
////                .setPhoneNumber(phoneNumber)
////                .setTimeout(60L, TimeUnit.SECONDS)
////                .setActivity(this)
////                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
////                    @Override
////                    public void onVerificationCompleted(PhoneAuthCredential credential) {
////                        if (progressBar != null) {
////                            progressBar.setVisibility(View.GONE);
////                        }
////                        signInWithCredential(credential);
////                    }
////
////                    @Override
////                    public void onVerificationFailed(FirebaseException e) {
////                        if (progressBar != null) {
////                            progressBar.setVisibility(View.GONE);
////                        }
////                        String errorMessage = e instanceof FirebaseTooManyRequestsException
////                                ? "Too many requests. Try again later."
////                                : "Failed to resend OTP. Check your network or phone number.";
////                        Toast.makeText(OtpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
////                    }
////
////                    @Override
////                    public void onCodeSent(String newVerificationId, PhoneAuthProvider.ForceResendingToken token) {
////                        if (progressBar != null) {
////                            progressBar.setVisibility(View.GONE);
////                        }
////                        verificationId = newVerificationId;
////                        Toast.makeText(OtpActivity.this, "OTP resent successfully", Toast.LENGTH_SHORT).show();
////                    }
////                })
////                .build();
////        PhoneAuthProvider.verifyPhoneNumber(options);
////    }
////}
//
//
//package com.ss.smartdairy;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.FirebaseException;
//import com.google.firebase.FirebaseTooManyRequestsException;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.PhoneAuthCredential;
//import com.google.firebase.auth.PhoneAuthOptions;
//import com.google.firebase.auth.PhoneAuthProvider;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import java.util.concurrent.TimeUnit;
//
//public class OtpActivity extends AppCompatActivity {
//
//    private EditText etOtp;
//    private Button btnVerify, btnResendOtp;
//    private ProgressBar progressBar;
//    private String verificationId;
//    private FirebaseAuth mAuth;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_otp);
//
//        etOtp = findViewById(R.id.etOtp);
//        btnVerify = findViewById(R.id.btnVerify);
//        btnResendOtp = findViewById(R.id.btnResendOtp);
//        progressBar = findViewById(R.id.progressBar);
//
//        mAuth = FirebaseAuth.getInstance();
//        verificationId = getIntent().getStringExtra("verificationId");
//        String phoneNumber = getIntent().getStringExtra("phone");
//
//        if (etOtp == null || btnVerify == null || btnResendOtp == null || progressBar == null) {
//            Log.e("OtpActivity", "One or more views are null");
//            Toast.makeText(this, "UI error. Please try again.", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        btnVerify.setOnClickListener(v -> {
//            String otp = etOtp.getText().toString().trim();
//            if (otp.isEmpty() || otp.length() != 6) {
//                etOtp.setError("Enter 6-digit OTP");
//                return;
//            }
//            verifyCode(otp);
//        });
//
//        btnResendOtp.setOnClickListener(v -> resendOtp(phoneNumber));
//    }
//
//    private void verifyCode(String code) {
//        progressBar.setVisibility(View.VISIBLE);
//        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
//        signInWithCredential(credential);
//    }
//
//    private void signInWithCredential(PhoneAuthCredential credential) {
//        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
//            progressBar.setVisibility(View.GONE);
//            if (task.isSuccessful()) {
//                if (mAuth.getCurrentUser() == null) {
//                    Log.e("OtpActivity", "User is not authenticated");
//                    Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show();
//                    startActivity(new Intent(this, LoginActivity.class));
//                    finish();
//                    return;
//                }
//                String uid = mAuth.getCurrentUser().getUid();
//                String phone = getIntent().getStringExtra("phone");
//                String cleanPhone = phone.replaceAll("[^0-9]", "");
//
//                // Check SharedPreferences first
//                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
//                boolean hasCompletedDataEntry = prefs.getBoolean("hasCompletedDataEntry_" + uid, false);
//                if (hasCompletedDataEntry) {
//                    Log.d("OtpActivity", "hasCompletedDataEntry is true, navigating to DashboardActivity");
//                    startActivity(new Intent(this, DashboardActivity.class));
//                    finish();
//                    return;
//                }
//
//                // Fallback to Firebase check
//                DatabaseReference dairyInfoRef = FirebaseDatabase.getInstance()
//                        .getReference("Dairy")
//                        .child("Users")
//                        .child(uid)
//                        .child("dairyInfo");
//
//                dairyInfoRef.get().addOnSuccessListener(snapshot -> {
//                    Log.d("OtpActivity", "Snapshot exists: " + snapshot.exists());
//                    Log.d("OtpActivity", "Snapshot data: " + snapshot.getValue());
//                    if (snapshot.exists()) {
//                        Log.d("OtpActivity", "Navigating to DashboardActivity");
//                        prefs.edit().putBoolean("hasCompletedDataEntry_" + uid, true).apply();
//                        startActivity(new Intent(this, DashboardActivity.class));
//                    } else {
//                        Log.d("OtpActivity", "Navigating to DataEntryActivity");
//                        Intent intent = new Intent(this, DataEntryActivity.class);
//                        intent.putExtra("phone", cleanPhone);
//                        startActivity(intent);
//                    }
//                    finish();
//                }).addOnFailureListener(e -> {
//                    Log.e("OtpActivity", "Failed to fetch dairyInfo: " + e.getMessage());
//                    Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    // Navigate to DataEntryActivity as a fallback
//                    Intent intent = new Intent(this, DataEntryActivity.class);
//                    intent.putExtra("phone", cleanPhone);
//                    startActivity(intent);
//                    finish();
//                });
//            } else {
//                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void resendOtp(String phoneNumber) {
//        progressBar.setVisibility(View.VISIBLE);
//        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
//                .setPhoneNumber(phoneNumber)
//                .setTimeout(60L, TimeUnit.SECONDS)
//                .setActivity(this)
//                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//                    @Override
//                    public void onVerificationCompleted(PhoneAuthCredential credential) {
//                        progressBar.setVisibility(View.GONE);
//                        signInWithCredential(credential);
//                    }
//
//                    @Override
//                    public void onVerificationFailed(FirebaseException e) {
//                        progressBar.setVisibility(View.GONE);
//                        String errorMessage = e instanceof FirebaseTooManyRequestsException
//                                ? "Too many requests. Try again later."
//                                : "Failed to resend OTP. Check your network or phone number.";
//                        Toast.makeText(OtpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
//                    }
//
//                    @Override
//                    public void onCodeSent(String newVerificationId, PhoneAuthProvider.ForceResendingToken token) {
//                        progressBar.setVisibility(View.GONE);
//                        verificationId = newVerificationId;
//                        Toast.makeText(OtpActivity.this, "OTP resent successfully", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .build();
//        PhoneAuthProvider.verifyPhoneNumber(options);
//    }
//}

package com.ss.smartdairy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    private EditText etOtp;
    private Button btnVerify, btnResendOtp;
    private ProgressBar progressBar;
    private String verificationId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        etOtp = findViewById(R.id.etOtp);
        btnVerify = findViewById(R.id.btnVerify);
        btnResendOtp = findViewById(R.id.btnResendOtp);
        progressBar = findViewById(R.id.progressBar);

        if (etOtp == null || btnVerify == null || btnResendOtp == null || progressBar == null) {
            Log.e("OtpActivity", "One or more views are null");
            Toast.makeText(this, "UI error. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        verificationId = getIntent().getStringExtra("verificationId");
        String phoneNumber = getIntent().getStringExtra("phone");

        btnVerify.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            if (otp.isEmpty() || otp.length() != 6) {
                etOtp.setError("Enter 6-digit OTP");
                return;
            }
            verifyCode(otp);
        });

        btnResendOtp.setOnClickListener(v -> resendOtp(phoneNumber));
    }

    private void verifyCode(String code) {
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                if (mAuth.getCurrentUser() == null) {
                    Log.e("OtpActivity", "User is not authenticated");
                    Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    return;
                }
                String uid = mAuth.getCurrentUser().getUid();
                String phone = getIntent().getStringExtra("phone");
                String cleanPhone = phone.replaceAll("[^0-9]", "");

                // Check SharedPreferences
                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                boolean hasCompletedDataEntry = prefs.getBoolean("hasCompletedDataEntry_" + uid, false);
                if (hasCompletedDataEntry) {
                    Log.d("OtpActivity", "hasCompletedDataEntry is true, navigating to DashboardActivity");
                    startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                    return;
                }

                // Fallback to Firebase check
                DatabaseReference dairyInfoRef = FirebaseDatabase.getInstance()
                        .getReference("Dairy")
                        .child("Users")
                        .child(uid)
                        .child("dairyInfo");

                dairyInfoRef.get().addOnSuccessListener(snapshot -> {
                    Log.d("OtpActivity", "Snapshot exists: " + snapshot.exists());
                    Log.d("OtpActivity", "Snapshot data: " + snapshot.getValue());
                    if (snapshot.exists()) {
                        Log.d("OtpActivity", "Navigating to DashboardActivity");
                        prefs.edit().putBoolean("hasCompletedDataEntry_" + uid, true).apply();
                        startActivity(new Intent(this, DashboardActivity.class));
                    } else {
                        Log.d("OtpActivity", "Navigating to DataEntryActivity");
                        Intent intent = new Intent(this, DataEntryActivity.class);
                        intent.putExtra("phone", cleanPhone);
                        startActivity(intent);
                    }
                    finish();
                }).addOnFailureListener(e -> {
                    Log.e("OtpActivity", "Failed to fetch dairyInfo: " + e.getMessage());
                    Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, DataEntryActivity.class);
                    intent.putExtra("phone", cleanPhone);
                    startActivity(intent);
                    finish();
                });
            } else {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendOtp(String phoneNumber) {
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        progressBar.setVisibility(View.GONE);
                        signInWithCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        progressBar.setVisibility(View.GONE);
                        String errorMessage = e instanceof FirebaseTooManyRequestsException
                                ? "Too many requests. Try again later."
                                : "Failed to resend OTP. Check your network or phone number.";
                        Toast.makeText(OtpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(String newVerificationId, PhoneAuthProvider.ForceResendingToken token) {
                        progressBar.setVisibility(View.GONE);
                        verificationId = newVerificationId;
                        Toast.makeText(OtpActivity.this, "OTP resent successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}