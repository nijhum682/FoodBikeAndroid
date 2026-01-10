package com.example.foodbikeandroid.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.foodbikeandroid.data.database.FoodBikeDatabase;
import com.example.foodbikeandroid.data.database.RestaurantDao;
import com.example.foodbikeandroid.data.database.ReviewDao;
import com.example.foodbikeandroid.data.model.Review;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewRepository {

    private static volatile ReviewRepository INSTANCE;
    private final ReviewDao reviewDao;
    private final RestaurantDao restaurantDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    private ReviewRepository(Context context) {
        FoodBikeDatabase database = FoodBikeDatabase.getInstance(context);
        reviewDao = database.reviewDao();
        restaurantDao = database.restaurantDao();
        executorService = Executors.newFixedThreadPool(2);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static ReviewRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ReviewRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ReviewRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public void insert(Review review, ReviewInsertCallback callback) {
        executorService.execute(() -> {
            try {
                reviewDao.insert(review);
                
                // Update restaurant average rating
                Double avgRating = reviewDao.getAverageRating(review.getRestaurantId());
                if (avgRating != null) {
                    restaurantDao.updateRating(review.getRestaurantId(), avgRating);
                }
                
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
            }
        });
    }

    public LiveData<List<Review>> getByRestaurant(String restaurantId) {
        return reviewDao.getByRestaurant(restaurantId);
    }

    public LiveData<List<Review>> getByUser(String userId) {
        return reviewDao.getByUser(userId);
    }

    public LiveData<Review> getByOrder(String orderId) {
        return reviewDao.getByOrderIdLiveData(orderId);
    }

    public void checkReviewExists(String orderId, ReviewExistsCallback callback) {
        executorService.execute(() -> {
            int count = reviewDao.checkReviewExists(orderId);
            boolean exists = count > 0;
            mainHandler.post(() -> callback.onResult(exists));
        });
    }

    public void getAverageRating(String restaurantId, AverageRatingCallback callback) {
        executorService.execute(() -> {
            Double avgRating = reviewDao.getAverageRating(restaurantId);
            int reviewCount = reviewDao.getReviewCount(restaurantId);
            mainHandler.post(() -> callback.onResult(avgRating != null ? avgRating : 0.0, reviewCount));
        });
    }

    public List<Review> getByRestaurantNewestSync(String restaurantId) {
        return reviewDao.getByRestaurantNewest(restaurantId);
    }

    public List<Review> getByRestaurantHighestRatedSync(String restaurantId) {
        return reviewDao.getByRestaurantHighestRated(restaurantId);
    }

    public List<Review> getByRestaurantLowestRatedSync(String restaurantId) {
        return reviewDao.getByRestaurantLowestRated(restaurantId);
    }

    public LiveData<List<Review>> getByRestaurantNewestLiveData(String restaurantId) {
        return reviewDao.getByRestaurantNewestLiveData(restaurantId);
    }

    public LiveData<List<Review>> getByRestaurantHighestRatedLiveData(String restaurantId) {
        return reviewDao.getByRestaurantHighestRatedLiveData(restaurantId);
    }

    public LiveData<List<Review>> getByRestaurantLowestRatedLiveData(String restaurantId) {
        return reviewDao.getByRestaurantLowestRatedLiveData(restaurantId);
    }

    public interface ReviewInsertCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface ReviewExistsCallback {
        void onResult(boolean exists);
    }

    public interface AverageRatingCallback {
        void onResult(double averageRating, int reviewCount);
    }
}
