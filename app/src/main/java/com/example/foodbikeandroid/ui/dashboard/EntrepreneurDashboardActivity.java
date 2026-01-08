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
import com.example.foodbikeandroid.data.model.ApplicationStatus;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.model.OrderStatus;
import com.example.foodbikeandroid.data.model.RestaurantApplication;
import com.example.foodbikeandroid.data.repository.OrderRepository;
import com.example.foodbikeandroid.data.repository.RestaurantApplicationRepository;
import com.example.foodbikeandroid.databinding.ActivityEntrepreneurDashboardBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;
import com.example.foodbikeandroid.ui.auth.SignInActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EntrepreneurDashboardActivity extends AppCompatActivity {

    private ActivityEntrepreneurDashboardBinding binding;
    private AuthViewModel authViewModel;
    private RestaurantApplicationRepository applicationRepository;
    private OrderRepository orderRepository;
    private MyRestaurantAdapter restaurantAdapter;
    
    private List<RestaurantApplication> approvedApplications = new ArrayList<>();
    private List<RestaurantApplication> pendingApplications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEntrepreneurDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        applicationRepository = RestaurantApplicationRepository.getInstance(this);
        orderRepository = new OrderRepository(getApplication());

        setupToolbar();
        displayUserInfo();
        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigation();
        setupSwipeRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(this::loadDashboardData);
    }

    private void setupRecyclerView() {
        restaurantAdapter = new MyRestaurantAdapter();
        restaurantAdapter.setOnRestaurantClickListener(new MyRestaurantAdapter.OnRestaurantClickListener() {
            @Override
            public void onRestaurantClick(RestaurantApplication restaurant) {
                openRestaurantOrders(restaurant);
            }

            @Override
            public void onViewOrdersClick(RestaurantApplication restaurant) {
                openRestaurantOrders(restaurant);
            }
        });
        
        binding.rvMyRestaurants.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMyRestaurants.setAdapter(restaurantAdapter);
    }

    private void loadDashboardData() {
        String username = authViewModel.getCurrentUsername();
        if (username == null) {
            binding.swipeRefresh.setRefreshing(false);
            return;
        }

        applicationRepository.getByEntrepreneur(username).observe(this, applications -> {
            binding.swipeRefresh.setRefreshing(false);
            
            if (applications != null) {
                approvedApplications.clear();
                pendingApplications.clear();
                boolean hasUnread = false;
                
                for (RestaurantApplication app : applications) {
                    if (app.getStatus() == ApplicationStatus.APPROVED) {
                        approvedApplications.add(app);
                    } else if (app.getStatus() == ApplicationStatus.PENDING) {
                        pendingApplications.add(app);
                    }
                    
                    if (app.getAdminMessage() != null && 
                            !app.getAdminMessage().isEmpty() && 
                            !app.isMessageViewed()) {
                        hasUnread = true;
                    }
                }
                
                binding.applicationsBadge.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
                
                updateDashboardStats();
                updateRestaurantsList();
            }
        });
    }

    private void updateDashboardStats() {
        binding.tvPendingCount.setText(String.valueOf(pendingApplications.size()));
        binding.tvApprovedCount.setText(String.valueOf(approvedApplications.size()));
        
        if (approvedApplications.isEmpty()) {
            binding.tvTotalOrders.setText("0");
            binding.tvTotalRevenue.setText("৳0");
        } else {
            loadTotalOrdersAndRevenue();
        }
    }

    private void loadTotalOrdersAndRevenue() {
        final int[] totalOrders = {0};
        final double[] totalRevenue = {0.0};
        final int[] completedRequests = {0};
        final int totalRequests = approvedApplications.size();
        
        for (RestaurantApplication app : approvedApplications) {
            orderRepository.getOrdersByRestaurantId(app.getApplicationId()).observe(this, orders -> {
                if (orders != null) {
                    totalOrders[0] += orders.size();
                    for (Order order : orders) {
                        if (order.getStatus() != OrderStatus.CANCELLED && 
                            order.getStatus() != OrderStatus.AUTO_CANCELLED) {
                            totalRevenue[0] += order.getTotalPrice();
                        }
                    }
                    
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
                    
                    restaurantAdapter.updateRestaurantStats(
                            app.getApplicationId(), pendingCount, todayCount, todayRevenue);
                }
                
                completedRequests[0]++;
                if (completedRequests[0] >= totalRequests) {
                    binding.tvTotalOrders.setText(String.valueOf(totalOrders[0]));
                    binding.tvTotalRevenue.setText("৳" + (int) totalRevenue[0]);
                }
            });
        }
    }

    private void updateRestaurantsList() {
        if (approvedApplications.isEmpty()) {
            binding.cardNoRestaurants.setVisibility(View.VISIBLE);
            binding.rvMyRestaurants.setVisibility(View.GONE);
            binding.tvSeeAllRestaurants.setVisibility(View.GONE);
        } else {
            binding.cardNoRestaurants.setVisibility(View.GONE);
            binding.rvMyRestaurants.setVisibility(View.VISIBLE);
            binding.tvSeeAllRestaurants.setVisibility(View.VISIBLE);
            restaurantAdapter.setRestaurants(approvedApplications);
        }
    }

    private long getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private void openRestaurantOrders(RestaurantApplication restaurant) {
        Intent intent = new Intent(this, RestaurantOrdersActivity.class);
        intent.putExtra(RestaurantOrdersActivity.EXTRA_RESTAURANT_ID, restaurant.getApplicationId());
        intent.putExtra(RestaurantOrdersActivity.EXTRA_RESTAURANT_NAME, restaurant.getRestaurantName());
        intent.putExtra(RestaurantOrdersActivity.EXTRA_RESTAURANT_LOCATION, 
                restaurant.getDistrict() + ", " + restaurant.getDivision());
        startActivity(intent);
    }

    private void openManageMenu() {
        String username = authViewModel.getCurrentUsername();
        if (username == null) {
            Toast.makeText(this, R.string.error_not_logged_in, Toast.LENGTH_SHORT).show();
            return;
        }

        applicationRepository.getByEntrepreneur(username).observe(this, applications -> {
            if (applications == null || applications.isEmpty()) {
                Toast.makeText(this, R.string.no_restaurants_to_manage, Toast.LENGTH_SHORT).show();
                return;
            }

            List<RestaurantApplication> manageableApps = new ArrayList<>();
            for (RestaurantApplication app : applications) {
                if (app.getStatus() == ApplicationStatus.APPROVED || 
                    app.getStatus() == ApplicationStatus.PENDING) {
                    manageableApps.add(app);
                }
            }

            if (manageableApps.isEmpty()) {
                Toast.makeText(this, R.string.no_restaurants_to_manage, Toast.LENGTH_SHORT).show();
                return;
            }

            if (manageableApps.size() == 1) {
                openMenuForApplication(manageableApps.get(0));
            } else {
                showRestaurantSelectionDialog(manageableApps);
            }
        });
    }

    private void showRestaurantSelectionDialog(List<RestaurantApplication> applications) {
        String[] restaurantNames = new String[applications.size()];
        for (int i = 0; i < applications.size(); i++) {
            RestaurantApplication app = applications.get(i);
            String status = app.getStatus() == ApplicationStatus.APPROVED ? 
                    getString(R.string.status_approved) : getString(R.string.status_pending);
            restaurantNames[i] = app.getRestaurantName() + " (" + status + ")";
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.select_restaurant)
                .setItems(restaurantNames, (dialog, which) -> {
                    openMenuForApplication(applications.get(which));
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void openMenuForApplication(RestaurantApplication application) {
        Intent intent = new Intent(this, ManageMenuActivity.class);
        intent.putExtra(ManageMenuActivity.EXTRA_APPLICATION_ID, application.getApplicationId());
        if (application.getStatus() == ApplicationStatus.APPROVED) {
            intent.putExtra(ManageMenuActivity.EXTRA_RESTAURANT_ID, application.getApplicationId());
        }
        startActivity(intent);
    }

    private void setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
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
        binding.tvUsername.setText(username != null ? username : "Restaurant Owner");
    }

    private void setupClickListeners() {
        binding.cardManageMenu.setOnClickListener(v -> openManageMenu());

        binding.cardPendingApplications.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyApplicationsActivity.class);
            startActivity(intent);
        });

        binding.cardApprovedRestaurants.setOnClickListener(v -> {
            if (approvedApplications.isEmpty()) {
                Toast.makeText(this, R.string.no_restaurants_yet, Toast.LENGTH_SHORT).show();
            }
        });

        binding.cardTotalOrders.setOnClickListener(v -> {
            if (approvedApplications.isEmpty()) {
                Toast.makeText(this, R.string.no_restaurants_yet, Toast.LENGTH_SHORT).show();
            } else if (approvedApplications.size() == 1) {
                openRestaurantOrders(approvedApplications.get(0));
            } else {
                showRestaurantSelectionForOrders();
            }
        });

        binding.tvSeeAllRestaurants.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyApplicationsActivity.class);
            startActivity(intent);
        });

        binding.cardNoRestaurants.setOnClickListener(v -> {
            Intent intent = new Intent(this, RestaurantApplicationActivity.class);
            startActivity(intent);
        });

        binding.btnApplyNow.setOnClickListener(v -> {
            Intent intent = new Intent(this, RestaurantApplicationActivity.class);
            startActivity(intent);
        });

        binding.cardMyApplications.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyApplicationsActivity.class);
            startActivity(intent);
        });

        binding.cardApplyRestaurant.setOnClickListener(v -> {
            Intent intent = new Intent(this, RestaurantApplicationActivity.class);
            startActivity(intent);
        });
    }

    private void showRestaurantSelectionForOrders() {
        String[] restaurantNames = new String[approvedApplications.size()];
        for (int i = 0; i < approvedApplications.size(); i++) {
            restaurantNames[i] = approvedApplications.get(i).getRestaurantName();
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.select_restaurant)
                .setItems(restaurantNames, (dialog, which) -> {
                    openRestaurantOrders(approvedApplications.get(which));
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_menu) {
                openManageMenu();
                return true;
            } else if (itemId == R.id.nav_orders) {
                if (approvedApplications.isEmpty()) {
                    Toast.makeText(this, R.string.no_restaurants_yet, Toast.LENGTH_SHORT).show();
                } else if (approvedApplications.size() == 1) {
                    openRestaurantOrders(approvedApplications.get(0));
                } else {
                    showRestaurantSelectionForOrders();
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                Toast.makeText(this, "Profile - Coming Soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void logout() {
        authViewModel.logout();
        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
