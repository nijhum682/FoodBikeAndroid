package com.example.foodbikeandroid.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "reviews")
public class Review {

    @PrimaryKey
    @NonNull
    private String reviewId;

    @NonNull
    private String restaurantId;

    @NonNull
    private String userId;

    @NonNull
    private String orderId;

    private int rating; // 1-5 stars

    private String comment; // Optional

    private long createdAt;

    // Constructor for Room
    public Review(@NonNull String reviewId, @NonNull String restaurantId, @NonNull String userId,
                  @NonNull String orderId, int rating, String comment, long createdAt) {
        this.reviewId = reviewId;
        this.restaurantId = restaurantId;
        this.userId = userId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Convenience constructor for creating new reviews
    @Ignore
    public Review(@NonNull String restaurantId, @NonNull String userId, @NonNull String orderId,
                  int rating, String comment) {
        this.reviewId = "REV_" + System.currentTimeMillis();
        this.restaurantId = restaurantId;
        this.userId = userId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    @NonNull
    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(@NonNull String reviewId) {
        this.reviewId = reviewId;
    }

    @NonNull
    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(@NonNull String restaurantId) {
        this.restaurantId = restaurantId;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(@NonNull String orderId) {
        this.orderId = orderId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
