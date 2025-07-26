////////package com.ss.smartdairy;
////////
////////import android.os.Bundle;
////////
////////import androidx.activity.EdgeToEdge;
////////import androidx.appcompat.app.AppCompatActivity;
////////import androidx.core.graphics.Insets;
////////import androidx.core.view.ViewCompat;
////////import androidx.core.view.WindowInsetsCompat;
////////
////////public class MilkCollectionActivity extends AppCompatActivity {
////////
////////    @Override
////////    protected void onCreate(Bundle savedInstanceState) {
////////        super.onCreate(savedInstanceState);
////////        EdgeToEdge.enable(this);
////////        setContentView(R.layout.activity_milk_collection);
////////    }
////////}
//////
//////package com.ss.smartdairy;
//////
//////import android.os.Bundle;
//////import android.widget.Button;
//////import android.widget.EditText;
//////import android.widget.TextView;
//////import android.widget.Toast;
//////
//////import androidx.appcompat.app.AppCompatActivity;
//////
//////import com.google.firebase.auth.FirebaseAuth;
//////import com.google.firebase.auth.FirebaseUser;
//////import com.google.firebase.database.DataSnapshot;
//////import com.google.firebase.database.DatabaseReference;
//////import com.google.firebase.database.FirebaseDatabase;
//////
//////import java.text.SimpleDateFormat;
//////import java.util.Calendar;
//////import java.util.Date;
//////import java.util.HashMap;
//////import java.util.Locale;
//////import java.util.Map;
//////
//////public class MilkCollectionActivity extends AppCompatActivity {
//////
//////    private EditText etMilkCode, etMilkQuantity, etFatSampleCode, etFatValue;
//////    private TextView tvDate, tvSession, tvMilkType;
//////    private Button btnSave;
//////
//////    private String adminMobile;
//////    private String todayDate, session;
//////
//////    @Override
//////    protected void onCreate(Bundle savedInstanceState) {
//////        super.onCreate(savedInstanceState);
//////        setContentView(R.layout.activity_milk_collection);
//////
//////        // UI
//////        etMilkCode = findViewById(R.id.etMilkCode1);
//////        etMilkQuantity = findViewById(R.id.etMilkQuantity1);
//////        etFatSampleCode = findViewById(R.id.etFatSampleCode1);
//////        etFatValue = findViewById(R.id.etFatValue1);
//////        tvDate = findViewById(R.id.tvDate1);
//////        tvSession = findViewById(R.id.tvSession1);
//////        tvMilkType = findViewById(R.id.tvMilkType1);
//////        btnSave = findViewById(R.id.btnSaveMilkCollection1);
//////
//////        // Firebase current user
//////        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//////        if (user == null || user.getPhoneNumber() == null) {
//////            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
//////            finish();
//////            return;
//////        }
//////
//////        adminMobile = user.getPhoneNumber().replace("+91", "");
//////
//////        // Set date and session
//////        Calendar cal = Calendar.getInstance();
//////        session = cal.get(Calendar.HOUR_OF_DAY) < 12 ? "morning" : "evening";
//////        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//////
//////        tvDate.setText("Date: " + todayDate);
//////        tvSession.setText("Session: " + session);
//////
//////        btnSave.setOnClickListener(v -> validateAndSave());
//////    }
//////
//////    private void validateAndSave() {
//////        String milkCode = etMilkCode.getText().toString().trim();
//////        String quantityStr = etMilkQuantity.getText().toString().trim();
//////        String fatSampleCode = etFatSampleCode.getText().toString().trim();
//////        String fatStr = etFatValue.getText().toString().trim();
//////
//////        if (milkCode.isEmpty() || quantityStr.isEmpty()) {
//////            Toast.makeText(this, "Milk code and quantity required", Toast.LENGTH_SHORT).show();
//////            return;
//////        }
//////
//////        double quantity;
//////        try {
//////            quantity = Double.parseDouble(quantityStr);
//////        } catch (Exception e) {
//////            Toast.makeText(this, "Invalid quantity format", Toast.LENGTH_SHORT).show();
//////            return;
//////        }
//////
//////        double fat = fatStr.isEmpty() ? -1 : Double.parseDouble(fatStr);
//////
//////        // 🔍 Search for farmer by milkCode
//////        DatabaseReference farmersRef = FirebaseDatabase.getInstance()
//////                .getReference("Dairy")
//////                .child("User")
//////                .child(adminMobile)
//////                .child("Farmers");
//////
//////        farmersRef.get().addOnSuccessListener(snapshot -> {
//////            boolean found = false;
//////            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
//////                String farmerMobile = farmerSnap.getKey();
//////                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
//////                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
//////
//////                String milkType = null;
//////                if (cowCode != null && cowCode.equals(milkCode)) {
//////                    milkType = "cow";
//////                } else if (buffaloCode != null && buffaloCode.equals(milkCode)) {
//////                    milkType = "buffalo";
//////                }
//////
//////                if (milkType != null) {
//////                    saveMilkData(farmerMobile, milkCode, milkType, quantity, fatSampleCode, fat);
//////                    tvMilkType.setText("Milk Type: " + milkType);
//////                    found = true;
//////                    break;
//////                }
//////            }
//////
//////            if (!found) {
//////                Toast.makeText(this, "Milk code not found in farmer list", Toast.LENGTH_SHORT).show();
//////            }
//////
//////        }).addOnFailureListener(e -> {
//////            Toast.makeText(this, "Error fetching farmers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//////        });
//////    }
//////
//////    private void saveMilkData(String farmerMobile, String milkCode, String milkType,
//////                              double quantity, String fatSampleCode, double fat) {
//////
//////        DatabaseReference milkRef = FirebaseDatabase.getInstance()
//////                .getReference("Dairy")
//////                .child("User")
//////                .child(adminMobile)
//////                .child("MilkCollection")
//////                .child(farmerMobile)
//////                .child(todayDate)
//////                .child(session);
//////
//////        Map<String, Object> data = new HashMap<>();
//////        data.put("milkCode", milkCode);
//////        data.put("milkType", milkType);
//////        data.put("quantity", quantity);
//////        data.put("fatSampleCode", fatSampleCode);
//////        if (fat != -1) data.put("fat", fat);
//////        data.put("timestamp", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
//////
//////        milkRef.setValue(data)
//////                .addOnSuccessListener(unused -> {
//////                    Toast.makeText(this, "Milk data saved!", Toast.LENGTH_SHORT).show();
//////                    finish();
//////                })
//////                .addOnFailureListener(e -> {
//////                    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//////                });
//////    }
//////}
////
//////
//////package com.ss.smartdairy;
//////
//////import android.os.Bundle;
//////import android.widget.Button;
//////import android.widget.EditText;
//////import android.widget.TextView;
//////import android.widget.Toast;
//////
//////import androidx.appcompat.app.AppCompatActivity;
//////
//////import com.google.firebase.auth.FirebaseAuth;
//////import com.google.firebase.auth.FirebaseUser;
//////import com.google.firebase.database.DataSnapshot;
//////import com.google.firebase.database.DatabaseReference;
//////import com.google.firebase.database.FirebaseDatabase;
//////
//////import java.text.SimpleDateFormat;
//////import java.util.Calendar;
//////import java.util.Date;
//////import java.util.HashMap;
//////import java.util.Locale;
//////import java.util.Map;
//////
//////public class MilkCollectionActivity extends AppCompatActivity {
//////
//////    private EditText etMilkCode, etMilkQuantity, etFatSampleCode, etFatValue;
//////    private TextView tvDate, tvSession, tvMilkType;
//////    private Button btnSave;
//////
//////    private String adminMobile;
//////    private String todayDate, session;
//////
//////    @Override
//////    protected void onCreate(Bundle savedInstanceState) {
//////        super.onCreate(savedInstanceState);
//////        setContentView(R.layout.activity_milk_collection);
//////
//////        // UI
//////        etMilkCode = findViewById(R.id.etMilkCode1);
//////        etMilkQuantity = findViewById(R.id.etMilkQuantity1);
//////        etFatSampleCode = findViewById(R.id.etFatSampleCode1);
//////        etFatValue = findViewById(R.id.etFatValue1);
//////        tvDate = findViewById(R.id.tvDate1);
//////        tvSession = findViewById(R.id.tvSession1);
//////        tvMilkType = findViewById(R.id.tvMilkType1);
//////        btnSave = findViewById(R.id.btnSaveMilkCollection1);
//////
//////        // Firebase current user
//////        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//////        if (user == null || user.getPhoneNumber() == null) {
//////            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
//////            finish();
//////            return;
//////        }
//////
//////        adminMobile = user.getPhoneNumber().replace("+91", "");
//////
//////        // Set date and session
//////        Calendar cal = Calendar.getInstance();
//////        session = cal.get(Calendar.HOUR_OF_DAY) < 12 ? "morning" : "evening";
//////        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//////
//////        tvDate.setText("Date: " + todayDate);
//////        tvSession.setText("Session: " + session);
//////
//////        btnSave.setOnClickListener(v -> validateAndSave());
//////    }
//////
////////    private void validateAndSave() {
////////        String milkCode = etMilkCode.getText().toString().trim();
////////        String quantityStr = etMilkQuantity.getText().toString().trim();
////////        String fatSampleCode = etFatSampleCode.getText().toString().trim();
////////        String fatStr = etFatValue.getText().toString().trim();
////////
////////        if (milkCode.isEmpty() || quantityStr.isEmpty()) {
////////            Toast.makeText(this, "Milk code and quantity required", Toast.LENGTH_SHORT).show();
////////            return;
////////        }
////////
////////        double quantity;
////////        try {
////////            quantity = Double.parseDouble(quantityStr);
////////        } catch (Exception e) {
////////            Toast.makeText(this, "Invalid quantity format", Toast.LENGTH_SHORT).show();
////////            return;
////////        }
////////
////////        double fat = fatStr.isEmpty() ? -1 : Double.parseDouble(fatStr);
////////
////////        // 🔍 Search for farmer by milkCode
////////        DatabaseReference farmersRef = FirebaseDatabase.getInstance()
////////                .getReference("Dairy")
////////                .child("User")
////////                .child(adminMobile)
////////                .child("Farmers");
////////
////////        farmersRef.get().addOnSuccessListener(snapshot -> {
////////            boolean found = false;
////////            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
////////                String farmerMobile = farmerSnap.getKey();
////////                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
////////                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
////////
////////                String milkType = null;
////////                if (cowCode != null && cowCode.equals(milkCode)) {
////////                    milkType = "cow";
////////                } else if (buffaloCode != null && buffaloCode.equals(milkCode)) {
////////                    milkType = "buffalo";
////////                }
////////
////////                if (milkType != null) {
////////                    tvMilkType.setText("Milk Type: " + milkType); // ✅ Show milk type
////////                    saveMilkData(farmerMobile, milkCode, milkType, quantity, fatSampleCode, fat); // ✅ Save per type
////////                    found = true;
////////                    break;
////////                }
////////            }
////////
////////            if (!found) {
////////                Toast.makeText(this, "Milk code not found in farmer list", Toast.LENGTH_SHORT).show();
////////            }
////////
////////        }).addOnFailureListener(e -> {
////////            Toast.makeText(this, "Error fetching farmers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
////////        });
////////    }
//////
//////    private void validateAndSave() {
//////        String milkCode = etMilkCode.getText().toString().trim();
//////        String quantityStr = etMilkQuantity.getText().toString().trim();
//////        String fatSampleCode = etFatSampleCode.getText().toString().trim();
//////        String fatStr = etFatValue.getText().toString().trim();
//////
//////        if (milkCode.isEmpty() || quantityStr.isEmpty()) {
//////            Toast.makeText(this, "Milk code and quantity required", Toast.LENGTH_SHORT).show();
//////            return;
//////        }
//////
//////        double quantity;
//////        try {
//////            quantity = Double.parseDouble(quantityStr);
//////        } catch (Exception e) {
//////            Toast.makeText(this, "Invalid quantity format", Toast.LENGTH_SHORT).show();
//////            return;
//////        }
//////
//////        double fat = fatStr.isEmpty() ? -1 : Double.parseDouble(fatStr);
//////
//////        DatabaseReference farmersRef = FirebaseDatabase.getInstance()
//////                .getReference("Dairy")
//////                .child("User")
//////                .child(adminMobile)
//////                .child("Farmers");
//////
//////        farmersRef.get().addOnSuccessListener(snapshot -> {
//////            boolean found = false;
//////
//////            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
//////                String farmerMobile = farmerSnap.getKey();
//////                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
//////                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
//////
//////                boolean isCow = cowCode != null && cowCode.equals(milkCode);
//////                boolean isBuffalo = buffaloCode != null && buffaloCode.equals(milkCode);
//////
//////                if (isCow || isBuffalo) {
//////                    StringBuilder typeBuilder = new StringBuilder("Milk Type: ");
//////                    if (isCow) {
//////                        typeBuilder.append("cow");
//////                        saveMilkData(farmerMobile, milkCode, "cow", quantity, fatSampleCode, fat);
//////                    }
//////                    if (isBuffalo) {
//////                        if (isCow) typeBuilder.append(" & ");
//////                        typeBuilder.append("buffalo");
//////                        saveMilkData(farmerMobile, milkCode, "buffalo", quantity, fatSampleCode, fat);
//////                    }
//////
//////                    tvMilkType.setText(typeBuilder.toString());
//////                    found = true;
//////                    break;
//////                }
//////            }
//////
//////            if (!found) {
//////                tvMilkType.setText("Milk Type: Not found");
//////                Toast.makeText(this, "Milk code not found in farmer list", Toast.LENGTH_SHORT).show();
//////            }
//////
//////        }).addOnFailureListener(e -> {
//////            Toast.makeText(this, "Error fetching farmers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//////        });
//////    }
//////
//////
//////
//////    private void saveMilkData(String farmerMobile, String milkCode, String milkType,
//////                              double quantity, String fatSampleCode, double fat) {
//////
//////        DatabaseReference milkRef = FirebaseDatabase.getInstance()
//////                .getReference("Dairy")
//////                .child("User")
//////                .child(adminMobile)
//////                .child("MilkCollection")
//////                .child(farmerMobile)
//////                .child(todayDate)
//////                .child(session)
//////                .child(milkType); // ✅ Save separately for cow & buffalo
//////
//////        Map<String, Object> data = new HashMap<>();
//////        data.put("milkCode", milkCode);
//////        data.put("milkType", milkType);
//////        data.put("quantity", quantity);
//////        data.put("fatSampleCode", fatSampleCode);
//////        if (fat != -1) data.put("fat", fat);
//////        data.put("timestamp", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
//////
//////        milkRef.setValue(data)
//////                .addOnSuccessListener(unused -> {
//////                    Toast.makeText(this, "Milk data saved for " + milkType, Toast.LENGTH_SHORT).show();
//////                    clearForm();
//////                })
//////                .addOnFailureListener(e -> {
//////                    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//////                });
//////    }
//////
//////    private void clearForm() {
//////        etMilkCode.setText("");
//////        etMilkQuantity.setText("");
//////        etFatSampleCode.setText("");
//////        etFatValue.setText("");
//////        tvMilkType.setText("");
//////        etMilkCode.requestFocus();
//////    }
//////}
////
////
////
////
////
////
////
////package com.ss.smartdairy;
////
////import android.os.Bundle;
////import android.text.Editable;
////import android.text.TextWatcher;
////import android.widget.Button;
////import android.widget.EditText;
////import android.widget.TextView;
////import android.widget.Toast;
////
////import androidx.appcompat.app.AppCompatActivity;
////
////import com.google.firebase.auth.FirebaseAuth;
////import com.google.firebase.auth.FirebaseUser;
////import com.google.firebase.database.DataSnapshot;
////import com.google.firebase.database.DatabaseReference;
////import com.google.firebase.database.FirebaseDatabase;
////
////import java.text.SimpleDateFormat;
////import java.util.Calendar;
////import java.util.Date;
////import java.util.HashMap;
////import java.util.Locale;
////import java.util.Map;
////
////public class MilkCollectionActivity extends AppCompatActivity {
////
////    private EditText etMilkCode, etMilkQuantity, etFatSampleCode, etFatValue;
////    private TextView tvDate, tvSession, tvMilkType;
////    private Button btnSave;
////
////    private String adminMobile;
////    private String todayDate, session;
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_milk_collection);
////
////        // UI
////        etMilkCode = findViewById(R.id.etMilkCode1);
////        etMilkQuantity = findViewById(R.id.etMilkQuantity1);
////        etFatSampleCode = findViewById(R.id.etFatSampleCode1);
////        etFatValue = findViewById(R.id.etFatValue1);
////        tvDate = findViewById(R.id.tvDate1);
////        tvSession = findViewById(R.id.tvSession1);
////        tvMilkType = findViewById(R.id.tvMilkType1);
////        btnSave = findViewById(R.id.btnSaveMilkCollection1);
////
////        // Firebase current user
////        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
////        if (user == null || user.getPhoneNumber() == null) {
////            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
////            finish();
////            return;
////        }
////
////        adminMobile = user.getPhoneNumber().replace("+91", "");
////
////        // Set date and session
////        Calendar cal = Calendar.getInstance();
////        session = cal.get(Calendar.HOUR_OF_DAY) < 12 ? "morning" : "evening";
////        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
////
////        tvDate.setText("Date: " + todayDate);
////        tvSession.setText("Session: " + session);
////
////        // Add TextWatcher to auto-fetch milk type
////        etMilkCode.addTextChangedListener(new TextWatcher() {
////            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
////            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
////            @Override
////            public void afterTextChanged(Editable s) {
////                if (s.length() >= 2) {
////                    fetchMilkType(s.toString().trim());
////                } else {
////                    tvMilkType.setText("");
////                }
////            }
////        });
////
////        btnSave.setOnClickListener(v -> validateAndSave());
////    }
////
////    private void fetchMilkType(String milkCode) {
////        DatabaseReference farmersRef = FirebaseDatabase.getInstance()
////                .getReference("Dairy")
////                .child("User")
////                .child(adminMobile)
////                .child("Farmers");
////
////        farmersRef.get().addOnSuccessListener(snapshot -> {
////            boolean found = false;
////            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
////                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
////                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
////
////                boolean isCow = cowCode != null && cowCode.equals(milkCode);
////                boolean isBuffalo = buffaloCode != null && buffaloCode.equals(milkCode);
////
////                if (isCow || isBuffalo) {
////                    StringBuilder typeBuilder = new StringBuilder("Milk Type: ");
////                    if (isCow) typeBuilder.append("cow");
////                    if (isBuffalo) {
////                        if (isCow) typeBuilder.append(" & ");
////                        typeBuilder.append("buffalo");
////                    }
////
////                    tvMilkType.setText(typeBuilder.toString());
////                    found = true;
////                    break;
////                }
////            }
////
////            if (!found) {
////                tvMilkType.setText("Milk Type: Not found");
////            }
////
////        }).addOnFailureListener(e -> {
////            Toast.makeText(this, "Error fetching milk type: " + e.getMessage(), Toast.LENGTH_SHORT).show();
////        });
////    }
////
////    private void validateAndSave() {
////        String milkCode = etMilkCode.getText().toString().trim();
////        String quantityStr = etMilkQuantity.getText().toString().trim();
////        String fatSampleCode = etFatSampleCode.getText().toString().trim();
////        String fatStr = etFatValue.getText().toString().trim();
////
////        if (milkCode.isEmpty() || quantityStr.isEmpty()) {
////            Toast.makeText(this, "Milk code and quantity required", Toast.LENGTH_SHORT).show();
////            return;
////        }
////
////        double quantity;
////        try {
////            quantity = Double.parseDouble(quantityStr);
////        } catch (Exception e) {
////            Toast.makeText(this, "Invalid quantity format", Toast.LENGTH_SHORT).show();
////            return;
////        }
////
////        double fat = fatStr.isEmpty() ? -1 : Double.parseDouble(fatStr);
////
////        DatabaseReference farmersRef = FirebaseDatabase.getInstance()
////                .getReference("Dairy")
////                .child("User")
////                .child(adminMobile)
////                .child("Farmers");
////
////        farmersRef.get().addOnSuccessListener(snapshot -> {
////            boolean found = false;
////
////            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
////                String farmerMobile = farmerSnap.getKey();
////                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
////                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
////
////                boolean isCow = cowCode != null && cowCode.equals(milkCode);
////                boolean isBuffalo = buffaloCode != null && buffaloCode.equals(milkCode);
////
////                if (isCow || isBuffalo) {
////                    if (isCow) {
////                        saveMilkData(farmerMobile, milkCode, "cow", quantity, fatSampleCode, fat);
////                    }
////                    if (isBuffalo) {
////                        saveMilkData(farmerMobile, milkCode, "buffalo", quantity, fatSampleCode, fat);
////                    }
////                    found = true;
////                    break;
////                }
////            }
////
////            if (!found) {
////                Toast.makeText(this, "Milk code not found in farmer list", Toast.LENGTH_SHORT).show();
////            }
////
////        }).addOnFailureListener(e -> {
////            Toast.makeText(this, "Error fetching farmers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
////        });
////    }
////
////    private void saveMilkData(String farmerMobile, String milkCode, String milkType,
////                              double quantity, String fatSampleCode, double fat) {
////
////        DatabaseReference milkRef = FirebaseDatabase.getInstance()
////                .getReference("Dairy")
////                .child("User")
////                .child(adminMobile)
////                .child("MilkCollection")
////                .child(farmerMobile)
////                .child(todayDate)
////                .child(session)
////                .child(milkType);
////
////        Map<String, Object> data = new HashMap<>();
////        data.put("milkCode", milkCode);
////        data.put("milkType", milkType);
////        data.put("quantity", quantity);
////        data.put("fatSampleCode", fatSampleCode);
////        if (fat != -1) data.put("fat", fat);
////        data.put("timestamp", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
////
////        milkRef.setValue(data)
////                .addOnSuccessListener(unused -> {
////                    Toast.makeText(this, "Milk data saved for " + milkType, Toast.LENGTH_SHORT).show();
////                    clearForm();
////                })
////                .addOnFailureListener(e -> {
////                    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
////                });
////    }
////
////    private void clearForm() {
////        etMilkCode.setText("");
////        etMilkQuantity.setText("");
////        etFatSampleCode.setText("");
////        etFatValue.setText("");
////        tvMilkType.setText("");
////        etMilkCode.requestFocus();
////    }
////}
//
//
////package com.ss.smartdairy;
////
////import android.os.Bundle;
////import android.text.Editable;
////import android.text.TextWatcher;
////import android.widget.Button;
////import android.widget.EditText;
////import android.widget.TextView;
////import android.widget.Toast;
////
////import androidx.appcompat.app.AppCompatActivity;
////
////import com.google.firebase.auth.FirebaseAuth;
////import com.google.firebase.auth.FirebaseUser;
////import com.google.firebase.database.DataSnapshot;
////import com.google.firebase.database.DatabaseReference;
////import com.google.firebase.database.FirebaseDatabase;
////
////import java.text.SimpleDateFormat;
////import java.util.Calendar;
////import java.util.Date;
////import java.util.HashMap;
////import java.util.Locale;
////import java.util.Map;
////
////public class MilkCollectionActivity extends AppCompatActivity {
////
////    private EditText etMilkCode, etMilkQuantity, etFatSampleCode, etFatValue, etFarmerName;
////    private TextView tvDate, tvSession, tvMilkType;
////    private Button btnSave;
////
////    private String adminMobile;
////    private String todayDate, session;
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_milk_collection);
////
////        etMilkCode = findViewById(R.id.etMilkCode1);
////        etMilkQuantity = findViewById(R.id.etMilkQuantity1);
////        etFatSampleCode = findViewById(R.id.etFatSampleCode1);
////        etFatValue = findViewById(R.id.etFatValue1);
////        etFarmerName = findViewById(R.id.etFarmerName1);
////        tvDate = findViewById(R.id.tvDate1);
////        tvSession = findViewById(R.id.tvSession1);
////        tvMilkType = findViewById(R.id.tvMilkType1);
////        btnSave = findViewById(R.id.btnSaveMilkCollection1);
////
////        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
////        if (user == null || user.getPhoneNumber() == null) {
////            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
////            finish();
////            return;
////        }
////
////        adminMobile = user.getPhoneNumber().replace("+91", "");
////
////        Calendar cal = Calendar.getInstance();
////        session = cal.get(Calendar.HOUR_OF_DAY) < 12 ? "morning" : "evening";
////        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
////
////        tvDate.setText("Date: " + todayDate);
////        tvSession.setText("Session: " + session);
////
////        etMilkCode.addTextChangedListener(new TextWatcher() {
////            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
////            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
////            @Override
////            public void afterTextChanged(Editable s) {
////                if (s.length() >= 2) {
////                    fetchMilkTypeAndName(s.toString().trim());
////                } else {
////                    tvMilkType.setText("");
////                    etFarmerName.setText("");
////                }
////            }
////        });
////
////        etFarmerName.addTextChangedListener(new TextWatcher() {
////            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
////            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
////            @Override
////            public void afterTextChanged(Editable s) {
////                if (s.length() >= 2) {
////                    fetchMilkCodeByName(s.toString().trim());
////                }
////            }
////        });
////
////        btnSave.setOnClickListener(v -> validateAndSave());
////    }
////
////    private void fetchMilkTypeAndName(String milkCode) {
////        DatabaseReference ref = FirebaseDatabase.getInstance()
////                .getReference("Dairy").child("User").child(adminMobile).child("Farmers");
////
////        ref.get().addOnSuccessListener(snapshot -> {
////            boolean found = false;
////            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
////                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
////                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
////                String farmerName = farmerSnap.child("name").getValue(String.class);
////
////                boolean isCow = cowCode != null && cowCode.equals(milkCode);
////                boolean isBuffalo = buffaloCode != null && buffaloCode.equals(milkCode);
////
////                if (isCow || isBuffalo) {
////                    StringBuilder typeBuilder = new StringBuilder("Milk Type: ");
////                    if (isCow) typeBuilder.append("cow");
////                    if (isBuffalo) {
////                        if (isCow) typeBuilder.append(" & ");
////                        typeBuilder.append("buffalo");
////                    }
////                    tvMilkType.setText(typeBuilder.toString());
////                    etFarmerName.setText(farmerName != null ? farmerName : "");
////                    found = true;
////                    break;
////                }
////            }
////            if (!found) {
////                tvMilkType.setText("Milk Type: Not found");
////                etFarmerName.setText("");
////            }
////        });
////    }
////
////    private void fetchMilkCodeByName(String name) {
////        DatabaseReference ref = FirebaseDatabase.getInstance()
////                .getReference("Dairy").child("User").child(adminMobile).child("Farmers");
////
////        ref.get().addOnSuccessListener(snapshot -> {
////            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
////                String farmerName = farmerSnap.child("name").getValue(String.class);
////                if (farmerName != null && farmerName.equalsIgnoreCase(name)) {
////                    String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
////                    String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
////                    StringBuilder codeBuilder = new StringBuilder();
////                    if (cowCode != null) codeBuilder.append(cowCode);
////                    else if (buffaloCode != null) codeBuilder.append(buffaloCode);
////                    etMilkCode.setText(codeBuilder.toString());
////                    break;
////                }
////            }
////        });
////    }
////
////    private void validateAndSave() {
////        String milkCode = etMilkCode.getText().toString().trim();
////        String quantityStr = etMilkQuantity.getText().toString().trim();
////        String fatSampleCode = etFatSampleCode.getText().toString().trim();
////        String fatStr = etFatValue.getText().toString().trim();
////
////        if (milkCode.isEmpty() || quantityStr.isEmpty()) {
////            Toast.makeText(this, "Milk code and quantity required", Toast.LENGTH_SHORT).show();
////            return;
////        }
////
////        double quantity;
////        try {
////            quantity = Double.parseDouble(quantityStr);
////        } catch (Exception e) {
////            Toast.makeText(this, "Invalid quantity format", Toast.LENGTH_SHORT).show();
////            return;
////        }
////
////        double fat = fatStr.isEmpty() ? -1 : Double.parseDouble(fatStr);
////
////        DatabaseReference farmersRef = FirebaseDatabase.getInstance()
////                .getReference("Dairy").child("User").child(adminMobile).child("Farmers");
////
////        farmersRef.get().addOnSuccessListener(snapshot -> {
////            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
////                String farmerMobile = farmerSnap.getKey();
////                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
////                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
////
////                boolean isCow = cowCode != null && cowCode.equals(milkCode);
////                boolean isBuffalo = buffaloCode != null && buffaloCode.equals(milkCode);
////
////                if (isCow) saveMilkData(farmerMobile, milkCode, "cow", quantity, fatSampleCode, fat);
////                if (isBuffalo) saveMilkData(farmerMobile, milkCode, "buffalo", quantity, fatSampleCode, fat);
////
////                if (isCow || isBuffalo) return;
////            }
////            Toast.makeText(this, "Milk code not found in farmer list", Toast.LENGTH_SHORT).show();
////        });
////    }
////
////    private void saveMilkData(String farmerMobile, String milkCode, String milkType,
////                              double quantity, String fatSampleCode, double fat) {
////        DatabaseReference milkRef = FirebaseDatabase.getInstance()
////                .getReference("Dairy").child("User").child(adminMobile)
////                .child("MilkCollection").child(farmerMobile)
////                .child(todayDate).child(session).child(milkType);
////
////        Map<String, Object> data = new HashMap<>();
////        data.put("milkCode", milkCode);
////        data.put("milkType", milkType);
////        data.put("quantity", quantity);
////        data.put("fatSampleCode", fatSampleCode);
////        if (fat != -1) data.put("fat", fat);
////        data.put("timestamp", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
////
////        milkRef.setValue(data).addOnSuccessListener(unused -> {
////            Toast.makeText(this, "Milk data saved for " + milkType, Toast.LENGTH_SHORT).show();
////            clearForm();
////        }).addOnFailureListener(e -> {
////            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
////        });
////    }
////
////    private void clearForm() {
////        etMilkCode.setText("");
////        etMilkQuantity.setText("");
////        etFatSampleCode.setText("");
////        etFatValue.setText("");
////        etFarmerName.setText("");
////        tvMilkType.setText("");
////        etMilkCode.requestFocus();
////    }
////}
//
//
//package com.ss.smartdairy;
//
//import android.os.Bundle;
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
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Locale;
//import java.util.Map;
//
//public class MilkCollectionActivity extends AppCompatActivity {
//
//    private EditText etMilkCode, etMilkQuantity, etFatSampleCode, etFatValue, etFarmerName;
//    private TextView tvDate, tvSession, tvMilkType;
//    private Button btnSave;
//
//    private String adminMobile;
//    private String todayDate, session;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_milk_collection);
//
//        etMilkCode = findViewById(R.id.etMilkCode1);
//        etMilkQuantity = findViewById(R.id.etMilkQuantity1);
//        etFatSampleCode = findViewById(R.id.etFatSampleCode1);
//        etFatValue = findViewById(R.id.etFatValue1);
//        etFarmerName = findViewById(R.id.etFarmerName1);
//        tvDate = findViewById(R.id.tvDate1);
//        tvSession = findViewById(R.id.tvSession1);
//        tvMilkType = findViewById(R.id.tvMilkType1);
//        btnSave = findViewById(R.id.btnSaveMilkCollection1);
//
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user == null || user.getPhoneNumber() == null) {
//            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        adminMobile = user.getPhoneNumber().replace("+91", "");
//
//        Calendar cal = Calendar.getInstance();
//        session = cal.get(Calendar.HOUR_OF_DAY) < 12 ? "morning" : "evening";
//        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//
//        tvDate.setText("Date: " + todayDate);
//        tvSession.setText("Session: " + session);
//
//        etMilkCode.addTextChangedListener(new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
//            @Override
//            public void afterTextChanged(Editable s) {
//                String code = s.toString().trim();
//                if (code.length() >= 2) {
//                    fetchMilkTypeAndNameOnly(code);
//                } else {
//                    tvMilkType.setText("");
//                    etFarmerName.setText("");
//                }
//            }
//        });
//
//        btnSave.setOnClickListener(v -> validateAndSave());
//    }
//
//    private void fetchMilkTypeAndNameOnly(String milkCode) {
//        DatabaseReference ref = FirebaseDatabase.getInstance()
//                .getReference("Dairy").child("User").child(adminMobile).child("Farmers");
//
//        ref.get().addOnSuccessListener(snapshot -> {
//            boolean found = false;
//
//            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
//                String farmerName = farmerSnap.child("name").getValue(String.class);
//                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
//                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
//
//                if (cowCode != null && cowCode.equals(milkCode)) {
//                    tvMilkType.setText("Milk Type: cow");
//                    etFarmerName.setText(farmerName != null ? farmerName : "");
//                    found = true;
//                    break;
//                } else if (buffaloCode != null && buffaloCode.equals(milkCode)) {
//                    tvMilkType.setText("Milk Type: buffalo");
//                    etFarmerName.setText(farmerName != null ? farmerName : "");
//                    found = true;
//                    break;
//                }
//            }
//
//            if (!found) {
//                tvMilkType.setText("Milk Type: Not found");
//                etFarmerName.setText("");
//            }
//        }).addOnFailureListener(e -> {
//            Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        });
//    }
//
//    private void validateAndSave() {
//        String milkCode = etMilkCode.getText().toString().trim();
//        String quantityStr = etMilkQuantity.getText().toString().trim();
//        String fatSampleCode = etFatSampleCode.getText().toString().trim();
//        String fatStr = etFatValue.getText().toString().trim();
//
//        if (milkCode.isEmpty() || quantityStr.isEmpty()) {
//            Toast.makeText(this, "Milk code and quantity required", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        double quantity;
//        try {
//            quantity = Double.parseDouble(quantityStr);
//        } catch (Exception e) {
//            Toast.makeText(this, "Invalid quantity format", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        double fat = fatStr.isEmpty() ? -1 : Double.parseDouble(fatStr);
//
//        DatabaseReference farmersRef = FirebaseDatabase.getInstance()
//                .getReference("Dairy").child("User").child(adminMobile).child("Farmers");
//
//        farmersRef.get().addOnSuccessListener(snapshot -> {
//            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
//                String farmerMobile = farmerSnap.getKey();
//                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
//                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
//
//                if (cowCode != null && cowCode.equals(milkCode)) {
//                    saveMilkData(farmerMobile, milkCode, "cow", quantity, fatSampleCode, fat);
//                    return;
//                } else if (buffaloCode != null && buffaloCode.equals(milkCode)) {
//                    saveMilkData(farmerMobile, milkCode, "buffalo", quantity, fatSampleCode, fat);
//                    return;
//                }
//            }
//
//            Toast.makeText(this, "Milk code not found", Toast.LENGTH_SHORT).show();
//        });
//    }
//
//    private void saveMilkData(String farmerMobile, String milkCode, String milkType,
//                              double quantity, String fatSampleCode, double fat) {
//
//        DatabaseReference milkRef = FirebaseDatabase.getInstance()
//                .getReference("Dairy")
//                .child("User").child(adminMobile)
//                .child("MilkCollection").child(farmerMobile)
//                .child(todayDate).child(session).child(milkType);
//
//        Map<String, Object> data = new HashMap<>();
//        data.put("milkCode", milkCode);
//        data.put("milkType", milkType);
//        data.put("quantity", quantity);
//        data.put("fatSampleCode", fatSampleCode);
//        if (fat != -1) data.put("fat", fat);
//        data.put("timestamp", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
//
//        milkRef.setValue(data).addOnSuccessListener(unused -> {
//            Toast.makeText(this, "Milk data saved for " + milkType, Toast.LENGTH_SHORT).show();
//            clearForm();
//        }).addOnFailureListener(e -> {
//            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        });
//    }
//
//    private void clearForm() {
//        etMilkCode.setText("");
//        etMilkQuantity.setText("");
//        etFatSampleCode.setText("");
//        etFatValue.setText("");
//        etFarmerName.setText("");
//        tvMilkType.setText("");
//        etMilkCode.requestFocus();
//    }
//}
//


package com.ss.smartdairy;

import android.app.AlertDialog;
import android.os.Bundle;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MilkCollectionActivity extends AppCompatActivity {

    private EditText etMilkCode, etMilkQuantity, etFatSampleCode, etFatValue, etFarmerName;
    private TextView tvDate, tvSession, tvMilkType;
    private Button btnSave;

    private String adminMobile;
    private String todayDate, session;
    private boolean isUpdatingFromName = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_milk_collection);

        etMilkCode = findViewById(R.id.etMilkCode1);
        etMilkQuantity = findViewById(R.id.etMilkQuantity1);
        etFatSampleCode = findViewById(R.id.etFatSampleCode1);
        etFatValue = findViewById(R.id.etFatValue1);
        etFarmerName = findViewById(R.id.etFarmerName1);
        tvDate = findViewById(R.id.tvDate1);
        tvSession = findViewById(R.id.tvSession1);
        tvMilkType = findViewById(R.id.tvMilkType1);
        btnSave = findViewById(R.id.btnSaveMilkCollection1);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getPhoneNumber() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adminMobile = user.getPhoneNumber().replace("+91", "");

        Calendar cal = Calendar.getInstance();
        session = cal.get(Calendar.HOUR_OF_DAY) < 12 ? "morning" : "evening";
        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        tvDate.setText("Date: " + todayDate);
        tvSession.setText("Session: " + session);

        etMilkCode.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingFromName) {
                    isUpdatingFromName = false;
                    return;
                }

                String code = s.toString().trim();
                if (code.length() >= 2) {
                    fetchMilkTypeAndNameOnly(code);
                } else {
                    tvMilkType.setText("");
                    etFarmerName.setText("");
                }
            }
        });

        etFarmerName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String name = s.toString().trim();
                if (name.length() >= 2) {
                    fetchMilkCodeByName(name);
                }
            }
        });

        btnSave.setOnClickListener(v -> validateAndSave());
    }

    //    private void fetchMilkTypeAndNameOnly(String milkCode) {
//        DatabaseReference ref = FirebaseDatabase.getInstance()
//                .getReference("Dairy").child("User").child(adminMobile).child("Farmers");
//
//        ref.get().addOnSuccessListener(snapshot -> {
//            boolean found = false;
//
//            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
//                String farmerName = farmerSnap.child("name").getValue(String.class);
//                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
//                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
//
//                if (cowCode != null && cowCode.equals(milkCode)) {
//                    tvMilkType.setText("Milk Type: cow");
//                    etFarmerName.setText(farmerName != null ? farmerName : "");
//                    found = true;
//                    break;
//                } else if (buffaloCode != null && buffaloCode.equals(milkCode)) {
//                    tvMilkType.setText("Milk Type: buffalo");
//                    etFarmerName.setText(farmerName != null ? farmerName : "");
//                    found = true;
//                    break;
//                }
//            }
//
//            if (!found) {
//                tvMilkType.setText("Milk Type: Not found");
//                etFarmerName.setText("");
//            }
//        }).addOnFailureListener(e -> {
//            Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        });
//    }
    private void fetchMilkTypeAndNameOnly(String milkCode) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Dairy").child("User").child(adminMobile).child("Farmers");

        ref.get().addOnSuccessListener(snapshot -> {
            boolean found = false;

            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
                String farmerName = farmerSnap.child("name").getValue(String.class);
                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);

                if (cowCode != null && cowCode.equals(milkCode)) {
                    tvMilkType.setText("Milk Type: cow");
                    etFarmerName.setText(farmerName != null ? farmerName : "");
                    found = true;
                    break;
                } else if (buffaloCode != null && buffaloCode.equals(milkCode)) {
                    tvMilkType.setText("Milk Type: buffalo");
                    etFarmerName.setText(farmerName != null ? farmerName : "");
                    found = true;
                    break;
                }
            }

            if (!found) {
                tvMilkType.setText("Milk Type: Not found");
                etFarmerName.setText("");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    private void fetchMilkCodeByName(String name) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Dairy").child("User").child(adminMobile).child("Farmers");

        ref.get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
                String farmerName = farmerSnap.child("name").getValue(String.class);
                if (farmerName != null && farmerName.equalsIgnoreCase(name)) {
                    String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
                    String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);

                    if (cowCode != null && buffaloCode != null) {
                        showMilkTypeSelectionDialog(cowCode, buffaloCode);
                    } else {
                        String selectedCode = (cowCode != null) ? cowCode : buffaloCode;
                        String selectedType = (cowCode != null) ? "cow" : "buffalo";

                        isUpdatingFromName = true;
                        etMilkCode.setText(selectedCode);
                        tvMilkType.setText("Milk Type: " + selectedType);
                    }
                    break;
                }
            }
        });
    }


    private void showMilkTypeSelectionDialog(String cowCode, String buffaloCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Milk Type");

        String[] options = {
                "🐄 Cow: " + cowCode,
                "🐃 Buffalo: " + buffaloCode
        };

        builder.setItems(options, (dialog, which) -> {
            isUpdatingFromName = true;
            if (which == 0) {
                etMilkCode.setText(cowCode);
                tvMilkType.setText("Milk Type: cow");
            } else {
                etMilkCode.setText(buffaloCode);
                tvMilkType.setText("Milk Type: buffalo");
            }
        });

        builder.show();
    }

    private void validateAndSave() {
        String milkCode = etMilkCode.getText().toString().trim();
        String quantityStr = etMilkQuantity.getText().toString().trim();
        String fatSampleCode = etFatSampleCode.getText().toString().trim();
        String fatStr = etFatValue.getText().toString().trim();

        if (milkCode.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(this, "Milk code and quantity required", Toast.LENGTH_SHORT).show();
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityStr);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid quantity format", Toast.LENGTH_SHORT).show();
            return;
        }

        double fat = fatStr.isEmpty() ? -1 : Double.parseDouble(fatStr);

        DatabaseReference farmersRef = FirebaseDatabase.getInstance()
                .getReference("Dairy").child("User").child(adminMobile).child("Farmers");

        farmersRef.get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
                String farmerMobile = farmerSnap.getKey();
                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);

                if (cowCode != null && cowCode.equals(milkCode)) {
                    saveMilkData(farmerMobile, milkCode, "cow", quantity, fatSampleCode, fat);
                    return;
                } else if (buffaloCode != null && buffaloCode.equals(milkCode)) {
                    saveMilkData(farmerMobile, milkCode, "buffalo", quantity, fatSampleCode, fat);
                    return;
                }
            }

            Toast.makeText(this, "Milk code not found", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveMilkData(String farmerMobile, String milkCode, String milkType,
                              double quantity, String fatSampleCode, double fat) {

        DatabaseReference milkRef = FirebaseDatabase.getInstance()
                .getReference("Dairy")
                .child("User").child(adminMobile)
                .child("MilkCollection").child(farmerMobile)
                .child(todayDate).child(session).child(milkType);

        Map<String, Object> data = new HashMap<>();
        data.put("milkCode", milkCode);
        data.put("milkType", milkType);
        data.put("quantity", quantity);
        data.put("fatSampleCode", fatSampleCode);
        if (fat != -1) data.put("fat", fat);
        data.put("timestamp", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));

        milkRef.setValue(data).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Milk data saved for " + milkType, Toast.LENGTH_SHORT).show();
            clearForm();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void clearForm() {
        etMilkCode.setText("");
        etMilkQuantity.setText("");
        etFatSampleCode.setText("");
        etFatValue.setText("");
        etFarmerName.setText("");
        tvMilkType.setText("");
        etMilkCode.requestFocus();
    }
}
