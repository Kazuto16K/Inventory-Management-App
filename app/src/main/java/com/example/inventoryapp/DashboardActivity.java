package com.example.inventoryapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class DashboardActivity extends AppCompatActivity implements InventoryAdapter.OnItemActionListener {

    private TextView tvWelcome, tvTotalItems, tvLowStock, tvOutOfStock, tvTotalValue;
    private RecyclerView recyclerView;
    private InventoryAdapter adapter;
    private EditText etSearch;
    private FloatingActionButton fabAdd;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private List<InventoryItem> itemList;

    private static final int REQUEST_ADD_ITEM = 100;
    private static final int REQUEST_EDIT_ITEM = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Inventory Dashboard");
        }

        tvWelcome = findViewById(R.id.tvWelcome);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvLowStock = findViewById(R.id.tvLowStock);
        tvOutOfStock = findViewById(R.id.tvOutOfStock);
        tvTotalValue = findViewById(R.id.tvTotalValue);
        etSearch = findViewById(R.id.etSearch);
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);

        tvWelcome.setText("Hello, " + sessionManager.getUsername() + "!");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AddItemActivity.class);
            startActivityForResult(intent, REQUEST_ADD_ITEM);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadData();
    }

    private void loadData() {
        itemList = dbHelper.getAllItems();
        adapter = new InventoryAdapter(this, itemList, this);
        recyclerView.setAdapter(adapter);
        updateStats();
    }

    private void updateStats() {
        tvTotalItems.setText(String.valueOf(dbHelper.getTotalItems()));
        tvLowStock.setText(String.valueOf(dbHelper.getLowStockCount()));
        tvOutOfStock.setText(String.valueOf(dbHelper.getOutOfStockCount()));
        tvTotalValue.setText(String.format("₹%.2f", dbHelper.getTotalInventoryValue()));
    }

    private void filterItems(String query) {
        if (query.isEmpty()) {
            adapter.updateList(dbHelper.getAllItems());
        } else {
            adapter.updateList(dbHelper.searchItems(query));
        }
    }

    @Override
    public void onEdit(InventoryItem item) {
        Intent intent = new Intent(this, EditItemActivity.class);
        intent.putExtra("item", item);
        startActivityForResult(intent, REQUEST_EDIT_ITEM);
    }

    @Override
    public void onDelete(InventoryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete \"" + item.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteItem(item.getId())) {
                        Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                        loadData();
                    } else {
                        Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> sessionManager.logout())
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
