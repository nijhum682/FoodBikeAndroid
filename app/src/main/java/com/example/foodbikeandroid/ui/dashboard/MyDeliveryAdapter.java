package com.example.foodbikeandroid.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.model.OrderStatus;
import com.example.foodbikeandroid.databinding.ItemMyDeliveryBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MyDeliveryAdapter extends RecyclerView.Adapter<MyDeliveryAdapter.DeliveryViewHolder> {

    private List<Order> orders = new ArrayList<>();
    private OnStatusUpdateListener statusUpdateListener;
    private RestaurantNameProvider restaurantNameProvider;

    public interface OnStatusUpdateListener {
        void onReadyForPickup(Order order);
        void onMarkDelivered(Order order);
    }

    public interface RestaurantNameProvider {
        String getRestaurantName(String restaurantId);
    }

    public void setOnStatusUpdateListener(OnStatusUpdateListener listener) {
        this.statusUpdateListener = listener;
    }

    public void setRestaurantNameProvider(RestaurantNameProvider provider) {
        this.restaurantNameProvider = provider;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders != null ? new ArrayList<>(orders) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyDeliveryBinding binding = ItemMyDeliveryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DeliveryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class DeliveryViewHolder extends RecyclerView.ViewHolder {
        private final ItemMyDeliveryBinding binding;

        DeliveryViewHolder(ItemMyDeliveryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Order order) {
            binding.tvOrderId.setText("#" + order.getOrderId());
            
            String restaurantName = restaurantNameProvider != null 
                    ? restaurantNameProvider.getRestaurantName(order.getRestaurantId()) 
                    : order.getRestaurantId();
            binding.tvRestaurantName.setText(restaurantName != null ? restaurantName : "Unknown Restaurant");
            
            binding.tvDistrict.setText(order.getDistrict());
            
            int itemCount = order.getItems() != null ? order.getItems().size() : 0;
            binding.tvItemCount.setText(itemView.getContext().getResources()
                    .getQuantityString(R.plurals.item_count_format, itemCount, itemCount));
            
            binding.tvTotalPrice.setText(String.format(Locale.getDefault(), "৳%.2f", order.getTotalPrice()));
            
            setupStatusChip(order);
            setupActionButton(order);
            setupDurationDisplay(order);
        }

        private void setupStatusChip(Order order) {
            OrderStatus status = order.getStatus();
            
            switch (status) {
                case PREPARING:
                    binding.chipStatus.setText(R.string.preparing);
                    binding.chipStatus.setChipBackgroundColorResource(R.color.warning);
                    break;
                case READY:
                    binding.chipStatus.setText(R.string.ready);
                    binding.chipStatus.setChipBackgroundColorResource(R.color.info);
                    break;
                case DELIVERED:
                    binding.chipStatus.setText(R.string.delivered);
                    binding.chipStatus.setChipBackgroundColorResource(R.color.success);
                    break;
                default:
                    binding.chipStatus.setText(status.name());
                    binding.chipStatus.setChipBackgroundColorResource(R.color.text_secondary);
            }
        }

        private void setupActionButton(Order order) {
            OrderStatus status = order.getStatus();
            
            if (status == OrderStatus.PREPARING) {
                binding.btnAction.setVisibility(View.VISIBLE);
                binding.btnAction.setText(R.string.ready_for_pickup);
                binding.btnAction.setBackgroundTintList(
                        ContextCompat.getColorStateList(itemView.getContext(), R.color.info));
                binding.btnAction.setOnClickListener(v -> {
                    if (statusUpdateListener != null) {
                        statusUpdateListener.onReadyForPickup(order);
                    }
                });
            } else if (status == OrderStatus.READY) {
                binding.btnAction.setVisibility(View.VISIBLE);
                binding.btnAction.setText(R.string.mark_delivered);
                binding.btnAction.setBackgroundTintList(
                        ContextCompat.getColorStateList(itemView.getContext(), R.color.success));
                binding.btnAction.setOnClickListener(v -> {
                    if (statusUpdateListener != null) {
                        statusUpdateListener.onMarkDelivered(order);
                    }
                });
            } else {
                binding.btnAction.setVisibility(View.GONE);
            }
        }

        private void setupDurationDisplay(Order order) {
            if (order.getStatus() == OrderStatus.DELIVERED && order.getAcceptedAt() > 0 && order.getDeliveredAt() > 0) {
                binding.layoutDuration.setVisibility(View.VISIBLE);
                long durationMillis = order.getDeliveredAt() - order.getAcceptedAt();
                binding.tvDuration.setText(formatDuration(durationMillis));
            } else if (order.getAcceptedAt() > 0) {
                binding.layoutDuration.setVisibility(View.VISIBLE);
                long elapsedMillis = System.currentTimeMillis() - order.getAcceptedAt();
                binding.tvDuration.setText(formatDuration(elapsedMillis));
            } else {
                binding.layoutDuration.setVisibility(View.GONE);
            }
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
    }
}
