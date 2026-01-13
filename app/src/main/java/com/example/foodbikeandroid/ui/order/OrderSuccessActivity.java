package com.example.foodbikeandroid.ui.order;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodbikeandroid.databinding.ActivityOrderSuccessBinding;
import com.example.foodbikeandroid.ui.dashboard.UserDashboardActivity;

public class OrderSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "order_id";
    
    private ActivityOrderSuccessBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        
        if (orderId != null) {
            binding.tvOrderId.setText(orderId);
        }

        binding.btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        binding.btnTrackOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.foodbikeandroid.ui.dashboard.UserOrderHistoryActivity.class);
            if (orderId != null) {
                intent.putExtra("ORDER_ID_FILTER", orderId);
            }
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, UserDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
