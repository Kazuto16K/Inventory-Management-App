package com.example.inventoryapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.List;
import java.util.Map;

public class UserManagementActivity extends AppCompatActivity implements UserAdapter.OnUserActionListener {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private DatabaseHelper dbHelper;
    private MaterialButtonToggleGroup toggleGroup;
    private TextView tvEmptyMessage;
    
    private static final int TAB_PENDING = 1;
    private static final int TAB_APPROVED = 2;
    
    private int currentTab = TAB_PENDING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.rvUsers);
        toggleGroup = findViewById(R.id.toggleGroupUsers);
        tvEmptyMessage = findViewById(R.id.tvEmptyUsers);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnPendingUsers) {
                    currentTab = TAB_PENDING;
                } else if (checkedId == R.id.btnApprovedUsers) {
                    currentTab = TAB_APPROVED;
                }
                loadUsers();
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        loadUsers();
    }

    private void loadUsers() {
        if (currentTab == TAB_PENDING) {
            dbHelper.getAllEmployees(false, this::updateUI);
        } else {
            dbHelper.getAllEmployees(true, this::updateUI);
        }
    }

    private void updateUI(List<Map<String, Object>> users) {
        if (users.isEmpty()) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            if (currentTab == TAB_PENDING) {
                tvEmptyMessage.setText("No pending approvals.");
            } else {
                tvEmptyMessage.setText("No approved employees.");
            }
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new UserAdapter(users, currentTab != TAB_PENDING, this);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onApprove(String email) {
        dbHelper.approveUser(email, success -> {
            if (success) {
                Toast.makeText(this, "User approved. Sending email...", Toast.LENGTH_SHORT).show();
                EmailSender.sendApprovalEmail(email, emailSuccess -> {
                    if (emailSuccess) {
                        Toast.makeText(this, "Approval email sent successfully!", Toast.LENGTH_SHORT).show();
                    }
                });
                loadUsers();
            }
        });
    }

    @Override
    public void onReject(String email) {
        dbHelper.deleteUser(email, success -> {
            if (success) {
                Toast.makeText(this, "User request rejected", Toast.LENGTH_SHORT).show();
                loadUsers();
            }
        });
    }

    @Override
    public void onDelete(String email) {
        dbHelper.deleteUser(email, success -> {
            if (success) {
                Toast.makeText(this, "User removed", Toast.LENGTH_SHORT).show();
                loadUsers();
            }
        });
    }
}
