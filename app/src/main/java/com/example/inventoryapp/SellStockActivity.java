package com.example.inventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.*;

public class SellStockActivity extends AppCompatActivity {

    private Spinner spinnerCategory, spinnerItems;
    private EditText etQuantity, etCustomer, etCustomerEmail;
    private TextView tvItemStockPrice, tvTotalItems, tvGrandTotal;
    private Button btnAddItem, btnFinalize;
    private ProgressBar pbSell;
    private LinearLayout layoutSummary;
    private RecyclerView rvCart;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private List<InventoryItem> filteredItems;
    private List<CartItem> cart = new ArrayList<>();
    private CartAdapter cartAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_stock);

        Toolbar toolbar = findViewById(R.id.toolbarSell);
        setSupportActionBar(toolbar);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerItems = findViewById(R.id.spinnerItems);
        etQuantity = findViewById(R.id.etSellQuantity);
        etCustomer = findViewById(R.id.etCustomer);
        etCustomerEmail = findViewById(R.id.etCustomerEmail);
        tvItemStockPrice = findViewById(R.id.tvItemStockPrice);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);
        btnAddItem = findViewById(R.id.btnAddItem);
        btnFinalize = findViewById(R.id.btnSellStock);
        pbSell = findViewById(R.id.pbSell);
        layoutSummary = findViewById(R.id.layoutSummary);
        rvCart = findViewById(R.id.rvCart);

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter();
        rvCart.setAdapter(cartAdapter);

        loadCategories();

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadItemsByCategory(spinnerCategory.getSelectedItem().toString());
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSelectedItemDetails();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnAddItem.setOnClickListener(v -> addToCart());
        btnFinalize.setOnClickListener(v -> finalizeSale());
        
        setupBottomNavigation();
    }

    private void updateSelectedItemDetails() {
        int selectedPos = spinnerItems.getSelectedItemPosition();
        if (selectedPos >= 0 && filteredItems != null && !filteredItems.isEmpty()) {
            InventoryItem selected = filteredItems.get(selectedPos);
            tvItemStockPrice.setText(String.format(Locale.getDefault(), 
                "Stock: %d | Price: ₹%.2f", selected.getQuantity(), selected.getPrice()));
        } else {
            tvItemStockPrice.setText("Stock: 0 | Price: ₹0.00");
        }
    }

    private void addToCart() {
        String qtyStr = etQuantity.getText().toString();
        if (TextUtils.isEmpty(qtyStr)) {
            Toast.makeText(this, "Enter quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        int qty = Integer.parseInt(qtyStr);
        int selectedPos = spinnerItems.getSelectedItemPosition();
        if (selectedPos < 0 || filteredItems == null || filteredItems.isEmpty()) {
            return;
        }

        InventoryItem selected = filteredItems.get(selectedPos);
        
        int alreadyInCartQty = 0;
        for (CartItem item : cart) {
            if (item.inventoryItem.getId().equals(selected.getId())) {
                alreadyInCartQty += item.quantity;
            }
        }

        if (qty + alreadyInCartQty > selected.getQuantity()) {
            Toast.makeText(this, "Not enough stock", Toast.LENGTH_SHORT).show();
            return;
        }

        cart.add(new CartItem(selected, qty));
        cartAdapter.notifyDataSetChanged();
        updateSummary();
        etQuantity.setText("");
    }

    private void updateSummary() {
        if (cart.isEmpty()) {
            layoutSummary.setVisibility(View.GONE);
            return;
        }
        layoutSummary.setVisibility(View.VISIBLE);
        int totalItems = 0;
        double grandTotal = 0;
        for (CartItem item : cart) {
            totalItems += item.quantity;
            grandTotal += item.quantity * item.inventoryItem.getPrice();
        }
        tvTotalItems.setText(String.valueOf(totalItems));
        tvGrandTotal.setText(String.format(Locale.getDefault(), "₹%.2f", grandTotal));
    }

    private void finalizeSale() {
        String customerName = etCustomer.getText().toString().trim();
        String customerEmail = etCustomerEmail.getText().toString().trim();

        if (TextUtils.isEmpty(customerName) || TextUtils.isEmpty(customerEmail)) {
            Toast.makeText(this, "Enter customer details", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cart.isEmpty()) return;

        // Show loading
        btnFinalize.setVisibility(View.INVISIBLE);
        pbSell.setVisibility(View.VISIBLE);

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String soldBy = sessionManager.getUsername();

        for (CartItem item : cart) {
            InventoryItem invItem = item.inventoryItem;
            invItem.setQuantity(invItem.getQuantity() - item.quantity);
            
            dbHelper.updateItem(invItem, success -> {
                if (success) {
                    dbHelper.insertSale(invItem.getId(), invItem.getName(), item.quantity, 
                        invItem.getPrice(), customerName, soldBy, timestamp, s -> {});
                    
                    AuditLog auditLog = new AuditLog("SOLD", invItem.getName(), invItem.getId(), 
                        item.quantity, soldBy, sessionManager.getEmail(), timestamp, "Sold to: " + customerName);
                    dbHelper.insertAuditLog(auditLog, s -> {});
                }
            });
        }

        EmailSender.sendInvoiceEmail(this, customerEmail, customerName, cart, success -> {
            runOnUiThread(() -> {
                pbSell.setVisibility(View.GONE);
                if (success) {
                    Toast.makeText(this, "Invoice sent successfully!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Sale complete, but invoice failed to send.", Toast.LENGTH_LONG).show();
                }
                finish();
            });
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_sell);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_sell) {
                return true;
            } else if (id == R.id.nav_reports) {
                startActivity(new Intent(this, ReportsActivity.class));
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

    private void loadCategories() {
        dbHelper.getAllCategories(categories -> {
            spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));
        });
    }

    private void loadItemsByCategory(String category) {
        dbHelper.getItemsByCategory(category, items -> {
            filteredItems = items;
            String[] names = new String[items.size()];
            for (int i = 0; i < items.size(); i++) names[i] = items.get(i).getName();
            spinnerItems.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
        });
    }

    public static class CartItem {
        InventoryItem inventoryItem;
        int quantity;
        CartItem(InventoryItem item, int qty) { this.inventoryItem = item; this.quantity = qty; }
    }

    private class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_sell_list, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CartItem item = cart.get(position);
            holder.tvName.setText(item.inventoryItem.getName());
            holder.tvDetails.setText(String.format(Locale.getDefault(), "Qty: %d x ₹%.2f", item.quantity, item.inventoryItem.getPrice()));
            holder.tvTotal.setText(String.format(Locale.getDefault(), "₹%.2f", item.quantity * item.inventoryItem.getPrice()));
            holder.btnRemove.setOnClickListener(v -> {
                cart.remove(position);
                notifyDataSetChanged();
                updateSummary();
            });
        }

        @Override
        public int getItemCount() { return cart.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDetails, tvTotal;
            ImageButton btnRemove;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvItemName);
                tvDetails = v.findViewById(R.id.tvItemDetails);
                tvTotal = v.findViewById(R.id.tvItemTotal);
                btnRemove = v.findViewById(R.id.btnRemoveItem);
            }
        }
    }
}
