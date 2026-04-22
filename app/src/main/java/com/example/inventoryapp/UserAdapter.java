package com.example.inventoryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<Map<String, Object>> users;
    private boolean isApprovedList;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onApprove(String email);
        void onReject(String email);
        void onDelete(String email);
    }

    public UserAdapter(List<Map<String, Object>> users, boolean isApprovedList, OnUserActionListener listener) {
        this.users = users;
        this.isApprovedList = isApprovedList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> user = users.get(position);
        String username = (String) user.get("username");
        String email = (String) user.get("email");

        holder.tvUserName.setText(username);
        holder.tvUserEmail.setText(email);

        if (isApprovedList) {
            holder.pendingActions.setVisibility(View.GONE);
            holder.btnDeleteUser.setVisibility(View.VISIBLE);
            holder.btnDeleteUser.setOnClickListener(v -> listener.onDelete(email));
        } else {
            holder.pendingActions.setVisibility(View.VISIBLE);
            holder.btnDeleteUser.setVisibility(View.GONE);
            holder.btnApprove.setOnClickListener(v -> listener.onApprove(email));
            holder.btnReject.setOnClickListener(v -> listener.onReject(email));
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail;
        LinearLayout pendingActions;
        ImageButton btnApprove, btnReject, btnDeleteUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            pendingActions = itemView.findViewById(R.id.pendingActions);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
