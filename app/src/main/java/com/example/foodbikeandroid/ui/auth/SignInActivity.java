package com.example.foodbikeandroid.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodbikeandroid.data.model.UserType;
import com.example.foodbikeandroid.databinding.ActivitySignInBinding;

/**
 * Activity for user login.
 */
public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Check if already logged in
        if (authViewModel.isLoggedIn()) {
            navigateToDashboard();
            return;
        }

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.btnSignIn.setOnClickListener(v -> attemptSignIn());
        binding.btnGoToSignUp.setOnClickListener(v -> navigateToSignUp());
    }

    private void attemptSignIn() {
        String username = binding.etUsername.getText().toString();
        String password = binding.etPassword.getText().toString();

        authViewModel.login(username, password);
    }

    private void observeViewModel() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnSignIn.setEnabled(!isLoading);
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                authViewModel.clearError();
            }
        });

        authViewModel.getAuthState().observe(this, state -> {
            if (state == AuthViewModel.AuthState.AUTHENTICATED) {
                navigateToDashboard();
            }
        });
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToDashboard() {
        UserType userType = authViewModel.getCurrentUserType();
        Intent intent = NavigationHelper.getDashboardIntent(this, userType);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
