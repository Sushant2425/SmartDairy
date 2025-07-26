//package com.ss.smartdairy;
//
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Locale;
//import java.util.Map;
//
//public class PendingFatEntryActivity extends AppCompatActivity {
//
//    private EditText etMilkCode, etFatSampleCode, etDate, etSession, etFatValue;
//    private TextView tvMilkType, tvFarmerMobile;
//    private Button btnUpdateFat;
//    private String adminMobile;
//    private final Handler handler = new Handler(Looper.getMainLooper());
//    private Runnable searchRunnable;
//    private static final long DEBOUNCE_DELAY = 500; // 500ms delay for debouncing
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_pending_fat_entry);
//
//        // Initialize UI elements with null checks
//        etMilkCode = findViewById(R.id.etMilkCode);
//        etFatSampleCode = findViewById(R.id.etFatSampleCode);
//        etDate = findViewById(R.id.etDate);
//        etSession = findViewById(R.id.etSession);
//        etFatValue = findViewById(R.id.etFatValue);
//        tvMilkType = findViewById(R.id.tvMilkType);
//        tvFarmerMobile = findViewById(R.id.tvFarmerMobile);
//        btnUpdateFat = findViewById(R.id.btnUpdateFat);
//
//        // Check if UI elements are properly initialized
//        if (etMilkCode == null || etFatSampleCode == null || etDate == null || etSession == null ||
//                etFatValue == null || tvMilkType == null || tvFarmerMobile == null || btnUpdateFat == null) {
//            Toast.makeText(this, "UI लोड करताना त्रुटी: लेआउट फाइल तपासा", Toast.LENGTH_LONG).show();
//            finish();
//            return;
//        }
//
//        // Firebase Authentication
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user == null || user.getPhoneNumber() == null) {
//            Toast.makeText(this, "प्रयोक्ता लॉगिन केलेला नाही", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//        adminMobile = user.getPhoneNumber().replace("+91", "");
//
//        // Set default date and session
//        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//        String session = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
//        session = Integer.parseInt(session) < 12 ? "morning" : "evening";
//        etDate.setText(todayDate);
//        etSession.setText(session);
//
//        // Add TextWatcher for milkCode and fatSampleCode with debouncing
//        TextWatcher textWatcher = new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                // Remove previous runnable to prevent multiple queries
//                if (searchRunnable != null) {
//                    handler.removeCallbacks(searchRunnable);
//                }
//
//                // Create new runnable for search
//                searchRunnable = () -> {
//                    String milkCode = etMilkCode.getText().toString().trim();
//                    String fatSampleCode = etFatSampleCode.getText().toString().trim();
//                    String date = etDate.getText().toString().trim();
//                    String session = etSession.getText().toString().trim();
//                    if (!milkCode.isEmpty() || !fatSampleCode.isEmpty()) {
//                        searchRecord(milkCode, fatSampleCode, date, session);
//                    } else {
//                        tvMilkType.setText("दुधाचा प्रकार: ");
//                        tvFarmerMobile.setText("शेतकरी मोबाइल: ");
//                        etMilkCode.setText("");
//                        etFatSampleCode.setText("");
//                    }
//                };
//
//                // Post search with debounce delay
//                handler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
//            }
//        };
//
//        etMilkCode.addTextChangedListener(textWatcher);
//        etFatSampleCode.addTextChangedListener(textWatcher);
//
//        btnUpdateFat.setOnClickListener(v -> validateAndUpdateFat());
//    }
//
//    private void searchRecord(String milkCode, String fatSampleCode, String date, String session) {
//        // Validate date and session before searching
//        if (date.isEmpty() || session.isEmpty()) {
//            tvMilkType.setText("दुधाचा प्रकार: ");
//            tvFarmerMobile.setText("शेतकरी मोबाइल: ");
//            etMilkCode.setText(milkCode); // Preserve current input
//            etFatSampleCode.setText(fatSampleCode); // Preserve current input
//            return;
//        }
//
//        try {
//            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
//        } catch (Exception e) {
//            tvMilkType.setText("दुधाचा प्रकार: ");
//            tvFarmerMobile.setText("शेतकरी मोबाइल: ");
//            etMilkCode.setText(milkCode); // Preserve current input
//            etFatSampleCode.setText(fatSampleCode); // Preserve current input
//            return;
//        }
//
//        if (!session.equalsIgnoreCase("morning") && !session.equalsIgnoreCase("evening")) {
//            tvMilkType.setText("दुधाचा प्रकार: ");
//            tvFarmerMobile.setText("शेतकरी मोबाइल: ");
//            etMilkCode.setText(milkCode); // Preserve current input
//            etFatSampleCode.setText(fatSampleCode); // Preserve current input
//            return;
//        }
//
//        DatabaseReference milkRef = FirebaseDatabase.getInstance()
//                .getReference("Dairy").child("User").child(adminMobile).child("MilkCollection");
//
//        milkRef.get().addOnSuccessListener(snapshot -> {
//            boolean found = false;
//            String targetFarmerMobile = null;
//            String targetMilkType = null;
//            String matchedMilkCode = null;
//            String matchedFatSampleCode = null;
//
//            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
//                String farmerMobile = farmerSnap.getKey();
//                DataSnapshot dateSnap = farmerSnap.child(date).child(session);
//
//                for (DataSnapshot milkTypeSnap : dateSnap.getChildren()) {
//                    String milkType = milkTypeSnap.getKey();
//                    String storedMilkCode = milkTypeSnap.child("milkCode").getValue(String.class);
//                    String storedFatSampleCode = milkTypeSnap.child("fatSampleCode").getValue(String.class);
//
//                    if ((milkCode.isEmpty() || (storedMilkCode != null && storedMilkCode.equals(milkCode))) &&
//                            (fatSampleCode.isEmpty() || (storedFatSampleCode != null && storedFatSampleCode.equals(fatSampleCode)))) {
//                        found = true;
//                        targetFarmerMobile = farmerMobile;
//                        targetMilkType = milkType;
//                        matchedMilkCode = storedMilkCode;
//                        matchedFatSampleCode = storedFatSampleCode;
//                        tvMilkType.setText("दुधाचा प्रकार: " + (milkType.equals("cow") ? "गाय" : "म्हैस"));
//                        tvFarmerMobile.setText("शेतकरी मोबाइल: " + farmerMobile);
//                        // Set the other code if it's empty
//                        if (milkCode.isEmpty() && matchedMilkCode != null) {
//                            etMilkCode.setText(matchedMilkCode);
//                        }
//                        if (fatSampleCode.isEmpty() && matchedFatSampleCode != null) {
//                            etFatSampleCode.setText(matchedFatSampleCode);
//                        }
//                        break;
//                    }
//                }
//                if (found) break;
//            }
//
//            if (!found) {
//                tvMilkType.setText("दुधाचा प्रकार: ");
//                tvFarmerMobile.setText("शेतकरी मोबाइल: ");
//                etMilkCode.setText(milkCode); // Preserve current input
//                etFatSampleCode.setText(fatSampleCode); // Preserve current input
//                Toast.makeText(this, "रेकॉर्ड सापडला नाही", Toast.LENGTH_SHORT).show();
//            }
//        }).addOnFailureListener(e -> {
//            tvMilkType.setText("दुधाचा प्रकार: ");
//            tvFarmerMobile.setText("शेतकरी मोबाइल: ");
//            etMilkCode.setText(milkCode); // Preserve current input
//            etFatSampleCode.setText(fatSampleCode); // Preserve current input
//            Toast.makeText(this, "डेटा मिळवताना त्रुटी: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        });
//    }
//
//        private void validateAndUpdateFat() {
//        String milkCode = etMilkCode.getText().toString().trim();
//        String fatSampleCode = etFatSampleCode.getText().toString().trim();
//        String date = etDate.getText().toString().trim();
//        String session = etSession.getText().toString().trim();
//        String fatStr = etFatValue.getText().toString().trim();
//
//        // Validate inputs
//        if (milkCode.isEmpty() && fatSampleCode.isEmpty()) {
//            Toast.makeText(this, "मिल्क कोड किंवा फॅट सॅम्पल कोड टाका", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (date.isEmpty() || session.isEmpty()) {
//            Toast.makeText(this, "तारीख आणि सत्र टाका", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (fatStr.isEmpty()) {
//            Toast.makeText(this, "फॅट टक्केवारी टाका", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        double fat;
//        try {
//            fat = Double.parseDouble(fatStr);
//            if (fat < 2.0 || fat > 8.0) {
//                Toast.makeText(this, "फॅट टक्केवारी 2.0 ते 8.0 च्या मध्ये असावी", Toast.LENGTH_SHORT).show();
//                return;
//            }
//        } catch (NumberFormatException e) {
//            Toast.makeText(this, "अवैध फॅट टक्केवारी", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Validate date format (YYYY-MM-DD)
//        try {
//            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
//        } catch (Exception e) {
//            Toast.makeText(this, "अवैध तारीख स्वरूप (YYYY-MM-DD)", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Validate session
//        if (!session.equalsIgnoreCase("morning") && !session.equalsIgnoreCase("evening")) {
//            Toast.makeText(this, "सत्र 'morning' किंवा 'evening' असावं", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Search for record and update
//        DatabaseReference milkRef = FirebaseDatabase.getInstance()
//                .getReference("Dairy").child("User").child(adminMobile).child("MilkCollection");
//
//        milkRef.get().addOnSuccessListener(snapshot -> {
//            boolean found = false;
//            String targetFarmerMobile = null;
//            String targetMilkType = null;
//
//            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
//                String farmerMobile = farmerSnap.getKey();
//                DataSnapshot dateSnap = farmerSnap.child(date).child(session);
//
//                for (DataSnapshot milkTypeSnap : dateSnap.getChildren()) {
//                    String milkType = milkTypeSnap.getKey();
//                    String storedMilkCode = milkTypeSnap.child("milkCode").getValue(String.class);
//                    String storedFatSampleCode = milkTypeSnap.child("fatSampleCode").getValue(String.class);
//
//                    if ((milkCode.isEmpty() || (storedMilkCode != null && storedMilkCode.equals(milkCode))) &&
//                            (fatSampleCode.isEmpty() || (storedFatSampleCode != null && storedFatSampleCode.equals(fatSampleCode)))) {
//                        found = true;
//                        targetFarmerMobile = farmerMobile;
//                        targetMilkType = milkType;
//                        break;
//                    }
//                }
//                if (found) break;
//            }
//
//            if (!found) {
//                Toast.makeText(this, "रेकॉर्ड सापडला नाही", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            // Update fat value
//            DatabaseReference targetRef = milkRef.child(targetFarmerMobile).child(date).child(session).child(targetMilkType);
//            Map<String, Object> updateData = new HashMap<>();
//            updateData.put("fat", fat);
//
//            targetRef.updateChildren(updateData).addOnSuccessListener(unused -> {
//                Toast.makeText(this, "फॅट टक्केवारी यशस्वीपणे अपडेट झाली", Toast.LENGTH_SHORT).show();
//                clearForm();
//            }).addOnFailureListener(e -> {
//                Toast.makeText(this, "अपडेट अयशस्वी: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//            });
//        }).addOnFailureListener(e -> {
//            Toast.makeText(this, "डेटा मिळवताना त्रुटी: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        });
//    }
//
//    private void clearForm() {
//        etMilkCode.setText("");
//        etFatSampleCode.setText("");
//        etFatValue.setText("");
//        tvMilkType.setText("दुधाचा प्रकार: ");
//        tvFarmerMobile.setText("शेतकरी मोबाइल: ");
//        etMilkCode.requestFocus();
//
//        // Reset date and session to default
//        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//        String session = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
//        session = Integer.parseInt(session) < 12 ? "morning" : "evening";
//        etDate.setText(todayDate);
//        etSession.setText(session);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        // Clean up handler callbacks to prevent memory leaks
//        if (searchRunnable != null) {
//            handler.removeCallbacks(searchRunnable);
//        }
//    }
//}

package com.ss.smartdairy;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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

    private EditText etMilkCode, etFatSampleCode, etDate, etSession, etFatValue;
    private TextView tvMilkType, tvFarmerMobile;
    private Button btnUpdateFat;
    private String adminMobile;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long DEBOUNCE_DELAY = 500; // 500ms delay for debouncing

    // Make TextWatcher a field for control during programmatic changes
    private TextWatcher textWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_fat_entry);

        // Initialize UI elements with null checks
        etMilkCode = findViewById(R.id.etMilkCode);
        etFatSampleCode = findViewById(R.id.etFatSampleCode);
        etDate = findViewById(R.id.etDate);
        etSession = findViewById(R.id.etSession);
        etFatValue = findViewById(R.id.etFatValue);
        tvMilkType = findViewById(R.id.tvMilkType);
        tvFarmerMobile = findViewById(R.id.tvFarmerMobile);
        btnUpdateFat = findViewById(R.id.btnUpdateFat);

        if (etMilkCode == null || etFatSampleCode == null || etDate == null || etSession == null ||
                etFatValue == null || tvMilkType == null || tvFarmerMobile == null || btnUpdateFat == null) {
            Toast.makeText(this, "UI लोड करताना त्रुटी: लेआउट फाइल तपासा", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Firebase Authentication
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getPhoneNumber() == null) {
            Toast.makeText(this, "प्रयोक्ता लॉगिन केलेला नाही", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        adminMobile = user.getPhoneNumber().replace("+91", "");

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String session = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
        session = Integer.parseInt(session) < 12 ? "morning" : "evening";
        etDate.setText(todayDate);
        etSession.setText(session);

        // Updated TextWatcher defined as field
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
                    if (!milkCode.isEmpty() || !fatSampleCode.isEmpty()) {
                        searchRecord(milkCode, fatSampleCode, date, session);
                    } else {
                        tvMilkType.setText("दुधाचा प्रकार: ");
                        tvFarmerMobile.setText("शेतकरी मोबाइल: ");
                        // Do NOT clear milk/sample codes here to avoid infinite loops; remove these lines:
                        // etMilkCode.setText("");
                        // etFatSampleCode.setText("");
                    }
                };

                handler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
            }
        };

        etMilkCode.addTextChangedListener(textWatcher);
        etFatSampleCode.addTextChangedListener(textWatcher);

        btnUpdateFat.setOnClickListener(v -> validateAndUpdateFat());
    }

    private void searchRecord(String milkCode, String fatSampleCode, String date, String session) {
        if (date.isEmpty() || session.isEmpty()) {
            tvMilkType.setText("दुधाचा प्रकार: ");
            tvFarmerMobile.setText("शेतकरी मोबाइल: ");
            return;
        }

        try {
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
        } catch (Exception e) {
            tvMilkType.setText("दुधाचा प्रकार: ");
            tvFarmerMobile.setText("शेतकरी मोबाइल: ");
            return;
        }

        if (!session.equalsIgnoreCase("morning") && !session.equalsIgnoreCase("evening")) {
            tvMilkType.setText("दुधाचा प्रकार: ");
            tvFarmerMobile.setText("शेतकरी मोबाइल: ");
            return;
        }

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

                    // Updated strict matching logic
                    boolean milkCodeMatch = !milkCode.isEmpty() && storedMilkCode != null && storedMilkCode.equals(milkCode);
                    boolean fatSampleCodeMatch = !fatSampleCode.isEmpty() && storedFatSampleCode != null && storedFatSampleCode.equals(fatSampleCode);

                    boolean validMatch;
                    if (!milkCode.isEmpty() && !fatSampleCode.isEmpty()) {
                        // Both codes must match if both given
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

                        // Temporarily disable TextWatcher when setting text programmatically to avoid infinite loops
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
                tvMilkType.setText("दुधाचा प्रकार: ");
                tvFarmerMobile.setText("शेतकरी मोबाइल: ");
                // Do NOT reset milk/sample codes to avoid clearing user's input
                Toast.makeText(this, "रेकॉर्ड सापडला नाही", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            tvMilkType.setText("दुधाचा प्रकार: ");
            tvFarmerMobile.setText("शेतकरी मोबाइल: ");
            // Do NOT reset milk/sample codes
            Toast.makeText(this, "डेटा मिळवताना त्रुटी: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void validateAndUpdateFat() {
        String milkCode = etMilkCode.getText().toString().trim();
        String fatSampleCode = etFatSampleCode.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String session = etSession.getText().toString().trim();
        String fatStr = etFatValue.getText().toString().trim();

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
            if (fat < 2.0 || fat > 8.0) {
                Toast.makeText(this, "फॅट टक्केवारी 2.0 ते 8.0 च्या मध्ये असावी", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "अवैध फॅट टक्केवारी", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
        } catch (Exception e) {
            Toast.makeText(this, "अवैध तारीख स्वरूप (YYYY-MM-DD)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!session.equalsIgnoreCase("morning") && !session.equalsIgnoreCase("evening")) {
            Toast.makeText(this, "सत्र 'morning' किंवा 'evening' असावं", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference milkRef = FirebaseDatabase.getInstance()
                .getReference("Dairy").child("User").child(adminMobile).child("MilkCollection");

        milkRef.get().addOnSuccessListener(snapshot -> {
            boolean found = false;
            String targetFarmerMobile = null;
            String targetMilkType = null;

            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
                String farmerMobile = farmerSnap.getKey();
                DataSnapshot dateSnap = farmerSnap.child(date).child(session);

                for (DataSnapshot milkTypeSnap : dateSnap.getChildren()) {
                    String milkType = milkTypeSnap.getKey();
                    String storedMilkCode = milkTypeSnap.child("milkCode").getValue(String.class);
                    String storedFatSampleCode = milkTypeSnap.child("fatSampleCode").getValue(String.class);

                    // Updated strict matching logic
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
                        break;
                    }
                }
                if (found) break;
            }

            if (!found) {
                Toast.makeText(this, "रेकॉर्ड सापडला नाही", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference targetRef = milkRef.child(targetFarmerMobile).child(date).child(session).child(targetMilkType);
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("fat", fat);

            targetRef.updateChildren(updateData).addOnSuccessListener(unused -> {
                Toast.makeText(this, "फॅट टक्केवारी यशस्वीपणे अपडेट झाली", Toast.LENGTH_SHORT).show();
                clearForm();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "अपडेट अयशस्वी: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "डेटा मिळवताना त्रुटी: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void clearForm() {
        etMilkCode.setText("");
        etFatSampleCode.setText("");
        etFatValue.setText("");
        tvMilkType.setText("दुधाचा प्रकार: ");
        tvFarmerMobile.setText("शेतकरी मोबाइल: ");
        etMilkCode.requestFocus();

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String session = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
        session = Integer.parseInt(session) < 12 ? "morning" : "evening";
        etDate.setText(todayDate);
        etSession.setText(session);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }
    }
}