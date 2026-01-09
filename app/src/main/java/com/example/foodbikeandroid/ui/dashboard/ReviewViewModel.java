package com.example.foodbikeandroid.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodbikeandroid.data.model.Review;
import com.example.foodbikeandroid.data.model.ReviewWithUserInfo;
import com.example.foodbikeandroid.data.repository.ReviewRepository;
import com.example.foodbikeandroid.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewViewModel extends AndroidViewModel {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ExecutorService executorService;

    private final MutableLiveData<String> restaurantId = new MutableLiveData<>();
    private final MutableLiveData<List<ReviewWithUserInfo>> reviewsWithUserInfo = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Double> averageRating = new MutableLiveData<>(0.0);
    private final MutableLiveData<Integer> reviewCount = new MutableLiveData<>(0);
    private final MutableLiveData<String> sortOrder = new MutableLiveData<>("newest"); // newest, highest, lowest
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> emptyMessage = new MutableLiveData<>("No reviews yet");

    // Cache for user data
    private Map<String, String> userCache;

    public ReviewViewModel(@NonNull Application application) {
        super(application);
        this.reviewRepository = ReviewRepository.getInstance(application);
        this.userRepository = UserRepository.getInstance(application);
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public void setRestaurantId(String id) {
        if (!id.equals(restaurantId.getValue())) {
            restaurantId.setValue(id);
            loadReviews();
            loadAverageRating();
        }
    }

    public LiveData<List<ReviewWithUserInfo>> getReviewsWithUserInfo() {
        return reviewsWithUserInfo;
    }

    public LiveData<Double> getAverageRating() {
        return averageRating;
    }

    public LiveData<Integer> getReviewCount() {
        return reviewCount;
    }

    public LiveData<String> getSortOrder() {
        return sortOrder;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getEmptyMessage() {
        return emptyMessage;
    }

    public void setSortOrder(String order) {
        sortOrder.setValue(order);
        loadReviews();
    }

    private void loadReviews() {
        String resId = restaurantId.getValue();
        if (resId == null || resId.isEmpty()) {
            reviewsWithUserInfo.setValue(new ArrayList<>());
            return;
        }

        isLoading.setValue(true);
        executorService.execute(() -> {
            try {
                List<Review> reviews = getReviewsBySortOrder(resId);
                
                if (reviews == null || reviews.isEmpty()) {
                    reviewsWithUserInfo.postValue(new ArrayList<>());
                    emptyMessage.postValue("No reviews yet");
                } else {
                    List<ReviewWithUserInfo> enrichedReviews = new ArrayList<>();
                    for (Review review : reviews) {
                        String username = userRepository.getUsernameSync(review.getUserId());
                        if (username == null) {
                            username = "Anonymous";
                        }
                        enrichedReviews.add(new ReviewWithUserInfo(review, username));
                    }
                    reviewsWithUserInfo.postValue(enrichedReviews);
                }
            } catch (Exception e) {
                emptyMessage.postValue("Error loading reviews");
                reviewsWithUserInfo.postValue(new ArrayList<>());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    private List<Review> getReviewsBySortOrder(String restaurantId) {
        String order = sortOrder.getValue();
        if (order == null) order = "newest";

        switch (order) {
            case "highest":
                return reviewRepository.getByRestaurantHighestRatedSync(restaurantId);
            case "lowest":
                return reviewRepository.getByRestaurantLowestRatedSync(restaurantId);
            case "newest":
            default:
                return reviewRepository.getByRestaurantNewestSync(restaurantId);
        }
    }

    private void loadAverageRating() {
        String resId = restaurantId.getValue();
        if (resId == null || resId.isEmpty()) {
            averageRating.setValue(0.0);
            reviewCount.setValue(0);
            return;
        }

        reviewRepository.getAverageRating(resId, (rating, count) -> {
            averageRating.postValue(rating);
            reviewCount.postValue(count);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
