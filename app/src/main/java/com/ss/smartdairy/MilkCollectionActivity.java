//////////package com.ss.smartdairy;
//////////
//////////import android.os.Bundle;
//////////
//////////import androidx.activity.EdgeToEdge;
//////////import androidx.appcompat.app.AppCompatActivity;
//////////import androidx.core.graphics.Insets;
//////////import androidx.core.view.ViewCompat;
//////////import androidx.core.view.WindowInsetsCompat;
//////////
//////////public class MilkCollectionActivity extends AppCompatActivity {
//////////
//////////    @Override
//////////    protected void onCreate(Bundle savedInstanceState) {
//////////        super.onCreate(savedInstanceState);
//////////        EdgeToEdge.enable(this);
//////////        setContentView(R.layout.activity_milk_collection);
//////////    }
//////////}
////////
////////package com.ss.smartdairy;
////////
////////import android.os.Bundle;
////////import android.widget.Button;
////////import android.widget.EditText;
////////import android.widget.TextView;
////////import android.widget.Toast;
////////
////////import androidx.appcompat.app.AppCompatActivity;
////////
////////import com.google.firebase.auth.FirebaseAuth;
////////import com.google.firebase.auth.FirebaseUser;
////////import com.google.firebase.database.DataSnapshot;
////////import com.google.firebase.database.DatabaseReference;
////////import com.google.firebase.database.FirebaseDatabase;
////////
////////import java.text.SimpleDateFormat;
////////import java.util.Calendar;
////////import java.util.Date;
////////import java.util.HashMap;
////////import java.util.Locale;
////////import java.util.Map;
////////
////////public class MilkCollectionActivity extends AppCompatActivity {
////////
////////    private EditText etMilkCode, etMilkQuantity, etFatSampleCode, etFatValue;
////////    private TextView tvDate, tvSession, tvMilkType;
////////    private Button btnSave;
////////
////////    private String adminMobile;
////////    private String todayDate, session;
////////
////////    @Override
////////    protected void onCreate(Bundle savedInstanceState) {
////////        super.onCreate(savedInstanceState);
////////        setContentView(R.layout.activity_milk_collection);
////////
////////        // UI
////////        etMilkCode = findViewById(R.id.etMilkCode1);
////////        etMilkQuantity = findViewById(R.id.etMilkQuantity1);
////////        etFatSampleCode = findViewById(R.id.etFatSampleCode1);
////////        etFatValue = findViewById(R.id.etFatValue1);
////////        tvDate = findViewById(R.id.tvDate1);
////////        tvSession = findViewById(R.id.tvSession1);
////////        tvMilkType = findViewById(R.id.tvMilkType1);
////////        btnSave = findViewById(R.id.btnSaveMilkCollection1);
////////
////////        // Firebase current user
////////        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
////////        if (user == null || user.getPhoneNumber() == null) {
////////            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
////////            finish();
////////            return;
////////        }
////////
////////        adminMobile = user.getPhoneNumber().replace("+91", "");
////////
////////        // Set date and session
////////        Calendar cal = Calendar.getInstance();
////////        session = cal.get(Calendar.HOUR_OF_DAY) < 12 ? "morning" : "evening";
////////        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
////////
////////        tvDate.setText("Date: " + todayDate);
////////        tvSession.setText("Session: " + session);
////////
////////        btnSave.setOnClickListener(v -> validateAndSave());
////////    }
////////
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
////////                    saveMilkData(farmerMobile, milkCode, milkType, quantity, fatSampleCode, fat);
////////                    tvMilkType.setText("Milk Type: " + milkType);
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
////////
////////    private void saveMilkData(String farmerMobile, String milkCode, String milkType,
////////                              double quantity, String fatSampleCode, double fat) {
////////
////////        DatabaseReference milkRef = FirebaseDatabase.getInstance()
////////                .getReference("Dairy")
////////                .child("User")
////////                .child(adminMobile)
////////                .child("MilkCollection")
////////                .child(farmerMobile)
////////                .child(todayDate)
////////                .child(session);
////////
////////        Map<String, Object> data = new HashMap<>();
////////        data.put("milkCode", milkCode);
////////        data.put("milkType", milkType);
////////        data.put("quantity", quantity);
////////        data.put("fatSampleCode", fatSampleCode);
////////        if (fat != -1) data.put("fat", fat);
////////        data.put("timestamp", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
////////
////////        milkRef.setValue(data)
////////                .addOnSuccessListener(unused -> {
////////                    Toast.makeText(this, "Milk data saved!", Toast.LENGTH_SHORT).show();
////////                    finish();
////////                })
////////                .addOnFailureListener(e -> {
////////                    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
////////                });
////////    }
////////}
//////
////////
////////package com.ss.smartdairy;
////////
////////import android.os.Bundle;
////////import android.widget.Button;
////////import android.widget.EditText;
////////import android.widget.TextView;
////////import android.widget.Toast;
////////
////////import androidx.appcompat.app.AppCompatActivity;
////////
////////import com.google.firebase.auth.FirebaseAuth;
////////import com.google.firebase.auth.FirebaseUser;
////////import com.google.firebase.database.DataSnapshot;
////////import com.google.firebase.database.DatabaseReference;
////////import com.google.firebase.database.FirebaseDatabase;
////////
////////import java.text.SimpleDateFormat;
////////import java.util.Calendar;
////////import java.util.Date;
////////import java.util.HashMap;
////////import java.util.Locale;
////////import java.util.Map;
////////
////////public class MilkCollectionActivity extends AppCompatActivity {
////////
////////    private EditText etMilkCode, etMilkQuantity, etFatSampleCode, etFatValue;
////////    private TextView tvDate, tvSession, tvMilkType;
////////    private Button btnSave;
////////
////////    private String adminMobile;
////////    private String todayDate, session;
////////
////////    @Override
////////    protected void onCreate(Bundle savedInstanceState) {
////////        super.onCreate(savedInstanceState);
////////        setContentView(R.layout.activity_milk_collection);
////////
////////        // UI
////////        etMilkCode = findViewById(R.id.etMilkCode1);
////////        etMilkQuantity = findViewById(R.id.etMilkQuantity1);
////////        etFatSampleCode = findViewById(R.id.etFatSampleCode1);
////////        etFatValue = findViewById(R.id.etFatValue1);
////////        tvDate = findViewById(R.id.tvDate1);
////////        tvSession = findViewById(R.id.tvSession1);
////////        tvMilkType = findViewById(R.id.tvMilkType1);
////////        btnSave = findViewById(R.id.btnSaveMilkCollection1);
////////
////////        // Firebase current user
////////        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
////////        if (user == null || user.getPhoneNumber() == null) {
////////            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
////////            finish();
////////            return;
////////        }
////////
////////        adminMobile = user.getPhoneNumber().replace("+91", "");
////////
////////        // Set date and session
////////        Calendar cal = Calendar.getInstance();
////////        session = cal.get(Calendar.HOUR_OF_DAY) < 12 ? "morning" : "evening";
////////        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
////////
////////        tvDate.setText("Date: " + todayDate);
////////        tvSession.setText("Session: " + session);
////////
////////        btnSave.setOnClickListener(v -> validateAndSave());
////////    }
////////
//////////    private void validateAndSave() {
//////////        String milkCode = etMilkCode.getText().toString().trim();
//////////        String quantityStr = etMilkQuantity.getText().toString().trim();
//////////        String fatSampleCode = etFatSampleCode.getText().toString().trim();
//////////        String fatStr = etFatValue.getText().toString().trim();
//////////
//////////        if (milkCode.isEmpty() || quantityStr.isEmpty()) {
//////////            Toast.makeText(this, "Milk code and quantity required", Toast.LENGTH_SHORT).show();
//////////            return;
//////////        }
//////////
//////////        double quantity;
//////////        try {
//////////            quantity = Double.parseDouble(quantityStr);
//////////        } catch (Exception e) {
//////////            Toast.makeText(this, "Invalid quantity format", Toast.LENGTH_SHORT).show();
//////////            return;
//////////        }
//////////
//////////        double fat = fatStr.isEmpty() ? -1 : Double.parseDouble(fatStr);
//////////
//////////        // 🔍 Search for farmer by milkCode
//////////        DatabaseReference farmersRef = FirebaseDatabase.getInstance()
//////////                .getReference("Dairy")
//////////                .child("User")
//////////                .child(adminMobile)
//////////                .child("Farmers");
//////////
//////////        farmersRef.get().addOnSuccessListener(snapshot -> {
//////////            boolean found = false;
//////////            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
//////////                String farmerMobile = farmerSnap.getKey();
//////////                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
//////////                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
//////////
//////////                String milkType = null;
//////////                if (cowCode != null && cowCode.equals(milkCode)) {
//////////                    milkType = "cow";
//////////                } else if (buffaloCode != null && buffaloCode.equals(milkCode)) {
//////////                    milkType = "buffalo";
//////////                }
//////////
//////////                if (milkType != null) {
//////////                    tvMilkType.setText("Milk Type: " + milkType); // ✅ Show milk type
//////////                    saveMilkData(farmerMobile, milkCode, milkType, quantity, fatSampleCode, fat); // ✅ Save per type
//////////                    found = true;
//////////                    break;
//////////                }
//////////            }
//////////
//////////            if (!found) {
//////////                Toast.makeText(this, "Milk code not found in farmer list", Toast.LENGTH_SHORT).show();
//////////            }
//////////
//////////        }).addOnFailureListener(e -> {
//////////            Toast.makeText(this, "Error fetching farmers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//////////        });
//////////    }
////////
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
////////        DatabaseReference farmersRef = FirebaseDatabase.getInstance()
////////                .getReference("Dairy")
////////                .child("User")
////////                .child(adminMobile)
////////                .child("Farmers");
////////
////////        farmersRef.get().addOnSuccessListener(snapshot -> {
////////            boolean found = false;
////////
////////            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
////////                String farmerMobile = farmerSnap.getKey();
////////                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
////////                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
////////
////////                boolean isCow = cowCode != null && cowCode.equals(milkCode);
////////                boolean isBuffalo = buffaloCode != null && buffaloCode.equals(milkCode);
////////
////////                if (isCow || isBuffalo) {
////////                    StringBuilder typeBuilder = new StringBuilder("Milk Type: ");
////////                    if (isCow) {
////////                        typeBuilder.append("cow");
////////                        saveMilkData(farmerMobile, milkCode, "cow", quantity, fatSampleCode, fat);
////////                    }
////////                    if (isBuffalo) {
////////                        if (isCow) typeBuilder.append(" & ");
////////                        typeBuilder.append("buffalo");
////////                        saveMilkData(farmerMobile, milkCode, "buffalo", quantity, fatSampleCode, fat);
////////                    }
////////
////////                    tvMilkType.setText(typeBuilder.toString());
////////                    found = true;
////////                    break;
////////                }
////////            }
////////
////////            if (!found) {
////////                tvMilkType.setText("Milk Type: Not found");
////////                Toast.makeText(this, "Milk code not found in farmer list", Toast.LENGTH_SHORT).show();
////////            }
////////
////////        }).addOnFailureListener(e -> {
////////            Toast.makeText(this, "Error fetching farmers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
////////        });
////////    }
////////
////////
////////
////////    private void saveMilkData(String farmerMobile, String milkCode, String milkType,
////////                              double quantity, String fatSampleCode, double fat) {
////////
////////        DatabaseReference milkRef = FirebaseDatabase.getInstance()
////////                .getReference("Dairy")
////////                .child("User")
////////                .child(adminMobile)
////////                .child("MilkCollection")
////////                .child(farmerMobile)
////////                .child(todayDate)
////////                .child(session)
////////                .child(milkType); // ✅ Save separately for cow & buffalo
////////
////////        Map<String, Object> data = new HashMap<>();
////////        data.put("milkCode", milkCode);
////////        data.put("milkType", milkType);
////////        data.put("quantity", quantity);
////////        data.put("fatSampleCode", fatSampleCode);
////////        if (fat != -1) data.put("fat", fat);
////////        data.put("timestamp", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
////////
////////        milkRef.setValue(data)
////////                .addOnSuccessListener(unused -> {
////////                    Toast.makeText(this, "Milk data saved for " + milkType, Toast.LENGTH_SHORT).show();
////////                    clearForm();
////////                })
////////                .addOnFailureListener(e -> {
////////                    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
////////                });
////////    }
////////
////////    private void clearForm() {
////////        etMilkCode.setText("");
////////        etMilkQuantity.setText("");
////////        etFatSampleCode.setText("");
////////        etFatValue.setText("");
////////        tvMilkType.setText("");
////////        etMilkCode.requestFocus();
////////    }
////////}
//////
//////
//////
//////
//////
//////
//////
//////package com.ss.smartdairy;
//////
//////import android.os.Bundle;
//////import android.text.Editable;
//////import android.text.TextWatcher;
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
//////        // Add TextWatcher to auto-fetch milk type
//////        etMilkCode.addTextChangedListener(new TextWatcher() {
//////            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//////            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
//////            @Override
//////            public void afterTextChanged(Editable s) {
//////                if (s.length() >= 2) {
//////                    fetchMilkType(s.toString().trim());
//////                } else {
//////                    tvMilkType.setText("");
//////                }
//////            }
//////        });
//////
//////        btnSave.setOnClickListener(v -> validateAndSave());
//////    }
//////
//////    private void fetchMilkType(String milkCode) {
//////        DatabaseReference farmersRef = FirebaseDatabase.getInstance()
//////                .getReference("Dairy")
//////                .child("User")
//////                .child(adminMobile)
//////                .child("Farmers");
//////
//////        farmersRef.get().addOnSuccessListener(snapshot -> {
//////            boolean found = false;
//////            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
//////                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
//////                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
//////
//////                boolean isCow = cowCode != null && cowCode.equals(milkCode);
//////                boolean isBuffalo = buffaloCode != null && buffaloCode.equals(milkCode);
//////
//////                if (isCow || isBuffalo) {
//////                    StringBuilder typeBuilder = new StringBuilder("Milk Type: ");
//////                    if (isCow) typeBuilder.append("cow");
//////                    if (isBuffalo) {
//////                        if (isCow) typeBuilder.append(" & ");
//////                        typeBuilder.append("buffalo");
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
//////            }
//////
//////        }).addOnFailureListener(e -> {
//////            Toast.makeText(this, "Error fetching milk type: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//////        });
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
//////                    if (isCow) {
//////                        saveMilkData(farmerMobile, milkCode, "cow", quantity, fatSampleCode, fat);
//////                    }
//////                    if (isBuffalo) {
//////                        saveMilkData(farmerMobile, milkCode, "buffalo", quantity, fatSampleCode, fat);
//////                    }
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
//////                .child(session)
//////                .child(milkType);
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
//////package com.ss.smartdairy;
//////
//////import android.os.Bundle;
//////import android.text.Editable;
//////import android.text.TextWatcher;
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
//////    private EditText etMilkCode, etMilkQuantity, etFatSampleCode, etFatValue, etFarmerName;
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
//////        etMilkCode = findViewById(R.id.etMilkCode1);
//////        etMilkQuantity = findViewById(R.id.etMilkQuantity1);
//////        etFatSampleCode = findViewById(R.id.etFatSampleCode1);
//////        etFatValue = findViewById(R.id.etFatValue1);
//////        etFarmerName = findViewById(R.id.etFarmerName1);
//////        tvDate = findViewById(R.id.tvDate1);
//////        tvSession = findViewById(R.id.tvSession1);
//////        tvMilkType = findViewById(R.id.tvMilkType1);
//////        btnSave = findViewById(R.id.btnSaveMilkCollection1);
//////
//////        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//////        if (user == null || user.getPhoneNumber() == null) {
//////            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
//////            finish();
//////            return;
//////        }
//////
//////        adminMobile = user.getPhoneNumber().replace("+91", "");
//////
//////        Calendar cal = Calendar.getInstance();
//////        session = cal.get(Calendar.HOUR_OF_DAY) < 12 ? "morning" : "evening";
//////        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//////
//////        tvDate.setText("Date: " + todayDate);
//////        tvSession.setText("Session: " + session);
//////
//////        etMilkCode.addTextChangedListener(new TextWatcher() {
//////            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//////            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
//////            @Override
//////            public void afterTextChanged(Editable s) {
//////                if (s.length() >= 2) {
//////                    fetchMilkTypeAndName(s.toString().trim());
//////                } else {
//////                    tvMilkType.setText("");
//////                    etFarmerName.setText("");
//////                }
//////            }
//////        });
//////
//////        etFarmerName.addTextChangedListener(new TextWatcher() {
//////            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//////            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
//////            @Override
//////            public void afterTextChanged(Editable s) {
//////                if (s.length() >= 2) {
//////                    fetchMilkCodeByName(s.toString().trim());
//////                }
//////            }
//////        });
//////
//////        btnSave.setOnClickListener(v -> validateAndSave());
//////    }
//////
//////    private void fetchMilkTypeAndName(String milkCode) {
//////        DatabaseReference ref = FirebaseDatabase.getInstance()
//////                .getReference("Dairy").child("User").child(adminMobile).child("Farmers");
//////
//////        ref.get().addOnSuccessListener(snapshot -> {
//////            boolean found = false;
//////            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
//////                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
//////                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
//////                String farmerName = farmerSnap.child("name").getValue(String.class);
//////
//////                boolean isCow = cowCode != null && cowCode.equals(milkCode);
//////                boolean isBuffalo = buffaloCode != null && buffaloCode.equals(milkCode);
//////
//////                if (isCow || isBuffalo) {
//////                    StringBuilder typeBuilder = new StringBuilder("Milk Type: ");
//////                    if (isCow) typeBuilder.append("cow");
//////                    if (isBuffalo) {
//////                        if (isCow) typeBuilder.append(" & ");
//////                        typeBuilder.append("buffalo");
//////                    }
//////                    tvMilkType.setText(typeBuilder.toString());
//////                    etFarmerName.setText(farmerName != null ? farmerName : "");
//////                    found = true;
//////                    break;
//////                }
//////            }
//////            if (!found) {
//////                tvMilkType.setText("Milk Type: Not found");
//////                etFarmerName.setText("");
//////            }
//////        });
//////    }
//////
//////    private void fetchMilkCodeByName(String name) {
//////        DatabaseReference ref = FirebaseDatabase.getInstance()
//////                .getReference("Dairy").child("User").child(adminMobile).child("Farmers");
//////
//////        ref.get().addOnSuccessListener(snapshot -> {
//////            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
//////                String farmerName = farmerSnap.child("name").getValue(String.class);
//////                if (farmerName != null && farmerName.equalsIgnoreCase(name)) {
//////                    String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
//////                    String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
//////                    StringBuilder codeBuilder = new StringBuilder();
//////                    if (cowCode != null) codeBuilder.append(cowCode);
//////                    else if (buffaloCode != null) codeBuilder.append(buffaloCode);
//////                    etMilkCode.setText(codeBuilder.toString());
//////                    break;
//////                }
//////            }
//////        });
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
//////        DatabaseReference farmersRef = FirebaseDatabase.getInstance()
//////                .getReference("Dairy").child("User").child(adminMobile).child("Farmers");
//////
//////        farmersRef.get().addOnSuccessListener(snapshot -> {
//////            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
//////                String farmerMobile = farmerSnap.getKey();
//////                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
//////                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
//////
//////                boolean isCow = cowCode != null && cowCode.equals(milkCode);
//////                boolean isBuffalo = buffaloCode != null && buffaloCode.equals(milkCode);
//////
//////                if (isCow) saveMilkData(farmerMobile, milkCode, "cow", quantity, fatSampleCode, fat);
//////                if (isBuffalo) saveMilkData(farmerMobile, milkCode, "buffalo", quantity, fatSampleCode, fat);
//////
//////                if (isCow || isBuffalo) return;
//////            }
//////            Toast.makeText(this, "Milk code not found in farmer list", Toast.LENGTH_SHORT).show();
//////        });
//////    }
//////
//////    private void saveMilkData(String farmerMobile, String milkCode, String milkType,
//////                              double quantity, String fatSampleCode, double fat) {
//////        DatabaseReference milkRef = FirebaseDatabase.getInstance()
//////                .getReference("Dairy").child("User").child(adminMobile)
//////                .child("MilkCollection").child(farmerMobile)
//////                .child(todayDate).child(session).child(milkType);
//////
//////        Map<String, Object> data = new HashMap<>();
//////        data.put("milkCode", milkCode);
//////        data.put("milkType", milkType);
//////        data.put("quantity", quantity);
//////        data.put("fatSampleCode", fatSampleCode);
//////        if (fat != -1) data.put("fat", fat);
//////        data.put("timestamp", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
//////
//////        milkRef.setValue(data).addOnSuccessListener(unused -> {
//////            Toast.makeText(this, "Milk data saved for " + milkType, Toast.LENGTH_SHORT).show();
//////            clearForm();
//////        }).addOnFailureListener(e -> {
//////            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//////        });
//////    }
//////
//////    private void clearForm() {
//////        etMilkCode.setText("");
//////        etMilkQuantity.setText("");
//////        etFatSampleCode.setText("");
//////        etFatValue.setText("");
//////        etFarmerName.setText("");
//////        tvMilkType.setText("");
//////        etMilkCode.requestFocus();
//////    }
//////}
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
////                String code = s.toString().trim();
////                if (code.length() >= 2) {
////                    fetchMilkTypeAndNameOnly(code);
////                } else {
////                    tvMilkType.setText("");
////                    etFarmerName.setText("");
////                }
////            }
////        });
////
////        btnSave.setOnClickListener(v -> validateAndSave());
////    }
////
////    private void fetchMilkTypeAndNameOnly(String milkCode) {
////        DatabaseReference ref = FirebaseDatabase.getInstance()
////                .getReference("Dairy").child("User").child(adminMobile).child("Farmers");
////
////        ref.get().addOnSuccessListener(snapshot -> {
////            boolean found = false;
////
////            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
////                String farmerName = farmerSnap.child("name").getValue(String.class);
////                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
////                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
////
////                if (cowCode != null && cowCode.equals(milkCode)) {
////                    tvMilkType.setText("Milk Type: cow");
////                    etFarmerName.setText(farmerName != null ? farmerName : "");
////                    found = true;
////                    break;
////                } else if (buffaloCode != null && buffaloCode.equals(milkCode)) {
////                    tvMilkType.setText("Milk Type: buffalo");
////                    etFarmerName.setText(farmerName != null ? farmerName : "");
////                    found = true;
////                    break;
////                }
////            }
////
////            if (!found) {
////                tvMilkType.setText("Milk Type: Not found");
////                etFarmerName.setText("");
////            }
////        }).addOnFailureListener(e -> {
////            Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
////                if (cowCode != null && cowCode.equals(milkCode)) {
////                    saveMilkData(farmerMobile, milkCode, "cow", quantity, fatSampleCode, fat);
////                    return;
////                } else if (buffaloCode != null && buffaloCode.equals(milkCode)) {
////                    saveMilkData(farmerMobile, milkCode, "buffalo", quantity, fatSampleCode, fat);
////                    return;
////                }
////            }
////
////            Toast.makeText(this, "Milk code not found", Toast.LENGTH_SHORT).show();
////        });
////    }
////
////    private void saveMilkData(String farmerMobile, String milkCode, String milkType,
////                              double quantity, String fatSampleCode, double fat) {
////
////        DatabaseReference milkRef = FirebaseDatabase.getInstance()
////                .getReference("Dairy")
////                .child("User").child(adminMobile)
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
////
//
//
//package com.ss.smartdairy;
//
//import android.app.AlertDialog;
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
//    private boolean isUpdatingFromName = false;
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
//                if (isUpdatingFromName) {
//                    isUpdatingFromName = false;
//                    return;
//                }
//
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
//        etFarmerName.addTextChangedListener(new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
//            @Override
//            public void afterTextChanged(Editable s) {
//                String name = s.toString().trim();
//                if (name.length() >= 2) {
//                    fetchMilkCodeByName(name);
//                }
//            }
//        });
//
//        btnSave.setOnClickListener(v -> validateAndSave());
//    }
//
//    //    private void fetchMilkTypeAndNameOnly(String milkCode) {
////        DatabaseReference ref = FirebaseDatabase.getInstance()
////                .getReference("Dairy").child("User").child(adminMobile).child("Farmers");
////
////        ref.get().addOnSuccessListener(snapshot -> {
////            boolean found = false;
////
////            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
////                String farmerName = farmerSnap.child("name").getValue(String.class);
////                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
////                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
////
////                if (cowCode != null && cowCode.equals(milkCode)) {
////                    tvMilkType.setText("Milk Type: cow");
////                    etFarmerName.setText(farmerName != null ? farmerName : "");
////                    found = true;
////                    break;
////                } else if (buffaloCode != null && buffaloCode.equals(milkCode)) {
////                    tvMilkType.setText("Milk Type: buffalo");
////                    etFarmerName.setText(farmerName != null ? farmerName : "");
////                    found = true;
////                    break;
////                }
////            }
////
////            if (!found) {
////                tvMilkType.setText("Milk Type: Not found");
////                etFarmerName.setText("");
////            }
////        }).addOnFailureListener(e -> {
////            Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
////        });
////    }
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
//
//    private void fetchMilkCodeByName(String name) {
//        DatabaseReference ref = FirebaseDatabase.getInstance()
//                .getReference("Dairy").child("User").child(adminMobile).child("Farmers");
//
//        ref.get().addOnSuccessListener(snapshot -> {
//            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
//                String farmerName = farmerSnap.child("name").getValue(String.class);
//                if (farmerName != null && farmerName.equalsIgnoreCase(name)) {
//                    String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
//                    String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);
//
//                    if (cowCode != null && buffaloCode != null) {
//                        showMilkTypeSelectionDialog(cowCode, buffaloCode);
//                    } else {
//                        String selectedCode = (cowCode != null) ? cowCode : buffaloCode;
//                        String selectedType = (cowCode != null) ? "cow" : "buffalo";
//
//                        isUpdatingFromName = true;
//                        etMilkCode.setText(selectedCode);
//                        tvMilkType.setText("Milk Type: " + selectedType);
//                    }
//                    break;
//                }
//            }
//        });
//    }
//
//
//    private void showMilkTypeSelectionDialog(String cowCode, String buffaloCode) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Choose Milk Type");
//
//        String[] options = {
//                "🐄 Cow: " + cowCode,
//                "🐃 Buffalo: " + buffaloCode
//        };
//
//        builder.setItems(options, (dialog, which) -> {
//            isUpdatingFromName = true;
//            if (which == 0) {
//                etMilkCode.setText(cowCode);
//                tvMilkType.setText("Milk Type: cow");
//            } else {
//                etMilkCode.setText(buffaloCode);
//                tvMilkType.setText("Milk Type: buffalo");
//            }
//        });
//
//        builder.show();
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



package com.ss.smartdairy;

import android.app.AlertDialog;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import java.util.List;

public class MilkCollectionActivity extends AppCompatActivity {

    private static final String TAG = "MilkCollection";
    private EditText etMilkCode, etMilkQuantity, etFatSampleCode, etFatValue, etFarmerName;
    private TextView tvDate, tvSession, tvMilkType, tvFatRate, tvTotalAmount;
    private Button btnSave;

    private String adminMobile;
    private String todayDate, session;
    private boolean isUpdatingFromMilkCode = false;
    private boolean isUpdatingFromFarmerName = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable calculationRunnable;
    private static final long DEBOUNCE_DELAY = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_milk_collection);

        initializeViews();
        setupFirebaseAuth();
        setupDateAndSession();
        setupTextWatchers();
        setupSaveButton();

        Log.d(TAG, "MilkCollectionActivity created successfully");
    }

    private void initializeViews() {
        etMilkCode = findViewById(R.id.etMilkCode1);
        etMilkQuantity = findViewById(R.id.etMilkQuantity1);
        etFatSampleCode = findViewById(R.id.etFatSampleCode1);
        etFatValue = findViewById(R.id.etFatValue1);
        etFarmerName = findViewById(R.id.etFarmerName1);
        tvDate = findViewById(R.id.tvDate1);
        tvSession = findViewById(R.id.tvSession1);
        tvMilkType = findViewById(R.id.tvMilkType1);
        tvFatRate = findViewById(R.id.tvFatRate);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnSave = findViewById(R.id.btnSaveMilkCollection1);

        Log.d(TAG, "Views initialized");
    }

    private void setupFirebaseAuth() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getPhoneNumber() == null) {
            Log.e(TAG, "User not authenticated");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adminMobile = user.getPhoneNumber().replace("+91", "");
        Log.d(TAG, "Admin mobile: " + adminMobile);
    }

    private void setupDateAndSession() {
        Calendar cal = Calendar.getInstance();
        session = cal.get(Calendar.HOUR_OF_DAY) < 12 ? "morning" : "evening";
        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        tvDate.setText("Date: " + todayDate);
        tvSession.setText("Session: " + session);

        Log.d(TAG, "Date: " + todayDate + ", Session: " + session);
    }

    private void setupTextWatchers() {
        // Milk Code TextWatcher - Auto-fetch farmer details
        etMilkCode.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingFromFarmerName) {
                    Log.d(TAG, "Skipping milk code processing - triggered by farmer name");
                    isUpdatingFromFarmerName = false;
                    return;
                }

                String code = s.toString().trim();
                Log.d(TAG, "Milk code entered: " + code);

                if (code.length() >= 2) {
                    fetchMilkTypeAndNameOnly(code);
                } else {
                    clearMilkTypeAndName();
                }
            }
        });

        // Farmer Name TextWatcher - Show dialog for milk type selection
        etFarmerName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingFromMilkCode) {
                    Log.d(TAG, "Skipping farmer name processing - triggered by milk code");
                    isUpdatingFromMilkCode = false;
                    return;
                }

                String name = s.toString().trim();
                Log.d(TAG, "Farmer name entered manually: " + name);

                if (name.length() >= 2) {
                    fetchMilkCodeByName(name);
                }
            }
        });

        // Milk Quantity TextWatcher - Trigger calculation when quantity changes
        etMilkQuantity.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "Milk quantity changed: " + s.toString());
                triggerCalculation();
            }
        });

        // Fat Value TextWatcher - Trigger calculation when fat changes
        etFatValue.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "Fat value changed: " + s.toString());
                triggerCalculation();
            }
        });
    }

    private void triggerCalculation() {
        if (calculationRunnable != null) {
            handler.removeCallbacks(calculationRunnable);
        }

        calculationRunnable = this::calculateAmount;
        handler.postDelayed(calculationRunnable, DEBOUNCE_DELAY);
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void clearMilkTypeAndName() {
        tvMilkType.setText("Milk Type: ");
        clearAmountInfo();

        // Set flag before clearing farmer name to prevent TextWatcher trigger
        isUpdatingFromMilkCode = true;
        etFarmerName.setText("");

        Log.d(TAG, "Cleared milk type and farmer name");
    }

    private void clearAmountInfo() {
        tvFatRate.setText("फॅट दर (प्रति लिटर): -");
        tvTotalAmount.setText("एकूण रक्कम: -");
    }

//    private void calculateAmount() {
//        Log.d(TAG, "calculateAmount() called");
//
//        String fatStr = etFatValue.getText().toString().trim();
//        String quantityStr = etMilkQuantity.getText().toString().trim();
//        String milkTypeStr = tvMilkType.getText().toString().replace("Milk Type: ", "").trim();
//
//        Log.d(TAG, "Input values - Fat: " + fatStr + ", Quantity: " + quantityStr + ", MilkType: " + milkTypeStr);
//
//        // Map milk type to database format
//        String milkType;
//        if (milkTypeStr.equals("cow")) {
//            milkType = "Cow";
//        } else if (milkTypeStr.equals("buffalo")) {
//            milkType = "Buffalo";
//        } else {
//            Log.w(TAG, "Invalid or missing milk type: " + milkTypeStr);
//            clearAmountInfo();
//            return;
//        }
//
//        // Validate inputs
//        if (fatStr.isEmpty() || quantityStr.isEmpty()) {
//            Log.w(TAG, "Fat or quantity is empty");
//            clearAmountInfo();
//            return;
//        }
//
//        double fatValue, milkQuantity;
//        try {
//            fatValue = Double.parseDouble(fatStr);
//            milkQuantity = Double.parseDouble(quantityStr);
//            Log.d(TAG, "Parsed values - Fat: " + fatValue + ", Quantity: " + milkQuantity);
//        } catch (NumberFormatException e) {
//            Log.e(TAG, "Number parsing error: " + e.getMessage());
//            clearAmountInfo();
//            return;
//        }
//
//        // Validate fat value range
//        if (fatValue < 2.0 || fatValue > 15.0) {
//            Log.w(TAG, "Fat value out of range: " + fatValue);
//            clearAmountInfo();
//            return;
//        }
//
//        // Validate quantity
//        if (milkQuantity <= 0) {
//            Log.w(TAG, "Invalid milk quantity: " + milkQuantity);
//            clearAmountInfo();
//            return;
//        }
//
//        // Format fatKey (e.g., 3.5 -> 3_5)
//        String fatKey = String.format(Locale.US, "%.1f", fatValue).replace(".", "_");
//
//        Log.d(TAG, "Fetching fat rate - Date: " + todayDate + ", MilkType: " + milkType + ", FatKey: " + fatKey);
//
//        // Fetch fat rate from RateChart
//        DatabaseReference rateRef = FirebaseDatabase.getInstance()
//                .getReference("Dairy/User").child(adminMobile).child("RateChart").child(todayDate).child(milkType).child(fatKey);
//
//        rateRef.get().addOnSuccessListener(snapshot -> {
//            Log.d(TAG, "Firebase query successful, snapshot exists: " + snapshot.exists());
//
//            if (snapshot.exists()) {
//                Double rate = snapshot.getValue(Double.class);
//                Log.d(TAG, "Retrieved rate: " + rate);
//
//                if (rate != null && rate > 0) {
//                    double totalAmount = rate * milkQuantity;
//                    tvFatRate.setText("फॅट दर (प्रति लिटर): ₹" + String.format(Locale.US, "%.2f", rate));
//                    tvTotalAmount.setText("एकूण रक्कम: ₹" + String.format(Locale.US, "%.2f", totalAmount));
//                    Log.d(TAG, "Amount calculated successfully - Rate: " + rate + ", Total: " + totalAmount);
//                } else {
//                    Log.w(TAG, "Rate is null or zero: " + rate);
//                    clearAmountInfo();
//                }
//            } else {
//                Log.w(TAG, "No data found for fatKey: " + fatKey);
//                // Try to find nearest fat rate
//                tryNearestFatRate(todayDate, milkType, fatValue, milkQuantity);
//            }
//        }).addOnFailureListener(e -> {
//            Log.e(TAG, "Firebase query failed: " + e.getMessage(), e);
//            clearAmountInfo();
//        });
//    }
//
//    private void tryNearestFatRate(String currentDate, String milkType, double fatValue, double milkQuantity) {
//        Log.d(TAG, "Trying to find nearest fat rate for: " + fatValue);
//
//        DatabaseReference allRatesRef = FirebaseDatabase.getInstance()
//                .getReference("Dairy/User").child(adminMobile).child("RateChart").child(currentDate).child(milkType);
//
//        allRatesRef.get().addOnSuccessListener(snapshot -> {
//            if (snapshot.exists()) {
//                double closestFat = -1;
//                double closestRate = 0;
//                double minDifference = Double.MAX_VALUE;
//
//                for (DataSnapshot child : snapshot.getChildren()) {
//                    String fatKey = child.getKey();
//                    Double rate = child.getValue(Double.class);
//
//                    if (fatKey != null && rate != null && rate > 0) {
//                        try {
//                            double fat = Double.parseDouble(fatKey.replace("_", "."));
//                            double difference = Math.abs(fat - fatValue);
//
//                            if (difference < minDifference) {
//                                minDifference = difference;
//                                closestFat = fat;
//                                closestRate = rate;
//                            }
//                        } catch (NumberFormatException e) {
//                            Log.w(TAG, "Invalid fat key format: " + fatKey);
//                        }
//                    }
//                }
//
//                if (closestFat > 0) {
//                    double totalAmount = closestRate * milkQuantity;
//                    tvFatRate.setText("फॅट दर (प्रति लिटर): ₹" + String.format(Locale.US, "%.2f", closestRate) + " (निकटतम: " + closestFat + "%)");
//                    tvTotalAmount.setText("एकूण रक्कम: ₹" + String.format(Locale.US, "%.2f", totalAmount));
//                    Log.d(TAG, "Used nearest fat rate - Fat: " + closestFat + ", Rate: " + closestRate);
//                } else {
//                    clearAmountInfo();
//                    Log.w(TAG, "No rates available for date: " + currentDate);
//                }
//            } else {
//                clearAmountInfo();
//                Log.w(TAG, "No rate chart available for date: " + currentDate);
//            }
//        }).addOnFailureListener(e -> {
//            Log.e(TAG, "Failed to fetch rate chart: " + e.getMessage(), e);
//            clearAmountInfo();
//        });
//    }

    private void calculateAmount() {
        Log.d(TAG, "calculateAmount() called");

        String fatStr = etFatValue.getText().toString().trim();
        String quantityStr = etMilkQuantity.getText().toString().trim();
        String milkTypeStr = tvMilkType.getText().toString().replace("Milk Type: ", "").trim();

        Log.d(TAG, "Input values - Fat: " + fatStr + ", Quantity: " + quantityStr + ", MilkType: " + milkTypeStr);

        // Map milk type to database format
        String milkType;
        if (milkTypeStr.equals("cow")) {
            milkType = "Cow";
        } else if (milkTypeStr.equals("buffalo")) {
            milkType = "Buffalo";
        } else {
            Log.w(TAG, "Invalid or missing milk type: " + milkTypeStr);
            clearAmountInfo();
            return;
        }

        // Validate inputs
        if (fatStr.isEmpty() || quantityStr.isEmpty()) {
            Log.w(TAG, "Fat or quantity is empty");
            clearAmountInfo();
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
            return;
        }

        // Validate fat value range
        if (fatValue < 2.0 || fatValue > 15.0) {
            Log.w(TAG, "Fat value out of range: " + fatValue);
            clearAmountInfo();
            return;
        }

        // Validate quantity
        if (milkQuantity <= 0) {
            Log.w(TAG, "Invalid milk quantity: " + milkQuantity);
            clearAmountInfo();
            return;
        }

        // Format fatKey (e.g., 3.5 -> 3_5)
        String fatKey = String.format(Locale.US, "%.1f", fatValue).replace(".", "_");

        Log.d(TAG, "Fetching fat rate - Date: " + todayDate + ", MilkType: " + milkType + ", FatKey: " + fatKey);

        // Try to fetch rate from today first, then fallback to nearest date
        fetchRateWithDateFallback(todayDate, milkType, fatKey, fatValue, milkQuantity);
    }

    private void fetchRateWithDateFallback(String startDate, String milkType, String fatKey, double fatValue, double milkQuantity) {
        DatabaseReference rateRef = FirebaseDatabase.getInstance()
                .getReference("Dairy/User").child(adminMobile).child("RateChart").child(startDate).child(milkType).child(fatKey);

        rateRef.get().addOnSuccessListener(snapshot -> {
            Log.d(TAG, "Firebase query for " + startDate + " successful, snapshot exists: " + snapshot.exists());

            if (snapshot.exists()) {
                Double rate = snapshot.getValue(Double.class);
                Log.d(TAG, "Retrieved rate from " + startDate + ": " + rate);

                if (rate != null && rate > 0) {
                    double totalAmount = rate * milkQuantity;
                    String dateIndicator = startDate.equals(todayDate) ? "" : " (from " + startDate + ")";
                    tvFatRate.setText("फॅट दर (प्रति लिटर): ₹" + String.format(Locale.US, "%.2f", rate) + dateIndicator);
                    tvTotalAmount.setText("एकूण रक्कम: ₹" + String.format(Locale.US, "%.2f", totalAmount));
                    Log.d(TAG, "Amount calculated successfully - Rate: " + rate + ", Total: " + totalAmount + ", Date: " + startDate);
                } else {
                    Log.w(TAG, "Rate is null or zero: " + rate);
                    // Try nearest fat rate for this date
                    tryNearestFatRate(startDate, milkType, fatValue, milkQuantity);
                }
            } else {
                Log.w(TAG, "No data found for date: " + startDate + ", fatKey: " + fatKey);
                // If today's rate not found, try previous dates
                if (startDate.equals(todayDate)) {
                    findNearestDateWithRate(milkType, fatKey, fatValue, milkQuantity);
                } else {
                    // If we're already trying a fallback date and exact fat not found, try nearest fat
                    tryNearestFatRate(startDate, milkType, fatValue, milkQuantity);
                }
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Firebase query failed for " + startDate + ": " + e.getMessage(), e);
            if (startDate.equals(todayDate)) {
                findNearestDateWithRate(milkType, fatKey, fatValue, milkQuantity);
            } else {
                clearAmountInfo();
            }
        });
    }

    private void findNearestDateWithRate(String milkType, String fatKey, double fatValue, double milkQuantity) {
        Log.d(TAG, "Searching for nearest date with rate data");

        // Generate list of dates to check (yesterday and up to 7 days back)
        List<String> datesToCheck = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Check previous 7 days
        for (int i = 1; i <= 7; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            String checkDate = dateFormat.format(calendar.getTime());
            datesToCheck.add(checkDate);
        }

        Log.d(TAG, "Will check dates: " + datesToCheck.toString());

        // Check each date sequentially
        checkDatesSequentially(datesToCheck, 0, milkType, fatKey, fatValue, milkQuantity);
    }

    private void checkDatesSequentially(List<String> dates, int index, String milkType, String fatKey, double fatValue, double milkQuantity) {
        if (index >= dates.size()) {
            Log.w(TAG, "No rate data found in any of the checked dates");
            clearAmountInfo();
            return;
        }

        String dateToCheck = dates.get(index);
        Log.d(TAG, "Checking date: " + dateToCheck + " (attempt " + (index + 1) + "/" + dates.size() + ")");

        DatabaseReference rateRef = FirebaseDatabase.getInstance()
                .getReference("Dairy/User").child(adminMobile).child("RateChart").child(dateToCheck).child(milkType).child(fatKey);

        rateRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Double rate = snapshot.getValue(Double.class);
                if (rate != null && rate > 0) {
                    double totalAmount = rate * milkQuantity;
                    tvFatRate.setText("फॅट दर (प्रति लिटर): ₹" + String.format(Locale.US, "%.2f", rate) + " (from " + dateToCheck + ")");
                    tvTotalAmount.setText("एकूण रक्कम: ₹" + String.format(Locale.US, "%.2f", totalAmount));
                    Log.d(TAG, "Found rate from " + dateToCheck + " - Rate: " + rate + ", Total: " + totalAmount);
                    return;
                }
            }

            Log.d(TAG, "No exact fat rate found for " + dateToCheck + ", trying nearest fat rate");
            // Try nearest fat rate for this date
            tryNearestFatRateForDate(dateToCheck, milkType, fatValue, milkQuantity, dates, index);

        }).addOnFailureListener(e -> {
            Log.w(TAG, "Failed to check date " + dateToCheck + ": " + e.getMessage());
            // Continue to next date
            checkDatesSequentially(dates, index + 1, milkType, fatKey, fatValue, milkQuantity);
        });
    }

    private void tryNearestFatRateForDate(String dateToCheck, String milkType, double fatValue, double milkQuantity, List<String> remainingDates, int currentIndex) {
        DatabaseReference allRatesRef = FirebaseDatabase.getInstance()
                .getReference("Dairy/User").child(adminMobile).child("RateChart").child(dateToCheck).child(milkType);

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
                    tvFatRate.setText("फॅट दर (प्रति लिटर): ₹" + String.format(Locale.US, "%.2f", closestRate) +
                            " (निकटतम: " + closestFat + "% from " + dateToCheck + ")");
                    tvTotalAmount.setText("एकूण रक्कम: ₹" + String.format(Locale.US, "%.2f", totalAmount));
                    Log.d(TAG, "Used nearest fat rate from " + dateToCheck + " - Fat: " + closestFat + ", Rate: " + closestRate);
                    return;
                }
            }

            // If no rate found for this date, continue to next date
            Log.d(TAG, "No rates available for " + dateToCheck + ", trying next date");
            checkDatesSequentially(remainingDates, currentIndex + 1, milkType, "", fatValue, milkQuantity);

        }).addOnFailureListener(e -> {
            Log.w(TAG, "Failed to fetch rate chart for " + dateToCheck + ": " + e.getMessage());
            // Continue to next date
            checkDatesSequentially(remainingDates, currentIndex + 1, milkType, "", fatValue, milkQuantity);
        });
    }

    private void tryNearestFatRate(String currentDate, String milkType, double fatValue, double milkQuantity) {
        Log.d(TAG, "Trying to find nearest fat rate for: " + fatValue + " on date: " + currentDate);

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
                    String dateIndicator = currentDate.equals(todayDate) ? "" : " from " + currentDate;
                    tvFatRate.setText("फॅट दर (प्रति लिटर): ₹" + String.format(Locale.US, "%.2f", closestRate) +
                            " (निकटतम: " + closestFat + "%" + dateIndicator + ")");
                    tvTotalAmount.setText("एकूण रक्कम: ₹" + String.format(Locale.US, "%.2f", totalAmount));
                    Log.d(TAG, "Used nearest fat rate from " + currentDate + " - Fat: " + closestFat + ", Rate: " + closestRate);
                } else {
                    // If this is today and no rates found, try previous dates
                    if (currentDate.equals(todayDate)) {
                        findNearestDateWithRate(milkType, "", fatValue, milkQuantity);
                    } else {
                        clearAmountInfo();
                        Log.w(TAG, "No rates available for date: " + currentDate);
                    }
                }
            } else {
                // If this is today and no rates found, try previous dates
                if (currentDate.equals(todayDate)) {
                    findNearestDateWithRate(milkType, "", fatValue, milkQuantity);
                } else {
                    clearAmountInfo();
                    Log.w(TAG, "No rate chart available for date: " + currentDate);
                }
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to fetch rate chart for " + currentDate + ": " + e.getMessage(), e);
            if (currentDate.equals(todayDate)) {
                findNearestDateWithRate(milkType, "", fatValue, milkQuantity);
            } else {
                clearAmountInfo();
            }
        });
    }


    // Fetch farmer details when milk code is entered (NO DIALOG)
    private void fetchMilkTypeAndNameOnly(String milkCode) {
        Log.d(TAG, "Fetching farmer details for milk code: " + milkCode);

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

                    // Set flag before updating farmer name to prevent TextWatcher trigger
                    isUpdatingFromMilkCode = true;
                    etFarmerName.setText(farmerName != null ? farmerName : "");

                    found = true;
                    Log.d(TAG, "Found cow milk code for farmer: " + farmerName);

                    // Trigger calculation if fat and quantity are available
                    triggerCalculation();
                    break;
                } else if (buffaloCode != null && buffaloCode.equals(milkCode)) {
                    tvMilkType.setText("Milk Type: buffalo");

                    // Set flag before updating farmer name to prevent TextWatcher trigger
                    isUpdatingFromMilkCode = true;
                    etFarmerName.setText(farmerName != null ? farmerName : "");

                    found = true;
                    Log.d(TAG, "Found buffalo milk code for farmer: " + farmerName);

                    // Trigger calculation if fat and quantity are available
                    triggerCalculation();
                    break;
                }
            }

            if (!found) {
                tvMilkType.setText("Milk Type: Not found");

                // Set flag before clearing farmer name to prevent TextWatcher trigger
                isUpdatingFromMilkCode = true;
                etFarmerName.setText("");

                clearAmountInfo();
                Log.w(TAG, "Milk code not found: " + milkCode);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching farmer data: " + e.getMessage());
            Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Fetch milk codes when farmer name is entered (SHOW DIALOG IF BOTH TYPES EXIST)
    private void fetchMilkCodeByName(String name) {
        Log.d(TAG, "Fetching milk codes for farmer name: " + name);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Dairy").child("User").child(adminMobile).child("Farmers");

        ref.get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
                String farmerName = farmerSnap.child("name").getValue(String.class);
                if (farmerName != null && farmerName.equalsIgnoreCase(name)) {
                    String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
                    String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);

                    Log.d(TAG, "Found farmer - Cow code: " + cowCode + ", Buffalo code: " + buffaloCode);

                    // Show dialog only if both cow and buffalo codes exist
                    if (cowCode != null && buffaloCode != null) {
                        Log.d(TAG, "Both milk types available, showing selection dialog");
                        showMilkTypeSelectionDialog(cowCode, buffaloCode);
                    } else {
                        // Auto-select if only one type is available
                        String selectedCode = (cowCode != null) ? cowCode : buffaloCode;
                        String selectedType = (cowCode != null) ? "cow" : "buffalo";

                        if (selectedCode != null) {
                            // Set flag before updating milk code to prevent TextWatcher trigger
                            isUpdatingFromFarmerName = true;
                            etMilkCode.setText(selectedCode);
                            tvMilkType.setText("Milk Type: " + selectedType);
                            Log.d(TAG, "Auto-selected " + selectedType + " with code: " + selectedCode);

                            // Trigger calculation
                            triggerCalculation();
                        }
                    }
                    break;
                }
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching farmer by name: " + e.getMessage());
            Toast.makeText(this, "Error fetching farmer data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showMilkTypeSelectionDialog(String cowCode, String buffaloCode) {
        Log.d(TAG, "Showing milk type selection dialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Milk Type");

        String[] options = {
                "🐄 Cow: " + cowCode,
                "🐃 Buffalo: " + buffaloCode
        };

        builder.setItems(options, (dialog, which) -> {
            // Set flag before updating milk code to prevent TextWatcher trigger
            isUpdatingFromFarmerName = true;

            if (which == 0) {
                etMilkCode.setText(cowCode);
                tvMilkType.setText("Milk Type: cow");
                Log.d(TAG, "User selected cow milk type");
            } else {
                etMilkCode.setText(buffaloCode);
                tvMilkType.setText("Milk Type: buffalo");
                Log.d(TAG, "User selected buffalo milk type");
            }

            // Trigger calculation after selection
            triggerCalculation();
        });

        builder.setOnCancelListener(dialog -> {
            Log.d(TAG, "Dialog cancelled by user");
        });

        builder.show();
    }

    private void validateAndSave() {
        String milkCode = etMilkCode.getText().toString().trim();
        String quantityStr = etMilkQuantity.getText().toString().trim();
        String fatSampleCode = etFatSampleCode.getText().toString().trim();
        String fatStr = etFatValue.getText().toString().trim();

        Log.d(TAG, "Validating input - Milk Code: " + milkCode + ", Quantity: " + quantityStr +
                ", Fat Sample Code: " + fatSampleCode + ", Fat: " + fatStr);

        // Validate required fields
        if (milkCode.isEmpty()) {
            Toast.makeText(this, "Milk code is required", Toast.LENGTH_SHORT).show();
            etMilkCode.requestFocus();
            return;
        }

        if (quantityStr.isEmpty()) {
            Toast.makeText(this, "Milk quantity is required", Toast.LENGTH_SHORT).show();
            etMilkQuantity.requestFocus();
            return;
        }

        // Validate milk type is selected
        String milkTypeText = tvMilkType.getText().toString();
        if (milkTypeText.isEmpty() || milkTypeText.equals("Milk Type: ") ||
                milkTypeText.contains("Not found")) {
            Toast.makeText(this, "Please select a valid milk code", Toast.LENGTH_SHORT).show();
            etMilkCode.requestFocus();
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityStr);
            if (quantity <= 0) {
                Toast.makeText(this, "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Invalid quantity format", Toast.LENGTH_SHORT).show();
            etMilkQuantity.requestFocus();
            return;
        }

        // Fat value is optional, default to -1 if not provided
        double fat = -1;
        if (!fatStr.isEmpty()) {
            try {
                fat = Double.parseDouble(fatStr);
                if (fat < 0 || fat > 15) {
                    Toast.makeText(this, "Fat percentage should be between 0-15", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(this, "Invalid fat value format", Toast.LENGTH_SHORT).show();
                etFatValue.requestFocus();
                return;
            }
        }

        Log.d(TAG, "Validation passed, proceeding to save");
        findFarmerAndSave(milkCode, quantity, fatSampleCode, fat);
    }

    private void findFarmerAndSave(String milkCode, double quantity, String fatSampleCode, double fat) {
        DatabaseReference farmersRef = FirebaseDatabase.getInstance()
                .getReference("Dairy").child("User").child(adminMobile).child("Farmers");

        farmersRef.get().addOnSuccessListener(snapshot -> {
            boolean found = false;

            for (DataSnapshot farmerSnap : snapshot.getChildren()) {
                String farmerMobile = farmerSnap.getKey();
                String cowCode = farmerSnap.child("cowMilkCode").getValue(String.class);
                String buffaloCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);

                if (cowCode != null && cowCode.equals(milkCode)) {
                    saveMilkData(farmerMobile, milkCode, "cow", quantity, fatSampleCode, fat);
                    found = true;
                    break;
                } else if (buffaloCode != null && buffaloCode.equals(milkCode)) {
                    saveMilkData(farmerMobile, milkCode, "buffalo", quantity, fatSampleCode, fat);
                    found = true;
                    break;
                }
            }

            if (!found) {
                Log.w(TAG, "Farmer not found for milk code: " + milkCode);
                Toast.makeText(this, "Milk code not found in farmer records", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error finding farmer: " + e.getMessage());
            Toast.makeText(this, "Error finding farmer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveMilkData(String farmerMobile, String milkCode, String milkType,
                              double quantity, String fatSampleCode, double fat) {

        Log.d(TAG, "Saving milk data - Farmer: " + farmerMobile + ", Type: " + milkType +
                ", Quantity: " + quantity);

        DatabaseReference milkRef = FirebaseDatabase.getInstance()
                .getReference("Dairy")
                .child("User").child(adminMobile)
                .child("MilkCollection").child(farmerMobile)
                .child(todayDate).child(session).child(milkType);

        Map<String, Object> data = new HashMap<>();
        data.put("milkCode", milkCode);
        data.put("milkType", milkType);
        data.put("quantity", quantity);
        data.put("timestamp", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));

        // Add fat sample code only if provided (optional)
        if (!fatSampleCode.isEmpty()) {
            data.put("fatSampleCode", fatSampleCode);
        }

        // Add fat value only if provided (optional)
        if (fat != -1) {
            data.put("fat", fat);

            // If fat is provided, also save calculated amount if available
            String totalAmountText = tvTotalAmount.getText().toString();
            if (totalAmountText.contains("₹")) {
                try {
                    String amountStr = totalAmountText.substring(totalAmountText.indexOf("₹") + 1).trim();
                    double totalAmount = Double.parseDouble(amountStr);
                    data.put("totalAmount", totalAmount);

                    String rateText = tvFatRate.getText().toString();
                    if (rateText.contains("₹")) {
                        String rateStr = rateText.substring(rateText.indexOf("₹") + 1, rateText.indexOf("(") > 0 ? rateText.indexOf("(") : rateText.length()).trim();
                        double rate = Double.parseDouble(rateStr);
                        data.put("fatRate", rate);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error parsing calculated amount: " + e.getMessage());
                }
            }
        }

        milkRef.setValue(data).addOnSuccessListener(unused -> {
            Log.d(TAG, "Milk data saved successfully");

            String message = "Milk collection saved successfully for " + milkType;
            if (fat != -1) {
                String totalAmountText = tvTotalAmount.getText().toString();
                if (totalAmountText.contains("₹")) {
                    message += "\n" + totalAmountText;
                }
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            clearForm();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to save milk data: " + e.getMessage());
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void clearForm() {
        // Clear all fields without triggering TextWatchers
        isUpdatingFromMilkCode = true;
        isUpdatingFromFarmerName = true;

        etMilkCode.setText("");
        etMilkQuantity.setText("");
        etFatSampleCode.setText("");
        etFatValue.setText("");
        etFarmerName.setText("");
        tvMilkType.setText("Milk Type: ");
        clearAmountInfo();

        etMilkCode.requestFocus();

        Log.d(TAG, "Form cleared");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (calculationRunnable != null) {
            handler.removeCallbacks(calculationRunnable);
        }
        Log.d(TAG, "Activity destroyed");
    }
}





