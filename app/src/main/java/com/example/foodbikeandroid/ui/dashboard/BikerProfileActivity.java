
package com.example.foodbikeandroid.ui.dashboard;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.databinding.ActivityBikerProfileBinding;
import com.example.foodbikeandroid.data.model.User;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;
import androidx.lifecycle.ViewModelProvider;

public class BikerProfileActivity extends AppCompatActivity {
    private ActivityBikerProfileBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBikerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Back button
        binding.ibBack.setOnClickListener(v -> finish());

        // Load User Data
        authViewModel.loadCurrentUser(new com.example.foodbikeandroid.data.repository.UserRepository.AuthCallback() {
            @Override
            public void onSuccess(com.example.foodbikeandroid.data.model.User user) {
                runOnUiThread(() -> {
                    // Start of UI update
                    binding.tvProfileUsername.setText(user.getUsername());
                    binding.tvProfileEmail.setText(user.getEmail());
                    binding.tvProfilePhone.setText(user.getPhoneNumber());
                    binding.tvProfileUserType.setText(user.getUserType().toString());

                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                    binding.tvProfileSignupDate.setText(sdf.format(new java.util.Date(user.getCreatedAt())));

                    binding.etProfileAddress.setText(user.getAddress());
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(BikerProfileActivity.this, "Error loading profile: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });

        binding.btnProfileSaveAddress.setOnClickListener(v -> {
            String newAddress = binding.etProfileAddress.getText().toString().trim();
            if(!newAddress.isEmpty()){
                // Ideally update user here, but providing feedback for now
                Toast.makeText(this, "Address updated (Implementation Pending)", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
