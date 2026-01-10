package com.example.foodbikeandroid.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.MenuItem;
import com.google.android.material.card.MaterialCardView;

public class MenuItemAdapter extends ListAdapter<MenuItem, MenuItemAdapter.MenuItemViewHolder> {

    private OnMenuItemClickListener listener;

    public interface OnMenuItemClickListener {
        void onMenuItemClick(MenuItem item);
    }

    public MenuItemAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<MenuItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<MenuItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull MenuItem oldItem, @NonNull MenuItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull MenuItem oldItem, @NonNull MenuItem newItem) {
            return oldItem.getId().equals(newItem.getId()) &&
                    oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getPrice() == newItem.getPrice() &&
                    oldItem.isAvailable() == newItem.isAvailable();
        }
    };

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu, parent, false);
        return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        MenuItem item = getItem(position);
        holder.bind(item);
    }

    class MenuItemViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardView;
        private final TextView tvName;
        private final TextView tvDescription;
        private final TextView tvPrice;
        private final TextView tvCategory;
        private final TextView tvUnavailable;
        private final Button btnAdd;

        MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardMenuItem);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvDescription = itemView.findViewById(R.id.tvItemDescription);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
            tvCategory = itemView.findViewById(R.id.tvItemCategory);
            tvUnavailable = itemView.findViewById(R.id.tvUnavailable);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }

        void bind(MenuItem item) {
            tvName.setText(item.getName());
            tvDescription.setText(item.getDescription());
            tvPrice.setText(String.format("৳%.0f", item.getPrice()));
            tvCategory.setText(item.getCategory());

            if (item.isAvailable()) {
                tvUnavailable.setVisibility(View.GONE);
                cardView.setAlpha(1.0f);
                btnAdd.setVisibility(View.VISIBLE);
                btnAdd.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onMenuItemClick(item);
                    }
                });
            } else {
                tvUnavailable.setVisibility(View.VISIBLE);
                cardView.setAlpha(0.6f);
                btnAdd.setVisibility(View.GONE);
            }
        }
    }
}
