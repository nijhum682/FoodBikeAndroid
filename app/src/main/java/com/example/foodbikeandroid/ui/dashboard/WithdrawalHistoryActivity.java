package com.example.foodbikeandroid.ui.dashboard;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.repository.WithdrawalRepository;
import com.google.android.material.appbar.MaterialToolbar;

public class WithdrawalHistoryActivity extends AppCompatActivity {

    private WithdrawalAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdrawal_history);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rvWithdrawals = findViewById(R.id.rvWithdrawals);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new WithdrawalAdapter();
        rvWithdrawals.setLayoutManager(new LinearLayoutManager(this));
        rvWithdrawals.setAdapter(adapter);

        WithdrawalRepository repository = new WithdrawalRepository(getApplication());
        repository.getAllWithdrawals().observe(this, withdrawals -> {
            if (withdrawals != null && !withdrawals.isEmpty()) {
                adapter.setWithdrawals(withdrawals);
                tvEmpty.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
}
