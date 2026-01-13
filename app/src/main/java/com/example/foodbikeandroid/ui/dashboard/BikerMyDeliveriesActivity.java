package com.example.foodbikeandroid.ui.dashboard;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.model.Restaurant;
import com.example.foodbikeandroid.data.repository.OrderRepository;
import com.example.foodbikeandroid.data.repository.RestaurantRepository;
import com.example.foodbikeandroid.databinding.ActivityBikerMyDeliveriesBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BikerMyDeliveriesActivity extends AppCompatActivity {

    private ActivityBikerMyDeliveriesBinding binding;
    private MyDeliveryAdapter adapter;
    private OrderRepository orderRepository;
    private RestaurantRepository restaurantRepository;
    private AuthViewModel authViewModel;

    private Map<String, String> restaurantNames = new HashMap<>();
    private LiveData<List<Order>> currentOrdersLiveData;
    private boolean showingActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBikerMyDeliveriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderRepository = new OrderRepository(getApplication());
        restaurantRepository = RestaurantRepository.getInstance(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupFilterTabs();
        setupSwipeRefresh();
        loadRestaurantNames();
        loadOrders();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new MyDeliveryAdapter();
        adapter.setRestaurantNameProvider(restaurantId -> restaurantNames.get(restaurantId));
        adapter.setOnStatusUpdateListener(new MyDeliveryAdapter.OnStatusUpdateListener() {
            @Override
            public void onMarkDelivered(Order order) {
                markOrderDelivered(order);
            }
        });

        binding.rvDeliveries.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDeliveries.setAdapter(adapter);
    }

    private void setupFilterTabs() {
        binding.chipActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showingActive = true;
                loadOrders();
            }
        });

        binding.chipCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showingActive = false;
                loadOrders();
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(this::loadOrders);
    }

    private void loadRestaurantNames() {
        restaurantRepository.getAllRestaurants().observe(this, restaurants -> {
            if (restaurants != null) {
                restaurantNames.clear();
                for (Restaurant restaurant : restaurants) {
                    restaurantNames.put(restaurant.getId(), restaurant.getName());
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void loadOrders() {
        String bikerId = authViewModel.getCurrentUsername();
        if (bikerId == null) {
            Toast.makeText(this, R.string.error_not_logged_in, Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentOrdersLiveData != null) {
            currentOrdersLiveData.removeObservers(this);
        }

        binding.swipeRefresh.setRefreshing(true);

        if (showingActive) {
            currentOrdersLiveData = orderRepository.getActiveOrdersByBiker(bikerId);
        } else {
            currentOrdersLiveData = orderRepository.getCompletedOrdersByBiker(bikerId);
        }

        currentOrdersLiveData.observe(this, orders -> {
            binding.swipeRefresh.setRefreshing(false);

            if (orders != null && !orders.isEmpty()) {
                adapter.setOrders(orders);
                binding.rvDeliveries.setVisibility(View.VISIBLE);
                binding.emptyState.setVisibility(View.GONE);
                
                binding.tvOrderCount.setText(getResources().getQuantityString(
                        R.plurals.delivery_count, orders.size(), orders.size()));
            } else {
                adapter.setOrders(null);
                binding.rvDeliveries.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.tvOrderCount.setText(R.string.no_orders_found);
                
                if (showingActive) {
                    binding.tvEmptyMessage.setText(R.string.no_active_deliveries_desc);
                } else {
                    binding.tvEmptyMessage.setText(R.string.no_completed_deliveries_desc);
                }
            }
        });
    }

        // Removed confirmation dialog for instant delivery confirmation

    private void markOrderDelivered(Order order) {
        // Check if this is a Cash on Delivery order
        if (order.getPaymentMethod() == com.example.foodbikeandroid.data.model.PaymentMethod.CASH_ON_DELIVERY) {
            // Show confirmation dialog for COD orders
            showCodConfirmationDialog(order);
        } else {
            // For non-COD orders, proceed directly with delivery confirmation
            confirmDelivery(order);
        }
    }

    private void showCodConfirmationDialog(Order order) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.cod_confirmation_title)
                .setMessage(getString(R.string.cod_confirmation_message, 
                        String.format(Locale.getDefault(), "৳%.2f", order.getTotalPrice())))
                .setPositiveButton(R.string.cod_yes_received, (dialog, which) -> {
                    // User confirmed receiving payment
                    confirmDelivery(order);
                })
                .setNegativeButton(R.string.cod_no_not_received, (dialog, which) -> {
                    // User did not receive payment
                    Toast.makeText(this, R.string.cod_payment_not_received, Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }

    private void confirmDelivery(Order order) {
        orderRepository.updateOrderStatusToDelivered(order.getOrderId(), new OrderRepository.StatusUpdateCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(BikerMyDeliveriesActivity.this,
                            getString(R.string.order_delivered_success, order.getOrderId()),
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(BikerMyDeliveriesActivity.this,
                            getString(R.string.error_updating_status, error),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
