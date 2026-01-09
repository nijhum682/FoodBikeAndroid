package com.example.foodbikeandroid.ui.dashboard;

import android.os.Bundle;
import android.text.format.DateFormat;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.database.AdminActionDao;
import com.example.foodbikeandroid.data.database.FoodBikeDatabase;
import com.example.foodbikeandroid.data.model.AdminAction;
import java.util.List;

public class AdminRecentActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminRecentActivityAdapter adapter;
    private AdminActionDao adminActionDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_recent_activity);
        recyclerView = findViewById(R.id.rvAdminRecentActivity);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminRecentActivityAdapter();
        recyclerView.setAdapter(adapter);
        adminActionDao = FoodBikeDatabase.getInstance(this).adminActionDao();
        adminActionDao.getAll().observe(this, actions -> {
            adapter.setActions(actions);
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
