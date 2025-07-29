package com.ss.smartdairy;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView tvDairyName, tvOwnerName, tvPhone;
    private CardView cardProfile, cardSettings;
    private TextView tvEditFarmers, tvRateChart, tvAddFarmers, tvViewFarmers;
    private Button btnLogout;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initializeViews(view);
        setupClickListeners();
        loadDairyInfo();

        return view;
    }

    private void initializeViews(View view) {
        // Profile Views
        tvDairyName = view.findViewById(R.id.tvDairyName);
        tvOwnerName = view.findViewById(R.id.tvOwnerName);
        tvPhone = view.findViewById(R.id.tvPhone);

        // Card Views
        cardProfile = view.findViewById(R.id.cardProfile);
        cardSettings = view.findViewById(R.id.cardSettings);

        // Settings Options
        tvRateChart = view.findViewById(R.id.tvRateChart);
        tvEditFarmers = view.findViewById(R.id.tvEditFarmers);
        tvAddFarmers = view.findViewById(R.id.tvAddFarmers);
        tvViewFarmers = view.findViewById(R.id.tvViewFarmers);

        // Logout Button
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void setupClickListeners() {
        tvRateChart.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), RateChartActivity.class);
            startActivity(intent);
        });

        tvViewFarmers.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ViewAllFarmersActivity.class);
            startActivity(intent);
        });

        tvEditFarmers.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditFarmersActivity.class);
            startActivity(intent);
        });

        tvAddFarmers.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddFarmersActivity.class);
            startActivity(intent);
        });

        // REMOVE THIS DUPLICATE CLICK LISTENER:
    /*
    tvViewFarmers.setOnClickListener(v -> {
        // Intent intent = new Intent(requireContext(), ViewFarmersActivity.class);
        // startActivity(intent);
        Toast.makeText(requireContext(), "View Farmers - Coming Soon", Toast.LENGTH_SHORT).show();
    });
    */

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }


    private void loadDairyInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getPhoneNumber() == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String adminMobile = user.getPhoneNumber().replace("+91", "");

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Dairy").child("User").child(adminMobile).child("DairyInfo");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null) return; // Fragment not attached

                String dairyName = snapshot.child("dairyName").getValue(String.class);
                String ownerName = snapshot.child("ownerName").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);

                // Set data with fallback values
                tvDairyName.setText(dairyName != null && !dairyName.isEmpty() ? dairyName : "Smart Dairy");
                tvOwnerName.setText(ownerName != null && !ownerName.isEmpty() ? ownerName : "Dairy Owner");
                tvPhone.setText(phone != null && !phone.isEmpty() ? phone : adminMobile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showLogoutConfirmation() {
        if (getContext() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Logout", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
