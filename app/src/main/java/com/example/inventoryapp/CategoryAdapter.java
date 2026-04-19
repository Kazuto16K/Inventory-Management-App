package com.example.inventoryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private Context context;
    private List<String> categories;
    private OnCategoryClickListener listener;
    private Map<String, Integer> categoryIcons;
    private DatabaseHelper dbHelper;

    private int expandedPosition = -1;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public CategoryAdapter(Context context, List<String> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
        this.dbHelper = new DatabaseHelper(context);
        initCategoryIcons();
    }

    private void initCategoryIcons() {
        categoryIcons = new HashMap<>();
        categoryIcons.put("Electronics", R.drawable.ic_category_electronics);
        categoryIcons.put("Clothing", R.drawable.ic_category_clothing);
        categoryIcons.put("Food & Beverage", R.drawable.ic_category_food);
        categoryIcons.put("Furniture", R.drawable.ic_category_furniture);
        categoryIcons.put("Tools", R.drawable.ic_settings_24dp);
        categoryIcons.put("Stationery", R.drawable.ic_reports_24dp);
        categoryIcons.put("Medicine", R.drawable.ic_category_default);
        categoryIcons.put("Sports", R.drawable.ic_category_default);
        categoryIcons.put("Toys", R.drawable.ic_category_default);
        categoryIcons.put("Other", R.drawable.ic_category_default);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categories.get(position);
        holder.tvCategoryName.setText(category);
        
        Integer iconRes = categoryIcons.get(category);
        if (iconRes == null) {
            iconRes = R.drawable.ic_category_default;
        }
        holder.ivCategoryImage.setImageResource(iconRes);

        boolean isExpanded = position == expandedPosition;
        holder.expandedSection.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.ivArrow.setRotation(isExpanded ? 180 : 0);

        if (isExpanded) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.rvCategoryItems.setVisibility(View.GONE);
            dbHelper.getItemsByCategory(category, items -> {
                holder.progressBar.setVisibility(View.GONE);
                holder.rvCategoryItems.setVisibility(View.VISIBLE);
                InventoryAdapter itemAdapter = new InventoryAdapter(context, items, (InventoryAdapter.OnItemActionListener) context);
                holder.rvCategoryItems.setLayoutManager(new LinearLayoutManager(context));
                holder.rvCategoryItems.setAdapter(itemAdapter);
            });
        }
        
        holder.headerLayout.setOnClickListener(v -> {
            int previousExpandedPosition = expandedPosition;
            if (isExpanded) {
                expandedPosition = -1;
            } else {
                expandedPosition = position;
            }
            notifyItemChanged(previousExpandedPosition);
            notifyItemChanged(expandedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        ImageView ivCategoryImage;
        ImageView ivArrow;
        LinearLayout headerLayout;
        LinearLayout expandedSection;
        RecyclerView rvCategoryItems;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            ivCategoryImage = itemView.findViewById(R.id.ivCategoryImage);
            ivArrow = itemView.findViewById(R.id.ivArrow);
            headerLayout = itemView.findViewById(R.id.headerLayout);
            expandedSection = itemView.findViewById(R.id.expandedSection);
            rvCategoryItems = itemView.findViewById(R.id.rvCategoryItems);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
