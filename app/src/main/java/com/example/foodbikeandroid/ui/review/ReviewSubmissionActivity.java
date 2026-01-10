package com.example.foodbikeandroid.ui.review;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.Review;
import com.example.foodbikeandroid.data.repository.RestaurantRepository;
import com.example.foodbikeandroid.data.repository.ReviewRepository;
import com.example.foodbikeandroid.databinding.ActivityReviewSubmissionBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;

public class ReviewSubmissionActivity extends AppCompatActivity {

    private ActivityReviewSubmissionBinding binding;
    private ReviewRepository reviewRepository;
    private RestaurantRepository restaurantRepository;
    private AuthViewModel authViewModel;

    private String orderId;
    private String restaurantId;
    private String restaurantName;
    private String userId;
    private int selectedRating = 0;
    private ImageView[] stars;
    private boolean isViewOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewSubmissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reviewRepository = ReviewRepository.getInstance(this);
        restaurantRepository = RestaurantRepository.getInstance(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        userId = authViewModel.getCurrentUsername();
        if (userId == null) {
            Toast.makeText(this, R.string.error_not_logged_in, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        orderId = getIntent().getStringExtra("ORDER_ID");
        restaurantId = getIntent().getStringExtra("RESTAURANT_ID");
        restaurantName = getIntent().getStringExtra("RESTAURANT_NAME");
        isViewOnly = getIntent().getBooleanExtra("VIEW_ONLY", false);

        if (orderId == null || restaurantId == null) {
            Toast.makeText(this, R.string.error_invalid_data, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupViews();
        setupStarRating();
        
        if (isViewOnly) {
            // Load and display existing review in read-only mode
            loadExistingReview();
        } else {
            setupSubmitButton();
            checkIfReviewExists();
        }
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViews() {
        binding.tvRestaurantName.setText(restaurantName != null ? restaurantName : getString(R.string.unknown_restaurant));
        binding.tvOrderId.setText(getString(R.string.order_id_format, orderId));
    }

    private void setupStarRating() {
        stars = new ImageView[]{
                binding.star1,
                binding.star2,
                binding.star3,
                binding.star4,
                binding.star5
        };

        for (int i = 0; i < stars.length; i++) {
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> setRating(rating));
        }
    }

    private void setRating(int rating) {
        selectedRating = rating;
        updateStarDisplay();
        updateRatingText();
        binding.btnSubmit.setEnabled(true);
    }

    private void updateStarDisplay() {
        int colorFilled = ContextCompat.getColor(this, R.color.warning);
        int colorEmpty = ContextCompat.getColor(this, R.color.text_hint);

        for (int i = 0; i < stars.length; i++) {
            stars[i].setColorFilter(i < selectedRating ? colorFilled : colorEmpty);
        }
    }

    private void updateRatingText() {
        String[] ratingTexts = {
                getString(R.string.tap_to_rate),
                getString(R.string.rating_poor),
                getString(R.string.rating_fair),
                getString(R.string.rating_good),
                getString(R.string.rating_very_good),
                getString(R.string.rating_excellent)
        };
        binding.tvRatingText.setText(ratingTexts[selectedRating]);
    }

    private void setupSubmitButton() {
        binding.btnSubmit.setOnClickListener(v -> submitReview());

        binding.etComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 500) {
                    binding.tilComment.setError(getString(R.string.comment_too_long));
                } else {
                    binding.tilComment.setError(null);
                }
            }
        });
    }

    private void checkIfReviewExists() {
        reviewRepository.checkReviewExists(orderId, exists -> {
            if (exists) {
                Toast.makeText(this, R.string.review_already_exists, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void loadExistingReview() {
        // Update toolbar title to indicate view mode
        binding.toolbar.setTitle(R.string.view_your_review);
        
        // Hide submit button in view-only mode
        binding.btnSubmit.setVisibility(android.view.View.GONE);
        
        // Disable the comment field
        binding.etComment.setEnabled(false);
        binding.etComment.setFocusable(false);
        
        // Disable star rating clicks
        for (ImageView star : stars) {
            star.setClickable(false);
        }
        
        // Fetch and display the existing review
        reviewRepository.getByOrder(orderId).observe(this, review -> {
            if (review != null) {
                // Set the rating
                selectedRating = review.getRating();
                updateStarDisplay();
                updateRatingText();
                
                // Set the comment
                if (review.getComment() != null && !review.getComment().isEmpty()) {
                    binding.etComment.setText(review.getComment());
                } else {
                    binding.tilComment.setVisibility(android.view.View.GONE);
                }
            } else {
                Toast.makeText(this, R.string.error_loading_reviews, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void submitReview() {
        if (selectedRating == 0) {
            Toast.makeText(this, R.string.please_select_rating, Toast.LENGTH_SHORT).show();
            return;
        }

        String comment = binding.etComment.getText() != null ? binding.etComment.getText().toString().trim() : null;
        if (comment != null && comment.isEmpty()) {
            comment = null;
        }

        if (comment != null && comment.length() > 500) {
            Toast.makeText(this, R.string.comment_too_long, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSubmit.setEnabled(false);

        Review review = new Review(restaurantId, userId, orderId, selectedRating, comment);

        reviewRepository.insert(review, new ReviewRepository.ReviewInsertCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ReviewSubmissionActivity.this, R.string.review_submitted, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ReviewSubmissionActivity.this, R.string.error_submitting_review, Toast.LENGTH_SHORT).show();
                binding.btnSubmit.setEnabled(true);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
