package com.example.foodbikeandroid.ui.dashboard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.Restaurant;
import com.google.android.material.card.MaterialCardView;

public class AdminRestaurantAdapter extends ListAdapter<Restaurant, AdminRestaurantAdapter.RestaurantViewHolder> {

    private OnRestaurantActionListener listener;

    public interface OnRestaurantActionListener {
        void onRestaurantClick(Restaurant restaurant);
        void onRestaurantDelete(Restaurant restaurant, int position);
        void onEditMenuClick(Restaurant restaurant);
        void onViewDetailsClick(Restaurant restaurant);
    }

    public AdminRestaurantAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Restaurant> DIFF_CALLBACK = new DiffUtil.ItemCallback<Restaurant>() {
        @Override
        public boolean areItemsTheSame(@NonNull Restaurant oldItem, @NonNull Restaurant newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Restaurant oldItem, @NonNull Restaurant newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getRating() == newItem.getRating() &&
                    oldItem.getAddress().equals(newItem.getAddress()) &&
                    oldItem.getDivision().equals(newItem.getDivision()) &&
                    oldItem.getDistrict().equals(newItem.getDistrict());
        }
    };

    public void setOnRestaurantActionListener(OnRestaurantActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_restaurant, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = getItem(position);
        holder.bind(restaurant);
    }

    public Restaurant getRestaurantAt(int position) {
        return getItem(position);
    }

    class RestaurantViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardView;
        private final TextView tvName;
        private final TextView tvId;
        private final TextView tvLocation;
        private final TextView tvRating;
        private final TextView tvMenuItemCount;
        private final ImageView ivEdit;
        private final ImageView ivEditMenu;
        private final ImageView ivViewDetails;

        RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardRestaurant);
            tvName = itemView.findViewById(R.id.tvRestaurantName);
            tvId = itemView.findViewById(R.id.tvRestaurantId);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvMenuItemCount = itemView.findViewById(R.id.tvMenuItemCount);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivEditMenu = itemView.findViewById(R.id.ivEditMenu);
            ivViewDetails = itemView.findViewById(R.id.ivViewDetails);
        }

        void bind(Restaurant restaurant) {
            tvName.setText(restaurant.getName());
            tvId.setText(restaurant.getId());
            tvLocation.setText(String.format("%s, %s - %s", 
                    restaurant.getDivision(), 
                    restaurant.getDistrict(), 
                    restaurant.getAddress()));
            tvRating.setText(String.format("%.1f", restaurant.getRating()));
            
            int menuCount = restaurant.getMenuItems() != null ? restaurant.getMenuItems().size() : 0;
            tvMenuItemCount.setText(itemView.getContext().getString(R.string.menu_item_count_short, menuCount));

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRestaurantClick(restaurant);
                }
            });

            ivEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRestaurantClick(restaurant);
                }
            });
            
            ivEditMenu.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditMenuClick(restaurant);
                }
            });

            ivViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetailsClick(restaurant);
                }
            });
        }
    }

    public static class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        private final AdminRestaurantAdapter adapter;
        private final Paint paint;
        private final Drawable deleteIcon;
        private final int iconMargin;

        public SwipeToDeleteCallback(AdminRestaurantAdapter adapter, RecyclerView recyclerView) {
            super(0, ItemTouchHelper.LEFT);
            this.adapter = adapter;
            
            paint = new Paint();
            paint.setColor(ContextCompat.getColor(recyclerView.getContext(), R.color.error));
            
            deleteIcon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.ic_delete);
            if (deleteIcon != null) {
                deleteIcon.setTint(ContextCompat.getColor(recyclerView.getContext(), R.color.white));
            }
            iconMargin = 32;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, 
                @NonNull RecyclerView.ViewHolder viewHolder, 
                @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            if (adapter.listener != null) {
                adapter.listener.onRestaurantDelete(adapter.getItem(position), position);
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                int actionState, boolean isCurrentlyActive) {
            
            View itemView = viewHolder.itemView;
            
            if (dX < 0) {
                float cornerRadius = 12 * itemView.getContext().getResources().getDisplayMetrics().density;
                RectF background = new RectF(
                        itemView.getRight() + dX - 20,
                        itemView.getTop() + 12,
                        itemView.getRight() - 16,
                        itemView.getBottom() - 12
                );
                c.drawRoundRect(background, cornerRadius, cornerRadius, paint);

                if (deleteIcon != null) {
                    int iconSize = 24 * (int) itemView.getContext().getResources().getDisplayMetrics().density;
                    int iconTop = itemView.getTop() + (itemView.getHeight() - iconSize) / 2;
                    int iconBottom = iconTop + iconSize;
                    int iconLeft = itemView.getRight() - iconMargin - iconSize;
                    int iconRight = itemView.getRight() - iconMargin;

                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIcon.draw(c);
                }
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        @Override
        public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
            return 0.5f;
        }
    }
}
