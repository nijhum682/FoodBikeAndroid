package com.example.foodbikeandroid.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.database.AdminActionDao;
import com.example.foodbikeandroid.data.database.FoodBikeDatabase;
import com.example.foodbikeandroid.data.database.OrderDao;
import com.example.foodbikeandroid.data.database.RestaurantApplicationDao;
import com.example.foodbikeandroid.data.database.RestaurantDao;
import com.example.foodbikeandroid.data.database.UserDao;
import com.example.foodbikeandroid.data.model.AdminAction;
import com.example.foodbikeandroid.data.model.ApplicationStatus;
import com.example.foodbikeandroid.databinding.ActivityAdminDashboardBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;
import com.example.foodbikeandroid.ui.auth.SignInActivity;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private AuthViewModel authViewModel;
    private AdminActionAdapter actionAdapter;

    private UserDao userDao;
    private RestaurantDao restaurantDao;
    private RestaurantApplicationDao applicationDao;
    private OrderDao orderDao;
    private AdminActionDao adminActionDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        FoodBikeDatabase database = FoodBikeDatabase.getInstance(this);
        userDao = database.userDao();
        restaurantDao = database.restaurantDao();
        applicationDao = database.restaurantApplicationDao();
        orderDao = database.orderDao();
        adminActionDao = database.adminActionDao();

        setupToolbar();
        setupBottomNavigation();
        setupRecyclerView();
        setupSwipeRefresh();
        displayUserInfo();
        setupClickListeners();
        loadStatistics();
        observeRecentActions();
    }

    private void setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                return true;
            } else if (itemId == R.id.nav_applications) {
                openReviewApplications();
                return true;
            } else if (itemId == R.id.nav_restaurants) {
                Toast.makeText(this, "Restaurants - Coming Soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_users) {
                Toast.makeText(this, "Users - Coming Soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        binding.bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
    }

    private void setupRecyclerView() {
        actionAdapter = new AdminActionAdapter();
        binding.rvRecentActivity.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecentActivity.setAdapter(actionAdapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary, R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(this::refreshAllData);
    }

    private boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        } else if (item.getItemId() == R.id.action_profile) {
            Toast.makeText(this, "Profile - Coming Soon", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void displayUserInfo() {
        String username = authViewModel.getCurrentUsername();
        binding.tvUsername.setText(username != null ? username : "Admin");
    }

    private void setupClickListeners() {
        binding.cardViewApplications.setOnClickListener(v -> {
            openReviewApplications();
        });

        binding.cardManageRestaurants.setOnClickListener(v -> {
            Toast.makeText(this, "Manage Restaurants - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        binding.cardViewHistory.setOnClickListener(v -> {
            Toast.makeText(this, "View History - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        binding.cardManageUsers.setOnClickListener(v -> {
            Toast.makeText(this, "Manage Users - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        binding.tvSeeAllActivity.setOnClickListener(v -> {
            Toast.makeText(this, "All Activity - Coming Soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadStatistics() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int totalUsers = userDao.getUserCount();
            int totalRestaurants = restaurantDao.getRestaurantCount();

            Calendar startOfDay = Calendar.getInstance();
            startOfDay.set(Calendar.HOUR_OF_DAY, 0);
            startOfDay.set(Calendar.MINUTE, 0);
            startOfDay.set(Calendar.SECOND, 0);
            startOfDay.set(Calendar.MILLISECOND, 0);
            long todayStart = startOfDay.getTimeInMillis();

            int todaysOrders = orderDao.getOrderCountAfterTimestamp(todayStart);

            runOnUiThread(() -> {
                binding.tvTotalUsers.setText(String.valueOf(totalUsers));
                binding.tvTotalRestaurants.setText(String.valueOf(totalRestaurants));
                binding.tvTodaysOrders.setText(String.valueOf(todaysOrders));
            });
        });

        applicationDao.getByStatus(ApplicationStatus.PENDING).observe(this, applications -> {
            int pendingCount = applications != null ? applications.size() : 0;
            binding.tvPendingApplications.setText(String.valueOf(pendingCount));
        });
    }

    private void observeRecentActions() {
        adminActionDao.getAll().observe(this, actions -> {
            if (actions != null && !actions.isEmpty()) {
                List<AdminAction> recentActions = actions.size() > 5 
                    ? actions.subList(0, 5) 
                    : actions;
                actionAdapter.setActions(recentActions);
                binding.rvRecentActivity.setVisibility(View.VISIBLE);
                binding.layoutNoActivity.setVisibility(View.GONE);
            } else {
                binding.rvRecentActivity.setVisibility(View.GONE);
                binding.layoutNoActivity.setVisibility(View.VISIBLE);
            }
        });
    }

    private void refreshAllData() {
        loadStatistics();
        binding.swipeRefresh.postDelayed(() -> {
            binding.swipeRefresh.setRefreshing(false);
            Toast.makeText(this, R.string.stats_refreshed, Toast.LENGTH_SHORT).show();
        }, 1000);
    }

    private void logout() {
        authViewModel.logout();
        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openReviewApplications() {
        Intent intent = new Intent(this, AdminReviewApplicationsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }
}
