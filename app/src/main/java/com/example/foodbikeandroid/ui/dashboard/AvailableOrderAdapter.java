package com.example.foodbikeandroid.ui.dashboard;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.databinding.ItemAvailableOrderBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AvailableOrderAdapter extends RecyclerView.Adapter<AvailableOrderAdapter.OrderViewHolder> {

    public enum SortOrder {
        NEWEST_FIRST,
        HIGHEST_VALUE
    }

    private List<Order> orders = new ArrayList<>();
    private List<Order> displayedOrders = new ArrayList<>();
    private OnOrderClickListener listener;
    private RestaurantNameProvider restaurantNameProvider;
    private SortOrder currentSortOrder = SortOrder.NEWEST_FIRST;

    public interface OnOrderClickListener {
        void onAcceptOrder(Order order);
    }

    public interface RestaurantNameProvider {
        String getRestaurantName(String restaurantId);
    }

    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void setRestaurantNameProvider(RestaurantNameProvider provider) {
        this.restaurantNameProvider = provider;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders != null ? new ArrayList<>(orders) : new ArrayList<>();
        applySorting();
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.currentSortOrder = sortOrder;
        applySorting();
    }

    private void applySorting() {
        displayedOrders = new ArrayList<>(orders);
        
        if (currentSortOrder == SortOrder.NEWEST_FIRST) {
            Collections.sort(displayedOrders, (o1, o2) -> Long.compare(o2.getCreatedAt(), o1.getCreatedAt()));
        } else {
            Collections.sort(displayedOrders, (o1, o2) -> Double.compare(o2.getTotalPrice(), o1.getTotalPrice()));
        }
        
        notifyDataSetChanged();
    }

    public int getOrderCount() {
        return displayedOrders.size();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAvailableOrderBinding binding = ItemAvailableOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(displayedOrders.get(position));
    }

    @Override
    public int getItemCount() {
        return displayedOrders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemAvailableOrderBinding binding;

        OrderViewHolder(ItemAvailableOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Order order) {
            binding.tvOrderId.setText("#" + order.getOrderId());
            binding.tvTimeSince.setText(getTimeSince(order.getCreatedAt()));
            
            String restaurantName = restaurantNameProvider != null 
                    ? restaurantNameProvider.getRestaurantName(order.getRestaurantId()) 
                    : order.getRestaurantId();
            binding.tvRestaurantName.setText(restaurantName != null ? restaurantName : "Unknown Restaurant");
            
            String fullAddress = order.getDeliveryAddress();
            String district = order.getDistrict();
            
            if (fullAddress == null || fullAddress.isEmpty()) {
                fullAddress = (district != null && !district.equals("Unknown")) ? district : "";
            } else {
                if (district != null && !district.isEmpty() && !district.equals("Unknown")) {
                    fullAddress = fullAddress + ", " + district;
                }
            }
            binding.tvDistrict.setText(fullAddress);
            
            int itemCount = order.getItems() != null ? order.getItems().size() : 0;
            binding.tvItemCount.setText(itemView.getContext().getResources()
                    .getQuantityString(R.plurals.item_count_format, itemCount, itemCount));
            
            binding.tvTotalPrice.setText(String.format(Locale.getDefault(), "৳%.2f", order.getTotalPrice()));
            
            binding.btnAcceptOrder.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAcceptOrder(order);
                }
            });
        }

        private String getTimeSince(long createdAt) {
            long now = System.currentTimeMillis();
            long diff = now - createdAt;
            
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            
            if (days > 0) {
                return days + (days == 1 ? " day ago" : " days ago");
            } else if (hours > 0) {
                return hours + (hours == 1 ? " hour ago" : " hours ago");
            } else if (minutes > 0) {
                return minutes + (minutes == 1 ? " min ago" : " mins ago");
            } else {
                return "Just now";
            }
        }
    }
}
