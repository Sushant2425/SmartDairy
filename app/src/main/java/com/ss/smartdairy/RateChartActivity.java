package com.ss.smartdairy;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RateChartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RateAdapter adapter;
    private RadioGroup radioGroupType;
    private String selectedType = "Cow";
    private List<RateModel> rateList = new ArrayList<>();
    private DatabaseReference rateRef;
    private String todayDate;

    private String selectedDate;

    private TextView textRateDateStatus;
    private String actualRateDate = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_chart);

        recyclerView = findViewById(R.id.recyclerViewRates);
        radioGroupType = findViewById(R.id.radioGroupType);
        Button btnSave = findViewById(R.id.btnSaveRates);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        todayDate     = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        selectedDate  = todayDate;   // सुरुवातीला आजचीच तारीख

        Button btnPickDate = findViewById(R.id.btnPickDate);

        textRateDateStatus = findViewById(R.id.textRateDateStatus);

        btnPickDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, yr, mon, day) -> {
                        cal.set(yr, mon, day);
                        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
                        Toast.makeText(this, "Date: " + selectedDate, Toast.LENGTH_SHORT).show();
                        loadRates();           // NEW → निवडलेली तारीख लोड करा
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String mobile = user != null ? user.getPhoneNumber().replace("+91", "") : "";

        rateRef = FirebaseDatabase.getInstance()
                .getReference("Dairy")
                .child("User")
                .child(mobile)
                .child("RateChart");

        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        loadRates();

        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioCow) selectedType = "Cow";
            else if (checkedId == R.id.radioBuffalo) selectedType = "Buffalo";
            loadRates();
        });

        btnSave.setOnClickListener(v -> saveRates());
    }

    private void loadRates() {
        double startFat = selectedType.equals("Cow") ? 2.0 : 3.0;
        double endFat = selectedType.equals("Cow") ? 8.0 : 15.0;

        Map<String, RateModel> tempMap = new LinkedHashMap<>();

        for (int i = 0; i <= (int) ((endFat - startFat) * 10); i++) {
            double fat = startFat + (i * 0.1);
            String fatStr = String.format(Locale.getDefault(), "%.1f", fat);
            tempMap.put(fatStr, new RateModel(fatStr, 0));
        }

        if (selectedDate == null || selectedDate.isEmpty()) {
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        }

        rateRef.child(selectedDate).child(selectedType).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // ✅ Data available → Show directly
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String key = child.getKey();
                        String fatStr = keyToFat(key);
                        Double rate = child.getValue(Double.class);
                        if (tempMap.containsKey(fatStr)) {
                            tempMap.get(fatStr).setRate(rate != null ? rate : 0);
                        }
                    }
                    actualRateDate = selectedDate; // 👉 Set current date as actual
                    updateRecycler(tempMap);

                } else {
                    // ❌ No data → Load from last available older date
                    rateRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DataSnapshot allDates = task.getResult();
                            String latest = getLastAvailableDate(allDates, selectedDate);

                            if (latest != null) {
                                DataSnapshot fallbackSnap = allDates.child(latest).child(selectedType);
                                for (DataSnapshot child : fallbackSnap.getChildren()) {
                                    String key = child.getKey();
                                    String fatStr = keyToFat(key);
                                    Double rate = child.getValue(Double.class);
                                    if (tempMap.containsKey(fatStr)) {
                                        tempMap.get(fatStr).setRate(rate != null ? rate : 0);
                                    }
                                }
                            }

                            updateRecycler(tempMap);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RateChartActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecycler(Map<String, RateModel> tempMap) {
        List<RateModel> freshList = new ArrayList<>(tempMap.values());

        if (actualRateDate.equals(selectedDate)) {
            textRateDateStatus.setText("Rates from: " + actualRateDate);
        } else {
            textRateDateStatus.setText("Rates from: " + actualRateDate + " (Selected: " + selectedDate + ")");
        }


        boolean isToday = selectedDate.equals(todayDate);   // आजची date आहे का?
        adapter = new RateAdapter(freshList, isToday);

        recyclerView.setAdapter(adapter);
        rateList = freshList;
    }

    private String getLastAvailableDate(DataSnapshot allDatesSnapshot, String currentDate) {
        String lastDate = null;
        for (DataSnapshot dateSnap : allDatesSnapshot.getChildren()) {
            String date = dateSnap.getKey();
            if (date != null && date.compareTo(currentDate) < 0) {
                if (lastDate == null || date.compareTo(lastDate) > 0) {
                    lastDate = date;
                }
            }
        }
        return lastDate;
    }

    private void saveRates() {
        Map<String, Object> map = new HashMap<>();
        Map<Double, List<String>> rateToFatList = new HashMap<>();

        for (RateModel model : adapter.getUpdatedRates()) {
            String key = fatToKey(model.getFat());
            double rate = model.getRate();
            map.put(key, rate);

            if (!rateToFatList.containsKey(rate)) {
                rateToFatList.put(rate, new ArrayList<>());
            }
            rateToFatList.get(rate).add(model.getFat());
        }

        // ✅ Check for duplicate rates
        for (Map.Entry<Double, List<String>> entry : rateToFatList.entrySet()) {
            if (entry.getKey() != 0 && entry.getValue().size() > 1) {
                String fats = String.join(", ", entry.getValue());
                Toast.makeText(this, "⚠️ Same rate " + entry.getKey() + " for FATs: " + fats, Toast.LENGTH_LONG).show();
                return;
            }
        }

        // ✅ Save only if valid
        rateRef.child(selectedDate).child(selectedType).setValue(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Rates saved successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save rates.", Toast.LENGTH_SHORT).show();
            }
        });
        // Auto backup to BackupRates as well
        rateRef.getParent().child("BackupRates").child(selectedDate).child(selectedType).setValue(map);
    }

    private String fatToKey(String fat) {
        return fat.replace(".", "_");
    }

    private String keyToFat(String key) {
        return key.replace("_", ".");
    }
}
