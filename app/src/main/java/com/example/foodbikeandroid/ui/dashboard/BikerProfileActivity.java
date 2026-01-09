
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
        String username = authViewModel.getCurrentUsername();
        binding.tvProfileUsername.setText("Username: " + (username != null ? username : ""));
        binding.tvProfileEmail.setText("Email: demo@email.com");
        binding.tvProfilePhone.setText("Phone: 0123456789");
        binding.tvProfileUserType.setText("User Type: Biker");
        binding.tvProfileSignupDate.setText("Signup Date: 2026-01-10");
        binding.etProfileAddress.setText("Demo Address");
        binding.btnProfileSaveAddress.setOnClickListener(v -> {
            String newAddress = binding.etProfileAddress.getText().toString().trim();
            Toast.makeText(this, "Address updated!", Toast.LENGTH_SHORT).show();
        });
    }
}
