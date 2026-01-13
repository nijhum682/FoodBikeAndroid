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
import com.example.foodbikeandroid.data.model.Restaurant;
import com.example.foodbikeandroid.data.model.RestaurantApplication;
import com.example.foodbikeandroid.data.repository.OrderRepository;
import com.example.foodbikeandroid.data.repository.RestaurantApplicationRepository;
import com.example.foodbikeandroid.data.repository.RestaurantRepository;
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
    private RestaurantRepository restaurantRepository;
    private OrderRepository orderRepository;
    private MyRestaurantAdapter restaurantAdapter;
    
    private List<RestaurantApplication> approvedApplications = new ArrayList<>();
    private List<RestaurantApplication> pendingApplications = new ArrayList<>();
    private Restaurant currentRestaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEntrepreneurDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        applicationRepository = RestaurantApplicationRepository.getInstance(this);
        restaurantRepository = RestaurantRepository.getInstance(getApplication());
        orderRepository = new OrderRepository(getApplication());

        setupToolbar();
        displayUserInfo();
        setupRecyclerView();
        setupClickListeners();
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
                updateNotificationBadge();
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
        final int[] newOrdersCount = {0};
        final int[] completedRequests = {0};
        final int totalRequests = approvedApplications.size();
        
        for (RestaurantApplication app : approvedApplications) {
            restaurantRepository.getAllRestaurants().observe(this, allRestaurants -> {
                if (allRestaurants == null) return;
                
                String restaurantId = null;
                for (Restaurant restaurant : allRestaurants) {
                    if (restaurant.getName().equals(app.getRestaurantName())) {
                        restaurantId = restaurant.getId();
                        break;
                    }
                }
                
                if (restaurantId == null) {
                    completedRequests[0]++;
                    return;
                }
                
                String finalRestaurantId = restaurantId;
                orderRepository.getOrdersByRestaurantId(finalRestaurantId).observe(this, orders -> {
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
                                newOrdersCount[0]++;
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
                        
                        if (newOrdersCount[0] > 0) {
                            binding.newOrdersBadge.setText(String.valueOf(newOrdersCount[0]));
                            binding.newOrdersBadge.setVisibility(View.VISIBLE);
                        } else {
                            binding.newOrdersBadge.setVisibility(View.GONE);
                        }
                    }
                });
            });
        }
    }

    private androidx.lifecycle.LiveData<List<Restaurant>> currentRestaurantsLiveData;

    private void updateRestaurantsList() {
        if (approvedApplications.isEmpty()) {
            binding.cardRestaurantStatus.setVisibility(View.GONE);
            binding.cardNoRestaurants.setVisibility(View.VISIBLE);
            binding.rvMyRestaurants.setVisibility(View.GONE);
            binding.tvSeeAllRestaurants.setVisibility(View.GONE);
        } else {
            binding.cardRestaurantStatus.setVisibility(View.VISIBLE);
            binding.cardNoRestaurants.setVisibility(View.GONE);
            binding.rvMyRestaurants.setVisibility(View.VISIBLE);
            binding.tvSeeAllRestaurants.setVisibility(View.VISIBLE);
            restaurantAdapter.setRestaurants(approvedApplications);
            
            String restaurantName = approvedApplications.get(0).getRestaurantName();
            if (restaurantName != null) {
                if (currentRestaurantsLiveData != null) {
                    currentRestaurantsLiveData.removeObservers(this);
                }
                
                currentRestaurantsLiveData = restaurantRepository.getAllRestaurants();
                currentRestaurantsLiveData.observe(this, allRestaurants -> {
                    if (allRestaurants != null) {
                        for (Restaurant restaurant : allRestaurants) {
                            if (restaurant.getName().equals(restaurantName)) {
                                currentRestaurant = restaurant;
                                binding.switchRestaurantStatus.setChecked(restaurant.isOpen());
                                if (restaurant.isOpen()) {
                                    binding.tvRestaurantStatus.setText("Open");
                                    binding.tvRestaurantStatus.setTextColor(getColor(R.color.success));
                                } else {
                                    binding.tvRestaurantStatus.setText("Closed");
                                    binding.tvRestaurantStatus.setTextColor(getColor(R.color.error));
                                }
                                break;
                            }
                        }
                    }
                });
            }
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
        androidx.lifecycle.LiveData<List<Restaurant>> restaurantsLiveData = restaurantRepository.getAllRestaurants();
        restaurantsLiveData.observe(this, new androidx.lifecycle.Observer<List<Restaurant>>() {
            @Override
            public void onChanged(List<Restaurant> allRestaurants) {
                restaurantsLiveData.removeObserver(this);
                if (allRestaurants == null) return;
                
                String restaurantId = null;
                for (Restaurant r : allRestaurants) {
                    if (r.getName().equals(restaurant.getRestaurantName())) {
                        restaurantId = r.getId();
                        break;
                    }
                }
                
                if (restaurantId != null) {
                    Intent intent = new Intent(EntrepreneurDashboardActivity.this, RestaurantOrdersActivity.class);
                    intent.putExtra(RestaurantOrdersActivity.EXTRA_RESTAURANT_ID, restaurantId);
                    intent.putExtra(RestaurantOrdersActivity.EXTRA_RESTAURANT_NAME, restaurant.getRestaurantName());
                    intent.putExtra(RestaurantOrdersActivity.EXTRA_RESTAURANT_LOCATION, 
                            restaurant.getDistrict() + ", " + restaurant.getDivision());
                    startActivity(intent);
                } else {
                    Toast.makeText(EntrepreneurDashboardActivity.this, "Restaurant not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openManageMenu() {
        String username = authViewModel.getCurrentUsername();
        if (username == null) {
            Toast.makeText(this, R.string.error_not_logged_in, Toast.LENGTH_SHORT).show();
            return;
        }

        androidx.lifecycle.LiveData<List<RestaurantApplication>> applicationsLiveData = applicationRepository.getByEntrepreneur(username);
        applicationsLiveData.observe(this, new androidx.lifecycle.Observer<List<RestaurantApplication>>() {
            @Override
            public void onChanged(List<RestaurantApplication> applications) {
                applicationsLiveData.removeObserver(this);
                
                if (applications == null || applications.isEmpty()) {
                    Toast.makeText(EntrepreneurDashboardActivity.this, R.string.no_restaurants_to_manage, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(EntrepreneurDashboardActivity.this, R.string.no_restaurants_to_manage, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (manageableApps.size() == 1) {
                    openMenuForApplication(manageableApps.get(0));
                } else {
                    showRestaurantSelectionDialog(manageableApps);
                }
            }
        });
    }

    private void openViewRatings() {
        if (approvedApplications.isEmpty()) {
            Toast.makeText(this, R.string.no_restaurants_yet, Toast.LENGTH_SHORT).show();
            return;
        }

        if (approvedApplications.size() == 1) {
            // Single restaurant - open reviews directly
            RestaurantApplication app = approvedApplications.get(0);
            openRatingsForRestaurant(app);
        } else {
            // Multiple restaurants - show selection dialog
            showRestaurantSelectionForRatings();
        }
    }

    private void showRestaurantSelectionForRatings() {
        String[] restaurantNames = new String[approvedApplications.size()];
        for (int i = 0; i < approvedApplications.size(); i++) {
            restaurantNames[i] = approvedApplications.get(i).getRestaurantName();
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.select_restaurant)
                .setItems(restaurantNames, (dialog, which) -> {
                    openRatingsForRestaurant(approvedApplications.get(which));
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void openRatingsForRestaurant(RestaurantApplication application) {
        androidx.lifecycle.LiveData<List<Restaurant>> restaurantsLiveData = restaurantRepository.getAllRestaurants();
        restaurantsLiveData.observe(this, new androidx.lifecycle.Observer<List<Restaurant>>() {
            @Override
            public void onChanged(List<Restaurant> allRestaurants) {
                restaurantsLiveData.removeObserver(this);
                
                if (allRestaurants != null) {
                    for (Restaurant restaurant : allRestaurants) {
                        if (restaurant.getName().equals(application.getRestaurantName())) {
                            Intent intent = new Intent(EntrepreneurDashboardActivity.this, ReviewsListActivity.class);
                            intent.putExtra(ReviewsListActivity.EXTRA_RESTAURANT_ID, restaurant.getId());
                            intent.putExtra(ReviewsListActivity.EXTRA_RESTAURANT_NAME, restaurant.getName());
                            startActivity(intent);
                            return;
                        }
                    }
                }
                Toast.makeText(EntrepreneurDashboardActivity.this, "Restaurant not found", Toast.LENGTH_SHORT).show();
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
        if (application.getStatus() == ApplicationStatus.APPROVED) {
            androidx.lifecycle.LiveData<List<Restaurant>> restaurantsLiveData = restaurantRepository.getAllRestaurants();
            restaurantsLiveData.observe(this, new androidx.lifecycle.Observer<List<Restaurant>>() {
                @Override
                public void onChanged(List<Restaurant> allRestaurants) {
                    restaurantsLiveData.removeObserver(this);
                    
                    if (allRestaurants != null) {
                        for (Restaurant r : allRestaurants) {
                            if (r.getName().equals(application.getRestaurantName())) {
                                Intent intent = new Intent(EntrepreneurDashboardActivity.this, ManageMenuActivity.class);
                                intent.putExtra(ManageMenuActivity.EXTRA_APPLICATION_ID, application.getApplicationId());
                                intent.putExtra(ManageMenuActivity.EXTRA_RESTAURANT_ID, r.getId());
                                startActivity(intent);
                                return;
                            }
                        }
                    }
                    // Fallback if not found (shouldn't happen if approved)
                    Toast.makeText(EntrepreneurDashboardActivity.this, "Error: Restaurant data not found", Toast.LENGTH_SHORT).show();
                }
            });
        }
 else {
            Intent intent = new Intent(this, ManageMenuActivity.class);
            intent.putExtra(ManageMenuActivity.EXTRA_APPLICATION_ID, application.getApplicationId());
            startActivity(intent);
        }
    }

    private void setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
        updateNotificationBadge();
    }

    private void updateNotificationBadge() {
        MenuItem notificationItem = binding.toolbar.getMenu().findItem(R.id.action_notifications);
        if (notificationItem != null) {
            boolean hasUnread = false;
            for (RestaurantApplication app : approvedApplications) {
                if (app.getAdminMessage() != null && !app.getAdminMessage().isEmpty() && !app.isMessageViewed()) {
                    hasUnread = true;
                    break;
                }
            }
            for (RestaurantApplication app : pendingApplications) {
                if (app.getAdminMessage() != null && !app.getAdminMessage().isEmpty() && !app.isMessageViewed()) {
                    hasUnread = true;
                    break;
                }
            }
            notificationItem.setIcon(hasUnread ? R.drawable.ic_badge : R.drawable.ic_info);
        }
    }

    private boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        } else if (item.getItemId() == R.id.action_notifications) {
            openNotifications();
            return true;
        }
        return false;
    }

    private void openNotifications() {
        Intent intent = new Intent(this, MyApplicationsActivity.class);
        startActivity(intent);
    }

    private void displayUserInfo() {
        String username = authViewModel.getCurrentUsername();
        binding.tvUsername.setText(username != null ? username + " (Entrepreneur)" : "Entrepreneur");
    }

    private void setupClickListeners() {
        binding.btnLogout.setOnClickListener(v -> logout());
        
        binding.switchRestaurantStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentRestaurant != null) {
                currentRestaurant.setOpen(isChecked);
                restaurantRepository.update(currentRestaurant, new RestaurantRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            if (isChecked) {
                                binding.tvRestaurantStatus.setText("Open");
                                binding.tvRestaurantStatus.setTextColor(getColor(R.color.success));
                                Toast.makeText(EntrepreneurDashboardActivity.this, 
                                    "Restaurant is now Open", Toast.LENGTH_SHORT).show();
                            } else {
                                binding.tvRestaurantStatus.setText("Closed");
                                binding.tvRestaurantStatus.setTextColor(getColor(R.color.error));
                                Toast.makeText(EntrepreneurDashboardActivity.this, 
                                    "Restaurant is now Closed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            buttonView.setChecked(!isChecked);
                            Toast.makeText(EntrepreneurDashboardActivity.this, 
                                "Error updating status: " + message, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        });
        
        binding.cardManageMenu.setOnClickListener(v -> openManageMenu());

        binding.cardViewRatings.setOnClickListener(v -> openViewRatings());

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

    private void logout() {
        authViewModel.logout();
        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
