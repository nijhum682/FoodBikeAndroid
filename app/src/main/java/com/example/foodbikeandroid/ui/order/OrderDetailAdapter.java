package com.example.foodbikeandroid.ui.order;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.data.model.CartItem;
import com.example.foodbikeandroid.databinding.ItemOrderDetailBinding;

public class OrderDetailAdapter extends ListAdapter<CartItem, OrderDetailAdapter.OrderDetailViewHolder> {

    private static final DiffUtil.ItemCallback<CartItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<CartItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            return oldItem.getMenuItem().getId().equals(newItem.getMenuItem().getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            return oldItem.getQuantity() == newItem.getQuantity() &&
                   oldItem.getMenuItem().getPrice() == newItem.getMenuItem().getPrice();
        }
    };

    public OrderDetailAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderDetailBinding binding = ItemOrderDetailBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false
        );
        return new OrderDetailViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
        CartItem item = getItem(position);
        holder.bind(item);
    }

    static class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderDetailBinding binding;

        OrderDetailViewHolder(ItemOrderDetailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CartItem item) {
            binding.tvItemName.setText(item.getMenuItem().getName());
            binding.tvItemDescription.setText(item.getMenuItem().getDescription());
            binding.tvQuantity.setText("x" + item.getQuantity());
            binding.tvItemPrice.setText(String.format("৳%.0f", item.getTotalPrice()));
        }
    }
}
