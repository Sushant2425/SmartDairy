//package com.ss.smartdairy;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.viewpager2.widget.ViewPager2;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//
//public class HomeFragment extends Fragment {
//
//    private static final String TAG = "HomeFragment";
//    private ViewPager2 viewPagerMilkOverview;
//    private MilkOverviewAdapter adapter;
//    private String currentUserPhone;
//    private List<MilkSummaryModel> milkSummaries;
//    private View rootView; // Store root view reference
//
//    public HomeFragment() {}
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//
//        rootView = inflater.inflate(R.layout.fragment_home, container, false);
//        Log.d(TAG, "HomeFragment view created");
//
//        initializeViews(rootView);
//        setupFirebaseAuth();
//        setupViewPager();
//        setupDynamicContent(rootView); // Pass the root view
//        fetchMilkData();
//
//        return rootView;
//    }
//
//    private void setupDynamicContent(View view) {
//        // Find views from the correct parent (root view, not ViewPager)
//        TextView tvDynamicGreeting = view.findViewById(R.id.tvDynamicGreeting);
//        TextView tvMainTitle = view.findViewById(R.id.tvMainTitle);
//        TextView tvSubTitle = view.findViewById(R.id.tvSubTitle);
//
//        // Add null checks to prevent crashes
//        if (tvDynamicGreeting == null || tvMainTitle == null || tvSubTitle == null) {
//            Log.w(TAG, "Dynamic content TextViews not found in layout. Make sure your layout has the required IDs.");
//            return;
//        }
//
//        Calendar cal = Calendar.getInstance();
//        int hour = cal.get(Calendar.HOUR_OF_DAY);
//
//        String greeting, title, subtitle;
//
//        if (hour >= 5 && hour < 12) {
//            greeting = "Good Morning ☀️";
//            title = "Ready for Today's Collection";
//            subtitle = "Start your morning milk collection";
//        } else if (hour >= 12 && hour < 17) {
//            greeting = "Good Afternoon 🌤️";
//            title = "Afternoon Collection Time";
//            subtitle = "Continue with your daily collection";
//        } else if (hour >= 17 && hour < 21) {
//            greeting = "Good Evening 🌅";
//            title = "Evening Collection Ready";
//            subtitle = "Time for evening milk collection";
//        } else {
//            greeting = "Good Evening 🌙";
//            title = "Smart Dairy Dashboard";
//            subtitle = "Plan tomorrow's collection";
//        }
//
//        tvDynamicGreeting.setText(greeting);
//        tvMainTitle.setText(title);
//        tvSubTitle.setText(subtitle);
//
//        Log.d(TAG, "Dynamic content updated successfully");
//    }
//
//    private void initializeViews(View view) {
//        // Initialize ViewPager first
//        viewPagerMilkOverview = view.findViewById(R.id.viewPagerMilkOverview);
//
//        // Click handlers with null checks
//        View cardPendingFat = view.findViewById(R.id.cardPendingFat);
//        if (cardPendingFat != null) {
//            cardPendingFat.setOnClickListener(v -> {
//                Intent i = new Intent(requireContext(), PendingFatEntryActivity.class);
//                startActivity(i);
//            });
//        } else {
//            Log.w(TAG, "cardPendingFat not found in layout");
//        }
//
//        View btnAddFarmer = view.findViewById(R.id.btnAddFarmer);
//        if (btnAddFarmer != null) {
//            btnAddFarmer.setOnClickListener(v -> {
//                Intent intent = new Intent(getActivity(), AddFarmersActivity.class);
//                startActivity(intent);
//            });
//        } else {
//            Log.w(TAG, "btnAddFarmer not found in layout");
//        }
//
//        View btnMilkCollection = view.findViewById(R.id.btnMilkCollection);
//        if (btnMilkCollection != null) {
//            btnMilkCollection.setOnClickListener(v -> {
//                Intent intent = new Intent(getActivity(), MilkCollectionActivity.class);
//                startActivity(intent);
//            });
//        } else {
//            Log.w(TAG, "btnMilkCollection not found in layout");
//        }
//
//        // Set current date and session with null checks
//        TextView tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
//        TextView tvCurrentSession = view.findViewById(R.id.tvCurrentSession);
//
//        if (tvCurrentDate != null) {
//            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
//            tvCurrentDate.setText(dateFormat.format(new Date()));
//        } else {
//            Log.w(TAG, "tvCurrentDate not found in layout");
//        }
//
//        if (tvCurrentSession != null) {
//            Calendar cal = Calendar.getInstance();
//            String session = cal.get(Calendar.HOUR_OF_DAY) < 12 ? "Morning" : "Evening";
//            tvCurrentSession.setText(session + " Session");
//        } else {
//            Log.w(TAG, "tvCurrentSession not found in layout");
//        }
//
//        Log.d(TAG, "Views initialized successfully");
//    }
//
//    private void setupFirebaseAuth() {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user == null || user.getPhoneNumber() == null) {
//            Log.e(TAG, "User not authenticated or phone number null");
//            if (getContext() != null) {
//                Toast.makeText(getContext(), "प्रयोक्ता लॉगिन केलेला नाही", Toast.LENGTH_SHORT).show();
//            }
//            return;
//        }
//        currentUserPhone = user.getPhoneNumber().replace("+91", "");
//        Log.d(TAG, "Current user phone: " + currentUserPhone);
//    }
//
//    private void setupViewPager() {
//        if (viewPagerMilkOverview == null) {
//            Log.e(TAG, "ViewPager not initialized");
//            return;
//        }
//
//        milkSummaries = new ArrayList<>();
//        adapter = new MilkOverviewAdapter(milkSummaries);
//        viewPagerMilkOverview.setAdapter(adapter);
//
//        // Configure ViewPager2
//        viewPagerMilkOverview.setOffscreenPageLimit(2);
//        viewPagerMilkOverview.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
//
//        // Disable nested scrolling
//        if (viewPagerMilkOverview.getChildCount() > 0) {
//            viewPagerMilkOverview.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
//        }
//
//        // Touch handling
//        viewPagerMilkOverview.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                v.getParent().requestDisallowInterceptTouchEvent(true);
//                return false;
//            }
//        });
//
//        // Page change callback
//        viewPagerMilkOverview.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                super.onPageSelected(position);
//                if (position < milkSummaries.size()) {
//                    String dateLabel = getDateLabel(position);
//                    Log.d(TAG, "Page selected: " + position + " (" + dateLabel + ")");
//
//                    // Optional: Show toast for current selection
//                    if (getContext() != null) {
//                        Toast.makeText(getContext(), dateLabel + " selected", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//                super.onPageScrollStateChanged(state);
//                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
//                    if (getParentFragment() != null && getParentFragment().getView() != null) {
//                        getParentFragment().getView().getParent().requestDisallowInterceptTouchEvent(true);
//                    }
//                }
//            }
//        });
//
//        Log.d(TAG, "ViewPager setup completed");
//    }
//
//    private String getDateLabel(int position) {
//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.DAY_OF_MONTH, -(3 - position));
//
//        Calendar today = Calendar.getInstance();
//        Calendar yesterday = Calendar.getInstance();
//        yesterday.add(Calendar.DAY_OF_MONTH, -1);
//
//        if (isSameDay(cal, today)) {
//            return "Today";
//        } else if (isSameDay(cal, yesterday)) {
//            return "Yesterday";
//        } else {
//            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
//            return dayFormat.format(cal.getTime());
//        }
//    }
//
//    private boolean isSameDay(Calendar cal1, Calendar cal2) {
//        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
//                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
//    }
//
//    private void fetchMilkData() {
//        if (currentUserPhone == null) {
//            Log.e(TAG, "Cannot fetch data - user phone is null");
//            return;
//        }
//
//        Log.d(TAG, "Starting to fetch milk data for last 4 days");
//
//        DatabaseReference milkRef = FirebaseDatabase.getInstance()
//                .getReference("Dairy/User").child(currentUserPhone).child("MilkCollection");
//
//        milkRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Log.d(TAG, "Firebase data received, snapshot exists: " + snapshot.exists());
//                processMilkData(snapshot);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Firebase data fetch cancelled: " + error.getMessage());
//                if (getContext() != null) {
//                    Toast.makeText(getContext(), "डेटा मिळवताना त्रुटी: " + error.getMessage(),
//                            Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }
//
//    // Rest of your existing methods remain the same...
//    private void processMilkData(DataSnapshot snapshot) {
//        Map<String, Map<String, DayMilkData>> dateWiseSessionData = new HashMap<>();
//
//        for (DataSnapshot farmerSnapshot : snapshot.getChildren()) {
//            String farmerPhone = farmerSnapshot.getKey();
//            Log.d(TAG, "Processing data for farmer: " + farmerPhone);
//
//            for (DataSnapshot dateSnapshot : farmerSnapshot.getChildren()) {
//                String date = dateSnapshot.getKey();
//                Log.d(TAG, "Processing date: " + date);
//
//                if (!dateWiseSessionData.containsKey(date)) {
//                    dateWiseSessionData.put(date, new HashMap<>());
//                }
//
//                Map<String, DayMilkData> sessionDataMap = dateWiseSessionData.get(date);
//
//                for (DataSnapshot sessionSnapshot : dateSnapshot.getChildren()) {
//                    String session = sessionSnapshot.getKey();
//                    Log.d(TAG, "Processing session: " + session + " for date: " + date);
//
//                    if (!sessionDataMap.containsKey(session)) {
//                        sessionDataMap.put(session, new DayMilkData());
//                    }
//
//                    DayMilkData dayData = sessionDataMap.get(session);
//
//                    for (DataSnapshot milkTypeSnapshot : sessionSnapshot.getChildren()) {
//                        String milkType = milkTypeSnapshot.getKey();
//                        Double quantity = milkTypeSnapshot.child("quantity").getValue(Double.class);
//
//                        if (quantity != null) {
//                            Log.d(TAG, "Found " + milkType + " milk: " + quantity + "L on " + date + " " + session);
//
//                            if ("cow".equalsIgnoreCase(milkType)) {
//                                dayData.addCowMilk(quantity);
//                            } else if ("buffalo".equalsIgnoreCase(milkType)) {
//                                dayData.addBuffaloMilk(quantity);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        Log.d(TAG, "Processed data for " + dateWiseSessionData.size() + " dates");
//        createMilkSummaryListWithSessions(dateWiseSessionData);
//    }
//
//    private void createMilkSummaryListWithSessions(Map<String, Map<String, DayMilkData>> dateWiseSessionData) {
//        List<MilkSummaryModel> summaries = new ArrayList<>();
//
//        Calendar calendar = Calendar.getInstance();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
//
//        List<String> dates = new ArrayList<>();
//        List<String> displayDates = new ArrayList<>();
//
//        for (int i = 0; i < 4; i++) {
//            String date = dateFormat.format(calendar.getTime());
//            String displayDate = displayFormat.format(calendar.getTime());
//
//            dates.add(date);
//            displayDates.add(displayDate);
//            calendar.add(Calendar.DAY_OF_MONTH, -1);
//        }
//
//        for (int i = dates.size() - 1; i >= 0; i--) {
//            String date = dates.get(i);
//            String displayDate = displayDates.get(i);
//
//            MilkSummaryModel summary = new MilkSummaryModel(displayDate);
//
//            if (dateWiseSessionData.containsKey(date)) {
//                Map<String, DayMilkData> sessionData = dateWiseSessionData.get(date);
//
//                for (Map.Entry<String, DayMilkData> entry : sessionData.entrySet()) {
//                    String session = entry.getKey();
//                    DayMilkData dayData = entry.getValue();
//
//                    double cowMilk = dayData.getTotalCowMilk();
//                    double buffaloMilk = dayData.getTotalBuffaloMilk();
//
//                    summary.addSessionData(session, cowMilk, buffaloMilk);
//                }
//            }
//
//            summaries.add(summary);
//        }
//
//        milkSummaries.clear();
//        milkSummaries.addAll(summaries);
//
//        if (adapter != null) {
//            adapter.updateData(milkSummaries);
//        }
//
//        if (summaries.size() > 0 && viewPagerMilkOverview != null) {
//            int todayPosition = summaries.size() - 1;
//            viewPagerMilkOverview.setCurrentItem(todayPosition, false);
//        }
//
//        Log.d(TAG, "Milk summary list created with " + summaries.size() + " items");
//    }
//
//    // Helper class
//    private static class DayMilkData {
//        private double totalCowMilk = 0.0;
//        private double totalBuffaloMilk = 0.0;
//
//        public void addCowMilk(double quantity) {
//            totalCowMilk += quantity;
//        }
//
//        public void addBuffaloMilk(double quantity) {
//            totalBuffaloMilk += quantity;
//        }
//
//        public double getTotalCowMilk() {
//            return totalCowMilk;
//        }
//
//        public double getTotalBuffaloMilk() {
//            return totalBuffaloMilk;
//        }
//
//        public double getTotalMilk() {
//            return totalCowMilk + totalBuffaloMilk;
//        }
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        rootView = null;
//        Log.d(TAG, "HomeFragment view destroyed");
//    }
//}



package com.ss.smartdairy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private ViewPager2 viewPagerMilkOverview;
    private MilkOverviewAdapter adapter;
    private String currentUserPhone;
    private List<MilkSummaryModel> milkSummaries;
    private View rootView; // Store root view reference

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        Log.d(TAG, "HomeFragment view created");

        initializeViews(rootView);
        setupFirebaseAuth();
        setupViewPager();
        setupDynamicContent(rootView); // Pass the root view
        fetchMilkData();

        return rootView;
    }

    private void setupDynamicContent(View view) {
        // Find views from the correct parent (root view, not ViewPager)
        TextView tvDynamicGreeting = view.findViewById(R.id.tvDynamicGreeting);
        TextView tvMainTitle = view.findViewById(R.id.tvMainTitle);
        TextView tvSubTitle = view.findViewById(R.id.tvSubTitle);

        // Add null checks to prevent crashes
        if (tvDynamicGreeting == null || tvMainTitle == null || tvSubTitle == null) {
            Log.w(TAG, "Dynamic content TextViews not found in layout. Make sure your layout has the required IDs.");
            return;
        }

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        String greeting, title, subtitle;

        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning ☀️";
            title = "Ready for Today's Collection";
            subtitle = "Start your morning milk collection";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon 🌤️";
            title = "Afternoon Collection Time";
            subtitle = "Continue with your daily collection";
        } else if (hour >= 17 && hour < 21) {
            greeting = "Good Evening 🌅";
            title = "Evening Collection Ready";
            subtitle = "Time for evening milk collection";
        } else {
            greeting = "Good Evening 🌙";
            title = "Smart Dairy Dashboard";
            subtitle = "Plan tomorrow's collection";
        }

        tvDynamicGreeting.setText(greeting);
        tvMainTitle.setText(title);
        tvSubTitle.setText(subtitle);

        Log.d(TAG, "Dynamic content updated successfully");
    }

    private void initializeViews(View view) {
        // Initialize ViewPager first
        viewPagerMilkOverview = view.findViewById(R.id.viewPagerMilkOverview);

        // Click handlers with null checks
        View cardPendingFat = view.findViewById(R.id.cardPendingFat);
        if (cardPendingFat != null) {
            cardPendingFat.setOnClickListener(v -> {
                Intent i = new Intent(requireContext(), PendingFatEntryActivity.class);
                startActivity(i);
            });
        } else {
            Log.w(TAG, "cardPendingFat not found in layout");
        }

        View btnAddFarmer = view.findViewById(R.id.btnAddFarmer);
        if (btnAddFarmer != null) {
            btnAddFarmer.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddFarmersActivity.class);
                startActivity(intent);
            });
        } else {
            Log.w(TAG, "btnAddFarmer not found in layout");
        }

        View btnMilkCollection = view.findViewById(R.id.btnMilkCollection);
        if (btnMilkCollection != null) {
            btnMilkCollection.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), MilkCollectionActivity.class);
                startActivity(intent);
            });
        } else {
            Log.w(TAG, "btnMilkCollection not found in layout");
        }

        // Set current date and session with null checks
        TextView tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
        TextView tvCurrentSession = view.findViewById(R.id.tvCurrentSession);

        if (tvCurrentDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            tvCurrentDate.setText(dateFormat.format(new Date()));
        } else {
            Log.w(TAG, "tvCurrentDate not found in layout");
        }

        if (tvCurrentSession != null) {
            Calendar cal = Calendar.getInstance();
            String session = cal.get(Calendar.HOUR_OF_DAY) < 12 ? "Morning" : "Evening";
            tvCurrentSession.setText(session + " Session");
        } else {
            Log.w(TAG, "tvCurrentSession not found in layout");
        }

        Log.d(TAG, "Views initialized successfully");
    }

    private void setupFirebaseAuth() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getPhoneNumber() == null) {
            Log.e(TAG, "User not authenticated or phone number null");
            if (getContext() != null) {
                Toast.makeText(getContext(), "प्रयोक्ता लॉगिन केलेला नाही", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        currentUserPhone = user.getPhoneNumber().replace("+91", "");
        Log.d(TAG, "Current user phone: " + currentUserPhone);
    }

    private void setupViewPager() {
        if (viewPagerMilkOverview == null) {
            Log.e(TAG, "ViewPager not initialized");
            return;
        }

        milkSummaries = new ArrayList<>();
        adapter = new MilkOverviewAdapter(milkSummaries);
        viewPagerMilkOverview.setAdapter(adapter);

        // Configure ViewPager2
        viewPagerMilkOverview.setOffscreenPageLimit(2);
        viewPagerMilkOverview.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        // Disable nested scrolling
        if (viewPagerMilkOverview.getChildCount() > 0) {
            viewPagerMilkOverview.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        }

        // Touch handling
        viewPagerMilkOverview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        // Page change callback
        viewPagerMilkOverview.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position < milkSummaries.size()) {
                    String dateLabel = getDateLabel(position);
                    Log.d(TAG, "Page selected: " + position + " (" + dateLabel + ")");

                    // Optional: Show toast for current selection
                    if (getContext() != null) {
                        Toast.makeText(getContext(), dateLabel + " selected", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    if (getParentFragment() != null && getParentFragment().getView() != null) {
                        getParentFragment().getView().getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
            }
        });

        Log.d(TAG, "ViewPager setup completed");
    }

    private String getDateLabel(int position) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -(3 - position));

        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        if (isSameDay(cal, today)) {
            return "Today";
        } else if (isSameDay(cal, yesterday)) {
            return "Yesterday";
        } else {
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
            return dayFormat.format(cal.getTime());
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void fetchMilkData() {
        if (currentUserPhone == null) {
            Log.e(TAG, "Cannot fetch data - user phone is null");
            return;
        }

        Log.d(TAG, "Starting to fetch milk data for last 4 days");

        DatabaseReference milkRef = FirebaseDatabase.getInstance()
                .getReference("Dairy/User").child(currentUserPhone).child("MilkCollection");

        milkRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Firebase data received, snapshot exists: " + snapshot.exists());
                processMilkData(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase data fetch cancelled: " + error.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "डेटा मिळवताना त्रुटी: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Rest of your existing methods remain the same...
    private void processMilkData(DataSnapshot snapshot) {
        Map<String, Map<String, DayMilkData>> dateWiseSessionData = new HashMap<>();

        for (DataSnapshot farmerSnapshot : snapshot.getChildren()) {
            String farmerPhone = farmerSnapshot.getKey();
            Log.d(TAG, "Processing data for farmer: " + farmerPhone);

            for (DataSnapshot dateSnapshot : farmerSnapshot.getChildren()) {
                String date = dateSnapshot.getKey();
                Log.d(TAG, "Processing date: " + date);

                if (!dateWiseSessionData.containsKey(date)) {
                    dateWiseSessionData.put(date, new HashMap<>());
                }

                Map<String, DayMilkData> sessionDataMap = dateWiseSessionData.get(date);

                for (DataSnapshot sessionSnapshot : dateSnapshot.getChildren()) {
                    String session = sessionSnapshot.getKey();
                    Log.d(TAG, "Processing session: " + session + " for date: " + date);

                    if (!sessionDataMap.containsKey(session)) {
                        sessionDataMap.put(session, new DayMilkData());
                    }

                    DayMilkData dayData = sessionDataMap.get(session);

                    for (DataSnapshot milkTypeSnapshot : sessionSnapshot.getChildren()) {
                        String milkType = milkTypeSnapshot.getKey();
                        Double quantity = milkTypeSnapshot.child("quantity").getValue(Double.class);

                        if (quantity != null) {
                            Log.d(TAG, "Found " + milkType + " milk: " + quantity + "L on " + date + " " + session);

                            if ("cow".equalsIgnoreCase(milkType)) {
                                dayData.addCowMilk(quantity);
                            } else if ("buffalo".equalsIgnoreCase(milkType)) {
                                dayData.addBuffaloMilk(quantity);
                            }
                        }
                    }
                }
            }
        }

        Log.d(TAG, "Processed data for " + dateWiseSessionData.size() + " dates");
        createMilkSummaryListWithSessions(dateWiseSessionData);
    }

    private void createMilkSummaryListWithSessions(Map<String, Map<String, DayMilkData>> dateWiseSessionData) {
        List<MilkSummaryModel> summaries = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        List<String> dates = new ArrayList<>();
        List<String> displayDates = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            String date = dateFormat.format(calendar.getTime());
            String displayDate = displayFormat.format(calendar.getTime());

            dates.add(date);
            displayDates.add(displayDate);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        for (int i = dates.size() - 1; i >= 0; i--) {
            String date = dates.get(i);
            String displayDate = displayDates.get(i);

            MilkSummaryModel summary = new MilkSummaryModel(displayDate);

            if (dateWiseSessionData.containsKey(date)) {
                Map<String, DayMilkData> sessionData = dateWiseSessionData.get(date);

                for (Map.Entry<String, DayMilkData> entry : sessionData.entrySet()) {
                    String session = entry.getKey();
                    DayMilkData dayData = entry.getValue();

                    double cowMilk = dayData.getTotalCowMilk();
                    double buffaloMilk = dayData.getTotalBuffaloMilk();

                    summary.addSessionData(session, cowMilk, buffaloMilk);
                }
            }

            summaries.add(summary);
        }

        milkSummaries.clear();
        milkSummaries.addAll(summaries);

        if (adapter != null) {
            adapter.updateData(milkSummaries);
        }

        if (summaries.size() > 0 && viewPagerMilkOverview != null) {
            int todayPosition = summaries.size() - 1;
            viewPagerMilkOverview.setCurrentItem(todayPosition, false);
        }

        Log.d(TAG, "Milk summary list created with " + summaries.size() + " items");
    }

    // Helper class
    private static class DayMilkData {
        private double totalCowMilk = 0.0;
        private double totalBuffaloMilk = 0.0;

        public void addCowMilk(double quantity) {
            totalCowMilk += quantity;
        }

        public void addBuffaloMilk(double quantity) {
            totalBuffaloMilk += quantity;
        }

        public double getTotalCowMilk() {
            return totalCowMilk;
        }

        public double getTotalBuffaloMilk() {
            return totalBuffaloMilk;
        }

        public double getTotalMilk() {
            return totalCowMilk + totalBuffaloMilk;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView = null;
        Log.d(TAG, "HomeFragment view destroyed");
    }
}
