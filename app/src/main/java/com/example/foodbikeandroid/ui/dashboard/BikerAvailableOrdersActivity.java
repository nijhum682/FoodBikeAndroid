package com.example.foodbikeandroid.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.LocationData;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.model.OrderStatus;
import com.example.foodbikeandroid.data.model.Restaurant;
import com.example.foodbikeandroid.data.repository.OrderRepository;
import com.example.foodbikeandroid.data.repository.RestaurantRepository;
import com.example.foodbikeandroid.databinding.ActivityBikerAvailableOrdersBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;
import com.google.android.material.chip.Chip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.ViewModelProvider;

public class BikerAvailableOrdersActivity extends AppCompatActivity {

    private ActivityBikerAvailableOrdersBinding binding;
    private AvailableOrderAdapter adapter;
    private OrderRepository orderRepository;
    private RestaurantRepository restaurantRepository;
    private AuthViewModel authViewModel;

    private String selectedDivision = "All Divisions";
    private String selectedDistrict = "All Districts";
    private Map<String, String> restaurantNames = new HashMap<>();
    private LiveData<List<Order>> currentOrdersLiveData;

    private final ActivityResultLauncher<Intent> orderDetailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadOrders();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBikerAvailableOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderRepository = new OrderRepository(getApplication());
        restaurantRepository = RestaurantRepository.getInstance(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupDivisionDropdown();
        setupDistrictChips();
        setupSortChips();
        setupSwipeRefresh();
        setupEmptyStateButton();
        loadRestaurantNames();
        loadOrders();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AvailableOrderAdapter();
        adapter.setRestaurantNameProvider(restaurantId -> restaurantNames.get(restaurantId));
        adapter.setOnOrderClickListener(this::acceptOrder);
        
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrders.setAdapter(adapter);
    }

    private void openOrderDetail(Order order) {
        Intent intent = new Intent(this, BikerOrderDetailActivity.class);
        intent.putExtra(BikerOrderDetailActivity.EXTRA_ORDER_ID, order.getOrderId());
        orderDetailLauncher.launch(intent);
    }

    private void setupDivisionDropdown() {
        List<String> divisions = LocationData.getAllDivisions();
        ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, divisions);
        binding.actvDivision.setAdapter(divisionAdapter);
        
        binding.actvDivision.setOnItemClickListener((parent, view, position, id) -> {
            selectedDivision = divisions.get(position);
            selectedDistrict = "All Districts";
            setupDistrictChips();
            loadOrders();
        });
    }

    private void setupDistrictChips() {
        binding.chipGroupDistricts.removeAllViews();
        
        List<String> districts = LocationData.getDistrictsForDivision(selectedDivision);
        
        for (String district : districts) {
            Chip chip = new Chip(this);
            chip.setText(district);
            chip.setCheckable(true);
            chip.setChecked(district.equals(selectedDistrict));
            chip.setChipBackgroundColorResource(R.color.light_gray);
            chip.setCheckedIconVisible(false);
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedDistrict = district;
                    loadOrders();
                }
            });
            
            binding.chipGroupDistricts.addView(chip);
        }
    }

    private void setupSortChips() {
        binding.chipSortNewest.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                adapter.setSortOrder(AvailableOrderAdapter.SortOrder.NEWEST_FIRST);
            }
        });

        binding.chipSortHighestValue.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                adapter.setSortOrder(AvailableOrderAdapter.SortOrder.HIGHEST_VALUE);
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(this::refreshOrders);
    }

    private void setupEmptyStateButton() {
        binding.btnRefresh.setOnClickListener(v -> refreshOrders());
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
        if (currentOrdersLiveData != null) {
            currentOrdersLiveData.removeObservers(this);
        }

        binding.swipeRefresh.setRefreshing(true);

        if (selectedDistrict.equals("All Districts")) {
            currentOrdersLiveData = orderRepository.getOrdersByStatus(OrderStatus.READY);
        } else {
            currentOrdersLiveData = orderRepository.getOrdersByDistrictAndStatus(selectedDistrict, OrderStatus.READY);
        }

        currentOrdersLiveData.observe(this, orders -> {
            binding.swipeRefresh.setRefreshing(false);
            
            if (orders != null && !orders.isEmpty()) {
                adapter.setOrders(orders);
                binding.rvOrders.setVisibility(View.VISIBLE);
                binding.emptyState.setVisibility(View.GONE);
                
                int count = adapter.getOrderCount();
                binding.tvOrderCount.setText(getResources().getQuantityString(
                        R.plurals.orders_available_count, count, count));
            } else {
                adapter.setOrders(null);
                binding.rvOrders.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.tvOrderCount.setText(getString(R.string.no_orders_found));
                
                if (!selectedDistrict.equals("All Districts")) {
                    binding.tvEmptyMessage.setText(getString(R.string.no_orders_in_district_specific, selectedDistrict));
                } else {
                    binding.tvEmptyMessage.setText(getString(R.string.no_orders_in_district));
                }
            }
        });
    }

    private void refreshOrders() {
        binding.swipeRefresh.setRefreshing(true);
        loadOrders();
    }

    private void acceptOrder(Order order) {
        String bikerId = authViewModel.getCurrentUsername();
        if (bikerId == null) {
            Toast.makeText(this, R.string.error_not_logged_in, Toast.LENGTH_SHORT).show();
            return;
        }

        orderRepository.tryAcceptOrder(order.getOrderId(), bikerId, new OrderRepository.AcceptOrderCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(BikerAvailableOrdersActivity.this, 
                            getString(R.string.delivery_accepted, order.getOrderId()), 
                            Toast.LENGTH_SHORT).show();
                    loadOrders();
                });
            }

            @Override
            public void onAlreadyTaken() {
                runOnUiThread(() -> {
                    Toast.makeText(BikerAvailableOrdersActivity.this, 
                            R.string.order_already_taken, 
                            Toast.LENGTH_LONG).show();
                    loadOrders();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(BikerAvailableOrdersActivity.this, 
                            getString(R.string.error_accepting_order, error), 
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
