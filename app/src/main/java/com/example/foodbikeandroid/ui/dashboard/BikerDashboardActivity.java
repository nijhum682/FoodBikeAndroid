package com.example.foodbikeandroid.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.databinding.ActivityBikerDashboardBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;
import com.example.foodbikeandroid.ui.auth.SignInActivity;

/**
 * Dashboard activity for Delivery Biker users.
 */
public class BikerDashboardActivity extends AppCompatActivity {

    private ActivityBikerDashboardBinding binding;
    private AuthViewModel authViewModel;
    private boolean isOnline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBikerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupToolbar();
        displayUserInfo();
        setupClickListeners();
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
        }
        return false;
    }

    private void displayUserInfo() {
        String username = authViewModel.getCurrentUsername();
        binding.tvUsername.setText(username != null ? username + " (Biker)" : "Biker");
    }

    private void setupClickListeners() {
        binding.btnLogout.setOnClickListener(v -> logout());
        
        binding.switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isOnline = isChecked;
            if (isChecked) {
                binding.tvStatus.setText(R.string.online);
                binding.tvStatus.setTextColor(getColor(R.color.success));
                Toast.makeText(this, "You are now online!", Toast.LENGTH_SHORT).show();
            } else {
                binding.tvStatus.setText(R.string.offline);
                binding.tvStatus.setTextColor(getColor(R.color.error));
                Toast.makeText(this, "You are now offline", Toast.LENGTH_SHORT).show();
            }
        });

        binding.cardFindDeliveries.setOnClickListener(v -> {
            Intent intent = new Intent(this, BikerAvailableOrdersActivity.class);
            startActivity(intent);
        });

        binding.cardMyDeliveries.setOnClickListener(v -> {
            Intent intent = new Intent(this, BikerMyDeliveriesActivity.class);
            startActivity(intent);
        });

        binding.cardDeliveryHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, BikerHistoryActivity.class);
            startActivity(intent);
        });

        binding.cardEarnings.setOnClickListener(v -> {
            Intent intent = new Intent(this, BikerHistoryActivity.class);
            startActivity(intent);
        });
        binding.cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, BikerProfileActivity.class);
            startActivity(intent);
        });
    }

    private void logout() {
        authViewModel.logout();
        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Profile info is now shown in BikerProfileActivity. No inline profile info in dashboard.
}
