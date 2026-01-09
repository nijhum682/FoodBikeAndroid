package com.example.foodbikeandroid.ui.dashboard;

import android.os.Bundle;
import android.widget.TextView;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.database.AdminActionDao;
import com.example.foodbikeandroid.data.database.FoodBikeDatabase;

public class AdminEarningsActivity extends AppCompatActivity {
    private TextView tvTotalEarnings;
    private TextView tvActionCount;
    private AdminActionDao adminActionDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_earnings);
        tvTotalEarnings = findViewById(R.id.tvTotalEarnings);
        tvActionCount = findViewById(R.id.tvActionCount);
        adminActionDao = FoodBikeDatabase.getInstance(this).adminActionDao();
        adminActionDao.getActionCount().observe(this, count -> {
            int earnings = (count != null ? count : 0) * 10;
            tvTotalEarnings.setText("৳" + earnings);
            tvActionCount.setText(count + " actions");
        });
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
