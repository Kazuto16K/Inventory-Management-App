package com.example.inventoryapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reports extends AppCompatActivity {

    private PieChart importChart, exportChart;
    private Button btnExportCsv, btnExportPdf;
    private DatabaseHelper dbHelper;
    private List<InventoryItem> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reports_analytics);

        dbHelper = new DatabaseHelper(this);

        importChart = findViewById(R.id.importChart);
        exportChart = findViewById(R.id.exportChart);
        btnExportCsv = findViewById(R.id.btnExportCsv);
        btnExportPdf = findViewById(R.id.btnExportPdf);

        loadData();

        btnExportCsv.setOnClickListener(v -> exportInventoryToCsv());
        btnExportPdf.setOnClickListener(v ->
                Toast.makeText(this, "PDF Exporting... (Simplified)", Toast.LENGTH_SHORT).show()
        );

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_reports);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_reports) {
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
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
            }
            return false;
        });
    }

    private void loadData() {
        dbHelper.getAllItems(items -> {
            this.itemList = items;
            setupImportChart(items);
        });

        dbHelper.getSaleLogs(logs -> {
            setupExportChart(logs);
        });
    }

    private void setupImportChart(List<InventoryItem> items) {
        Map<String, Integer> categoryMap = new HashMap<>();
        for (InventoryItem item : items) {
            categoryMap.put(item.getCategory(),
                    categoryMap.getOrDefault(item.getCategory(), 0) + item.getQuantity());
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Current Stock");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        importChart.setData(data);
        importChart.getDescription().setEnabled(false);
        importChart.invalidate();
    }

    private void setupExportChart(List<SaleLog> logs) {
        Map<String, Integer> itemMap = new HashMap<>();
        for (SaleLog log : logs) {
            itemMap.put(log.getItemName(),
                    itemMap.getOrDefault(log.getItemName(), 0) + log.getQuantity());
        }

        List<PieEntry> entries = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, Integer> entry : itemMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            if (++count >= 5) break; 
        }

        PieDataSet dataSet = new PieDataSet(entries, "Top 5 Sold Items");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        exportChart.setData(data);
        exportChart.getDescription().setEnabled(false);
        exportChart.invalidate();
    }

    private void exportInventoryToCsv() {
        if (itemList == null || itemList.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder csvContent = new StringBuilder();
        csvContent.append("ID,Name,Category,Quantity,Price,Description\n");

        for (InventoryItem item : itemList) {
            csvContent.append(item.getId()).append(",")
                    .append(item.getName()).append(",")
                    .append(item.getCategory()).append(",")
                    .append(item.getQuantity()).append(",")
                    .append(item.getPrice()).append(",")
                    .append(item.getDescription()).append("\n");
        }

        try {
            File file = new File(getExternalFilesDir(null), "inventory_report.csv");
            FileOutputStream out = new FileOutputStream(file);
            out.write(csvContent.toString().getBytes());
            out.close();

            Uri path = FileProvider.getUriForFile(this,
                    "com.example.inventoryapp.fileprovider", file);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Inventory Report");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(intent, "Share Report"));

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
