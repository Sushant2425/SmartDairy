package com.ss.smartdairy;

import android.content.ActivityNotFoundException;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class FarmerReportActivity extends AppCompatActivity {

    private List<FarmerReportModel> farmerList = new ArrayList<>();
    private DatabaseReference farmersRef;

    CheckBox checkName, checkMobile, checkAddress, checkHasCow, checkHasBuffalo,checkCowMilkCode, checkBuffaloMilkCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_report); // ✅ LINK XML

        Button btnGenerate = findViewById(R.id.btnGenerateFarmerReport);

        // Init checkboxes
        checkName = findViewById(R.id.checkName);
        checkMobile = findViewById(R.id.checkMobile);
        checkAddress = findViewById(R.id.checkAddress);
        checkHasCow = findViewById(R.id.checkHasCow);
        checkHasBuffalo = findViewById(R.id.checkHasBuffalo);
        checkCowMilkCode = findViewById(R.id.checkCowMilkCode);
        checkBuffaloMilkCode = findViewById(R.id.checkBuffaloMilkCode);


        btnGenerate.setOnClickListener(v -> {
            String mobile = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().replace("+91", "");
            farmersRef = FirebaseDatabase.getInstance()
                    .getReference("Dairy")
                    .child("User")
                    .child(mobile)
                    .child("Farmers");

            fetchFarmersAndGeneratePDF();
        });
    }


    private void fetchFarmersAndGeneratePDF() {
        farmersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                farmerList.clear();

                for (DataSnapshot farmerSnap : snapshot.getChildren()) {
                    String name = farmerSnap.child("name").getValue(String.class);
                    String mobile = farmerSnap.child("mobile").getValue(String.class);
                    String address = farmerSnap.child("address").getValue(String.class);
                    boolean hasCow = Boolean.TRUE.equals(farmerSnap.child("hasCow").getValue(Boolean.class));
                    boolean hasBuffalo = Boolean.TRUE.equals(farmerSnap.child("hasBuffalo").getValue(Boolean.class));
                    String cowMilkCode = farmerSnap.child("cowMilkCode").getValue(String.class);
                    String buffaloMilkCode = farmerSnap.child("buffaloMilkCode").getValue(String.class);


                    farmerList.add(new FarmerReportModel(name, mobile, address, hasCow, hasBuffalo, cowMilkCode, buffaloMilkCode));

                }

                if (farmerList.isEmpty()) {
                    Toast.makeText(FarmerReportActivity.this, "No farmers found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                createFarmersReportPDF();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FarmerReportActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createFarmersReportPDF() {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        paint.setTextSize(12f);

        int pageWidth = 600;
        int pageHeight = 800;
        int margin = 20;
        int lineHeight = 30;

        int[] columnWidths = getColumnWidths(); // Helper method to define columns based on checkboxes

        int totalTableWidth = 0;
        for (int w : columnWidths) totalTableWidth += w;

        int xStart = margin;
        int yStart = margin + 40;
        int y = yStart;

        int maxRowsPerPage = (pageHeight - yStart - 50) / lineHeight;
        int rowCount = 0;
        int pageNumber = 1;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Title
        paint.setFakeBoldText(true);
        canvas.drawText("👥 Farmers Report", xStart, margin + 20, paint);
        paint.setFakeBoldText(false);

        // Draw header
        drawTableRow(canvas, paint, xStart, y, true);
        y += lineHeight;
        drawLine(canvas, xStart, y, xStart + totalTableWidth, y);
        y += 4;

        for (int i = 0; i < farmerList.size(); i++) {
            if (rowCount == maxRowsPerPage) {
                pdfDocument.finishPage(page);
                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();

                y = yStart;
                rowCount = 0;

                paint.setFakeBoldText(true);
                canvas.drawText("👥 Farmers Report (Page " + pageNumber + ")", xStart, margin + 20, paint);
                paint.setFakeBoldText(false);

                drawTableRow(canvas, paint, xStart, y, true);
                y += lineHeight;
                drawLine(canvas, xStart, y, xStart + totalTableWidth, y);
                y += 4;
            }

            drawTableRow(canvas, paint, xStart, y, false, i);
            y += lineHeight;
            drawLine(canvas, xStart, y, xStart + totalTableWidth, y);
            rowCount++;
        }

        pdfDocument.finishPage(page);

        try {
            File dir = new File(getExternalFilesDir(null), "FarmerReports");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "FarmersReport.pdf");
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            fos.close();
            pdfDocument.close();

            Toast.makeText(this, "✅ Farmers PDF Saved", Toast.LENGTH_SHORT).show();
            openPdfFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private int[] getColumnWidths() {
        List<Integer> widths = new ArrayList<>();
        widths.add(40); // Sr No.

        if (checkName.isChecked()) widths.add(100);
        if (checkMobile.isChecked()) widths.add(100);
        if (checkAddress.isChecked()) widths.add(100);
        if (checkHasCow.isChecked()) widths.add(50);
        if (checkHasBuffalo.isChecked()) widths.add(60);
        if (checkCowMilkCode.isChecked()) widths.add(70);
        if (checkBuffaloMilkCode.isChecked()) widths.add(70);

        int[] arr = new int[widths.size()];
        for (int i = 0; i < widths.size(); i++) arr[i] = widths.get(i);
        return arr;
    }

    private void drawLine(Canvas canvas, int startX, int startY, int stopX, int stopY) {
        canvas.drawLine(startX, startY, stopX, stopY, new Paint());
    }

    private void drawTableRow(Canvas canvas, Paint paint, int xStart, int y, boolean isHeader) {
        drawTableRow(canvas, paint, xStart, y, isHeader, -1); // for headers, index -1
    }

    private void drawTableRow(Canvas canvas, Paint paint, int xStart, int y, boolean isHeader, int index) {
        int x = xStart;
        int srNo = index + 1;
        FarmerReportModel farmer = (index >= 0) ? farmerList.get(index) : null;

        paint.setFakeBoldText(isHeader);

        // Draw columns
        canvas.drawText(isHeader ? "Sr" : String.valueOf(srNo), x, y, paint);
        x += 40;

        if (checkName.isChecked()) {
            String value = isHeader ? "Name" : farmer.getName();
            canvas.drawText(value, x, y, paint);
            x += 100;
        }

        if (checkMobile.isChecked()) {
            String value = isHeader ? "Mobile" : farmer.getMobile();
            canvas.drawText(value, x, y, paint);
            x += 100;
        }

        if (checkAddress.isChecked()) {
            String value = isHeader ? "Address" : farmer.getAddress();
            canvas.drawText(value, x, y, paint);
            x += 100;
        }

        if (checkHasCow.isChecked()) {
            String value = isHeader ? "Cow" : (farmer.isHasCow() ? "✔" : "✘");
            canvas.drawText(value, x, y, paint);
            x += 50;
        }

        if (checkHasBuffalo.isChecked()) {
            String value = isHeader ? "Buffalo" : (farmer.isHasBuffalo() ? "✔" : "✘");
            canvas.drawText(value, x, y, paint);
            x += 60;
        }

        if (checkCowMilkCode.isChecked()) {
            String value = isHeader ? "Cow Code" : farmer.getCowMilkCode();
            canvas.drawText(value, x, y, paint);
            x += 70;
        }

        if (checkBuffaloMilkCode.isChecked()) {
            String value = isHeader ? "Buff Code" : farmer.getBuffaloMilkCode();
            canvas.drawText(value, x, y, paint);
            x += 70;
        }

        paint.setFakeBoldText(false);
    }


    private String padRight(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text.substring(0, length - 1) + " "; // safer
        return String.format("%-" + length + "s", text);
    }


    private void openPdfFile(File pdfFile) {
        if (pdfFile.exists()) {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No PDF viewer found!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "PDF file not found!", Toast.LENGTH_SHORT).show();
        }
    }
}
