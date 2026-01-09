package com.example.foodbikeandroid.data.model;

import androidx.annotation.NonNull;

/**
 * Model combining Review data with User information
 * Used to display reviews with user details (username, avatar initial)
 */
public class ReviewWithUserInfo {
    
    private final Review review;
    private final String username;
    private final String userInitial;

    public ReviewWithUserInfo(@NonNull Review review, @NonNull String username) {
        this.review = review;
        this.username = username;
        // Extract first character as initial
        this.userInitial = username != null && !username.isEmpty() 
            ? String.valueOf(username.charAt(0)).toUpperCase() 
            : "?";
    }

    @NonNull
    public Review getReview() {
        return review;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @NonNull
    public String getUserInitial() {
        return userInitial;
    }

    public String getReviewId() {
        return review.getReviewId();
    }

    public String getRestaurantId() {
        return review.getRestaurantId();
    }

    public String getUserId() {
        return review.getUserId();
    }

    public int getRating() {
        return review.getRating();
    }

    public String getComment() {
        return review.getComment();
    }

    public long getCreatedAt() {
        return review.getCreatedAt();
    }

    public boolean hasComment() {
        return review.getComment() != null && !review.getComment().trim().isEmpty();
    }
}
