package com.example.foodbikeandroid.ui.dashboard;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.CartItem;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.model.OrderStatus;
import com.example.foodbikeandroid.data.model.PaymentMethod;
import com.example.foodbikeandroid.databinding.ItemRestaurantOrderBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RestaurantOrderAdapter extends RecyclerView.Adapter<RestaurantOrderAdapter.OrderViewHolder> {

    private List<Order> orders = new ArrayList<>();
    private List<Order> filteredOrders = new ArrayList<>();
    private OnOrderActionListener listener;
    private OrderStatus currentFilter = null;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

    public interface OnOrderActionListener {
        void onAcceptOrder(Order order);
        void onRejectOrder(Order order);
        void onMarkReady(Order order);
        String getCustomerName(String userId);
    }

    public void setOnOrderActionListener(OnOrderActionListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
        applyFilter();
    }

    public void setFilter(OrderStatus status) {
        this.currentFilter = status;
        applyFilter();
    }

    private void applyFilter() {
        filteredOrders.clear();
        if (currentFilter == null) {
            filteredOrders.addAll(orders);
        } else {
            for (Order order : orders) {
                if (order.getStatus() == currentFilter) {
                    filteredOrders.add(order);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRestaurantOrderBinding binding = ItemRestaurantOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(filteredOrders.get(position));
    }

    @Override
    public int getItemCount() {
        return filteredOrders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemRestaurantOrderBinding binding;

        OrderViewHolder(ItemRestaurantOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Order order) {
            binding.tvOrderId.setText("#" + order.getOrderId());
            binding.tvTimestamp.setText(dateFormat.format(new Date(order.getCreatedAt())));
            
            String customerName = listener != null ? listener.getCustomerName(order.getUserId()) : order.getUserId();
            binding.tvCustomerName.setText(customerName);
            
            String fullAddress = order.getDeliveryAddress();
            String district = order.getDistrict();
            
            if (fullAddress == null || fullAddress.isEmpty()) {
                fullAddress = (district != null && !district.equals("Unknown")) ? district : "";
            } else {
                if (district != null && !district.isEmpty() && !district.equals("Unknown")) {
                    fullAddress = fullAddress + ", " + district;
                }
            }
            binding.tvDeliveryLocation.setText(fullAddress);
            
            StringBuilder itemsBuilder = new StringBuilder();
            List<CartItem> items = order.getItems();
            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    CartItem item = items.get(i);
                    itemsBuilder.append("• ")
                            .append(item.getQuantity())
                            .append("x ")
                            .append(item.getMenuItem().getName());
                    if (i < items.size() - 1) {
                        itemsBuilder.append("\n");
                    }
                }
            }
            binding.tvOrderItems.setText(itemsBuilder.toString());
            
            binding.tvTotal.setText("৳" + (int) order.getTotalPrice());
            
            String paymentText = order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY ?
                    binding.getRoot().getContext().getString(R.string.cash_on_delivery) :
                    binding.getRoot().getContext().getString(R.string.mobile_banking);
            binding.chipPayment.setText(paymentText);
            
            setupStatusChip(order.getStatus());
            setupActionButtons(order);
        }

        private void setupStatusChip(OrderStatus status) {
            int colorRes;
            int textRes;
            
            switch (status) {
                case PENDING:
                    colorRes = R.color.warning;
                    textRes = R.string.pending;
                    break;
                case CONFIRMED:
                    colorRes = R.color.info;
                    textRes = R.string.confirmed;
                    break;
                case PREPARING:
                    colorRes = R.color.secondary;
                    textRes = R.string.preparing;
                    break;
                case READY:
                    colorRes = R.color.success;
                    textRes = R.string.ready;
                    break;
                case DELIVERED:
                    colorRes = R.color.success;
                    textRes = R.string.delivered;
                    break;
                case CANCELLED:
                case AUTO_CANCELLED:
                    colorRes = R.color.error;
                    textRes = R.string.cancelled;
                    break;
                default:
                    colorRes = R.color.text_secondary;
                    textRes = R.string.pending;
            }
            
            binding.chipStatus.setText(textRes);
            binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(
                    ContextCompat.getColor(binding.getRoot().getContext(), colorRes)));
            binding.chipStatus.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.white));
        }

        private void setupActionButtons(Order order) {
            binding.btnAccept.setVisibility(View.GONE);
            binding.btnReject.setVisibility(View.GONE);
            binding.btnMarkReady.setVisibility(View.GONE);

            switch (order.getStatus()) {
                case PENDING:
                    binding.btnAccept.setVisibility(View.VISIBLE);
                    binding.btnReject.setVisibility(View.VISIBLE);
                    binding.btnAccept.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onAcceptOrder(order);
                        }
                    });
                    binding.btnReject.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onRejectOrder(order);
                        }
                    });
                    break;
                case CONFIRMED:
                case PREPARING:
                    binding.btnMarkReady.setVisibility(View.VISIBLE);
                    binding.btnMarkReady.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onMarkReady(order);
                        }
                    });
                    break;
            }
        }
    }
}
