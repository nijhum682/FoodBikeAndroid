package com.example.foodbikeandroid.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.Restaurant;
import com.google.android.material.card.MaterialCardView;

public class RestaurantAdapter extends ListAdapter<Restaurant, RestaurantAdapter.RestaurantViewHolder> {

    private OnRestaurantClickListener listener;

    public interface OnRestaurantClickListener {
        void onRestaurantClick(Restaurant restaurant);
    }

    public RestaurantAdapter() {
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
                    oldItem.getAddress().equals(newItem.getAddress());
        }
    };

    public void setOnRestaurantClickListener(OnRestaurantClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = getItem(position);
        holder.bind(restaurant);
    }

    class RestaurantViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardView;
        private final TextView tvName;
        private final TextView tvLocation;
        private final TextView tvCuisine;
        private final TextView tvRating;
        private final TextView tvStatus;
        private final RatingBar ratingBar;
        private final ImageView ivRestaurant;

        RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardRestaurant);
            tvName = itemView.findViewById(R.id.tvRestaurantName);
            tvLocation = itemView.findViewById(R.id.tvRestaurantLocation);
            tvCuisine = itemView.findViewById(R.id.tvCuisineType);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            ivRestaurant = itemView.findViewById(R.id.ivRestaurant);
        }

        void bind(Restaurant restaurant) {
            tvName.setText(restaurant.getName());
            tvLocation.setText(restaurant.getFullLocation());
            tvCuisine.setText(restaurant.getCuisineType());
            tvRating.setText(String.format("%.1f", restaurant.getRating()));
            ratingBar.setRating((float) restaurant.getRating());
            
            if (restaurant.isOpen()) {
                tvStatus.setText(R.string.open_now);
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.success));
            } else {
                tvStatus.setText(R.string.closed);
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.error));
            }

            int imageRes = getImageForCuisine(restaurant.getCuisineType());
            ivRestaurant.setImageResource(imageRes);

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRestaurantClick(restaurant);
                }
            });
        }

        private int getImageForCuisine(String cuisineType) {
            if (cuisineType == null) return R.drawable.ic_restaurant;
            switch (cuisineType.toLowerCase()) {
                case "pizza":
                    return R.drawable.ic_restaurant;
                case "burger":
                    return R.drawable.ic_restaurant;
                case "seafood":
                    return R.drawable.ic_restaurant;
                case "cafe":
                    return R.drawable.ic_restaurant;
                case "dessert":
                    return R.drawable.ic_restaurant;
                default:
                    return R.drawable.ic_restaurant;
            }
        }
    }
}
