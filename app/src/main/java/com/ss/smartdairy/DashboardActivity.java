//package com.ss.smartdairy;
//
//import android.annotation.SuppressLint;
//import android.os.Bundle;
//import android.widget.LinearLayout;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import androidx.fragment.app.Fragment;
//
//public class DashboardActivity extends AppCompatActivity {
//
//
//        LinearLayout navHome, navReports, navProfile;
//
//
//        @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            setContentView(R.layout.activity_dashboard); // your layout
//
//            navHome = findViewById(R.id.nav_home);
//            navReports = findViewById(R.id.nav_reports);
//            navProfile = findViewById(R.id.nav_profile);
//
//            // Default fragment
//            loadFragment(new HomeFragment());
//
//            navHome.setOnClickListener(v -> loadFragment(new HomeFragment()));
//            navReports.setOnClickListener(v -> loadFragment(new ReportsFragment()));
//            navProfile.setOnClickListener(v -> loadFragment(new ProfileFragment()));
//        }
//
//        private void loadFragment(Fragment fragment) {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.frame_container, fragment)
//                    .commit();
//        }
//    }


package com.ss.smartdairy;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DashboardActivity extends AppCompatActivity {

    LinearLayout navHome, navReports, navProfile;
    TextView tvDairyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        navHome = findViewById(R.id.nav_home);
        navReports = findViewById(R.id.nav_reports);
        navProfile = findViewById(R.id.nav_profile);
        tvDairyName = findViewById(R.id.tvDairyName);  // ✅ Make sure this exists in your layout

        if (navHome == null || navReports == null || navProfile == null || tvDairyName == null) {
            Toast.makeText(this, "Layout not initialized properly", Toast.LENGTH_LONG).show();
            return;
        }

        loadFragment(new HomeFragment());

        navHome.setOnClickListener(v -> loadFragment(new HomeFragment()));
        navReports.setOnClickListener(v -> loadFragment(new ReportsFragment()));
        navProfile.setOnClickListener(v -> loadFragment(new ProfileFragment()));

        loadDairyInfo(); // ✅ Load Firebase Dairy info into the top TextView
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }

    private void loadDairyInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("FirebaseError", "No user logged in");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String mobile = user.getPhoneNumber();
        if (mobile == null || mobile.isEmpty()) {
            Log.e("FirebaseError", "Phone number is null or empty");
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mobile.startsWith("+91")) {
            mobile = mobile.substring(3); // Remove +91
        }
        Log.d("Firebase", "Mobile number: " + mobile);

        DatabaseReference dairyRef = FirebaseDatabase.getInstance()
                .getReference("Dairy").child("Users").child(mobile).child("DairyInfo");
        Log.d("Firebase", "Database path: " + dairyRef.toString());

        dairyRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Log.d("FirebaseData", "Data: " + snapshot.getValue().toString());
                String dairyName = snapshot.child("dairyName").getValue(String.class);
                tvDairyName.setText(dairyName != null ? dairyName : "Smart Dairy");
            } else {
                Log.d("FirebaseData", "No data found at path");
                tvDairyName.setText("Smart Dairy");
            }
        }).addOnFailureListener(e -> {
            Log.e("FirebaseError", "Error: " + e.getMessage());
            Toast.makeText(this, "Error loading dairy info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
