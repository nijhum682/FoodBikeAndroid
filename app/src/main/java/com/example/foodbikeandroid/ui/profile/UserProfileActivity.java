package com.example.foodbikeandroid.ui.profile;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.User;
import com.example.foodbikeandroid.data.repository.UserRepository;
import com.example.foodbikeandroid.data.session.SessionManager;
import com.example.foodbikeandroid.databinding.ActivityUserProfileBinding;

public class UserProfileActivity extends AppCompatActivity {
    private ActivityUserProfileBinding binding;
    private SessionManager sessionManager;
    private UserRepository userRepository;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = SessionManager.getInstance(this);
        userRepository = UserRepository.getInstance(this);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Load user info
        String username = sessionManager.getUsername();
        if (username != null) {
            userRepository.getUserByUsername(username, new UserRepository.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    runOnUiThread(() -> {
                        currentUser = user;
                        binding.etName.setText(currentUser.getUsername());
                        binding.etEmail.setText(currentUser.getEmail());
                        binding.etPhone.setText(currentUser.getPhoneNumber());
                        binding.etAddress.setText(currentUser.getAddress());
                        binding.etPassword.setText(currentUser.getPassword());
                    });
                }
                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> Toast.makeText(UserProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
                }
            });
        }

        binding.btnUpdate.setOnClickListener(v -> {
            if (currentUser != null) {
                currentUser.setUsername(binding.etName.getText().toString().trim());
                currentUser.setEmail(binding.etEmail.getText().toString().trim());
                currentUser.setPhoneNumber(binding.etPhone.getText().toString().trim());
                currentUser.setAddress(binding.etAddress.getText().toString().trim());
                currentUser.setPassword(binding.etPassword.getText().toString().trim());
                userRepository.updateUser(currentUser, new UserRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(UserProfileActivity.this,"t", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> Toast.makeText(UserProfileActivity.this, message, Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });
    }
}
