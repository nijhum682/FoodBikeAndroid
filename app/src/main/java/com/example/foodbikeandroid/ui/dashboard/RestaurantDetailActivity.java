package com.example.foodbikeandroid.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.cart.CartManager;
import com.example.foodbikeandroid.data.model.MenuItem;
import com.example.foodbikeandroid.data.model.Restaurant;
import com.example.foodbikeandroid.data.model.UserType;
import com.example.foodbikeandroid.databinding.ActivityRestaurantDetailBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;
import com.example.foodbikeandroid.ui.order.CartActivity;
import com.example.foodbikeandroid.data.repository.ReviewRepository;

import java.util.List;
import java.util.Locale;

public class RestaurantDetailActivity extends AppCompatActivity implements CartManager.CartUpdateListener {

    public static final String EXTRA_RESTAURANT_ID = "restaurant_id";
    
    private ActivityRestaurantDetailBinding binding;
    private RestaurantViewModel viewModel;
    private AuthViewModel authViewModel;
    private ReviewRepository reviewRepository;
    private MenuItemAdapter menuAdapter;
    private CartManager cartManager;
    private String currentRestaurantId;
    private String currentRestaurantName;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRestaurantDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RestaurantViewModel.class);
        reviewRepository = ReviewRepository.getInstance(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        cartManager = CartManager.getInstance();
        cartManager.setCartUpdateListener(this);
        
        // Check if current user is admin
        UserType userType = authViewModel.getCurrentUserType();
        isAdmin = (userType == UserType.ADMIN);
        
        setupToolbar();
        setupRecyclerView();
        setupCartButton();
        setupReviewsSection();
        setupAdminEditMenu();
        
        String restaurantId = getIntent().getStringExtra(EXTRA_RESTAURANT_ID);
        if (restaurantId != null) {
            currentRestaurantId = restaurantId;
            loadRestaurantDetails(restaurantId);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
    
    private void setupAdminEditMenu() {
        if (isAdmin) {
            binding.fabEditMenu.setVisibility(View.VISIBLE);
            binding.fabEditMenu.setOnClickListener(v -> {
                if (currentRestaurantId != null) {
                    Intent intent = new Intent(this, AdminEditMenuActivity.class);
                    intent.putExtra(AdminEditMenuActivity.EXTRA_RESTAURANT_ID, currentRestaurantId);
                    startActivity(intent);
                }
            });
        } else {
            binding.fabEditMenu.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        menuAdapter = new MenuItemAdapter();
        binding.rvMenuItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMenuItems.setAdapter(menuAdapter);
        
        menuAdapter.setOnMenuItemClickListener(item -> {
            if (item.isAvailable()) {
                cartManager.addItem(item, currentRestaurantId, currentRestaurantName);
                Toast.makeText(this, 
                    getString(R.string.item_added_to_cart, item.getName()), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCartButton() {
        binding.cardViewCart.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });
        updateCartUI();
    }

    private void loadRestaurantDetails(String restaurantId) {
        viewModel.getRestaurantById(restaurantId).observe(this, restaurant -> {
            if (restaurant != null) {
                currentRestaurantName = restaurant.getName();
                displayRestaurantDetails(restaurant);
                loadReviewsData(restaurantId);
            }
        });
    }

    private void setupReviewsSection() {
        binding.btnViewReviews.setOnClickListener(v -> {
            if (currentRestaurantId != null) {
                Intent intent = new Intent(this, ReviewsListActivity.class);
                intent.putExtra(ReviewsListActivity.EXTRA_RESTAURANT_ID, currentRestaurantId);
                intent.putExtra(ReviewsListActivity.EXTRA_RESTAURANT_NAME, currentRestaurantName);
                startActivity(intent);
            }
        });
    }

    private void loadReviewsData(String restaurantId) {
        reviewRepository.getAverageRating(restaurantId, (averageRating, count) -> {
            runOnUiThread(() -> {
                // Update average rating display
                binding.tvAverageRating.setText(String.format(Locale.getDefault(), "%.1f", averageRating));
                binding.rbAverageRating.setRating((float) averageRating);

                // Update review count
                String reviewCountText;
                if (count == 1) {
                    reviewCountText = getString(R.string.review_count, count);
                } else {
                    reviewCountText = getString(R.string.review_count_plural, count);
                }
                binding.tvReviewsCount.setText(reviewCountText);

                // Show or hide reviews section based on count
                if (count == 0) {
                    binding.tvAverageRating.setText("0.0");
                    binding.rbAverageRating.setRating(0);
                }
            });
        });
    }

    private void displayRestaurantDetails(Restaurant restaurant) {
        binding.toolbar.setTitle(restaurant.getName());
        binding.tvRestaurantName.setText(restaurant.getName());
        binding.tvCuisineType.setText(restaurant.getCuisineType());
        binding.tvLocation.setText(restaurant.getFullLocation());
        binding.tvAddress.setText(restaurant.getAddress());
        binding.tvRating.setText(String.format("%.1f", restaurant.getRating()));
        binding.ratingBar.setRating((float) restaurant.getRating());
        binding.tvOpeningHours.setText(restaurant.getOpeningHours());
        
        if (restaurant.isOpen()) {
            binding.tvStatus.setText(R.string.open_now);
            binding.tvStatus.setTextColor(getColor(R.color.success));
        } else {
            binding.tvStatus.setText(R.string.closed);
            binding.tvStatus.setTextColor(getColor(R.color.error));
        }

        List<MenuItem> menuItems = restaurant.getMenuItems();
        if (menuItems != null && !menuItems.isEmpty()) {
            menuAdapter.submitList(menuItems);
        }
    }

    private void updateCartUI() {
        int itemCount = cartManager.getItemCount();
        double total = cartManager.getTotalPrice();
        
        if (itemCount > 0 && (currentRestaurantId == null || 
                currentRestaurantId.equals(cartManager.getCurrentRestaurantId()))) {
            binding.cardViewCart.setVisibility(View.VISIBLE);
            binding.tvCartItemCount.setText(getString(R.string.view_cart, itemCount));
            binding.tvCartTotal.setText(String.format("৳%.0f", total));
        } else {
            binding.cardViewCart.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCartUpdated(int itemCount, double totalPrice) {
        updateCartUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartUI();
        // Reload reviews data in case a new review was added
        if (currentRestaurantId != null) {
            loadReviewsData(currentRestaurantId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cartManager.setCartUpdateListener(null);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
