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
    import androidx.fragment.app.Fragment;
    
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;
    
    public class ProfileFragment extends Fragment {
    
        private TextView tvDairyName, tvOwnerName, tvPhone, tvStartDate,tvRateChart;
    
        public ProfileFragment() {}
    
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_profile, container, false);
    
            // Initialize views
            tvDairyName = view.findViewById(R.id.tvDairyName);
            tvOwnerName = view.findViewById(R.id.tvOwnerName);
            tvPhone = view.findViewById(R.id.tvPhone);
            tvStartDate = view.findViewById(R.id.tvStartDate);
            tvRateChart = view.findViewById(R.id.tvRateChart);

            Button btnLogout = view.findViewById(R.id.btnLogout);

            // Set click listener
            tvRateChart.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), RateChartActivity.class);
                startActivity(intent);
            });
    
            btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    
            loadDairyInfo();
    
            return view;


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
                    String dairyName = snapshot.child("dairyName").getValue(String.class);
                    String ownerName = snapshot.child("ownerName").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String startDate = snapshot.child("startDate").getValue(String.class);
    
                    tvDairyName.setText("Dairy Name: " + (dairyName != null ? dairyName : ""));
                    tvOwnerName.setText("Owner Name: " + (ownerName != null ? ownerName : ""));
                    tvPhone.setText("Phone: " + (phone != null ? phone : ""));
                    tvStartDate.setText("Start Date: " + (startDate != null ? startDate : ""));
                }
    
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(requireContext(), "Error loading profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    
        private void showLogoutConfirmation() {
    
            new AlertDialog.Builder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(requireActivity(), LoginActivity.class));
                        requireActivity().finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

    }
