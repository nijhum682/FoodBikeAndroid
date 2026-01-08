package com.example.foodbikeandroid.ui.order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.CartItem;

public class OrderSummaryAdapter extends ListAdapter<CartItem, OrderSummaryAdapter.OrderSummaryViewHolder> {

    public OrderSummaryAdapter() {
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

    @NonNull
    @Override
    public OrderSummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_summary, parent, false);
        return new OrderSummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderSummaryViewHolder holder, int position) {
        CartItem item = getItem(position);
        holder.bind(item);
    }

    static class OrderSummaryViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvItemName;
        private final TextView tvQuantity;
        private final TextView tvPrice;

        OrderSummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }

        void bind(CartItem item) {
            tvItemName.setText(item.getMenuItem().getName());
            tvQuantity.setText(String.format("x%d", item.getQuantity()));
            tvPrice.setText(String.format("৳%.0f", item.getTotalPrice()));
        }
    }
}
