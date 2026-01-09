package com.example.foodbikeandroid.ui.dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.model.Restaurant;
import com.example.foodbikeandroid.data.model.User;
import com.example.foodbikeandroid.data.repository.OrderRepository;
import com.example.foodbikeandroid.data.repository.RestaurantRepository;
import com.example.foodbikeandroid.data.repository.UserRepository;
import com.example.foodbikeandroid.databinding.ActivityBikerHistoryBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BikerHistoryActivity extends AppCompatActivity {

    private static final double BASE_DELIVERY_FEE = 30.0;
    private static final double PERCENTAGE_BONUS = 0.05;

    private ActivityBikerHistoryBinding binding;
    private HistoryDeliveryAdapter adapter;
    private OrderRepository orderRepository;
    private RestaurantRepository restaurantRepository;
    private UserRepository userRepository;
    private AuthViewModel authViewModel;

    private Map<String, String> restaurantNames = new HashMap<>();
    private Map<String, String> customerNames = new HashMap<>();
    private LiveData<List<Order>> currentOrdersLiveData;
    private String bikerId;

    public enum DateFilter {
        TODAY, THIS_WEEK, THIS_MONTH, ALL_TIME
    }

    private DateFilter currentFilter = DateFilter.ALL_TIME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBikerHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderRepository = new OrderRepository(getApplication());
        restaurantRepository = RestaurantRepository.getInstance(this);
        userRepository = UserRepository.getInstance(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        bikerId = authViewModel.getCurrentUsername();
        if (bikerId == null) {
            Toast.makeText(this, R.string.error_not_logged_in, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupRecyclerView();
        setupFilterChips();
        setupExportButton();
        setupSwipeRefresh();
        loadRestaurantNames();
        loadCustomerNames();
        loadStats();
        loadHistory();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new HistoryDeliveryAdapter();
        adapter.setRestaurantNameProvider(restaurantId -> restaurantNames.get(restaurantId));
        adapter.setCustomerNameProvider(userId -> customerNames.get(userId));

        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(adapter);
    }

    private void setupFilterChips() {
        binding.chipToday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilter = DateFilter.TODAY;
                loadHistory();
            }
        });

        binding.chipThisWeek.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilter = DateFilter.THIS_WEEK;
                loadHistory();
            }
        });

        binding.chipThisMonth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilter = DateFilter.THIS_MONTH;
                loadHistory();
            }
        });

        binding.chipAllTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilter = DateFilter.ALL_TIME;
                loadHistory();
            }
        });
    }

    private void setupExportButton() {
        binding.btnExport.setOnClickListener(v -> exportHistory());
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadStats();
            loadHistory();
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

    private void loadCustomerNames() {
        userRepository.getAllUsers().observe(this, users -> {
            if (users != null) {
                customerNames.clear();
                for (User user : users) {
                    customerNames.put(user.getUsername(), user.getUsername());
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void loadStats() {
        orderRepository.getTotalDeliveryCount(bikerId).observe(this, count -> {
            if (count != null) {
                binding.tvTotalDeliveries.setText(String.valueOf(count));
            } else {
                binding.tvTotalDeliveries.setText("0");
            }
        });

        orderRepository.getDeliveryCountAfter(bikerId, orderRepository.getStartOfWeek()).observe(this, count -> {
            if (count != null) {
                binding.tvWeekDeliveries.setText(String.valueOf(count));
            } else {
                binding.tvWeekDeliveries.setText("0");
            }
        });

        orderRepository.getAverageDeliveryTime(bikerId).observe(this, avgTime -> {
            if (avgTime != null && avgTime > 0) {
                binding.tvAvgDeliveryTime.setText(formatDuration(avgTime));
            } else {
                binding.tvAvgDeliveryTime.setText("-");
            }
        });

        orderRepository.getTotalDeliveryValue(bikerId).observe(this, totalValue -> {
            if (totalValue != null) {
                orderRepository.getTotalDeliveryCount(bikerId).observe(this, count -> {
                    if (count != null && count > 0) {
                        double totalEarnings = (count * BASE_DELIVERY_FEE) + (totalValue * PERCENTAGE_BONUS);
                        binding.tvTotalEarnings.setText(String.format(Locale.getDefault(), "৳%.2f", totalEarnings));
                    } else {
                        binding.tvTotalEarnings.setText("৳0.00");
                    }
                });
            } else {
                binding.tvTotalEarnings.setText("৳0.00");
            }
        });
    }

    private void loadHistory() {
        if (currentOrdersLiveData != null) {
            currentOrdersLiveData.removeObservers(this);
        }

        binding.swipeRefresh.setRefreshing(true);

        long startTime = getStartTimeForFilter();

        if (startTime > 0) {
            currentOrdersLiveData = orderRepository.getCompletedOrdersByBikerAfter(bikerId, startTime);
        } else {
            currentOrdersLiveData = orderRepository.getCompletedOrdersByBiker(bikerId);
        }

        currentOrdersLiveData.observe(this, orders -> {
            binding.swipeRefresh.setRefreshing(false);

            if (orders != null && !orders.isEmpty()) {
                adapter.setOrders(orders);
                binding.rvHistory.setVisibility(View.VISIBLE);
                binding.emptyState.setVisibility(View.GONE);

                binding.tvHistoryCount.setText(getResources().getQuantityString(
                        R.plurals.delivery_count, orders.size(), orders.size()));

                double periodEarnings = calculatePeriodEarnings(orders);
                binding.tvPeriodEarnings.setText(String.format(Locale.getDefault(), "৳%.2f", periodEarnings));
                binding.tvPeriodEarnings.setVisibility(View.VISIBLE);
                binding.tvPeriodEarningsLabel.setVisibility(View.VISIBLE);
            } else {
                adapter.setOrders(null);
                binding.rvHistory.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.tvHistoryCount.setText(R.string.no_deliveries_found);
                binding.tvPeriodEarnings.setVisibility(View.GONE);
                binding.tvPeriodEarningsLabel.setVisibility(View.GONE);
            }
        });
    }

    private long getStartTimeForFilter() {
        switch (currentFilter) {
            case TODAY:
                return getStartOfDay();
            case THIS_WEEK:
                return orderRepository.getStartOfWeek();
            case THIS_MONTH:
                return orderRepository.getStartOfMonth();
            case ALL_TIME:
            default:
                return 0;
        }
    }

    private long getStartOfDay() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private double calculatePeriodEarnings(List<Order> orders) {
        double totalValue = 0;
        for (Order order : orders) {
            totalValue += order.getTotalPrice();
        }
        return (orders.size() * BASE_DELIVERY_FEE) + (totalValue * PERCENTAGE_BONUS);
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

    private void exportHistory() {
        orderRepository.exportDeliveryHistory(bikerId, new OrderRepository.ExportCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                runOnUiThread(() -> {
                    if (orders == null || orders.isEmpty()) {
                        Toast.makeText(BikerHistoryActivity.this, R.string.no_data_to_export, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    generateAndShareCsv(orders);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(BikerHistoryActivity.this,
                            getString(R.string.export_failed, error), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void generateAndShareCsv(List<Order> orders) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());
            String fileName = "delivery_history_" + dateFormat.format(new Date()) + ".csv";

            File exportDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, fileName);
            FileWriter writer = new FileWriter(file);

            writer.append("Order ID,Restaurant,Customer,District,Total,Delivery Time,Date,Earnings\n");

            SimpleDateFormat csvDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            for (Order order : orders) {
                String restaurantName = restaurantNames.get(order.getRestaurantId());
                String customerName = customerNames.get(order.getUserId());

                long deliveryTime = 0;
                if (order.getAcceptedAt() > 0 && order.getDeliveredAt() > 0) {
                    deliveryTime = (order.getDeliveredAt() - order.getAcceptedAt()) / 60000;
                }

                double earnings = BASE_DELIVERY_FEE + (order.getTotalPrice() * PERCENTAGE_BONUS);

                writer.append(String.format(Locale.US, "%s,%s,%s,%s,%.2f,%d min,%s,%.2f\n",
                        order.getOrderId(),
                        restaurantName != null ? restaurantName : "Unknown",
                        customerName != null ? customerName : "Unknown",
                        order.getDistrict(),
                        order.getTotalPrice(),
                        deliveryTime,
                        order.getDeliveredAt() > 0 ? csvDateFormat.format(new Date(order.getDeliveredAt())) : "-",
                        earnings
                ));
            }

            writer.flush();
            writer.close();

            Uri fileUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, getString(R.string.export_history)));

        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.export_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }
}
