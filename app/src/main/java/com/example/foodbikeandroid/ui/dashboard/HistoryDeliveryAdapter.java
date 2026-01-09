package com.example.foodbikeandroid.ui.dashboard;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.databinding.ItemHistoryDeliveryBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HistoryDeliveryAdapter extends RecyclerView.Adapter<HistoryDeliveryAdapter.HistoryViewHolder> {

    private List<Order> orders = new ArrayList<>();
    private RestaurantNameProvider restaurantNameProvider;
    private CustomerNameProvider customerNameProvider;

    public interface RestaurantNameProvider {
        String getRestaurantName(String restaurantId);
    }

    public interface CustomerNameProvider {
        String getCustomerName(String userId);
    }

    public void setRestaurantNameProvider(RestaurantNameProvider provider) {
        this.restaurantNameProvider = provider;
    }

    public void setCustomerNameProvider(CustomerNameProvider provider) {
        this.customerNameProvider = provider;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders != null ? new ArrayList<>(orders) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryDeliveryBinding binding = ItemHistoryDeliveryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemHistoryDeliveryBinding binding;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        HistoryViewHolder(ItemHistoryDeliveryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Order order) {
            binding.tvOrderId.setText("#" + order.getOrderId());

            String restaurantName = restaurantNameProvider != null
                    ? restaurantNameProvider.getRestaurantName(order.getRestaurantId())
                    : order.getRestaurantId();
            binding.tvRestaurantName.setText(restaurantName != null ? restaurantName : "Unknown Restaurant");

            String customerName = customerNameProvider != null
                    ? customerNameProvider.getCustomerName(order.getUserId())
                    : order.getUserId();
            binding.tvCustomerName.setText(customerName != null ? customerName : "Unknown Customer");

            binding.tvDistrict.setText(order.getDistrict());

            binding.tvTotalPrice.setText(String.format(Locale.getDefault(), "৳%.2f", order.getTotalPrice()));

            if (order.getDeliveredAt() > 0) {
                Date deliveredDate = new Date(order.getDeliveredAt());
                binding.tvDate.setText(dateFormat.format(deliveredDate));
                binding.tvTime.setText(timeFormat.format(deliveredDate));
            } else {
                binding.tvDate.setText("-");
                binding.tvTime.setText("-");
            }

            if (order.getAcceptedAt() > 0 && order.getDeliveredAt() > 0) {
                long durationMillis = order.getDeliveredAt() - order.getAcceptedAt();
                binding.tvDeliveryTime.setText(formatDuration(durationMillis));
            } else {
                binding.tvDeliveryTime.setText("-");
            }

            double earnings = calculateEarnings(order.getTotalPrice());
            binding.tvEarnings.setText(String.format(Locale.getDefault(), "৳%.2f", earnings));
        }

        private String formatDuration(long durationMillis) {
            long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60;

            if (hours > 0) {
                return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
            } else {
                return String.format(Locale.getDefault(), "%dm", minutes);
            }
        }

        private double calculateEarnings(double orderTotal) {
            double baseDeliveryFee = 30.0;
            double percentageBonus = orderTotal * 0.05;
            return baseDeliveryFee + percentageBonus;
        }
    }
}
