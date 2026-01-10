package com.example.foodbikeandroid.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodbikeandroid.data.model.User;
import com.example.foodbikeandroid.databinding.ActivityUserProfileBinding;

public class UserProfileActivity extends AppCompatActivity {

    private ActivityUserProfileBinding binding;
    private ProfileViewModel viewModel;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupToolbar();
        observeUser();
        setupSaveButton();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void observeUser() {
        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                currentUser = user;
                populateUserData(user);
            }
        });

        viewModel.getUpdateMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage();
            }
        });

        viewModel.getIsUpdateSuccess().observe(this, isSuccess -> {
            if (isSuccess) {
                finish(); // Go back to dashboard on successful update
            }
        });
    }

    private void populateUserData(User user) {
        binding.tvHeaderUsername.setText(user.getUsername());
        binding.etUsername.setText(user.getUsername());
        binding.etEmail.setText(user.getEmail());
        binding.etPhone.setText(user.getPhoneNumber());
        binding.etAddress.setText(user.getAddress());
        binding.etPassword.setText(user.getPassword());
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> {
            if (currentUser == null) return;

            String email = getText(binding.etEmail);
            String phone = getText(binding.etPhone);
            String address = getText(binding.etAddress);
            String password = getText(binding.etPassword);

            viewModel.updateUser(currentUser, email, phone, address, password);
        });
    }

    private String getText(EditText editText) {
        return editText.getText().toString().trim();
    }
}
