package com.example.foodbikeandroid.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.Restaurant;
import com.example.foodbikeandroid.databinding.ActivityUserDashboardBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;
import com.example.foodbikeandroid.ui.auth.SignInActivity;

import java.util.List;

public class UserDashboardActivity extends AppCompatActivity {

    private ActivityUserDashboardBinding binding;
    private AuthViewModel authViewModel;
    private RestaurantViewModel restaurantViewModel;
    private RestaurantAdapter restaurantAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        restaurantViewModel = new ViewModelProvider(this).get(RestaurantViewModel.class);

        setupToolbar();
        displayUserInfo();
        setupBottomNavigation();
        setupRecyclerView();
        setupFilters();
        setupSearch();
        observeRestaurants();
    }

    private void setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    private boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        } else if (item.getItemId() == R.id.action_notifications) {
            Toast.makeText(this, "Notifications - Coming Soon", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_refund_history) {
            Intent intent = new Intent(this, RefundHistoryActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    private void displayUserInfo() {
        String username = authViewModel.getCurrentUsername();
        binding.tvUsername.setText(username != null ? username + " (User)" : "User");
    }

    private void setupRecyclerView() {
        restaurantAdapter = new RestaurantAdapter();
        binding.rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRestaurants.setAdapter(restaurantAdapter);
        
        restaurantAdapter.setOnRestaurantClickListener(restaurant -> {
            Intent intent = new Intent(this, RestaurantDetailActivity.class);
            intent.putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_ID, restaurant.getId());
            startActivity(intent);
        });
    }

    private void setupFilters() {
        List<String> divisions = restaurantViewModel.getDivisions();
        ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, divisions);
        divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDivision.setAdapter(divisionAdapter);

        binding.spinnerDivision.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDivision = divisions.get(position);
                restaurantViewModel.setSelectedDivision(selectedDivision);
                updateDistrictSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        updateDistrictSpinner();
    }

    private void updateDistrictSpinner() {
        List<String> districts = restaurantViewModel.getDistrictsForCurrentDivision();
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, districts);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDistrict.setAdapter(districtAdapter);

        binding.spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDistrict = districts.get(position);
                restaurantViewModel.setSelectedDistrict(selectedDistrict);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                restaurantViewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeRestaurants() {
        restaurantViewModel.getRestaurants().observe(this, restaurants -> {
            if (restaurants != null && !restaurants.isEmpty()) {
                restaurantAdapter.submitList(restaurants);
                binding.rvRestaurants.setVisibility(View.VISIBLE);
                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.tvRestaurantCount.setText(
                    getString(R.string.restaurant_count, restaurants.size()));
            } else {
                binding.rvRestaurants.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                binding.tvRestaurantCount.setText(getString(R.string.restaurant_count, 0));
            }
        });
    }

    private void setupBottomNavigation() {
        binding.btnLogout.setOnClickListener(v -> logout());
        
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_refund_history) {
                Intent refundIntent = new Intent(this, RefundHistoryActivity.class);
                startActivity(refundIntent);
                return true;
            } else if (itemId == R.id.nav_search) {
                binding.etSearch.requestFocus();
                return true;
            } else if (itemId == R.id.nav_orders) {
                Intent ordersIntent = new Intent(this, UserOrderHistoryActivity.class);
                startActivity(ordersIntent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent profileIntent = new Intent(this, com.example.foodbikeandroid.ui.profile.UserProfileActivity.class);
                startActivity(profileIntent);
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
