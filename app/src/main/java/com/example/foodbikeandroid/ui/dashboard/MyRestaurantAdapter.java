package com.example.foodbikeandroid.ui.dashboard;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.data.model.RestaurantApplication;
import com.example.foodbikeandroid.databinding.ItemMyRestaurantBinding;

import java.util.ArrayList;
import java.util.List;

public class MyRestaurantAdapter extends RecyclerView.Adapter<MyRestaurantAdapter.RestaurantViewHolder> {

    private List<RestaurantApplication> restaurants = new ArrayList<>();
    private OnRestaurantClickListener listener;

    public interface OnRestaurantClickListener {
        void onRestaurantClick(RestaurantApplication restaurant);
        void onViewOrdersClick(RestaurantApplication restaurant);
    }

    public void setOnRestaurantClickListener(OnRestaurantClickListener listener) {
        this.listener = listener;
    }

    public void setRestaurants(List<RestaurantApplication> restaurants) {
        this.restaurants = restaurants != null ? restaurants : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateRestaurantStats(String applicationId, int pendingOrders, int todayOrders, double todayRevenue) {
        for (int i = 0; i < restaurants.size(); i++) {
            if (restaurants.get(i).getApplicationId().equals(applicationId)) {
                notifyItemChanged(i, new RestaurantStats(pendingOrders, todayOrders, todayRevenue));
                break;
            }
        }
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyRestaurantBinding binding = ItemMyRestaurantBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RestaurantViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        holder.bind(restaurants.get(position));
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.get(0) instanceof RestaurantStats) {
            RestaurantStats stats = (RestaurantStats) payloads.get(0);
            holder.updateStats(stats);
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    class RestaurantViewHolder extends RecyclerView.ViewHolder {
        private final ItemMyRestaurantBinding binding;

        RestaurantViewHolder(ItemMyRestaurantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RestaurantApplication restaurant) {
            binding.tvRestaurantName.setText(restaurant.getRestaurantName());
            binding.tvLocation.setText(restaurant.getDistrict() + ", " + restaurant.getDivision());
            
            binding.tvPendingOrders.setText("0");
            binding.tvTodayOrders.setText("0");
            binding.tvTodayRevenue.setText("৳0");

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRestaurantClick(restaurant);
                }
            });

            binding.btnViewOrders.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewOrdersClick(restaurant);
                }
            });
        }

        void updateStats(RestaurantStats stats) {
            binding.tvPendingOrders.setText(String.valueOf(stats.pendingOrders));
            binding.tvTodayOrders.setText(String.valueOf(stats.todayOrders));
            binding.tvTodayRevenue.setText("৳" + (int) stats.todayRevenue);
        }
    }

    static class RestaurantStats {
        final int pendingOrders;
        final int todayOrders;
        final double todayRevenue;

        RestaurantStats(int pendingOrders, int todayOrders, double todayRevenue) {
            this.pendingOrders = pendingOrders;
            this.todayOrders = todayOrders;
            this.todayRevenue = todayRevenue;
        }
    }
}
