package com.ss.smartdairy;

import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class RateChartReportActivity extends AppCompatActivity {

    private String selectedType = "Cow";
    private String selectedDate;
    private DatabaseReference rateRef;
    private List<RateModel> rateList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_chart_report);

        RadioGroup radioGroupType = findViewById(R.id.radioGroupType);
        Button btnPickDate = findViewById(R.id.btnPickDate);
        Button btnExportPdf = findViewById(R.id.btnExportPdf);

        Calendar cal = Calendar.getInstance();
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

        String mobile = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().replace("+91", "");
        rateRef = FirebaseDatabase.getInstance()
                .getReference("Dairy")
                .child("User")
                .child(mobile)
                .child("RateChart");

        btnPickDate.setOnClickListener(v -> {
            new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {
                        cal.set(year, month, day);
                        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
                        Toast.makeText(this, "Selected: " + selectedDate, Toast.LENGTH_SHORT).show();
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioCow) selectedType = "Cow";
            else if (checkedId == R.id.radioBuffalo) selectedType = "Buffalo";
        });

        btnExportPdf.setOnClickListener(v -> fetchRatesAndGeneratePDF());
    }

    private void fetchRatesAndGeneratePDF() {
        rateRef.child(selectedDate).child(selectedType).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rateList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String fat = child.getKey().replace("_", ".");
                        Double rate = child.getValue(Double.class);
                        rateList.add(new RateModel(fat, rate != null ? rate : 0));
                    }
                    createPdfFromRates();
                } else {
                    // Use nearest past available rate
                    fetchNearestAvailablePastRates();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RateChartReportActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchNearestAvailablePastRates() {
        rateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> availableDates = new ArrayList<>();
                for (DataSnapshot dateSnap : snapshot.getChildren()) {
                    String dateKey = dateSnap.getKey();
                    if (dateKey != null && dateKey.compareTo(selectedDate) <= 0) {
                        availableDates.add(dateKey);
                    }
                }

                if (availableDates.isEmpty()) {
                    Toast.makeText(RateChartReportActivity.this, "❌ No older rates found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Sort in descending to get latest older
                Collections.sort(availableDates, Collections.reverseOrder());
                String fallbackDate = availableDates.get(0);
                DataSnapshot typeSnapshot = snapshot.child(fallbackDate).child(selectedType);

                if (typeSnapshot.exists()) {
                    selectedDate = fallbackDate; // Update selectedDate to used fallback
                    rateList.clear();

                    for (DataSnapshot child : typeSnapshot.getChildren()) {
                        String fat = child.getKey().replace("_", ".");
                        Double rate = child.getValue(Double.class);
                        rateList.add(new RateModel(fat, rate != null ? rate : 0));
                    }

                    Toast.makeText(RateChartReportActivity.this, "⚠️ Showing nearest past rate from: " + fallbackDate, Toast.LENGTH_LONG).show();
                    createPdfFromRates();
                } else {
                    Toast.makeText(RateChartReportActivity.this, "❌ No rates found for " + selectedType, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RateChartReportActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createPdfFromRates() {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        int pageWidth = 300;
        int pageHeight = 600;

        int x = 10, y = 30;
        int lineHeight = 20;
        int maxLinesPerPage = (pageHeight - 100) / lineHeight;

        int currentLine = 0;
        int pageNumber = 1;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setTextSize(14f);
        canvas.drawText("Rate Chart Report", x, y, paint);
        y += lineHeight;
        canvas.drawText("Date: " + selectedDate + " (" + selectedType + ")", x, y, paint);
        y += lineHeight;
        canvas.drawText("----------------------------", x, y, paint);
        y += lineHeight;
        canvas.drawText(" FAT Value      |    Rate ₹", x, y, paint);
        y += lineHeight;
        canvas.drawText("----------------------------", x, y, paint);
        y += lineHeight;

        for (RateModel model : rateList) {
            if (currentLine >= maxLinesPerPage) {
                pdfDocument.finishPage(page);
                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 30;
                canvas.drawText("Rate Chart Report (contd.)", x, y, paint);
                y += lineHeight;
                canvas.drawText("Date: " + selectedDate + " (" + selectedType + ")", x, y, paint);
                y += lineHeight;
                canvas.drawText("----------------------------", x, y, paint);
                canvas.drawText(" FAT Value      |    Rate ₹", x, y + lineHeight, paint);
                canvas.drawText("----------------------------", x, y + 2 * lineHeight, paint);
                y += 3 * lineHeight;
                currentLine = 0;
            }

            canvas.drawText("     " + model.getFat() + "         |    ₹" + model.getRate(), x, y, paint);
            y += lineHeight;
            currentLine++;
        }

        pdfDocument.finishPage(page);

        try {
            File dir = new File(getExternalFilesDir(null), "RateReports");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "RateReport_" + selectedDate + "_" + selectedType + ".pdf");
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            fos.close();
            pdfDocument.close();

            Toast.makeText(this, "✅ PDF Saved Successfully", Toast.LENGTH_SHORT).show();

            // OPEN the file directly after saving
            openPdfFile(file);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



    private void openPdfFile(File pdfFile) {
        if (pdfFile.exists()) {
            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    pdfFile
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "⚠️ No PDF viewer found! Install one like Adobe or Google Drive.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "❌ PDF file not found!", Toast.LENGTH_SHORT).show();
        }
    }

}
