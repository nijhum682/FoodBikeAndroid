package com.example.foodbikeandroid.ui.dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.model.Restaurant;
import com.example.foodbikeandroid.data.model.User;
import com.example.foodbikeandroid.data.model.Withdrawal;
import com.example.foodbikeandroid.data.repository.OrderRepository;
import com.example.foodbikeandroid.data.repository.RestaurantRepository;
import com.example.foodbikeandroid.data.repository.UserRepository;
import com.example.foodbikeandroid.data.repository.WithdrawalRepository;
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
    private WithdrawalRepository withdrawalRepository;
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
        withdrawalRepository = new WithdrawalRepository(getApplication());
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
        setupWithdrawButton();
        setupWithdrawalHistoryButton();
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

    private void setupWithdrawButton() {
        binding.btnExport.setText(R.string.withdraw_money);
        binding.btnExport.setOnClickListener(v -> showWithdrawalDialog());
    }

    private void setupWithdrawalHistoryButton() {
        binding.btnWithdrawalHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, WithdrawalHistoryActivity.class);
            intent.putExtra("username", bikerId);
            intent.putExtra("userType", "BIKER");
            startActivity(intent);
        });
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

        // Calculate Remaining Balance as total earnings from all orders minus total withdrawals
        orderRepository.getCompletedOrdersByBiker(bikerId).observe(this, allOrders -> {
            if (allOrders != null) {
                // Calculate total earnings from all completed orders
                double totalEarnings = 0;
                for (Order order : allOrders) {
                    double orderEarnings = BASE_DELIVERY_FEE + (order.getTotalPrice() * PERCENTAGE_BONUS);
                    totalEarnings += orderEarnings;
                }
                
                // Get total withdrawn amount
                final double finalTotalEarnings = totalEarnings;
                withdrawalRepository.getTotalWithdrawnByUser(bikerId, "BIKER").observe(this, totalWithdrawn -> {
                    double withdrawn = totalWithdrawn != null ? totalWithdrawn : 0.0;
                    double remainingBalance = finalTotalEarnings - withdrawn;
                    binding.tvTotalEarnings.setText(String.format(Locale.getDefault(), "৳%.2f", remainingBalance));
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

                // Calculate total earnings from filtered orders (Period Earnings)
                double periodEarnings = 0;
                for (Order order : orders) {
                    double orderEarnings = BASE_DELIVERY_FEE + (order.getTotalPrice() * PERCENTAGE_BONUS);
                    periodEarnings += orderEarnings;
                }
                
                binding.tvPeriodEarnings.setText(String.format(Locale.getDefault(), "৳%.2f", periodEarnings));
                binding.tvPeriodEarnings.setVisibility(View.VISIBLE);
                binding.tvPeriodEarningsLabel.setVisibility(View.VISIBLE);
                
                // Calculate Remaining Balance as Period Earnings - Total Withdrawals
                final double finalPeriodEarnings = periodEarnings;
                withdrawalRepository.getTotalWithdrawnByUser(bikerId, "BIKER").observe(this, totalWithdrawn -> {
                    double withdrawn = totalWithdrawn != null ? totalWithdrawn : 0.0;
                    double remainingBalance = finalPeriodEarnings - withdrawn;
                    binding.tvTotalEarnings.setText(String.format(Locale.getDefault(), "৳%.2f", remainingBalance));
                });
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

    // Withdrawal methods
    // Withdrawal methods
    private void showWithdrawalDialog() {
        // Calculate available balance from all completed orders minus withdrawals
        LiveData<List<Order>> ordersLiveData = orderRepository.getCompletedOrdersByBiker(bikerId);
        ordersLiveData.observe(this, new androidx.lifecycle.Observer<List<Order>>() {
            @Override
            public void onChanged(List<Order> orders) {
                // Remove observer using the same LiveData instance
                ordersLiveData.removeObserver(this);
                
                if (orders != null) {
                    // Calculate total earnings from all orders
                    double totalEarnings = 0;
                    for (Order order : orders) {
                        double orderEarnings = BASE_DELIVERY_FEE + (order.getTotalPrice() * PERCENTAGE_BONUS);
                        totalEarnings += orderEarnings;
                    }
                    
                    final double finalTotalEarnings = totalEarnings;
                    
                    // Get total withdrawn amount
                    LiveData<Double> withdrawnLiveData = withdrawalRepository.getTotalWithdrawnByUser(bikerId, "BIKER");
                    withdrawnLiveData.observe(BikerHistoryActivity.this, new androidx.lifecycle.Observer<Double>() {
                        @Override
                        public void onChanged(Double totalWithdrawn) {
                            // Remove observer using the same LiveData instance
                            withdrawnLiveData.removeObserver(this);
                            
                            double withdrawn = totalWithdrawn != null ? totalWithdrawn : 0.0;
                            double availableBalance = finalTotalEarnings - withdrawn;
                            
                            if (availableBalance <= 0) {
                                Toast.makeText(BikerHistoryActivity.this, R.string.no_balance_to_withdraw, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String[] methods = {getString(R.string.bank_account), getString(R.string.bkash), getString(R.string.nagad)};
                            
                            AlertDialog methodDialog = new AlertDialog.Builder(BikerHistoryActivity.this)
                                    .setTitle(R.string.select_withdrawal_method)
                                    .setItems(methods, (dialog, which) -> {
                                        dialog.dismiss();
                                        String method = which == 0 ? "Bank" : (which == 1 ? "Bkash" : "Nagad");
                                        showAccountNumberDialog(method, availableBalance);
                                    })
                                    .setNegativeButton(R.string.cancel, null)
                                    .setCancelable(false)
                                    .create();
                            
                            methodDialog.show();
                        }
                    });
                } else {
                    Toast.makeText(BikerHistoryActivity.this, R.string.no_balance_to_withdraw, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showAccountNumberDialog(String method, double maxAmount) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_input, null);
        EditText etInput = dialogView.findViewById(R.id.etInput);
        etInput.setHint(R.string.account_number_hint);
        // Make account number visible (not password)
        etInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.enter_account_number, method))
                .setView(dialogView)
                .setPositiveButton(R.string.next, null)
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(false)
                .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String accountNumber = etInput.getText().toString().trim();
                if (accountNumber.isEmpty()) {
                    Toast.makeText(this, R.string.account_number_required, Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                showAmountDialog(method, accountNumber, maxAmount);
            });
        });
        
        dialog.show();
    }

    private void showAmountDialog(String method, String accountNumber, double maxAmount) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_input, null);
        EditText etInput = dialogView.findViewById(R.id.etInput);
        etInput.setHint(R.string.enter_amount);
        etInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.enter_withdrawal_amount)
                .setView(dialogView)
                .setPositiveButton(R.string.next, null)
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(false)
                .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String amountStr = etInput.getText().toString().trim();
                if (amountStr.isEmpty()) {
                    Toast.makeText(this, R.string.amount_required, Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        Toast.makeText(this, R.string.amount_must_be_positive, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (amount > maxAmount) {
                        Toast.makeText(this, getString(R.string.insufficient_balance, String.format(Locale.getDefault(), "৳%.2f", maxAmount)), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dialog.dismiss();
                    showOtpDialog(method, accountNumber, amount);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, R.string.invalid_amount, Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        dialog.show();
    }

    private void showOtpDialog(String method, String accountNumber, double amount) {
        // Generate random 6-digit OTP
        String generatedOtp = String.format(Locale.getDefault(), "%06d", new java.util.Random().nextInt(1000000));
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_input, null);
        EditText etInput = dialogView.findViewById(R.id.etInput);
        etInput.setHint(R.string.otp_hint);
        etInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.enter_otp)
                .setMessage(getString(R.string.otp_sent_with_code, generatedOtp))
                .setView(dialogView)
                .setPositiveButton(R.string.verify, null)
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(false)
                .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String otp = etInput.getText().toString().trim();
                if (otp.isEmpty()) {
                    Toast.makeText(this, R.string.otp_required, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!otp.equals(generatedOtp)) {
                    Toast.makeText(this, R.string.invalid_otp, Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                showPinDialog(method, accountNumber, amount);
            });
        });
        
        dialog.show();
    }

    private void showPinDialog(String method, String accountNumber, double amount) {
        // Generate random 4-digit PIN
        String generatedPin = String.format(Locale.getDefault(), "%04d", new java.util.Random().nextInt(10000));
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_input, null);
        EditText etInput = dialogView.findViewById(R.id.etInput);
        etInput.setHint(R.string.pin_hint);
        etInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.enter_pin)
                .setMessage(getString(R.string.pin_sent_with_code, generatedPin))
                .setView(dialogView)
                .setPositiveButton(R.string.confirm, null)
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(false)
                .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String pin = etInput.getText().toString().trim();
                if (pin.isEmpty()) {
                    Toast.makeText(this, R.string.pin_required, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!pin.equals(generatedPin)) {
                    Toast.makeText(this, R.string.invalid_pin, Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                processWithdrawal(method, accountNumber, amount);
            });
        });
        
        dialog.show();
    }

    private void processWithdrawal(String method, String accountNumber, double amount) {
        // Create withdrawal record
        Withdrawal withdrawal = new Withdrawal(bikerId, "BIKER", amount, method, accountNumber);
        withdrawalRepository.insert(withdrawal, 
            () -> {
                // Deduct amount from user earnings
                userRepository.deductEarnings(bikerId, amount);

                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.withdrawal_successful)
                            .setMessage(getString(R.string.withdrawal_success_message, 
                                    String.format(Locale.getDefault(), "৳%.2f", amount), method, accountNumber))
                            .setPositiveButton(R.string.ok, (dialog, which) -> {
                                // Reload stats to show updated balance
                                loadStats();
                            })
                            .setCancelable(false)
                            .show();
                });
            },
            () -> {
                // Error callback
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                });
            }
        );
    }
}
