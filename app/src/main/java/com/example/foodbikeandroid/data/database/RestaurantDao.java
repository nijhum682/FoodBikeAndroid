package com.example.foodbikeandroid.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.foodbikeandroid.data.model.Restaurant;

import java.util.List;

@Dao
public interface RestaurantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Restaurant restaurant);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Restaurant> restaurants);

    @Update
    void update(Restaurant restaurant);

    @Delete
    void delete(Restaurant restaurant);

    @Query("SELECT * FROM restaurants ORDER BY rating DESC")
    LiveData<List<Restaurant>> getAllRestaurants();

    @Query("SELECT * FROM restaurants ORDER BY rating DESC")
    List<Restaurant> getAllRestaurantsSync();

    @Query("SELECT * FROM restaurants WHERE id = :id")
    LiveData<Restaurant> getRestaurantById(String id);

    @Query("SELECT * FROM restaurants WHERE id = :id")
    Restaurant getRestaurantByIdSync(String id);

    @Query("SELECT * FROM restaurants WHERE division = :division ORDER BY rating DESC")
    LiveData<List<Restaurant>> getRestaurantsByDivision(String division);

    @Query("SELECT * FROM restaurants WHERE district = :district ORDER BY rating DESC")
    LiveData<List<Restaurant>> getRestaurantsByDistrict(String district);

    @Query("SELECT * FROM restaurants WHERE division = :division AND district = :district ORDER BY rating DESC")
    LiveData<List<Restaurant>> getRestaurantsByLocation(String division, String district);

    @Query("SELECT * FROM restaurants WHERE name LIKE '%' || :query || '%' OR cuisineType LIKE '%' || :query || '%' ORDER BY rating DESC")
    LiveData<List<Restaurant>> searchRestaurants(String query);

    @Query("SELECT * FROM restaurants WHERE (name LIKE '%' || :query || '%' OR cuisineType LIKE '%' || :query || '%') AND division = :division ORDER BY rating DESC")
    LiveData<List<Restaurant>> searchRestaurantsInDivision(String query, String division);

    @Query("SELECT * FROM restaurants WHERE (name LIKE '%' || :query || '%' OR cuisineType LIKE '%' || :query || '%') AND division = :division AND district = :district ORDER BY rating DESC")
    LiveData<List<Restaurant>> searchRestaurantsInLocation(String query, String division, String district);

    @Query("SELECT * FROM restaurants WHERE cuisineType = :cuisineType ORDER BY rating DESC")
    LiveData<List<Restaurant>> getRestaurantsByCuisine(String cuisineType);

    @Query("SELECT * FROM restaurants WHERE isOpen = 1 ORDER BY rating DESC")
    LiveData<List<Restaurant>> getOpenRestaurants();

    @Query("SELECT COUNT(*) FROM restaurants")
    int getRestaurantCount();

    @Query("SELECT COUNT(*) FROM restaurants WHERE id LIKE :prefix || '%'")
    int getRestaurantCountByPrefix(String prefix);

    @Query("UPDATE restaurants SET rating = :rating WHERE id = :restaurantId")
    void updateRating(String restaurantId, double rating);

    @Query("UPDATE restaurants SET earnings = earnings + :amount WHERE id = :restaurantId")
    void addEarnings(String restaurantId, double amount);

    @Query("DELETE FROM restaurants")
    void deleteAll();
}
