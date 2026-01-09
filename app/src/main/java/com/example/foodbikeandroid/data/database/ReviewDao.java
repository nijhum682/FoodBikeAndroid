package com.example.foodbikeandroid.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.foodbikeandroid.data.model.Review;

import java.util.List;

@Dao
public interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Review review);

    @Query("SELECT * FROM reviews WHERE restaurantId = :restaurantId ORDER BY createdAt DESC")
    LiveData<List<Review>> getByRestaurant(String restaurantId);

    @Query("SELECT * FROM reviews WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<Review>> getByUser(String userId);

    @Query("SELECT AVG(rating) FROM reviews WHERE restaurantId = :restaurantId")
    Double getAverageRating(String restaurantId);

    @Query("SELECT COUNT(*) FROM reviews WHERE orderId = :orderId")
    int checkReviewExists(String orderId);

    @Query("SELECT * FROM reviews WHERE orderId = :orderId LIMIT 1")
    Review getByOrderId(String orderId);

    @Query("SELECT COUNT(*) FROM reviews WHERE restaurantId = :restaurantId")
    int getReviewCount(String restaurantId);

    @Query("SELECT * FROM reviews WHERE restaurantId = :restaurantId ORDER BY createdAt DESC")
    List<Review> getByRestaurantNewest(String restaurantId);

    @Query("SELECT * FROM reviews WHERE restaurantId = :restaurantId ORDER BY rating DESC, createdAt DESC")
    List<Review> getByRestaurantHighestRated(String restaurantId);

    @Query("SELECT * FROM reviews WHERE restaurantId = :restaurantId ORDER BY rating ASC, createdAt DESC")
    List<Review> getByRestaurantLowestRated(String restaurantId);

    @Query("SELECT * FROM reviews WHERE restaurantId = :restaurantId ORDER BY createdAt DESC")
    LiveData<List<Review>> getByRestaurantNewestLiveData(String restaurantId);

    @Query("SELECT * FROM reviews WHERE restaurantId = :restaurantId ORDER BY rating DESC, createdAt DESC")
    LiveData<List<Review>> getByRestaurantHighestRatedLiveData(String restaurantId);

    @Query("SELECT * FROM reviews WHERE restaurantId = :restaurantId ORDER BY rating ASC, createdAt DESC")
    LiveData<List<Review>> getByRestaurantLowestRatedLiveData(String restaurantId);
}
