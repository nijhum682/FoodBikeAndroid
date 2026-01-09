package com.example.foodbikeandroid.ui.dashboard;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.data.model.CartItem;
import com.example.foodbikeandroid.databinding.ItemOrderDetailBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailItemAdapter extends RecyclerView.Adapter<OrderDetailItemAdapter.ItemViewHolder> {

    private List<CartItem> items = new ArrayList<>();

    public void setItems(List<CartItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderDetailBinding binding = ItemOrderDetailBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderDetailBinding binding;

        ItemViewHolder(ItemOrderDetailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CartItem item) {
            if (item.getMenuItem() != null) {
                binding.tvItemName.setText(item.getMenuItem().getName());
                binding.tvItemDescription.setText(item.getMenuItem().getDescription());
                binding.tvQuantity.setText("x" + item.getQuantity());
                binding.tvItemPrice.setText(String.format(Locale.getDefault(), 
                        "৳%.2f", item.getTotalPrice()));
            }
        }
    }
}
