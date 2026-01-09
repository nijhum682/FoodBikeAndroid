package com.example.foodbikeandroid.ui.dashboard;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.model.OrderStatus;
import com.example.foodbikeandroid.data.model.PaymentMethod;
import com.example.foodbikeandroid.data.model.Restaurant;
import com.example.foodbikeandroid.data.model.User;
import com.example.foodbikeandroid.data.repository.OrderRepository;
import com.example.foodbikeandroid.data.repository.RestaurantRepository;
import com.example.foodbikeandroid.data.repository.UserRepository;
import com.example.foodbikeandroid.databinding.ActivityBikerOrderDetailBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;

import java.util.Locale;

public class BikerOrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "extra_order_id";

    private ActivityBikerOrderDetailBinding binding;
    private OrderRepository orderRepository;
    private RestaurantRepository restaurantRepository;
    private UserRepository userRepository;
    private AuthViewModel authViewModel;
    private OrderDetailItemAdapter itemAdapter;

    private Order currentOrder;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBikerOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        if (orderId == null) {
            Toast.makeText(this, R.string.error_order_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        orderRepository = new OrderRepository(getApplication());
        restaurantRepository = RestaurantRepository.getInstance(this);
        userRepository = UserRepository.getInstance(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupAcceptButton();
        loadOrderDetails();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        itemAdapter = new OrderDetailItemAdapter();
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrderItems.setAdapter(itemAdapter);
    }

    private void setupAcceptButton() {
        binding.btnAcceptDelivery.setOnClickListener(v -> acceptDelivery());
    }

    private void loadOrderDetails() {
        orderRepository.getOrderById(orderId).observe(this, order -> {
            if (order != null) {
                currentOrder = order;
                displayOrderDetails(order);
                loadRestaurantDetails(order.getRestaurantId());
                loadCustomerDetails(order.getUserId());
            } else {
                Toast.makeText(this, R.string.error_order_not_found, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayOrderDetails(Order order) {
        binding.tvOrderId.setText("#" + order.getOrderId());
        binding.tvTimeSince.setText(getTimeSince(order.getCreatedAt()));
        
        binding.chipStatus.setText(getStatusText(order.getStatus()));
        binding.chipStatus.setChipBackgroundColorResource(getStatusColor(order.getStatus()));
        
        if (order.getItems() != null) {
            itemAdapter.setItems(order.getItems());
        }
        
        binding.tvPaymentMethod.setText(getPaymentMethodText(order.getPaymentMethod()));
        binding.tvTotalPrice.setText(String.format(Locale.getDefault(), "৳%.2f", order.getTotalPrice()));
        
        binding.tvDeliveryDistrict.setText(order.getDistrict());
        
        boolean canAccept = order.getStatus() == OrderStatus.CONFIRMED && order.getBikerId() == null;
        binding.btnAcceptDelivery.setVisibility(canAccept ? View.VISIBLE : View.GONE);
    }

    private void loadRestaurantDetails(String restaurantId) {
        restaurantRepository.getRestaurantById(restaurantId).observe(this, restaurant -> {
            if (restaurant != null) {
                binding.tvRestaurantName.setText(restaurant.getName());
                binding.tvRestaurantAddress.setText(restaurant.getAddress() + ", " + restaurant.getFullLocation());
            } else {
                binding.tvRestaurantName.setText(R.string.unknown_restaurant);
                binding.tvRestaurantAddress.setText("");
            }
        });
    }

    private void loadCustomerDetails(String userId) {
        userRepository.getUserByUsername(userId).observe(this, user -> {
            if (user != null) {
                binding.tvCustomerName.setText(user.getUsername());
            } else {
                binding.tvCustomerName.setText(userId);
            }
        });
    }

    private void acceptDelivery() {
        String bikerId = authViewModel.getCurrentUsername();
        if (bikerId == null) {
            Toast.makeText(this, R.string.error_not_logged_in, Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentOrder == null) {
            Toast.makeText(this, R.string.error_order_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        orderRepository.tryAcceptOrder(orderId, bikerId, new OrderRepository.AcceptOrderCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(BikerOrderDetailActivity.this, 
                            getString(R.string.delivery_accepted, orderId), 
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onAlreadyTaken() {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(BikerOrderDetailActivity.this, 
                            R.string.order_already_taken, 
                            Toast.LENGTH_LONG).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(BikerOrderDetailActivity.this, 
                            getString(R.string.error_accepting_order, error), 
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showLoading(boolean show) {
        binding.loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnAcceptDelivery.setEnabled(!show);
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

    private String getStatusText(OrderStatus status) {
        switch (status) {
            case PENDING: return getString(R.string.pending);
            case CONFIRMED: return getString(R.string.confirmed);
            case PREPARING: return getString(R.string.preparing);
            case READY: return getString(R.string.ready);
            case DELIVERED: return getString(R.string.delivered);
            case CANCELLED: return getString(R.string.cancelled);
            default: return status.name();
        }
    }

    private int getStatusColor(OrderStatus status) {
        switch (status) {
            case CONFIRMED: return R.color.success;
            case PREPARING: return R.color.warning;
            case READY: return R.color.primary;
            case DELIVERED: return R.color.success;
            case CANCELLED:
            case AUTO_CANCELLED: return R.color.error;
            default: return R.color.text_secondary;
        }
    }

    private String getPaymentMethodText(PaymentMethod method) {
        if (method == PaymentMethod.CASH_ON_DELIVERY) {
            return getString(R.string.cash_on_delivery);
        } else {
            return getString(R.string.mobile_banking);
        }
    }
}
