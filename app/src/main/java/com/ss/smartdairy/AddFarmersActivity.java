package com.ss.smartdairy;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AddFarmersActivity extends AppCompatActivity {

    EditText etFarmerName, etMobile, etAddress;
    EditText etCowMilkCode, etBuffaloMilkCode;
    Switch switchCow, switchBuffalo;
    Button btnAddFarmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_farmers);

        // Bind views
        etFarmerName = findViewById(R.id.etFarmerName);
        etMobile = findViewById(R.id.etMobile);
        etAddress = findViewById(R.id.etAddress);
        etCowMilkCode = findViewById(R.id.etCowMilkCode);
        etBuffaloMilkCode = findViewById(R.id.etBuffaloMilkCode);
        switchCow = findViewById(R.id.switchCow);
        switchBuffalo = findViewById(R.id.switchBuffalo);
        btnAddFarmer = findViewById(R.id.btnAddFarmer);

        // Handle cow switch visibility
        switchCow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etCowMilkCode.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Handle buffalo switch visibility
        switchBuffalo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etBuffaloMilkCode.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Save farmer data
        btnAddFarmer.setOnClickListener(v -> saveFarmerData());
    }

    private void saveFarmerData() {
        String name = etFarmerName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String cowMilkCode = switchCow.isChecked() ? etCowMilkCode.getText().toString().trim() : "";
        String buffaloMilkCode = switchBuffalo.isChecked() ? etBuffaloMilkCode.getText().toString().trim() : "";

        // Validate required fields
        if (name.isEmpty()) {
            etFarmerName.setError("Enter name");
            etFarmerName.requestFocus();
            return;
        }

        if (mobile.isEmpty() || mobile.length() != 10) {
            etMobile.setError("Enter valid 10-digit mobile number");
            etMobile.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            etAddress.setError("Enter address");
            etAddress.requestFocus();
            return;
        }

        if (switchCow.isChecked() && cowMilkCode.isEmpty()) {
            etCowMilkCode.setError("Enter cow milk code");
            etCowMilkCode.requestFocus();
            return;
        }

        if (switchBuffalo.isChecked() && buffaloMilkCode.isEmpty()) {
            etBuffaloMilkCode.setError("Enter buffalo milk code");
            etBuffaloMilkCode.requestFocus();
            return;
        }

        // Prepare data
        HashMap<String, Object> farmerData = new HashMap<>();
        farmerData.put("name", name);
        farmerData.put("mobile", mobile);
        farmerData.put("address", address);
        farmerData.put("cowMilkCode", cowMilkCode);
        farmerData.put("buffaloMilkCode", buffaloMilkCode);

        // Save to Firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Farmers");
        ref.child(mobile).setValue(farmerData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(AddFarmersActivity.this, "Farmer added successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddFarmersActivity.this, "Failed to add farmer: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
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
    }
}


