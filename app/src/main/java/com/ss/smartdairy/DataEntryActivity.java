////
////package com.ss.smartdairy;
////
////import android.app.DatePickerDialog;
////import android.content.Intent;
////import android.content.SharedPreferences;
////import android.os.Bundle;
////import android.widget.Button;
////import android.widget.Toast;
////
////import androidx.appcompat.app.AppCompatActivity;
////
////import com.google.android.material.textfield.TextInputEditText;
////import com.google.firebase.auth.FirebaseAuth;
////import com.google.firebase.database.DatabaseReference;
////import com.google.firebase.database.FirebaseDatabase;
////
////import java.util.Calendar;
////import java.util.HashMap;
////import java.util.Map;
////
////public class DataEntryActivity extends AppCompatActivity {
////
////    TextInputEditText etOwner, etContact, etDairyName, etAddress, etStartDate;
////    Button btnSave;
////    String phoneFromIntent;
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_data_entry);
////
////        etOwner = findViewById(R.id.et_owner_name);
////        etContact = findViewById(R.id.et_contact);
////        etDairyName = findViewById(R.id.et_dairy_name);
////        etAddress = findViewById(R.id.et_address);
////        etStartDate = findViewById(R.id.et_start_date);
////        btnSave = findViewById(R.id.btn_save);
////
////        if (etOwner == null || etContact == null || etDairyName == null || etAddress == null || etStartDate == null || btnSave == null) {
////            Toast.makeText(this, "UI error. Please try again.", Toast.LENGTH_SHORT).show();
////            finish();
////            return;
////        }
////
////        phoneFromIntent = getIntent().getStringExtra("phone");
////        if (phoneFromIntent != null && phoneFromIntent.length() >= 10) {
////            // Extract last 10 digits if phone includes country code
////            phoneFromIntent = phoneFromIntent.length() > 10 ? phoneFromIntent.substring(phoneFromIntent.length() - 10) : phoneFromIntent;
////            etContact.setText(phoneFromIntent);
////            etContact.setEnabled(false); // Prevent editing
////        }
////
////        etStartDate.setOnClickListener(v -> openDatePicker());
////
////        btnSave.setOnClickListener(v -> validateAndSave());
////    }
////
////    private void openDatePicker() {
////        Calendar calendar = Calendar.getInstance();
////        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
////            String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
////            etStartDate.setText(date);
////        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
////        dialog.show();
////    }
////
////    private void validateAndSave() {
////        String owner = etOwner.getText().toString().trim();
////        String contact = etContact.getText().toString().trim();
////        String dairy = etDairyName.getText().toString().trim();
////        String address = etAddress.getText().toString().trim();
////        String startDate = etStartDate.getText().toString().trim();
////
////        if (owner.isEmpty() || owner.split("\\s+").length > 3) {
////            etOwner.setError("Enter full name (max 3 words)");
////            return;
////        }
////
////        if (contact.length() != 10 || !contact.matches("\\d+")) {
////            etContact.setError("Enter valid 10-digit number");
////            return;
////        }
////
////        if (dairy.isEmpty()) {
////            etDairyName.setError("Dairy name required");
////            return;
////        }
////
////        if (address.isEmpty()) {
////            etAddress.setError("Address required");
////            return;
////        }
////
////        if (startDate.isEmpty()) {
////            etStartDate.setError("Start date required");
////            return;
////        }
////
////        Map<String, Object> userData = new HashMap<>();
////        userData.put("ownerName", owner);
////        userData.put("contact", contact);
////        userData.put("dairyName", dairy);
////        userData.put("address", address);
////        userData.put("startDate", startDate);
////
////        FirebaseDatabase.getInstance()
////                .getReference("Dairy")
////                .child("User")
////                .child(phoneFromIntent)
////                .child("DairyInfo")
////                .updateChildren(userData)
////                .addOnSuccessListener(unused -> {
////                    Toast.makeText(this, "Data saved!", Toast.LENGTH_SHORT).show();
////                    SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
////                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
////                    prefs.edit().putBoolean("hasCompletedDataEntry_" + uid, true).apply();
////                    startActivity(new Intent(this, DashboardActivity.class));
////                    finish();
////                }).addOnFailureListener(e ->
////                        Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show()
////                );
////    }
////}
////
////
////
//
//package com.ss.smartdairy;
//
//import android.app.DatePickerDialog;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.Map;
//
//public class DataEntryActivity extends AppCompatActivity {
//
//    TextInputEditText etOwner, etContact, etDairyName, etAddress, etStartDate;
//    Button btnSave;
//    String phoneFromIntent;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_data_entry);
//
//        etOwner = findViewById(R.id.et_owner_name);
//        etContact = findViewById(R.id.et_contact);
//        etDairyName = findViewById(R.id.et_dairy_name);
//        etAddress = findViewById(R.id.et_address);
//        etStartDate = findViewById(R.id.et_start_date);
//        btnSave = findViewById(R.id.btn_save);
//
//        if (etOwner == null || etContact == null || etDairyName == null || etAddress == null || etStartDate == null || btnSave == null) {
//            Toast.makeText(this, "UI error. Please try again.", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        phoneFromIntent = getIntent().getStringExtra("phone");
//        if (phoneFromIntent != null && phoneFromIntent.length() >= 10) {
//            // Extract last 10 digits if phone includes country code
//            phoneFromIntent = phoneFromIntent.length() > 10 ? phoneFromIntent.substring(phoneFromIntent.length() - 10) : phoneFromIntent;
//            etContact.setText(phoneFromIntent);
//            etContact.setEnabled(false); // Prevent editing
//        } else {
//            Toast.makeText(this, "Invalid phone number received", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        etStartDate.setOnClickListener(v -> openDatePicker());
//
//        btnSave.setOnClickListener(v -> validateAndSave());
//    }
//
//    private void openDatePicker() {
//        Calendar calendar = Calendar.getInstance();
//        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
//            String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
//            etStartDate.setText(date);
//        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//        dialog.show();
//    }
//
//    private void validateAndSave() {
//        String owner = etOwner.getText().toString().trim();
//        String contact = etContact.getText().toString().trim();
//        String dairy = etDairyName.getText().toString().trim();
//        String address = etAddress.getText().toString().trim();
//        String startDate = etStartDate.getText().toString().trim();
//
//        if (owner.isEmpty() || owner.split("\\s+").length > 3) {
//            etOwner.setError("Enter full name (max 3 words)");
//            return;
//        }
//
//        if (contact.length() != 10 || !contact.matches("\\d+")) {
//            etContact.setError("Enter valid 10-digit number");
//            return;
//        }
//
//        if (dairy.isEmpty()) {
//            etDairyName.setError("Dairy name required");
//            return;
//        }
//
//        if (address.isEmpty()) {
//            etAddress.setError("Address required");
//            return;
//        }
//
//        if (startDate.isEmpty()) {
//            etStartDate.setError("Start date required");
//            return;
//        }
//
//        Map<String, Object> userData = new HashMap<>();
//        userData.put("ownerName", owner);
//        userData.put("contact", contact);
//        userData.put("dairyName", dairy);
//        userData.put("address", address);
//        userData.put("startDate", startDate);
//        userData.put("phone", "+91" + phoneFromIntent);
//        userData.put("timestamp", System.currentTimeMillis());
//
//        FirebaseDatabase.getInstance()
//                .getReference("Dairy")
//                .child("User")
//                .child(phoneFromIntent)
//                .child("DairyInfo")
//                .setValue(userData)
//                .addOnSuccessListener(unused -> {
//                    Toast.makeText(this, "Data saved!", Toast.LENGTH_SHORT).show();
//                    SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
//                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                    prefs.edit().putBoolean("hasCompletedDataEntry_" + uid, true).apply();
//                    startActivity(new Intent(this, DashboardActivity.class));
//                    finish();
//                }).addOnFailureListener(e ->
//                        Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show()
//                );
//    }
//}

package com.ss.smartdairy;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DataEntryActivity extends AppCompatActivity {

    private static final String TAG = "DataEntryActivity";
    TextInputEditText etOwner, etContact, etDairyName, etAddress, etStartDate;
    Button btnSave;
    String phoneFromIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_entry);

        etOwner = findViewById(R.id.et_owner_name);
        etContact = findViewById(R.id.et_contact);
        etDairyName = findViewById(R.id.et_dairy_name);
        etAddress = findViewById(R.id.et_address);
        etStartDate = findViewById(R.id.et_start_date);
        btnSave = findViewById(R.id.btn_save);

        if (etOwner == null || etContact == null || etDairyName == null || etAddress == null || etStartDate == null || btnSave == null) {
            Log.e(TAG, "UI initialization failed");
            Toast.makeText(this, "UI error. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        phoneFromIntent = getIntent().getStringExtra("phone");
        if (phoneFromIntent != null && phoneFromIntent.length() >= 10) {
            phoneFromIntent = phoneFromIntent.length() > 10 ? phoneFromIntent.substring(phoneFromIntent.length() - 10) : phoneFromIntent;
            Log.d(TAG, "Received phone number: " + phoneFromIntent);
            etContact.setText(phoneFromIntent);
            etContact.setEnabled(false);
        } else {
            Log.e(TAG, "Invalid phone number received: " + phoneFromIntent);
            Toast.makeText(this, "Invalid phone number received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etStartDate.setOnClickListener(v -> openDatePicker());

        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            etStartDate.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void validateAndSave() {
        String owner = etOwner.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String dairy = etDairyName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();

        if (owner.isEmpty() || owner.split("\\s+").length > 3) {
            etOwner.setError("Enter full name (max 3 words)");
            Log.w(TAG, "Invalid owner name: " + owner);
            return;
        }

        if (contact.length() != 10 || !contact.matches("\\d+")) {
            etContact.setError("Enter valid 10-digit number");
            Log.w(TAG, "Invalid contact: " + contact);
            return;
        }

        if (dairy.isEmpty()) {
            etDairyName.setError("Dairy name required");
            Log.w(TAG, "Dairy name empty");
            return;
        }

        if (address.isEmpty()) {
            etAddress.setError("Address required");
            Log.w(TAG, "Address empty");
            return;
        }

        if (startDate.isEmpty()) {
            etStartDate.setError("Start date required");
            Log.w(TAG, "Start date empty");
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("ownerName", owner);
        userData.put("contact", contact);
        userData.put("dairyName", dairy);
        userData.put("address", address);
        userData.put("startDate", startDate);
        userData.put("phone", "+91" + phoneFromIntent);
        userData.put("timestamp", System.currentTimeMillis());

        Log.d(TAG, "Saving data for phone: " + phoneFromIntent);
        FirebaseDatabase.getInstance()
                .getReference("Dairy")
                .child("User")
                .child(phoneFromIntent)
                .child("DairyInfo")
                .setValue(userData)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Successfully saved data for phone: " + phoneFromIntent);
                    Toast.makeText(this, "Data saved!", Toast.LENGTH_SHORT).show();
                    SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    prefs.edit().putBoolean("hasCompletedDataEntry_" + uid, true).apply();
                    startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save data for phone: " + phoneFromIntent, e);
                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}