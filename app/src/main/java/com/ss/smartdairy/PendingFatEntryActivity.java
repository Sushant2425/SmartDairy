package com.ss.smartdairy;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PendingFatEntryActivity extends AppCompatActivity {

    private static final String TAG = "PendingFatEntry";
    private EditText etMilkCode, etFatSampleCode, etDate, etSession, etFatValue;
    private TextView tvMilkType, tvFarmerMobile, tvMilkQuantity, tvFatRate, tvTotalAmount;
    private Button btnUpdateFat;
    private String adminMobile;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long DEBOUNCE_DELAY = 500;

    private TextWatcher textWatcher;
    private TextWatcher fatValueWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_fat_entry);
        Log.d(TAG, "Activity created");

        initializeViews();
        setupFirebaseAuth();
        setupTextWatchers();
        setDefaultValues();

        btnUpdateFat.setOnClickListener(v -> validateAndUpdateFat());
    }

    private void initializeViews() {
        etMilkCode = findViewById(R.id.etMilkCode);
        etFatSampleCode = findViewById(R.id.etFatSampleCode);
        etDate = findViewById(R.id.etDate);
        etSession = findViewById(R.id.etSession);
        etFatValue = findViewById(R.id.etFatValue);
        tvMilkType = findViewById(R.id.tvMilkType);
        tvFarmerMobile = findViewById(R.id.tvFarmerMobile);
        btnUpdateFat = findViewById(R.id.btnUpdateFat);
        tvMilkQuantity = findViewById(R.id.tvMilkQuantity);
        tvFatRate = findViewById(R.id.tvFatRate);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        if (etMilkCode == null || etFatSampleCode == null || etDate == null || etSession == null ||
                etFatValue == null || tvMilkType == null || tvFarmerMobile == null || tvMilkQuantity == null || btnUpdateFat == null) {
            Log.e(TAG, "UI initialization failed - some views are null");
            Toast.makeText(this, "UI लोड करताना त्रुटी: लेआउट फाइल तपासा", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d(TAG, "All views initialized successfully");
    }

    private void setupFirebaseAuth() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getPhoneNumber() == null) {
            Log.e(TAG, "User not authenticated or phone number null");
            Toast.makeText(this, "प्रयोक्ता लॉगिन केलेला नाही", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        adminMobile = user.getPhoneNumber().replace("+91", "");
        Log.d(TAG, "Admin mobile set: " + adminMobile);
    }

    private void setupTextWatchers() {
        // TextWatcher for milk code and fat sample code
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    String milkCode = etMilkCode.getText().toString().trim();
                    String fatSampleCode = etFatSampleCode.getText().toString().trim();
                    String date = etDate.getText().toString().trim();
                    String session = etSession.getText().toString().trim();

                    Log.d(TAG, "TextWatcher triggered - MilkCode: " + milkCode + ", FatSampleCode: " + fatSampleCode);

                    if (!milkCode.isEmpty() || !fatSampleCode.isEmpty()) {
                        searchRecord(milkCode, fatSampleCode, date, session);
                    } else {
                        clearMilkInfo();
                    }
                };

                handler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
            }
        };

        // TextWatcher for fat value to trigger calculation
        fatValueWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "Fat value changed: " + s.toString());
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> calculateAmount();
                handler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
            }
        };

        etMilkCode.addTextChangedListener(textWatcher);
        etFatSampleCode.addTextChangedListener(textWatcher);
        etFatValue.addTextChangedListener(fatValueWatcher);
    }

    private void setDefaultValues() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String session = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
        session = Integer.parseInt(session) < 12 ? "morning" : "evening";
        etDate.setText(todayDate);
        etSession.setText(session);
        Log.d(TAG, "Default values set - Date: " + todayDate + ", Session: " + session);
    }

    private void calculateAmount() {
        Log.d(TAG, "calculateAmount() called");

        String fatStr = etFatValue.getText().toString().trim();
        String quantityStr = tvMilkQuantity.getText().toString().replaceAll("[^\\d.]", "").trim();
        String milkTypeStr = tvMilkType.getText().toString().replace("दुधाचा प्रकार: ", "").trim();

        Log.d(TAG, "Input values - Fat: " + fatStr + ", Quantity: " + quantityStr + ", MilkType: " + milkTypeStr);

        // Map display text to database milk type
        String milkType;
        if (milkTypeStr.equals("गाय")) {
            milkType = "Cow";
        } else if (milkTypeStr.equals("म्हैस")) {
            milkType = "Buffalo";
        } else {
            Log.w(TAG, "Invalid or missing milk type: " + milkTypeStr);
            clearAmountInfo();
            if (!milkTypeStr.isEmpty()) {
                Toast.makeText(this, "दुधाचा प्रकार निवडा (गाय/म्हैस).", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Validate inputs
        if (fatStr.isEmpty() || quantityStr.isEmpty()) {
            Log.w(TAG, "Fat or quantity is empty");
            clearAmountInfo();
            if (!fatStr.isEmpty() && quantityStr.isEmpty()) {
                Toast.makeText(this, "दुधाचे प्रमाण उपलब्ध नाही.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        double fatValue, milkQuantity;
        try {
            fatValue = Double.parseDouble(fatStr);
            milkQuantity = Double.parseDouble(quantityStr);
            Log.d(TAG, "Parsed values - Fat: " + fatValue + ", Quantity: " + milkQuantity);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Number parsing error: " + e.getMessage());
            clearAmountInfo();
            Toast.makeText(this, "अवैध फॅट किंवा प्रमाण मूल्य.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate fat value range
        if (fatValue < 2.0 || fatValue > 15.0) {
            Log.w(TAG, "Fat value out of range: " + fatValue);
            clearAmountInfo();
            Toast.makeText(this, "फॅट टक्केवारी 2.0 ते 15.0 च्या मध्ये असावी", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format fatKey (e.g., 3.5 -> 3_5)
        String fatKey = String.format(Locale.US, "%.1f", fatValue).replace(".", "_");

        // Get current date dynamically
        String currentDate = etDate.getText().toString().trim();
        if (currentDate.isEmpty()) {
            currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        }

        Log.d(TAG, "Fetching fat rate - Date: " + currentDate + ", MilkType: " + milkType + ", FatKey: " + fatKey);

        // Corrected Firebase reference
        DatabaseReference rateRef = FirebaseDatabase.getInstance()
                .getReference("Dairy/User").child(adminMobile).child("RateChart").child(currentDate).child(milkType).child(fatKey);

        Log.d(TAG, "Firebase path: " + rateRef.toString());

        String finalCurrentDate = currentDate;
        rateRef.get().addOnSuccessListener(snapshot -> {
            Log.d(TAG, "Firebase query successful, snapshot exists: " + snapshot.exists());

            if (snapshot.exists()) {
                Double rate = snapshot.getValue(Double.class);
                Log.d(TAG, "Retrieved rate: " + rate);

                if (rate != null && rate > 0) {
                    double totalAmount = rate * milkQuantity;
                    tvFatRate.setText("फॅट दर (प्रति लिटर): ₹" + String.format(Locale.US, "%.2f", rate));
                    tvTotalAmount.setText("एकूण रक्कम: ₹" + String.format(Locale.US, "%.2f", totalAmount));
                    Log.d(TAG, "Amount calculated successfully - Rate: " + rate + ", Total: " + totalAmount);
                } else {
                    Log.w(TAG, "Rate is null or zero: " + rate);
                    clearAmountInfo();
                    Toast.makeText(this, "या फॅट साठी दर सेट केलेला नाही.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.w(TAG, "No data found for fatKey: " + fatKey);
                clearAmountInfo();

                // Try to find nearest fat rate
                tryNearestFatRate(finalCurrentDate, milkType, fatValue, milkQuantity);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Firebase query failed: " + e.getMessage(), e);
            clearAmountInfo();
            Toast.makeText(this, "फॅट दर मिळवताना त्रुटी: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void tryNearestFatRate(String currentDate, String milkType, double fatValue, double milkQuantity) {
        Log.d(TAG, "Trying to find nearest fat rate for: " + fatValue);

        // Try rounded fat value
        double roundedFat = Math.round(fatValue * 10.0) / 10.0;
        String roundedFatKey = String.format(Locale.US, "%.1f", roundedFat).replace(".", "_");

        if (roundedFatKey.equals(String.format(Locale.US, "%.1f", fatValue).replace(".", "_"))) {
            // Already tried this, so look for the entire rate chart to find closest
            DatabaseReference allRatesRef = FirebaseDatabase.getInstance()
                    .getReference("Dairy/User").child(adminMobile).child("RateChart").child(currentDate).child(milkType);

            allRatesRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    double closestFat = -1;
                    double closestRate = 0;
                    double minDifference = Double.MAX_VALUE;

                    for (DataSnapshot child : snapshot.getChildren()) {
                        String fatKey = child.getKey();
                        Double rate = child.getValue(Double.class);

                        if (fatKey != null && rate != null && rate > 0) {
                            try {
                                double fat = Double.parseDouble(fatKey.replace("_", "."));
                                double difference = Math.abs(fat - fatValue);

                                if (difference < minDifference) {
                                    minDifference = difference;
                                    closestFat = fat;
                                    closestRate = rate;
                                }
                            } catch (NumberFormatException e) {
                                Log.w(TAG, "Invalid fat key format: " + fatKey);
                            }
                        }
                    }

                    if (closestFat > 0) {
                        double totalAmount = closestRate * milkQuantity;
                        tvFatRate.setText("फॅट दर (प्रति लिटर): ₹" + String.format(Locale.US, "%.2f", closestRate) + " (निकटतम: " + closestFat + "%)");
                        tvTotalAmount.setText("एकूण रक्कम: ₹" + String.format(Locale.US, "%.2f", totalAmount));
                        Log.d(TAG, "Used nearest fat rate - Fat: " + closestFat + ", Rate: " + closestRate);
                    } else {
                        Toast.makeText(this, "या तारखेसाठी कोणताही दर उपलब्ध नाही.", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "No rates available for date: " + currentDate);
                    }
                } else {
                    Toast.makeText(this, "या तारखेसाठी दर चार्ट उपलब्ध नाही.", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "No rate chart available for date: " + currentDate);
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to fetch rate chart: " + e.getMessage(), e);
                Toast.makeText(this, "दर चार्ट मिळवताना त्रुटी.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void clearAmountInfo() {
        tvFatRate.setText("फॅट दर (प्रति लिटर): -");
        tvTotalAmount.setText("एकूण रक्कम: -");
    }

    private void clearMilkInfo() {
        tvMilkType.setText("दुधाचा प्रकार: ");
        tvFarmerMobile.setText("शेतकरी मोबाइल: ");
        tvMilkQuantity.setText("दुध प्रमाण (लिटरमध्ये): ");
        clearAmountInfo();
    }

    private void searchRecord(String milkCode, String fatSampleCode, String date, String session) {
        if (date.isEmpty() || session.isEmpty()) {
            clearMilkInfo();
            return;
        }

        try {
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
        } catch (Exception e) {
            Log.e(TAG, "Invalid date format: " + date);
            clearMilkInfo();
            return;
        }

        if (!session.equalsIgnoreCase("morning") && !session.equalsIgnoreCase("evening")) {
            Log.w(TAG, "Invalid session: " + session);
            clearMilkInfo();
            return;
        }

        Log.d(TAG, "Searching record - MilkCode: " + milkCode + ", FatSampleCode: " + fatSampleCode + ", Date: " + date + ", Session: " + session);

        DatabaseReference milkRef = FirebaseDatabase.getInstance()
                .getReference("Dairy").child("User").child(adminMobile).child("MilkCollection");

        milkRef.get().addOnSuccessListener(snapshot -> {
            boolean found = false;
            String targetFarmerMobile = null;
            String targetMilkType = null;
            String matchedMilkCode = null;
            String matchedFatSampleCode = null;

            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
                String farmerMobile = farmerSnap.getKey();
                DataSnapshot dateSnap = farmerSnap.child(date).child(session);

                for (DataSnapshot milkTypeSnap : dateSnap.getChildren()) {
                    String milkType = milkTypeSnap.getKey();
                    String storedMilkCode = milkTypeSnap.child("milkCode").getValue(String.class);
                    String storedFatSampleCode = milkTypeSnap.child("fatSampleCode").getValue(String.class);

                    boolean milkCodeMatch = !milkCode.isEmpty() && storedMilkCode != null && storedMilkCode.equals(milkCode);
                    boolean fatSampleCodeMatch = !fatSampleCode.isEmpty() && storedFatSampleCode != null && storedFatSampleCode.equals(fatSampleCode);

                    boolean validMatch;
                    if (!milkCode.isEmpty() && !fatSampleCode.isEmpty()) {
                        validMatch = milkCodeMatch && fatSampleCodeMatch;
                    } else if (!milkCode.isEmpty()) {
                        validMatch = milkCodeMatch;
                    } else {
                        validMatch = fatSampleCodeMatch;
                    }

                    if (validMatch) {
                        found = true;
                        targetFarmerMobile = farmerMobile;
                        targetMilkType = milkType;
                        matchedMilkCode = storedMilkCode;
                        matchedFatSampleCode = storedFatSampleCode;

                        tvMilkType.setText("दुधाचा प्रकार: " + (milkType.equals("cow") ? "गाय" : "म्हैस"));
                        tvFarmerMobile.setText("शेतकरी मोबाइल: " + farmerMobile);

                        Double milkQty = milkTypeSnap.child("quantity").getValue(Double.class);
                        if (milkQty != null) {
                            tvMilkQuantity.setText("दुध प्रमाण (लिटरमध्ये): " + milkQty);
                            Log.d(TAG, "Record found - Farmer: " + farmerMobile + ", Type: " + milkType + ", Quantity: " + milkQty);

                            // Trigger calculation if fat value is available
                            if (!etFatValue.getText().toString().trim().isEmpty()) {
                                calculateAmount();
                            }
                        } else {
                            tvMilkQuantity.setText("दुध प्रमाण (लिटरमध्ये): -");
                        }

                        // Update codes if they were searched by partial match
                        etMilkCode.removeTextChangedListener(textWatcher);
                        etFatSampleCode.removeTextChangedListener(textWatcher);

                        if (milkCode.isEmpty() && matchedMilkCode != null) {
                            etMilkCode.setText(matchedMilkCode);
                        }
                        if (fatSampleCode.isEmpty() && matchedFatSampleCode != null) {
                            etFatSampleCode.setText(matchedFatSampleCode);
                        }

                        etMilkCode.addTextChangedListener(textWatcher);
                        etFatSampleCode.addTextChangedListener(textWatcher);

                        break;
                    }
                }
                if (found) break;
            }

            if (!found) {
                Log.w(TAG, "No record found for given criteria");
                clearMilkInfo();
                Toast.makeText(this, "रेकॉर्ड सापडला नाही", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to search record: " + e.getMessage(), e);
            clearMilkInfo();
            Toast.makeText(this, "डेटा मिळवताना त्रुटी: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void validateAndUpdateFat() {
        String milkCode = etMilkCode.getText().toString().trim();
        String fatSampleCode = etFatSampleCode.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String session = etSession.getText().toString().trim();
        String fatStr = etFatValue.getText().toString().trim();

        Log.d(TAG, "Validating update - MilkCode: " + milkCode + ", FatCode: " + fatSampleCode + ", Fat: " + fatStr);

        if (milkCode.isEmpty() && fatSampleCode.isEmpty()) {
            Toast.makeText(this, "मिल्क कोड किंवा फॅट सॅम्पल कोड टाका", Toast.LENGTH_SHORT).show();
            return;
        }
        if (date.isEmpty() || session.isEmpty()) {
            Toast.makeText(this, "तारीख आणि सत्र टाका", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fatStr.isEmpty()) {
            Toast.makeText(this, "फॅट टक्केवारी टाका", Toast.LENGTH_SHORT).show();
            return;
        }

        double fat;
        try {
            fat = Double.parseDouble(fatStr);
            if (fat < 2.0 || fat > 15.0) {
                Toast.makeText(this, "फॅट टक्केवारी 2.0 ते 15.0 च्या मध्ये असावी", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid fat value: " + fatStr);
            Toast.makeText(this, "अवैध फॅट टक्केवारी", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
        } catch (Exception e) {
            Log.e(TAG, "Invalid date format: " + date);
            Toast.makeText(this, "अवैध तारीख स्वरूप (YYYY-MM-DD)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!session.equalsIgnoreCase("morning") && !session.equalsIgnoreCase("evening")) {
            Log.w(TAG, "Invalid session: " + session);
            Toast.makeText(this, "सत्र 'morning' किंवा 'evening' असावं", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference milkRef = FirebaseDatabase.getInstance()
                .getReference("Dairy").child("User").child(adminMobile).child("MilkCollection");

        milkRef.get().addOnSuccessListener(snapshot -> {
            boolean found = false;
            String targetFarmerMobile = null;
            String targetMilkType = null;
            Double milkQuantity = null;

            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
                String farmerMobile = farmerSnap.getKey();
                DataSnapshot dateSnap = farmerSnap.child(date).child(session);

                for (DataSnapshot milkTypeSnap : dateSnap.getChildren()) {
                    String milkType = milkTypeSnap.getKey();
                    String storedMilkCode = milkTypeSnap.child("milkCode").getValue(String.class);
                    String storedFatSampleCode = milkTypeSnap.child("fatSampleCode").getValue(String.class);

                    boolean milkCodeMatch = !milkCode.isEmpty() && storedMilkCode != null && storedMilkCode.equals(milkCode);
                    boolean fatSampleCodeMatch = !fatSampleCode.isEmpty() && storedFatSampleCode != null && storedFatSampleCode.equals(fatSampleCode);

                    boolean validMatch;
                    if (!milkCode.isEmpty() && !fatSampleCode.isEmpty()) {
                        validMatch = milkCodeMatch && fatSampleCodeMatch;
                    } else if (!milkCode.isEmpty()) {
                        validMatch = milkCodeMatch;
                    } else {
                        validMatch = fatSampleCodeMatch;
                    }

                    if (validMatch) {
                        found = true;
                        targetFarmerMobile = farmerMobile;
                        targetMilkType = milkType;
                        milkQuantity = milkTypeSnap.child("quantity").getValue(Double.class);
                        break;
                    }
                }
                if (found) break;
            }

            if (!found) {
                Log.w(TAG, "No record found for update");
                Toast.makeText(this, "रेकॉर्ड सापडला नाही", Toast.LENGTH_SHORT).show();
                return;
            }

            if (milkQuantity == null || milkQuantity <= 0) {
                Log.w(TAG, "Invalid milk quantity: " + milkQuantity);
                Toast.makeText(this, "दुधाचे प्रमाण मिळाले नाही", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Updating fat for farmer: " + targetFarmerMobile + ", type: " + targetMilkType + ", quantity: " + milkQuantity);

            // Calculate total amount before saving
            calculateAndSaveFatWithAmount(targetFarmerMobile, targetMilkType, date, session, fat, milkQuantity);

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to fetch data for update: " + e.getMessage(), e);
            Toast.makeText(this, "डेटा मिळवताना त्रुटी: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void calculateAndSaveFatWithAmount(String farmerMobile, String milkType, String date,
                                               String session, double fat, double milkQuantity) {

        Log.d(TAG, "Calculating total amount for fat: " + fat + ", quantity: " + milkQuantity);

        // Format fatKey (e.g., 3.5 -> 3_5)
        String fatKey = String.format(Locale.US, "%.1f", fat).replace(".", "_");

        // Map milk type for rate chart
        String rateChartMilkType = milkType.equals("cow") ? "Cow" : "Buffalo";

        // Get fat rate from RateChart
        DatabaseReference rateRef = FirebaseDatabase.getInstance()
                .getReference("Dairy/User").child(adminMobile).child("RateChart").child(date).child(rateChartMilkType).child(fatKey);

        rateRef.get().addOnSuccessListener(rateSnapshot -> {
            Double rate = null;
            double totalAmount = 0.0;
            String rateSource = "exact";

            if (rateSnapshot.exists()) {
                rate = rateSnapshot.getValue(Double.class);
                Log.d(TAG, "Found exact rate: " + rate + " for fat: " + fat);
            }

            if (rate != null && rate > 0) {
                totalAmount = rate * milkQuantity;
                Log.d(TAG, "Calculated total amount: " + totalAmount);
                saveFatAndAmount(farmerMobile, milkType, date, session, fat, rate, totalAmount, rateSource);
            } else {
                // Try to find nearest rate
                Log.d(TAG, "Exact rate not found, searching for nearest rate");
                findNearestRateAndSave(farmerMobile, milkType, date, session, fat, milkQuantity, rateChartMilkType);
            }

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to fetch rate: " + e.getMessage());
            // Save without amount calculation
            saveFatAndAmount(farmerMobile, milkType, date, session, fat, null, 0.0, "rate_not_found");
        });
    }

    private void findNearestRateAndSave(String farmerMobile, String milkType, String date,
                                        String session, double fat, double milkQuantity, String rateChartMilkType) {

        DatabaseReference allRatesRef = FirebaseDatabase.getInstance()
                .getReference("Dairy/User").child(adminMobile).child("RateChart").child(date).child(rateChartMilkType);

        allRatesRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                double closestFat = -1;
                double closestRate = 0;
                double minDifference = Double.MAX_VALUE;

                for (DataSnapshot child : snapshot.getChildren()) {
                    String fatKey = child.getKey();
                    Double rate = child.getValue(Double.class);

                    if (fatKey != null && rate != null && rate > 0) {
                        try {
                            double fatValue = Double.parseDouble(fatKey.replace("_", "."));
                            double difference = Math.abs(fatValue - fat);

                            if (difference < minDifference) {
                                minDifference = difference;
                                closestFat = fatValue;
                                closestRate = rate;
                            }
                        } catch (NumberFormatException e) {
                            Log.w(TAG, "Invalid fat key format: " + fatKey);
                        }
                    }
                }

                if (closestFat > 0) {
                    double totalAmount = closestRate * milkQuantity;
                    Log.d(TAG, "Using nearest rate - Fat: " + closestFat + ", Rate: " + closestRate + ", Amount: " + totalAmount);
                    saveFatAndAmount(farmerMobile, milkType, date, session, fat, closestRate, totalAmount, "nearest_" + closestFat);
                } else {
                    Log.w(TAG, "No rates available for date: " + date);
                    saveFatAndAmount(farmerMobile, milkType, date, session, fat, null, 0.0, "no_rate_available");
                }
            } else {
                Log.w(TAG, "No rate chart available for date: " + date);
                saveFatAndAmount(farmerMobile, milkType, date, session, fat, null, 0.0, "no_rate_chart");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to fetch rate chart: " + e.getMessage());
            saveFatAndAmount(farmerMobile, milkType, date, session, fat, null, 0.0, "rate_fetch_error");
        });
    }

    private void saveFatAndAmount(String farmerMobile, String milkType, String date, String session,
                                  double fat, Double rate, double totalAmount, String rateSource) {

        DatabaseReference targetRef = FirebaseDatabase.getInstance()
                .getReference("Dairy").child("User").child(adminMobile)
                .child("MilkCollection").child(farmerMobile).child(date).child(session).child(milkType);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("fat", fat);

        // Add rate and total amount if available
        if (rate != null && rate > 0) {
            updateData.put("fatRate", rate);
            updateData.put("totalAmount", totalAmount);
            updateData.put("rateSource", rateSource); // Track how rate was determined
        }

        // Add timestamp for when fat was updated
        updateData.put("fatUpdatedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        Log.d(TAG, "Saving update data: " + updateData.toString());

        targetRef.updateChildren(updateData).addOnSuccessListener(unused -> {
            Log.d(TAG, "Fat and amount updated successfully");

            String message = "फॅट टक्केवारी यशस्वीपणे अपडेट झाली";
            if (rate != null && rate > 0) {
                message += "\nदर: ₹" + String.format(Locale.US, "%.2f", rate) + "/लिटर";
                message += "\nएकूण रक्कम: ₹" + String.format(Locale.US, "%.2f", totalAmount);
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            // Recalculate amount display after update
            calculateAmount();
            clearForm();

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to update fat and amount: " + e.getMessage(), e);
            Toast.makeText(this, "अपडेट अयशस्वी: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    private void clearForm() {
        etMilkCode.setText("");
        etFatSampleCode.setText("");
        etFatValue.setText("");
        clearMilkInfo();
        etMilkCode.requestFocus();

        setDefaultValues();
        Log.d(TAG, "Form cleared");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }
        Log.d(TAG, "Activity destroyed");
    }
}
