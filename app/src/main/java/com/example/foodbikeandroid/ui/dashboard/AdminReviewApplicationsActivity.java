package com.example.foodbikeandroid.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.ApplicationStatus;
import com.example.foodbikeandroid.databinding.ActivityAdminReviewApplicationsBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class AdminReviewApplicationsActivity extends AppCompatActivity {

    private ActivityAdminReviewApplicationsBinding binding;

    private static final String[] TAB_TITLES = {"Pending", "Approved", "Rejected"};
    private static final ApplicationStatus[] TAB_STATUSES = {
            ApplicationStatus.PENDING,
            ApplicationStatus.APPROVED,
            ApplicationStatus.REJECTED
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminReviewApplicationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupViewPager();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewPager() {
        ApplicationsPagerAdapter pagerAdapter = new ApplicationsPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])
        ).attach();
    }

    private class ApplicationsPagerAdapter extends FragmentStateAdapter {

        public ApplicationsPagerAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return ApplicationListFragment.newInstance(TAB_STATUSES[position]);
        }

        @Override
        public int getItemCount() {
            return TAB_TITLES.length;
        }
    }
}
