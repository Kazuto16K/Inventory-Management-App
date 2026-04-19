package com.example.inventoryapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private Context context;
    private List<InventoryItem> items;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onEdit(InventoryItem item);
        void onDelete(InventoryItem item);
        void onItemClick(InventoryItem item);
    }

    public InventoryAdapter(Context context, List<InventoryItem> items, OnItemActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inventory_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = items.get(position);

        holder.tvItemName.setText(item.getName());
        holder.tvCategory.setText(item.getCategory() != null ? item.getCategory() : "Uncategorized");
        holder.tvQuantity.setText("Qty: " + item.getQuantity());
        holder.tvPrice.setText(String.format("₹%.2f", item.getPrice()));
        holder.tvDescription.setText(item.getDescription() != null ? item.getDescription() : "No description");

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                .load(item.getImageUrl())
                .transform(new CircleCrop())
                .placeholder(R.drawable.app_logo)
                .into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageResource(R.drawable.app_logo);
        }

        // Dark mode compatible status coloring
        int redColor = ContextCompat.getColor(context, R.color.status_red);
        int orangeColor = ContextCompat.getColor(context, R.color.status_orange);
        int greenColor = ContextCompat.getColor(context, R.color.status_green);
        int surfaceColor = ContextCompat.getColor(context, R.color.surface);

        if (item.isOutOfStock()) {
            holder.tvStatus.setText("OUT OF STOCK");
            holder.tvStatus.setTextColor(redColor);
            holder.tvQuantity.setTextColor(redColor);
            
            // Subtle tinted background for status indication
            if ((context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) 
                    == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#332222")); // Dark Red tint
            } else {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE")); // Light Red tint
            }
            
        } else if (item.isLowStock()) {
            holder.tvStatus.setText("LOW STOCK");
            holder.tvStatus.setTextColor(orangeColor);
            holder.tvQuantity.setTextColor(orangeColor);
            
            if ((context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) 
                    == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#332B22")); // Dark Orange tint
            } else {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFF8E1")); // Light Orange tint
            }
            
        } else {
            holder.tvStatus.setText("IN STOCK");
            holder.tvStatus.setTextColor(greenColor);
            holder.tvQuantity.setTextColor(greenColor);
            holder.cardView.setCardBackgroundColor(surfaceColor);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(List<InventoryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvItemName, tvCategory, tvQuantity, tvPrice, tvDescription, tvStatus;
        ImageButton btnEdit, btnDelete;
        ImageView ivProductImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            cardView = itemView.findViewById(R.id.cardView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
