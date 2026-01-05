package com.example.foodbikeandroid.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.databinding.ActivityAdminDashboardBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;
import com.example.foodbikeandroid.ui.auth.SignInActivity;

/**
 * Dashboard activity for Admin users.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
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
        binding.cardManageUsers.setOnClickListener(v -> {
            Toast.makeText(this, "Manage Users - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        binding.cardViewReports.setOnClickListener(v -> {
            Toast.makeText(this, "View Reports - Coming Soon", Toast.LENGTH_SHORT).show();
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
