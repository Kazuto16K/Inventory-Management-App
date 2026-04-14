package com.example.inventoryapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
<<<<<<< HEAD
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
=======

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;
>>>>>>> 484d8b78888c3971ddd600b793d63e7ac4af9043
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddItemActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 101;

    private EditText etBarcode, etItemName, etQuantity, etPrice, etDescription, etMinStock;
    private Spinner spinnerCategory;
<<<<<<< HEAD
    private Button btnSave, btnCancel, btnAddImage;
    private ImageButton btnScanBarcode;
    private ImageView ivProductImage;
    private DatabaseHelper dbHelper;
    private Uri imageUri = null;
    private Uri cameraUri = null;

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    Glide.with(this).load(imageUri).into(ivProductImage);
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraUri != null) {
                    imageUri = cameraUri;
                    Glide.with(this).load(imageUri).into(ivProductImage);
                }
            });
=======
    private Button btnSave, btnCancel;
    private TextInputLayout tilBarcode;
    private DatabaseHelper dbHelper;
    private String[] categories = {"Electronics", "Clothing", "Food & Beverage", "Furniture",
            "Tools", "Stationery", "Medicine", "Sports", "Toys", "Other"};
>>>>>>> 484d8b78888c3971ddd600b793d63e7ac4af9043

    // ── Feature 1: ZXing barcode scanner launcher ──
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), this::onScanResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        dbHelper = new DatabaseHelper(this);

        etBarcode     = findViewById(R.id.etBarcode);
        etItemName    = findViewById(R.id.etItemName);
        etQuantity    = findViewById(R.id.etQuantity);
        etPrice       = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        etMinStock    = findViewById(R.id.etMinStock);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave       = findViewById(R.id.btnSave);
        btnCancel     = findViewById(R.id.btnCancel);
<<<<<<< HEAD
        btnScanBarcode = findViewById(R.id.btnScanBarcode);
        ivProductImage = findViewById(R.id.ivProductImage);
        btnAddImage    = findViewById(R.id.btnAddImage);
=======
        tilBarcode    = findViewById(R.id.tilBarcode);
>>>>>>> 484d8b78888c3971ddd600b793d63e7ac4af9043

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveItem());
        btnCancel.setOnClickListener(v -> finish());
        btnAddImage.setOnClickListener(v -> showImagePickerDialog());

        // ── Feature 1: Launch scanner when scan button tapped ──
        tilBarcode.setEndIconOnClickListener(v -> launchScanner());

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_add);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_add) {
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_sell) {
                startActivity(new Intent(this, SellStockActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_reports) {
                startActivity(new Intent(this, Reports.class));
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

    // ── Image Selection ──
    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Camera
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSION_REQUEST);
                } else {
                    launchCamera();
                }
            } else if (which == 1) {
                // Gallery
                galleryLauncher.launch("image/*");
            }
        });
        builder.show();
    }

    private void launchCamera() {
        File photoFile = new File(getCacheDir(), "camera_image_" + System.currentTimeMillis() + ".jpg");
        cameraUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", photoFile);
        cameraLauncher.launch(cameraUri);
    }

    // ── Feature 1: Request camera permission then launch ZXing ──
    private void launchScanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            startBarcodeScanner();
        }
    }

    private void startBarcodeScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan a barcode or QR code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        options.setBarcodeImageEnabled(false);
        barcodeLauncher.launch(options);
    }

    // ── Feature 1: Handle scan result ──
    private void onScanResult(ScanIntentResult result) {
        if (result.getContents() != null) {
            String scannedBarcode = result.getContents();
            etBarcode.setText(scannedBarcode);
            autoFillProductDetails(scannedBarcode);
            Toast.makeText(this, "Barcode scanned: " + scannedBarcode,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void autoFillProductDetails(String barcode) {
        dbHelper.getItemByBarcode(barcode, item -> {
            if (item != null) {
                etItemName.setText(item.getName());
                etPrice.setText(String.valueOf(item.getPrice()));
                
                // Set category spinner
                if (item.getCategory() != null) {
                    for (int i = 0; i < categories.length; i++) {
                        if (categories[i].equals(item.getCategory())) {
                            spinnerCategory.setSelection(i);
                            break;
                        }
                    }
                }
                
                if (item.getDescription() != null) {
                    etDescription.setText(item.getDescription());
                }
                
                Toast.makeText(this, "Product details auto-filled!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBarcodeScanner();
            } else {
                Toast.makeText(this,
                        "Camera permission is required to scan barcodes",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveItem() {
        String barcode     = etBarcode.getText().toString().trim();
        String name        = etItemName.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String priceStr    = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String minStockStr = etMinStock.getText().toString().trim();
        String category    = spinnerCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(name)) {
            etItemName.setError("Item name is required");
            etItemName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(quantityStr)) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return;
        }

        int quantity = Integer.parseInt(quantityStr);
        double price = Double.parseDouble(priceStr);
        int minStock = TextUtils.isEmpty(minStockStr) ? 5 : Integer.parseInt(minStockStr);

        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

<<<<<<< HEAD
        if (imageUri != null) {
            uploadImageAndSave(name, category, quantity, price, description, minStock, createdAt);
        } else {
            InventoryItem item = new InventoryItem(
                    name, category, quantity, price,
                    description, minStock, createdAt, null
            );
            saveToDatabase(item);
        }
    }
=======
        InventoryItem item = new InventoryItem(
                name, barcode, category, quantity, price,
                description, minStock, createdAt
        );
>>>>>>> 484d8b78888c3971ddd600b793d63e7ac4af9043

    private void uploadImageAndSave(String name, String category, int quantity, double price,
                                    String description, int minStock, String createdAt) {
        btnSave.setEnabled(false);
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("inventory_images/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        InventoryItem item = new InventoryItem(
                                name, category, quantity, price,
                                description, minStock, createdAt, uri.toString()
                        );
                        saveToDatabase(item);
                    });
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToDatabase(InventoryItem item) {
        dbHelper.addItem(item, success -> {
            btnSave.setEnabled(true);
            if (success) {
                Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
