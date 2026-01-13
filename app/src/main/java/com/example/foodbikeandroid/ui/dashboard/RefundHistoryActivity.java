package com.example.foodbikeandroid.ui.dashboard;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.repository.OrderRepository;
import com.example.foodbikeandroid.databinding.ActivityRefundHistoryBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;

import java.util.List;

public class RefundHistoryActivity extends AppCompatActivity {

    private ActivityRefundHistoryBinding binding;
    private OrderRepository orderRepository;
    private AuthViewModel authViewModel;
    private UserOrderHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRefundHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderRepository = new OrderRepository(getApplication());
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupToolbar();
        setupRecyclerView();
        loadRefundHistory();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new UserOrderHistoryAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void loadRefundHistory() {
        String userId = authViewModel.getCurrentUsername();
        if (userId == null) {
            finish();
            return;
        }

        orderRepository.getRefundedOrdersByUserId(userId).observe(this, orders -> {
            if (orders != null && !orders.isEmpty()) {
                adapter.submitList(orders);
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.layoutEmpty.setVisibility(View.GONE);
            } else {
                binding.recyclerView.setVisibility(View.GONE);
                binding.layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
