package com.example.inventoryapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;

public class AuditActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TableLayout auditTable;

    private double totalStockValue = 0;
    private double totalRevenue = 0;
    private double profitLoss = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audit);

        dbHelper = new DatabaseHelper(this);

        auditTable = findViewById(R.id.auditTable);

        Button exportBtn =
                findViewById(R.id.btnExportExcel);

        loadAuditData();

        exportBtn.setOnClickListener(
                v -> exportExcel()
        );

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_reports) {
                startActivity(new Intent(this, ReportsActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_sell) {
                startActivity(new Intent(this, SellStockActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(this, AddItemActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadAuditData() {

        dbHelper.getTotalInventoryValue(stockValue -> {

            totalStockValue = stockValue;

            dbHelper.getTotalRevenue(revenue -> {

                totalRevenue = revenue;

                profitLoss = totalRevenue - totalStockValue;

                addRow("Current Inventory Value",  totalStockValue);
                addRow("Total Sales Revenue",       totalRevenue);
                addRow("Revenue vs Stock Difference", profitLoss);
            });
        });
    }


    private void addRow(String label,
                        double value) {

        TableRow row =
                new TableRow(this);

        TextView col1 =
                new TextView(this);

        TextView col2 =
                new TextView(this);

        col1.setText(label);
        col2.setText(String.format("₹%.2f", value));

        col1.setPadding(20,20,20,20);
        col2.setPadding(20,20,20,20);

        row.addView(col1);
        row.addView(col2);

        auditTable.addView(row);
    }


    private void exportExcel() {
        try {
            // Fix: Changed extension to .csv as writing plain text to .xls 
            // causes "corrupted file" warnings in modern Excel/mobile viewers.
            // CSV is universally supported by Excel without corruption errors.
            File file = new File(getExternalFilesDir(null), "audit_report.csv");
            FileOutputStream fos = new FileOutputStream(file);

            // Use comma separation for CSV format
            String data = "Metric,Value\n"
                    + "Inventory Value," + String.format("%.2f", totalStockValue) + "\n"
                    + "Revenue," + String.format("%.2f", totalRevenue) + "\n"
                    + "Profit/Loss," + String.format("%.2f", profitLoss);

            fos.write(data.getBytes());
            fos.close();

            Uri path = FileProvider.getUriForFile(this, "com.example.inventoryapp.fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv"); // Correct MIME type for CSV
            intent.putExtra(Intent.EXTRA_STREAM, path);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(intent, "Share Audit Report"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
