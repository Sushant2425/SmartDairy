package com.ss.smartdairy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditFarmersActivity extends AppCompatActivity {

    private static final String TAG = "EditFarmersActivity";

    private DatabaseReference farmersRef;
    private EditText etMobileSearch, etFarmerName, etFarmerAddress, etCowMilkCode, etBuffaloMilkCode;
    private CheckBox cbHasCow, cbHasBuffalo; // Changed from Switch to CheckBox
    private Button btnSearchFarmer, btnUpdateFarmer;
    private LinearLayout layoutFarmerDetails, layoutSearchSection;
    private Toolbar toolbar;

    private String currentUserMobile = "";
    private String currentFarmerId = "";
    private FirebaseAuth mAuth;
    private String farmerMobileFromIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_farmers);

        mAuth = FirebaseAuth.getInstance();

        if (!getCurrentUserMobile()) {
            return;
        }

        checkForPassedFarmerMobile();
        initViews();
        setupClickListeners();
        setupCheckBoxListeners(); // Changed from Switch to CheckBox

        if (farmerMobileFromIntent != null) {
            autoFetchFarmerData();
        }
    }

    private void checkForPassedFarmerMobile() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("farmerMobile")) {
            farmerMobileFromIntent = intent.getStringExtra("farmerMobile");
            Log.d(TAG, "Farmer mobile received from intent: " + farmerMobileFromIntent);
        }
    }

    private boolean getCurrentUserMobile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        String phoneNumber = currentUser.getPhoneNumber();
        if (phoneNumber == null || !phoneNumber.startsWith("+91") || phoneNumber.length() != 13) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        currentUserMobile = phoneNumber.substring(3);

        farmersRef = FirebaseDatabase.getInstance()
                .getReference("Dairy/User")
                .child(currentUserMobile)
                .child("Farmers");

        return true;
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etMobileSearch = findViewById(R.id.etMobileSearch);
        etFarmerName = findViewById(R.id.etFarmerName);
        etFarmerAddress = findViewById(R.id.etFarmerAddress);
        etCowMilkCode = findViewById(R.id.etCowMilkCode);
        etBuffaloMilkCode = findViewById(R.id.etBuffaloMilkCode);
        cbHasCow = findViewById(R.id.cbHasCow); // Changed to CheckBox
        cbHasBuffalo = findViewById(R.id.cbHasBuffalo); // Changed to CheckBox
        btnSearchFarmer = findViewById(R.id.btnSearchFarmer);
        btnUpdateFarmer = findViewById(R.id.btnUpdateFarmer);
        layoutFarmerDetails = findViewById(R.id.layoutFarmerDetails);
        layoutSearchSection = findViewById(R.id.layoutSearchSection);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Farmer Details");
        }

        layoutFarmerDetails.setVisibility(View.GONE);
    }

    private void autoFetchFarmerData() {
        if (farmerMobileFromIntent == null || farmerMobileFromIntent.trim().isEmpty()) {
            return;
        }

        // Hide search section when auto-loading
        layoutSearchSection.setVisibility(View.GONE);

        etMobileSearch.setText(farmerMobileFromIntent);
        etMobileSearch.setEnabled(false);

        btnSearchFarmer.setText("Loading farmer data...");
        btnSearchFarmer.setEnabled(false);

        farmersRef.child(farmerMobileFromIntent)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        btnSearchFarmer.setText("Search Farmer");
                        btnSearchFarmer.setEnabled(true);

                        if (snapshot.exists()) {
                            currentFarmerId = farmerMobileFromIntent;
                            loadFarmerDataToForm(snapshot);

                            String farmerName = snapshot.child("name").getValue(String.class);
                            if (getSupportActionBar() != null && farmerName != null) {
                                getSupportActionBar().setTitle("Edit: " + farmerName);
                            }
                        } else {
                            Toast.makeText(EditFarmersActivity.this, "Farmer data not found", Toast.LENGTH_SHORT).show();
                            layoutSearchSection.setVisibility(View.VISIBLE);
                            etMobileSearch.setEnabled(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        btnSearchFarmer.setText("Search Farmer");
                        btnSearchFarmer.setEnabled(true);
                        Toast.makeText(EditFarmersActivity.this,
                                "Failed to load farmer data: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        layoutSearchSection.setVisibility(View.VISIBLE);
                        etMobileSearch.setEnabled(true);
                    }
                });
    }

    private void setupClickListeners() {
        btnSearchFarmer.setOnClickListener(v -> searchFarmerByMobile());
        btnUpdateFarmer.setOnClickListener(v -> updateFarmerData());
    }

    // Changed from Switch to CheckBox listeners
    private void setupCheckBoxListeners() {
        cbHasCow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etCowMilkCode.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) etCowMilkCode.setText("");
        });

        cbHasBuffalo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etBuffaloMilkCode.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) etBuffaloMilkCode.setText("");
        });
    }

    private void searchFarmerByMobile() {
        String mobileNumber = etMobileSearch.getText().toString().trim();

        if (mobileNumber.isEmpty()) {
            etMobileSearch.setError("Please enter mobile number");
            etMobileSearch.requestFocus();
            return;
        }

        if (mobileNumber.length() != 10 || !mobileNumber.matches("\\d+")) {
            etMobileSearch.setError("Please enter valid 10-digit mobile number");
            etMobileSearch.requestFocus();
            return;
        }

        btnSearchFarmer.setText("Searching...");
        btnSearchFarmer.setEnabled(false);

        farmersRef.child(mobileNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        btnSearchFarmer.setText("Search Farmer");
                        btnSearchFarmer.setEnabled(true);

                        if (snapshot.exists()) {
                            currentFarmerId = mobileNumber;
                            loadFarmerDataToForm(snapshot);
                        } else {
                            Toast.makeText(EditFarmersActivity.this,
                                    "No farmer found with mobile: " + mobileNumber,
                                    Toast.LENGTH_SHORT).show();
                            layoutFarmerDetails.setVisibility(View.GONE);
                            currentFarmerId = "";
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        btnSearchFarmer.setText("Search Farmer");
                        btnSearchFarmer.setEnabled(true);
                        Toast.makeText(EditFarmersActivity.this,
                                "Search failed: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadFarmerDataToForm(DataSnapshot farmerSnapshot) {
        try {
            String name = farmerSnapshot.child("name").getValue(String.class);
            String address = farmerSnapshot.child("address").getValue(String.class);
            String cowMilkCode = farmerSnapshot.child("cowMilkCode").getValue(String.class);
            String buffaloMilkCode = farmerSnapshot.child("buffaloMilkCode").getValue(String.class);
            Boolean hasCow = farmerSnapshot.child("hasCow").getValue(Boolean.class);
            Boolean hasBuffalo = farmerSnapshot.child("hasBuffalo").getValue(Boolean.class);

            etFarmerName.setText(name != null ? name : "");
            etFarmerAddress.setText(address != null ? address : "");
            etCowMilkCode.setText(cowMilkCode != null ? cowMilkCode : "");
            etBuffaloMilkCode.setText(buffaloMilkCode != null ? buffaloMilkCode : "");

            boolean cowChecked = (hasCow != null ? hasCow : false);
            boolean buffaloChecked = (hasBuffalo != null ? hasBuffalo : false);

            cbHasCow.setChecked(cowChecked);
            cbHasBuffalo.setChecked(buffaloChecked);

            etCowMilkCode.setVisibility(cowChecked ? View.VISIBLE : View.GONE);
            etBuffaloMilkCode.setVisibility(buffaloChecked ? View.VISIBLE : View.GONE);

            layoutFarmerDetails.setVisibility(View.VISIBLE);

            Toast.makeText(this, "Farmer data loaded successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error loading farmer data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            layoutFarmerDetails.setVisibility(View.GONE);
        }
    }

    private void updateFarmerData() {
        if (currentFarmerId.isEmpty()) {
            Toast.makeText(this, "Please search farmer first", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etFarmerName.getText().toString().trim();
        String address = etFarmerAddress.getText().toString().trim();
        String cowMilkCode = cbHasCow.isChecked() ? etCowMilkCode.getText().toString().trim() : "";
        String buffaloMilkCode = cbHasBuffalo.isChecked() ? etBuffaloMilkCode.getText().toString().trim() : "";

        if (!validateInputs(name, address, cowMilkCode, buffaloMilkCode)) {
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("address", address);
        updates.put("hasCow", cbHasCow.isChecked());
        updates.put("hasBuffalo", cbHasBuffalo.isChecked());
        updates.put("cowMilkCode", cowMilkCode);
        updates.put("buffaloMilkCode", buffaloMilkCode);
        updates.put("mobile", currentFarmerId);

        btnUpdateFarmer.setText("Updating...");
        btnUpdateFarmer.setEnabled(false);

        farmersRef.child(currentFarmerId)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    btnUpdateFarmer.setText("Update Farmer Data");
                    btnUpdateFarmer.setEnabled(true);
                    Toast.makeText(this, "Farmer data updated successfully!", Toast.LENGTH_SHORT).show();

                    // Reset form after successful update
                    clearForm();
                })
                .addOnFailureListener(e -> {
                    btnUpdateFarmer.setText("Update Farmer Data");
                    btnUpdateFarmer.setEnabled(true);
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInputs(String name, String address, String cowMilkCode, String buffaloMilkCode) {
        if (name.isEmpty()) {
            etFarmerName.setError("Enter name");
            etFarmerName.requestFocus();
            return false;
        }

        if (address.isEmpty()) {
            etFarmerAddress.setError("Enter address");
            etFarmerAddress.requestFocus();
            return false;
        }

        // At least one animal must be selected
        if (!cbHasCow.isChecked() && !cbHasBuffalo.isChecked()) {
            Toast.makeText(this, "Please select at least one animal (Cow or Buffalo)", Toast.LENGTH_LONG).show();
            return false;
        }

        if (cbHasCow.isChecked() && cowMilkCode.isEmpty()) {
            etCowMilkCode.setError("Enter cow milk code");
            etCowMilkCode.requestFocus();
            return false;
        }

        if (cbHasBuffalo.isChecked() && buffaloMilkCode.isEmpty()) {
            etBuffaloMilkCode.setError("Enter buffalo milk code");
            etBuffaloMilkCode.requestFocus();
            return false;
        }

        return true;
    }

    private void clearForm() {
        etMobileSearch.setText("");
        etMobileSearch.setEnabled(true);
        etFarmerName.setText("");
        etFarmerAddress.setText("");
        etCowMilkCode.setText("");
        etBuffaloMilkCode.setText("");
        cbHasCow.setChecked(false);
        cbHasBuffalo.setChecked(false);
        etCowMilkCode.setVisibility(View.GONE);
        etBuffaloMilkCode.setVisibility(View.GONE);
        layoutFarmerDetails.setVisibility(View.GONE);
        layoutSearchSection.setVisibility(View.VISIBLE);
        currentFarmerId = "";

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Farmer Details");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
