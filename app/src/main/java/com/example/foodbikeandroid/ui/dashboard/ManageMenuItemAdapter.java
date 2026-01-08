package com.example.foodbikeandroid.ui.dashboard;

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
import com.example.foodbikeandroid.data.model.MenuItem;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ManageMenuItemAdapter extends ListAdapter<MenuItem, ManageMenuItemAdapter.MenuItemViewHolder> {

    private OnMenuItemActionListener listener;

    public interface OnMenuItemActionListener {
        void onEditClick(MenuItem item, int position);
        void onDeleteClick(MenuItem item, int position);
        void onAvailabilityChanged(MenuItem item, int position, boolean isAvailable);
    }

    public ManageMenuItemAdapter() {
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
                    oldItem.getDescription().equals(newItem.getDescription()) &&
                    oldItem.getPrice() == newItem.getPrice() &&
                    oldItem.isAvailable() == newItem.isAvailable();
        }
    };

    public void setOnMenuItemActionListener(OnMenuItemActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_menu, parent, false);
        return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        MenuItem item = getItem(position);
        holder.bind(item, position);
    }

    class MenuItemViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardView;
        private final TextView tvName;
        private final TextView tvDescription;
        private final TextView tvPrice;
        private final SwitchMaterial switchAvailable;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardManageMenuItem);
            tvName = itemView.findViewById(R.id.tvManageItemName);
            tvDescription = itemView.findViewById(R.id.tvManageItemDescription);
            tvPrice = itemView.findViewById(R.id.tvManageItemPrice);
            switchAvailable = itemView.findViewById(R.id.switchAvailable);
            btnEdit = itemView.findViewById(R.id.btnEditItem);
            btnDelete = itemView.findViewById(R.id.btnDeleteItem);
        }

        void bind(MenuItem item, int position) {
            tvName.setText(item.getName());
            tvDescription.setText(item.getDescription());
            tvPrice.setText(String.format("৳%.0f", item.getPrice()));
            
            switchAvailable.setOnCheckedChangeListener(null);
            switchAvailable.setChecked(item.isAvailable());
            
            cardView.setAlpha(item.isAvailable() ? 1.0f : 0.6f);

            switchAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onAvailabilityChanged(item, position, isChecked);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(item, position);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(item, position);
                }
            });
        }
    }
}
