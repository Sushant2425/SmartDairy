//package com.ss.smartdairy;
//
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Switch;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import java.util.HashMap;
//
//public class AddFarmersActivity extends AppCompatActivity {
//
//    EditText etFarmerName, etMobile, etAddress;
//    EditText etCowMilkCode, etBuffaloMilkCode;
//    Switch switchCow, switchBuffalo;
//    Button btnAddFarmer;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_farmers);
//
//        // Bind views
//        etFarmerName = findViewById(R.id.etFarmerName);
//        etMobile = findViewById(R.id.etMobile);
//        etAddress = findViewById(R.id.etAddress);
//        etCowMilkCode = findViewById(R.id.etCowMilkCode);
//        etBuffaloMilkCode = findViewById(R.id.etBuffaloMilkCode);
//        switchCow = findViewById(R.id.switchCow);
//        switchBuffalo = findViewById(R.id.switchBuffalo);
//        btnAddFarmer = findViewById(R.id.btnAddFarmer);
//
//        // Handle cow switch visibility
//        switchCow.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            etCowMilkCode.setVisibility(isChecked ? View.VISIBLE : View.GONE);
//        });
//
//        // Handle buffalo switch visibility
//        switchBuffalo.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            etBuffaloMilkCode.setVisibility(isChecked ? View.VISIBLE : View.GONE);
//        });
//
//        // Save farmer data
//        btnAddFarmer.setOnClickListener(v -> saveFarmerData());
//    }
//
//    private void saveFarmerData() {
//        String name = etFarmerName.getText().toString().trim();
//        String mobile = etMobile.getText().toString().trim();
//        String address = etAddress.getText().toString().trim();
//        String cowMilkCode = switchCow.isChecked() ? etCowMilkCode.getText().toString().trim() : "";
//        String buffaloMilkCode = switchBuffalo.isChecked() ? etBuffaloMilkCode.getText().toString().trim() : "";
//
//        // Validate required fields
//        if (name.isEmpty()) {
//            etFarmerName.setError("Enter name");
//            etFarmerName.requestFocus();
//            return;
//        }
//
//        if (mobile.isEmpty() || mobile.length() != 10) {
//            etMobile.setError("Enter valid 10-digit mobile number");
//            etMobile.requestFocus();
//            return;
//        }
//
//        if (address.isEmpty()) {
//            etAddress.setError("Enter address");
//            etAddress.requestFocus();
//            return;
//        }
//
//        if (switchCow.isChecked() && cowMilkCode.isEmpty()) {
//            etCowMilkCode.setError("Enter cow milk code");
//            etCowMilkCode.requestFocus();
//            return;
//        }
//
//        if (switchBuffalo.isChecked() && buffaloMilkCode.isEmpty()) {
//            etBuffaloMilkCode.setError("Enter buffalo milk code");
//            etBuffaloMilkCode.requestFocus();
//            return;
//        }
//
//        // Prepare data
//        HashMap<String, Object> farmerData = new HashMap<>();
//        farmerData.put("name", name);
//        farmerData.put("mobile", mobile);
//        farmerData.put("address", address);
//        farmerData.put("cowMilkCode", cowMilkCode);
//        farmerData.put("buffaloMilkCode", buffaloMilkCode);
//
//        // Save to Firebase
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Farmers");
//        ref.child(mobile).setValue(farmerData)
//                .addOnSuccessListener(unused -> {
//                    Toast.makeText(AddFarmersActivity.this, "Farmer added successfully", Toast.LENGTH_SHORT).show();
//                    clearFields();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(AddFarmersActivity.this, "Failed to add farmer: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                });
//    }
//
//    private void clearFields() {
//        etFarmerName.setText("");
//        etMobile.setText("");
//        etAddress.setText("");
//        etCowMilkCode.setText("");
//        etBuffaloMilkCode.setText("");
//        switchCow.setChecked(false);
//        switchBuffalo.setChecked(false);
//        etCowMilkCode.setVisibility(View.GONE);
//        etBuffaloMilkCode.setVisibility(View.GONE);
//    }
//}
//
//
package com.ss.smartdairy;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddFarmersActivity extends AppCompatActivity {

    private static final String TAG = "AddFarmersActivity";
    private EditText etFarmerName, etMobile, etAddress;
    private EditText etCowMilkCode, etBuffaloMilkCode;
    private Switch switchCow, switchBuffalo;
    private Button btnAddFarmer;

    private DatabaseReference farmersRef;
    private String dairyOwnerMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_farmers);

        // Initialize Firebase references
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getPhoneNumber() == null) {
            Toast.makeText(this, "Not authenticated properly", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Extract dairy owner's phone number (without +91)
        String phoneWithCode = currentUser.getPhoneNumber();
        dairyOwnerMobile = phoneWithCode.startsWith("+91") ? phoneWithCode.substring(3) : phoneWithCode;

        // Initialize database reference under the dairy owner's account
        farmersRef = FirebaseDatabase.getInstance()
                .getReference("Dairy")
                .child("User")
                .child(dairyOwnerMobile)
                .child("Farmers");

        initializeViews();
        setupSwitchListeners();
    }

    private void initializeViews() {
        etFarmerName = findViewById(R.id.etFarmerName);
        etMobile = findViewById(R.id.etMobile);
        etAddress = findViewById(R.id.etAddress);
        etCowMilkCode = findViewById(R.id.etCowMilkCode);
        etBuffaloMilkCode = findViewById(R.id.etBuffaloMilkCode);
        switchCow = findViewById(R.id.switchCow);
        switchBuffalo = findViewById(R.id.switchBuffalo);
        btnAddFarmer = findViewById(R.id.btnAddFarmer);

        btnAddFarmer.setOnClickListener(v -> validateAndSaveFarmerData());
    }

    private void setupSwitchListeners() {
        switchCow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etCowMilkCode.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) etCowMilkCode.setText("");
        });

        switchBuffalo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etBuffaloMilkCode.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) etBuffaloMilkCode.setText("");
        });
    }

    private void validateAndSaveFarmerData() {
        String name = etFarmerName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String cowMilkCode = switchCow.isChecked() ? etCowMilkCode.getText().toString().trim() : "";
        String buffaloMilkCode = switchBuffalo.isChecked() ? etBuffaloMilkCode.getText().toString().trim() : "";

        if (!validateInputs(name, mobile, address, cowMilkCode, buffaloMilkCode)) {
            return;
        }

        // Create farmer data
        Map<String, Object> farmerData = new HashMap<>();
        farmerData.put("name", name);
        farmerData.put("mobile", mobile);
        farmerData.put("address", address);
        farmerData.put("hasCow", switchCow.isChecked());
        farmerData.put("hasBuffalo", switchBuffalo.isChecked());
        farmerData.put("cowMilkCode", cowMilkCode);
        farmerData.put("buffaloMilkCode", buffaloMilkCode);
//        farmerData.put("timestamp", System.currentTimeMillis());
//        farmerData.put("dairyOwnerMobile", dairyOwnerMobile); // Reference to owner

        // Save under the dairy owner's account using farmer's mobile as key
        farmersRef.child(mobile).setValue(farmerData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Farmer added successfully under owner: " + dairyOwnerMobile);
                    Toast.makeText(this, "Farmer added successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add farmer: " + e.getMessage());
                    Toast.makeText(this, "Failed to add farmer: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private boolean validateInputs(String name, String mobile, String address,
                                   String cowMilkCode, String buffaloMilkCode) {
        if (name.isEmpty()) {
            etFarmerName.setError("Enter name");
            etFarmerName.requestFocus();
            return false;
        }

        if (mobile.isEmpty() || mobile.length() != 10 || !mobile.matches("\\d+")) {
            etMobile.setError("Enter valid 10-digit mobile number");
            etMobile.requestFocus();
            return false;
        }

        if (address.isEmpty()) {
            etAddress.setError("Enter address");
            etAddress.requestFocus();
            return false;
        }

        if (switchCow.isChecked() && cowMilkCode.isEmpty()) {
            etCowMilkCode.setError("Enter cow milk code");
            etCowMilkCode.requestFocus();
            return false;
        }

        if (switchBuffalo.isChecked() && buffaloMilkCode.isEmpty()) {
            etBuffaloMilkCode.setError("Enter buffalo milk code");
            etBuffaloMilkCode.requestFocus();
            return false;
        }

        return true;
    }

    private void clearFields() {
        etFarmerName.setText("");
        etMobile.setText("");
        etAddress.setText("");
        etCowMilkCode.setText("");
        etBuffaloMilkCode.setText("");
        switchCow.setChecked(false);

        switchBuffalo.setChecked(false);
        etCowMilkCode.setVisibility(View.GONE);
        etBuffaloMilkCode.setVisibility(View.GONE);
        etFarmerName.requestFocus();
    }
}

