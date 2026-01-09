package com.example.foodbikeandroid.ui.dashboard;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.databinding.ActivityReviewsListBinding;

import java.util.Locale;

public class ReviewsListActivity extends AppCompatActivity {

    public static final String EXTRA_RESTAURANT_ID = "restaurant_id";
    public static final String EXTRA_RESTAURANT_NAME = "restaurant_name";

    private ActivityReviewsListBinding binding;
    private ReviewViewModel viewModel;
    private ReviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewsListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ReviewViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupSortSpinner();
        setupObservers();

        // Get restaurant ID from intent
        String restaurantId = getIntent().getStringExtra(EXTRA_RESTAURANT_ID);
        String restaurantName = getIntent().getStringExtra(EXTRA_RESTAURANT_NAME);
        
        if (restaurantId != null) {
            viewModel.setRestaurantId(restaurantId);
        }
        
        if (restaurantName != null) {
            setTitle(getString(R.string.reviews) + " - " + restaurantName);
        } else {
            setTitle(getString(R.string.reviews));
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new ReviewAdapter(this);
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReviews.setAdapter(adapter);
    }

    private void setupSortSpinner() {
        binding.spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sortOrder;
                switch (position) {
                    case 1: // Highest Rated
                        sortOrder = "highest";
                        break;
                    case 2: // Lowest Rated
                        sortOrder = "lowest";
                        break;
                    case 0: // Newest
                    default:
                        sortOrder = "newest";
                        break;
                }
                viewModel.setSortOrder(sortOrder);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Default to newest
                viewModel.setSortOrder("newest");
            }
        });
    }

    private void setupObservers() {
        // Observe reviews with user info
        viewModel.getReviewsWithUserInfo().observe(this, reviews -> {
            if (reviews != null && !reviews.isEmpty()) {
                adapter.setReviews(reviews);
                binding.rvReviews.setVisibility(View.VISIBLE);
                binding.emptyState.setVisibility(View.GONE);
            } else {
                binding.rvReviews.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        });

        // Observe average rating
        viewModel.getAverageRating().observe(this, averageRating -> {
            if (averageRating != null) {
                binding.tvAverageRating.setText(String.format(Locale.getDefault(), "%.1f", averageRating));
                binding.rbAverageRating.setRating(averageRating.floatValue());
            }
        });

        // Observe review count
        viewModel.getReviewCount().observe(this, count -> {
            if (count != null) {
                String reviewCountText;
                if (count == 1) {
                    reviewCountText = getString(R.string.review_count, count);
                } else {
                    reviewCountText = getString(R.string.review_count_plural, count);
                }
                binding.tvReviewCount.setText(reviewCountText);
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                binding.progressLoading.setVisibility(View.VISIBLE);
            } else {
                binding.progressLoading.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
