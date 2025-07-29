package com.ss.smartdairy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewAllFarmersActivity extends AppCompatActivity {

    private static final String TAG = "ViewAllFarmersActivity";

    private RecyclerView recyclerViewFarmers;
    private FarmersAdapter farmersAdapter;
    private ProgressBar progressBar;
    private LinearLayout layoutNoFarmers;
    private Button btnAddFirstFarmer;
    private Toolbar toolbar;

    private DatabaseReference farmersRef;
    private String currentUserMobile;
    private List<Farmer> farmersList;
    private ValueEventListener farmersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_farmers);

        if (!initializeUser()) {
            return;
        }

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadAllFarmers();
    }

    private boolean initializeUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Log.w(TAG, "No user logged in");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        String phoneNumber = currentUser.getPhoneNumber();
        if (phoneNumber == null || !phoneNumber.startsWith("+91") || phoneNumber.length() != 13) {
            Log.w(TAG, "Invalid phone number: " + phoneNumber);
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        currentUserMobile = phoneNumber.substring(3);
        Log.d(TAG, "Current user mobile: " + currentUserMobile);

        farmersRef = FirebaseDatabase.getInstance()
                .getReference("Dairy/User")
                .child(currentUserMobile)
                .child("Farmers");

        return true;
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewFarmers = findViewById(R.id.recyclerViewFarmers);
        progressBar = findViewById(R.id.progressBar);
        layoutNoFarmers = findViewById(R.id.tvNoFarmers);
        btnAddFirstFarmer = findViewById(R.id.btnAddFirstFarmer);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("All Farmers");
        }

        farmersList = new ArrayList<>();
    }

    private void setupClickListeners() {
        // Add Farmer button click
        btnAddFirstFarmer.setOnClickListener(v -> {
            Intent intent = new Intent(ViewAllFarmersActivity.this, AddFarmersActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        farmersAdapter = new FarmersAdapter(farmersList, new FarmersAdapter.OnFarmerClickListener() {
            @Override
            public void onEditClick(Farmer farmer) {
                Intent intent = new Intent(ViewAllFarmersActivity.this, EditFarmersActivity.class);
                intent.putExtra("farmerMobile", farmer.getMobile());
                startActivity(intent);
            }

            @Override
            public void onCallClick(Farmer farmer) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(android.net.Uri.parse("tel:" + farmer.getMobile()));
                try {
                    startActivity(callIntent);
                } catch (Exception e) {
                    Toast.makeText(ViewAllFarmersActivity.this, "Unable to make call", Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerViewFarmers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFarmers.setAdapter(farmersAdapter);
    }

    private void loadAllFarmers() {
        showLoading(true);

        farmersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                farmersList.clear();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    showNoFarmers();
                    return;
                }

                for (DataSnapshot farmerSnapshot : snapshot.getChildren()) {
                    try {
                        Farmer farmer = createFarmerFromSnapshot(farmerSnapshot);
                        if (farmer != null) {
                            farmersList.add(farmer);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing farmer data", e);
                    }
                }

                if (farmersList.isEmpty()) {
                    showNoFarmers();
                } else {
                    showFarmers();
                }

                farmersAdapter.notifyDataSetChanged();
                Log.d(TAG, "Loaded " + farmersList.size() + " farmers");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Log.e(TAG, "Failed to load farmers", error.toException());
                Toast.makeText(ViewAllFarmersActivity.this,
                        "Failed to load farmers: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        farmersRef.addValueEventListener(farmersListener);
    }

    private Farmer createFarmerFromSnapshot(DataSnapshot snapshot) {
        try {
            String mobile = snapshot.getKey();
            String name = snapshot.child("name").getValue(String.class);
            String address = snapshot.child("address").getValue(String.class);
            String cowMilkCode = snapshot.child("cowMilkCode").getValue(String.class);
            String buffaloMilkCode = snapshot.child("buffaloMilkCode").getValue(String.class);
            Boolean hasCow = snapshot.child("hasCow").getValue(Boolean.class);
            Boolean hasBuffalo = snapshot.child("hasBuffalo").getValue(Boolean.class);

            if (mobile == null || name == null || name.trim().isEmpty()) {
                Log.w(TAG, "Invalid farmer data - missing mobile or name");
                return null;
            }

            return new Farmer(
                    mobile,
                    name.trim(),
                    address != null ? address.trim() : "",
                    cowMilkCode != null ? cowMilkCode.trim() : "",
                    buffaloMilkCode != null ? buffaloMilkCode.trim() : "",
                    hasCow != null ? hasCow : false,
                    hasBuffalo != null ? hasBuffalo : false
            );
        } catch (Exception e) {
            Log.e(TAG, "Error creating farmer from snapshot", e);
            return null;
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewFarmers.setVisibility(show ? View.GONE : View.VISIBLE);
        layoutNoFarmers.setVisibility(View.GONE);
    }

    private void showNoFarmers() {
        progressBar.setVisibility(View.GONE);
        recyclerViewFarmers.setVisibility(View.GONE);
        layoutNoFarmers.setVisibility(View.VISIBLE);
    }

    private void showFarmers() {
        progressBar.setVisibility(View.GONE);
        recyclerViewFarmers.setVisibility(View.VISIBLE);
        layoutNoFarmers.setVisibility(View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove Firebase listener to prevent memory leaks
        if (farmersRef != null && farmersListener != null) {
            farmersRef.removeEventListener(farmersListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from AddFarmersActivity
        if (farmersRef != null && farmersListener != null) {
            loadAllFarmers();
        }
    }
}
