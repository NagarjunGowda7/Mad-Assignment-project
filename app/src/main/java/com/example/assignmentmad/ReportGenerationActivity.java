package com.example.assignmentmad;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

public class ReportGenerationActivity extends AppCompatActivity {
    private Button btnGenerateReport;
    private DatabaseReference mDatabase;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_generation);

        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        mDatabase = FirebaseUtils.getDatabaseReference().child("users");

        btnGenerateReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ReportGenerationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ReportGenerationActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_EXTERNAL_STORAGE);
                } else {
                    generateReport();
                }
            }
        });
    }

    private void generateReport() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    createPdfReport(dataMap);
                } else {
                    Toast.makeText(ReportGenerationActivity.this, "No user data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    private void createPdfReport(HashMap<String, Object> dataMap) {
        try {
            String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PDFReports";
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            Document document = new Document();
            String fileName = "UserReport_" + System.currentTimeMillis() + ".pdf";
            String filePath = folder.getAbsolutePath() + "/" + fileName;
            PdfWriter.getInstance(document, new FileOutputStream(filePath));

            document.open();

            for (HashMap.Entry<String, Object> entry : dataMap.entrySet()) {
                String userId = entry.getKey();
                HashMap<String, Object> userMap = (HashMap<String, Object>) entry.getValue();

                String username = (String) userMap.get("username");
                String name = (String) userMap.get("name");
                String dateOfBirth = (String) userMap.get("dateOfBirth");
                String gender = (String) userMap.get("gender");
                String address = (String) userMap.get("address");

                document.add(new Paragraph("Username: " + username));
                document.add(new Paragraph("Name: " + name));
                document.add(new Paragraph("Date of Birth: " + dateOfBirth));
                document.add(new Paragraph("Gender: " + gender));
                document.add(new Paragraph("Address: " + address));
                document.add(new Paragraph("\n")); // Add a blank line between user details
            }

            document.close();

            Toast.makeText(ReportGenerationActivity.this, "Report generated successfully: " + filePath, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ReportGenerationActivity.this, "Failed to generate report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateReport();
            } else {
                Toast.makeText(this, "Write External Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}