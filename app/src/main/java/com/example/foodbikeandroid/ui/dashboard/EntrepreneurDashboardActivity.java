package com.example.foodbikeandroid.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.databinding.ActivityEntrepreneurDashboardBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;
import com.example.foodbikeandroid.ui.auth.SignInActivity;
public class EntrepreneurDashboardActivity extends AppCompatActivity {

    private ActivityEntrepreneurDashboardBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEntrepreneurDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupToolbar();
        displayUserInfo();
        setupClickListeners();
        setupBottomNavigation();
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
        binding.cardManageMenu.setOnClickListener(v -> {
            Toast.makeText(this, "Manage Menu - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        binding.cardViewOrders.setOnClickListener(v -> {
            Toast.makeText(this, "View Orders - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        binding.cardRestaurantProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Restaurant Profile - Coming Soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.nav_menu) {
                Toast.makeText(this, "Menu - Coming Soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_orders) {
                Toast.makeText(this, "Orders - Coming Soon", Toast.LENGTH_SHORT).show();
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
