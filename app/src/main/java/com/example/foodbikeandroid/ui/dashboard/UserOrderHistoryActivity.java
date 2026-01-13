package com.example.foodbikeandroid.ui.dashboard;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.model.OrderStatus;
import com.example.foodbikeandroid.data.model.Restaurant;
import com.example.foodbikeandroid.data.repository.OrderRepository;
import com.example.foodbikeandroid.data.repository.RestaurantRepository;
import com.example.foodbikeandroid.data.repository.ReviewRepository;
import com.example.foodbikeandroid.databinding.ActivityUserOrderHistoryBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserOrderHistoryActivity extends AppCompatActivity {

    private ActivityUserOrderHistoryBinding binding;
    private UserOrderHistoryAdapter adapter;
    private OrderRepository orderRepository;
    private RestaurantRepository restaurantRepository;
    private ReviewRepository reviewRepository;
    private AuthViewModel authViewModel;

    private Map<String, String> restaurantNames = new HashMap<>();
    private List<Order> allOrders = new ArrayList<>();
    private LiveData<List<Order>> currentOrdersLiveData;
    private String userId;

    public enum OrderFilter {
        ALL, ACTIVE, COMPLETED, CANCELLED
    }

    private OrderFilter currentFilter = OrderFilter.ALL;
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderRepository = new OrderRepository(getApplication());
        restaurantRepository = RestaurantRepository.getInstance(this);
        reviewRepository = ReviewRepository.getInstance(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        userId = authViewModel.getCurrentUsername();
        if (userId == null) {
            Toast.makeText(this, R.string.error_not_logged_in, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupRecyclerView();
        setupFilterChips();
        setupSearch();
        setupSwipeRefresh();
        
        // Check for specific order to track
        String orderIdFilter = getIntent().getStringExtra("ORDER_ID_FILTER");
        if (orderIdFilter != null && !orderIdFilter.isEmpty()) {
            searchQuery = orderIdFilter.toLowerCase(Locale.getDefault());
            binding.etSearch.setText(orderIdFilter);
        }
        
        loadRestaurantNames();
        loadOrders();
        showLoading();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new UserOrderHistoryAdapter();
        adapter.setRestaurantNameProvider(restaurantId -> restaurantNames.get(restaurantId));
        adapter.setOrderClickListener(order -> {
            // Navigate to OrderDetailActivity
            android.content.Intent intent = new android.content.Intent(this, 
                com.example.foodbikeandroid.ui.order.OrderDetailActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            startActivity(intent);
        });
        adapter.setReviewClickListener(order -> {
            // Navigate to ReviewSubmissionActivity
            android.content.Intent intent = new android.content.Intent(this,
                com.example.foodbikeandroid.ui.review.ReviewSubmissionActivity.class);
            intent.putExtra("ORDER_ID", order.getOrderId());
            intent.putExtra("RESTAURANT_ID", order.getRestaurantId());
            String restaurantName = restaurantNames.get(order.getRestaurantId());
            intent.putExtra("RESTAURANT_NAME", restaurantName);
            startActivityForResult(intent, 100);
        });
        adapter.setViewReviewClickListener(order -> {
            // Navigate to ReviewSubmissionActivity in view-only mode
            android.content.Intent intent = new android.content.Intent(this,
                com.example.foodbikeandroid.ui.review.ReviewSubmissionActivity.class);
            intent.putExtra("ORDER_ID", order.getOrderId());
            intent.putExtra("RESTAURANT_ID", order.getRestaurantId());
            String restaurantName = restaurantNames.get(order.getRestaurantId());
            intent.putExtra("RESTAURANT_NAME", restaurantName);
            intent.putExtra("VIEW_ONLY", true);
            startActivity(intent);
        });

        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrders.setAdapter(adapter);
        
        // Load reviewed order IDs
        loadReviewedOrders();
    }

    private void setupFilterChips() {
        binding.chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilter = OrderFilter.ALL;
                applyFilters();
            }
        });

        binding.chipActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilter = OrderFilter.ACTIVE;
                applyFilters();
            }
        });

        binding.chipCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilter = OrderFilter.COMPLETED;
                applyFilters();
            }
        });

        binding.chipCancelled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilter = OrderFilter.CANCELLED;
                applyFilters();
            }
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase(Locale.getDefault()).trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadRestaurantNames();
            loadOrders();
        });
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
        // Remove previous observer if exists
        if (currentOrdersLiveData != null) {
            currentOrdersLiveData.removeObservers(this);
        }

        currentOrdersLiveData = orderRepository.getOrdersByUserId(userId);
        currentOrdersLiveData.observe(this, orders -> {
            binding.swipeRefresh.setRefreshing(false);
            hideLoading();

            if (orders != null) {
                // Sort by date (newest first)
                allOrders = new ArrayList<>(orders);
                Collections.sort(allOrders, (o1, o2) -> 
                        Long.compare(o2.getCreatedAt(), o1.getCreatedAt()));
                applyFilters();
            } else {
                showEmptyState(getString(R.string.no_orders_title), getString(R.string.no_orders_message));
            }
        });
    }

    private void applyFilters() {
        List<Order> filteredOrders = new ArrayList<>();

        for (Order order : allOrders) {
            // Apply status filter
            boolean matchesFilter = false;
            switch (currentFilter) {
                case ALL:
                    matchesFilter = true;
                    break;
                case ACTIVE:
                    matchesFilter = isActiveStatus(order.getStatus());
                    break;
                case COMPLETED:
                    matchesFilter = order.getStatus() == OrderStatus.DELIVERED;
                    break;
                case CANCELLED:
                    matchesFilter = order.getStatus() == OrderStatus.CANCELLED 
                            || order.getStatus() == OrderStatus.AUTO_CANCELLED;
                    break;
            }

            if (!matchesFilter) continue;

            // Apply search filter
            if (!searchQuery.isEmpty()) {
                String restaurantName = restaurantNames.get(order.getRestaurantId());
                boolean matchesSearch = false;

                // Search by order ID
                if (order.getOrderId().toLowerCase(Locale.getDefault()).contains(searchQuery)) {
                    matchesSearch = true;
                }

                // Search by restaurant name
                if (restaurantName != null && 
                        restaurantName.toLowerCase(Locale.getDefault()).contains(searchQuery)) {
                    matchesSearch = true;
                }

                if (!matchesSearch) continue;
            }

            filteredOrders.add(order);
        }

        // Update UI
        adapter.submitList(filteredOrders);
        updateOrderCount(filteredOrders.size());

        if (filteredOrders.isEmpty()) {
            String emptyTitle;
            String emptyMessage;

            if (!searchQuery.isEmpty()) {
                emptyTitle = getString(R.string.no_search_results);
                emptyMessage = getString(R.string.no_search_results_message);
            } else {
                switch (currentFilter) {
                    case ACTIVE:
                        emptyTitle = getString(R.string.no_active_orders);
                        emptyMessage = getString(R.string.no_active_orders_message);
                        break;
                    case COMPLETED:
                        emptyTitle = getString(R.string.no_completed_orders);
                        emptyMessage = getString(R.string.no_completed_orders_message);
                        break;
                    case CANCELLED:
                        emptyTitle = getString(R.string.no_cancelled_orders);
                        emptyMessage = getString(R.string.no_cancelled_orders_message);
                        break;
                    default:
                        emptyTitle = getString(R.string.no_orders_title);
                        emptyMessage = getString(R.string.no_orders_message);
                        break;
                }
            }
            showEmptyState(emptyTitle, emptyMessage);
        } else {
            hideEmptyState();
        }
    }

    private boolean isActiveStatus(OrderStatus status) {
        return status == OrderStatus.PENDING
                || status == OrderStatus.CONFIRMED
                || status == OrderStatus.PREPARING
                || status == OrderStatus.READY;
    }

    private void updateOrderCount(int count) {
        binding.tvOrderCount.setText(getResources().getQuantityString(
                R.plurals.order_count_format, count, count));
    }

    private void loadReviewedOrders() {
        reviewRepository.getByUser(userId).observe(this, reviews -> {
            if (reviews != null) {
                java.util.Set<String> reviewedOrderIds = new java.util.HashSet<>();
                for (com.example.foodbikeandroid.data.model.Review review : reviews) {
                    reviewedOrderIds.add(review.getOrderId());
                }
                adapter.setReviewedOrderIds(reviewedOrderIds);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Review submitted, reload reviewed orders
            loadReviewedOrders();
        }
    }

    private void showLoading() {
        binding.layoutLoading.setVisibility(View.VISIBLE);
        binding.rvOrders.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
    }

    private void hideLoading() {
        binding.layoutLoading.setVisibility(View.GONE);
    }

    private void showEmptyState(String title, String message) {
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
        binding.rvOrders.setVisibility(View.GONE);
        binding.tvEmptyTitle.setText(title);
        binding.tvEmptyMessage.setText(message);
    }

    private void hideEmptyState() {
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.rvOrders.setVisibility(View.VISIBLE);
    }
}
