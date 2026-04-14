package com.example.inventoryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private Context context;
    private List<String> categories;
    private OnCategoryClickListener listener;
    private Map<String, Integer> categoryIcons;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public CategoryAdapter(Context context, List<String> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
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
        
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        ImageView ivCategoryImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            ivCategoryImage = itemView.findViewById(R.id.ivCategoryImage);
        }
    }
}
