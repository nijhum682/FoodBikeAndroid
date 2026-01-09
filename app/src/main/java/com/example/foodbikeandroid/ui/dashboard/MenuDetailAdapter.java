package com.example.foodbikeandroid.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MenuDetailAdapter extends RecyclerView.Adapter<MenuDetailAdapter.MenuViewHolder> {

    private List<MenuItem> menuItems = new ArrayList<>();

    public void setMenuItems(List<MenuItem> items) {
        this.menuItems = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_detail, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem item = menuItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvItemName;
        private final TextView tvItemDescription;
        private final TextView tvItemCategory;
        private final TextView tvItemPrice;
        private final TextView tvItemAvailability;

        MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemDescription = itemView.findViewById(R.id.tvItemDescription);
            tvItemCategory = itemView.findViewById(R.id.tvItemCategory);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            tvItemAvailability = itemView.findViewById(R.id.tvItemAvailability);
        }

        void bind(MenuItem item) {
            tvItemName.setText(item.getName());
            tvItemDescription.setText(item.getDescription());
            
            String category = item.getCategory();
            if (category != null && !category.isEmpty()) {
                tvItemCategory.setVisibility(View.VISIBLE);
                tvItemCategory.setText(itemView.getContext().getString(R.string.category_format, category));
            } else {
                tvItemCategory.setVisibility(View.GONE);
            }

            String price = "৳" + String.format("%.0f", item.getPrice());
            tvItemPrice.setText(price);

            if (item.isAvailable()) {
                tvItemAvailability.setText(R.string.available);
                tvItemAvailability.setTextColor(itemView.getContext().getColor(R.color.success));
            } else {
                tvItemAvailability.setText(R.string.item_unavailable);
                tvItemAvailability.setTextColor(itemView.getContext().getColor(R.color.error));
            }
        }
    }
}
