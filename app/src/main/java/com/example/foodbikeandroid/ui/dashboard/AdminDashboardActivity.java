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
        setupRecyclerView();
        setupSwipeRefresh();
        displayUserInfo();
        setupClickListeners();
        loadStatistics();
        observeRecentActions();seedDummyDataIfEmpty();
    }

    private void seedDummyDataIfEmpty() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                int count = restaurantDao.getRestaurantCount();
                
                // Force restore if data is missing or incomplete (e.g. only 1 restaurant)
                if (count < 100) {
                    runOnUiThread(() -> Toast.makeText(this, "Resetting Data to 136 Restaurants...", Toast.LENGTH_SHORT).show());
                    
                    // Clear existing partial data
                    restaurantDao.deleteAll();
                    
                    java.util.List<com.example.foodbikeandroid.data.model.Restaurant> dummyRestaurants = new java.util.ArrayList<>();
                    java.util.List<String> divisions = com.example.foodbikeandroid.data.LocationData.getAllDivisions();
                    
                    int totalGenerated = 0;

                    for (String division : divisions) {
                        if ("All Divisions".equals(division)) continue;
                        
                        java.util.List<String> districts = com.example.foodbikeandroid.data.LocationData.getDistrictsForDivision(division);
                        if (districts == null) continue;

                        for (String district : districts) {
                            if ("All Districts".equals(district)) continue;
                            
                            int numRestos = "Dhaka".equals(district) ? 10 : 2;
                            
                            for (int k = 1; k <= numRestos; k++) {
                                totalGenerated++;
                                com.example.foodbikeandroid.data.model.Restaurant r = new com.example.foodbikeandroid.data.model.Restaurant(
                                    "dummy_rest_" + totalGenerated,
                                    district + " Restaurant " + k,
                                    division,
                                    district,
                                    "Road " + (int)(Math.random()*20) + ", " + district
                                );
                                r.setRating(2.5 + Math.random() * 2.5); 
                                r.setMenuItems(new java.util.ArrayList<>());
                                dummyRestaurants.add(r);
                            }
                        }
                    }
                    
                    restaurantDao.insertAll(dummyRestaurants);
                    
                    int finalTotal = totalGenerated;
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Success! Reset & Restored " + finalTotal + " Restaurants.", Toast.LENGTH_LONG).show();
                        loadStatistics(); 
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Restoration Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
    }


    private void setupRecyclerView() {
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary, R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(this::refreshAllData);
    }

    private boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        } else if (item.getItemId() == R.id.action_notifications) {
            Toast.makeText(this, "Notifications - Coming Soon", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void displayUserInfo() {
        String username = authViewModel.getCurrentUsername();
        binding.tvUsername.setText(username != null ? username + " (Admin)" : "Admin");
    }

    private void setupClickListeners() {
        binding.btnLogout.setOnClickListener(v -> logout());
        
        binding.cardViewApplications.setOnClickListener(v -> {
            openReviewApplications();
        });

        binding.cardManageRestaurants.setOnClickListener(v -> {
            openManageRestaurants();
        });

        binding.cardRecentActivity.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, AdminRecentActivity.class));
        });

        binding.cardEarnings.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, AdminEarningsActivity.class));
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
        // Recent activity RecyclerView removed from layout, so this logic is no longer needed
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

    private void openManageRestaurants() {
        Intent intent = new Intent(this, AdminManageRestaurantsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }
}
