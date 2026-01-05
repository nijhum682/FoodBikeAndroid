package com.example.foodbikeandroid.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodbikeandroid.data.model.UserType;
import com.example.foodbikeandroid.databinding.ActivitySignUpBinding;

/**
 * Activity for user registration.
 */
public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private AuthViewModel authViewModel;
    private UserType selectedUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupUserTypeDropdown();
        setupClickListeners();
        observeViewModel();
    }

    private void setupUserTypeDropdown() {
        String[] userTypes = new String[]{
                UserType.REGULAR_USER.getDisplayName(),
                UserType.ENTREPRENEUR.getDisplayName(),
                UserType.DELIVERY_BIKER.getDisplayName()
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                userTypes
        );

        binding.actvUserType.setAdapter(adapter);
        binding.actvUserType.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            selectedUserType = UserType.fromDisplayName(selected);
        });
    }

    private void setupClickListeners() {
        binding.btnSignUp.setOnClickListener(v -> attemptSignUp());
        binding.btnGoToSignIn.setOnClickListener(v -> navigateToSignIn());
    }

    private void attemptSignUp() {
        String username = binding.etUsername.getText().toString();
        String email = binding.etEmail.getText().toString();
        String phone = binding.etPhone.getText().toString();
        String password = binding.etPassword.getText().toString();
        String confirmPassword = binding.etConfirmPassword.getText().toString();

        authViewModel.register(username, password, confirmPassword, email, phone, selectedUserType);
    }

    private void observeViewModel() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnSignUp.setEnabled(!isLoading);
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

    private void navigateToSignIn() {
        Intent intent = new Intent(this, SignInActivity.class);
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
