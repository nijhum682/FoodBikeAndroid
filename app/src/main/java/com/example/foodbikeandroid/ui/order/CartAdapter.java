package com.example.foodbikeandroid.ui.order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.CartItem;

public class CartAdapter extends ListAdapter<CartItem, CartAdapter.CartViewHolder> {

    private CartItemListener listener;

    public interface CartItemListener {
        void onIncreaseQuantity(CartItem item);
        void onDecreaseQuantity(CartItem item);
        void onRemoveItem(CartItem item);
    }

    public CartAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<CartItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<CartItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            return oldItem.getMenuItem().getId().equals(newItem.getMenuItem().getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            return oldItem.getMenuItem().getId().equals(newItem.getMenuItem().getId()) &&
                    oldItem.getQuantity() == newItem.getQuantity();
        }
    };

    public void setCartItemListener(CartItemListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = getItem(position);
        holder.bind(item);
    }

    class CartViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvItemName;
        private final TextView tvItemPrice;
        private final TextView tvQuantity;
        private final TextView tvTotalPrice;
        private final ImageButton btnDecrease;
        private final ImageButton btnIncrease;
        private final ImageButton btnRemove;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        void bind(CartItem item) {
            tvItemName.setText(item.getMenuItem().getName());
            tvItemPrice.setText(String.format("৳%.0f each", item.getMenuItem().getPrice()));
            tvQuantity.setText(String.valueOf(item.getQuantity()));
            tvTotalPrice.setText(String.format("৳%.0f", item.getTotalPrice()));

            btnIncrease.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIncreaseQuantity(item);
                }
            });

            btnDecrease.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDecreaseQuantity(item);
                }
            });

            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveItem(item);
                }
            });
        }
    }
}
