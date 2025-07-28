package com.ss.smartdairy;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";
    LinearLayout navHome, navReports, navProfile;
    TextView tvDairyName;
    ViewPager2 viewPager;
    DashboardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initializeViews();
        setupViewPager();
        setupTabClickListeners();
        loadDairyInfo();
    }

    private void initializeViews() {
        navHome = findViewById(R.id.nav_home);
        navReports = findViewById(R.id.nav_reports);
        navProfile = findViewById(R.id.nav_profile);
        tvDairyName = findViewById(R.id.tvDairyName);
        viewPager = findViewById(R.id.viewPager);

        Log.d(TAG, "Views initialized");
    }

    private void setupViewPager() {
        adapter = new DashboardAdapter(this);
        viewPager.setAdapter(adapter);

        // Disable user swipe input
        viewPager.setUserInputEnabled(false);

        // Optional: Also disable nested scrolling to prevent any scroll conflicts
        viewPager.setNestedScrollingEnabled(false);

        Log.d(TAG, "ViewPager setup with swipe disabled");
    }

    private void setupTabClickListeners() {
        // Tab click change fragment
        navHome.setOnClickListener(v -> {
            Log.d(TAG, "Home tab clicked");
            viewPager.setCurrentItem(0, false); // false = no smooth scroll animation
            updateTabSelection(0);
        });

        navReports.setOnClickListener(v -> {
            Log.d(TAG, "Reports tab clicked");
            viewPager.setCurrentItem(1, false);
            updateTabSelection(1);
        });

        navProfile.setOnClickListener(v -> {
            Log.d(TAG, "Profile tab clicked");
            viewPager.setCurrentItem(2, false);
            updateTabSelection(2);
        });

        // Since swipe is disabled, we can remove the page change callback
        // But keeping it for potential future use or debugging
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d(TAG, "Page changed to position: " + position);
                updateTabSelection(position);
            }
        });
    }

    private void updateTabSelection(int position) {
        // Reset all tab backgrounds/colors
        resetTabStyles();

        // Highlight selected tab
        switch (position) {
            case 0:
                // Highlight home tab
                navHome.setSelected(true);
                Log.d(TAG, "Home tab selected");
                break;
            case 1:
                // Highlight reports tab
                navReports.setSelected(true);
                Log.d(TAG, "Reports tab selected");
                break;
            case 2:
                // Highlight profile tab
                navProfile.setSelected(true);
                Log.d(TAG, "Profile tab selected");
                break;
        }
    }

    private void resetTabStyles() {
        navHome.setSelected(false);
        navReports.setSelected(false);
        navProfile.setSelected(false);
    }

    private void loadDairyInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "No user logged in");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String mobile = user.getPhoneNumber();
        if (mobile == null || mobile.isEmpty()) {
            Log.e(TAG, "Phone number is null or empty");
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mobile.startsWith("+91")) {
            mobile = mobile.substring(3);
        }

        Log.d(TAG, "Loading dairy info for mobile: " + mobile);

        DatabaseReference dairyRef = FirebaseDatabase.getInstance()
                .getReference("Dairy").child("User").child(mobile).child("DairyInfo");

        dairyRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String dairyName = snapshot.child("dairyName").getValue(String.class);
                String displayName = dairyName != null ? dairyName : "Smart Dairy";
                tvDairyName.setText(displayName);
                Log.d(TAG, "Dairy name loaded: " + displayName);
            } else {
                tvDairyName.setText("Smart Dairy");
                Log.d(TAG, "No dairy info found, using default name");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading dairy info: " + e.getMessage());
            Toast.makeText(this, "Error loading dairy info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            tvDairyName.setText("Smart Dairy"); // Fallback
        });
    }
}
