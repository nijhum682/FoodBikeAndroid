package com.example.foodbikeandroid.ui.dashboard;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.model.OrderStatus;
import com.example.foodbikeandroid.data.repository.OrderRepository;
import com.example.foodbikeandroid.data.repository.UserRepository;
import com.example.foodbikeandroid.databinding.ActivityRestaurantOrdersBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestaurantOrdersActivity extends AppCompatActivity {

    public static final String EXTRA_RESTAURANT_ID = "restaurant_id";
    public static final String EXTRA_RESTAURANT_NAME = "restaurant_name";
    public static final String EXTRA_RESTAURANT_LOCATION = "restaurant_location";

    private ActivityRestaurantOrdersBinding binding;
    private OrderRepository orderRepository;
    private UserRepository userRepository;
    private RestaurantOrderAdapter adapter;
    private String restaurantId;
    private Map<String, String> userNameCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRestaurantOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        restaurantId = getIntent().getStringExtra(EXTRA_RESTAURANT_ID);
        String restaurantName = getIntent().getStringExtra(EXTRA_RESTAURANT_NAME);
        String restaurantLocation = getIntent().getStringExtra(EXTRA_RESTAURANT_LOCATION);

        if (restaurantId == null) {
            finish();
            return;
        }

        orderRepository = new OrderRepository(getApplication());
        userRepository = UserRepository.getInstance(this);

        setupToolbar();
        setupRestaurantInfo(restaurantName, restaurantLocation);
        setupRecyclerView();
        setupFilterChips();
        setupSwipeRefresh();
        loadOrders();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRestaurantInfo(String name, String location) {
        binding.tvRestaurantName.setText(name != null ? name : "Restaurant");
        binding.tvRestaurantLocation.setText(location != null ? location : "");
    }

    private void setupRecyclerView() {
        adapter = new RestaurantOrderAdapter();
        adapter.setOnOrderActionListener(new RestaurantOrderAdapter.OnOrderActionListener() {
            @Override
            public void onAcceptOrder(Order order) {
                confirmAcceptOrder(order);
            }

            @Override
            public void onRejectOrder(Order order) {
                confirmRejectOrder(order);
            }

            @Override
            public void onMarkReady(Order order) {
                markOrderReady(order);
            }

            @Override
            public String getCustomerName(String userId) {
                return userNameCache.getOrDefault(userId, userId);
            }
        });
        
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrders.setAdapter(adapter);
    }

    private void setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                adapter.setFilter(null);
            } else if (checkedId == R.id.chipPending) {
                adapter.setFilter(OrderStatus.PENDING);
            } else if (checkedId == R.id.chipConfirmed) {
                adapter.setFilter(OrderStatus.CONFIRMED);
            } else if (checkedId == R.id.chipPreparing) {
                adapter.setFilter(OrderStatus.PREPARING);
            } else if (checkedId == R.id.chipReady) {
                adapter.setFilter(OrderStatus.READY);
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(this::loadOrders);
    }

    private void loadOrders() {
        orderRepository.getOrdersByRestaurantId(restaurantId).observe(this, orders -> {
            binding.swipeRefresh.setRefreshing(false);
            
            if (orders == null || orders.isEmpty()) {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.rvOrders.setVisibility(View.GONE);
                updateStats(0, 0, 0.0);
            } else {
                binding.emptyState.setVisibility(View.GONE);
                binding.rvOrders.setVisibility(View.VISIBLE);
                adapter.setOrders(orders);
                
                loadUserNames(orders);
                calculateStats(orders);
            }
        });
    }

    private void loadUserNames(List<Order> orders) {
        for (Order order : orders) {
            String userId = order.getUserId();
            if (!userNameCache.containsKey(userId)) {
                userRepository.getUserByUsername(userId, new UserRepository.AuthCallback() {
                    @Override
                    public void onSuccess(com.example.foodbikeandroid.data.model.User user) {
                        runOnUiThread(() -> {
                            userNameCache.put(userId, user.getUsername());
                            adapter.notifyDataSetChanged();
                        });
                    }

                    @Override
                    public void onError(String message) {
                        userNameCache.put(userId, userId);
                    }
                });
            }
        }
    }

    private void calculateStats(List<Order> orders) {
        int pendingCount = 0;
        int todayCount = 0;
        double todayRevenue = 0.0;
        
        long startOfDay = getStartOfDay();
        
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.PENDING) {
                pendingCount++;
            }
            if (order.getCreatedAt() >= startOfDay) {
                todayCount++;
                if (order.getStatus() != OrderStatus.CANCELLED && 
                    order.getStatus() != OrderStatus.AUTO_CANCELLED) {
                    todayRevenue += order.getTotalPrice();
                }
            }
        }
        
        updateStats(pendingCount, todayCount, todayRevenue);
    }

    private void updateStats(int pendingCount, int todayCount, double todayRevenue) {
        binding.tvPendingOrders.setText(String.valueOf(pendingCount));
        binding.tvTodayOrders.setText(String.valueOf(todayCount));
        binding.tvTodayRevenue.setText("৳" + (int) todayRevenue);
    }

    private long getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private void confirmAcceptOrder(Order order) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.accept_order)
                .setMessage(getString(R.string.confirm_accept_order, order.getOrderId()))
                .setPositiveButton(R.string.accept_order, (dialog, which) -> {
                    orderRepository.updateOrderStatus(order.getOrderId(), OrderStatus.CONFIRMED);
                    Toast.makeText(this, R.string.order_accepted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmRejectOrder(Order order) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.reject)
                .setMessage(getString(R.string.confirm_reject_order, order.getOrderId()))
                .setPositiveButton(R.string.reject, (dialog, which) -> {
                    orderRepository.updateOrderStatus(order.getOrderId(), OrderStatus.CANCELLED);
                    Toast.makeText(this, R.string.order_rejected, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void markOrderReady(Order order) {
        orderRepository.updateOrderStatus(order.getOrderId(), OrderStatus.READY);
        Toast.makeText(this, R.string.order_marked_ready, Toast.LENGTH_SHORT).show();
    }
}
