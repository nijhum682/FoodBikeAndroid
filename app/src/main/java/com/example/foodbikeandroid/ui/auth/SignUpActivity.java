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
                UserType.DELIVERY_BIKER.getDisplayName(),
                UserType.ADMIN.getDisplayName()
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
        if (!validateInput()) {
            return;
        }

        String username = binding.etUsername.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String password = binding.etPassword.getText().toString();
        String confirmPassword = binding.etConfirmPassword.getText().toString();

        authViewModel.register(username, password, confirmPassword, email, phone, selectedUserType, address);
    }

    private boolean validateInput() {
        boolean isValid = true;
        
        String username = binding.etUsername.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String password = binding.etPassword.getText().toString();
        String confirmPassword = binding.etConfirmPassword.getText().toString();

        if (username.isEmpty()) {
            binding.etUsername.setError("Username is required");
            isValid = false;
        }

        if (email.isEmpty()) {
            binding.etEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Invalid email format");
            isValid = false;
        }

        if (phone.isEmpty()) {
            binding.etPhone.setError("Phone number is required");
            isValid = false;
        }

        if (address.isEmpty()) {
            binding.etAddress.setError("Address is required");
            isValid = false;
        }

        if (password.isEmpty()) {
            binding.etPassword.setError("Password is required");
            isValid = false;
        } else {
            // Password must contain uppercase, lowercase, number, and special symbol
            String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{6,}$";
            if (!password.matches(passwordPattern)) {
                binding.etPassword.setError("Password must contain at least 1 uppercase, 1 lowercase, 1 number, and 1 special character (@#$%^&+=!)");
                isValid = false;
            }
        }

        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.setError("Please confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        return isValid;
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
