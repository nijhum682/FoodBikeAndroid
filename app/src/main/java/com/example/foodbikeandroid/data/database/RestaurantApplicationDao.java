package com.example.foodbikeandroid.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.foodbikeandroid.data.model.ApplicationStatus;
import com.example.foodbikeandroid.data.model.RestaurantApplication;

import java.util.List;

@Dao
public interface RestaurantApplicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RestaurantApplication application);

    @Update
    void update(RestaurantApplication application);

    @Query("SELECT * FROM restaurant_applications WHERE applicationId = :applicationId")
    RestaurantApplication getById(String applicationId);

    @Query("SELECT * FROM restaurant_applications WHERE entrepreneurUsername = :username ORDER BY appliedDate DESC")
    LiveData<List<RestaurantApplication>> getByEntrepreneur(String username);

    @Query("SELECT * FROM restaurant_applications WHERE status = :status ORDER BY appliedDate DESC")
    LiveData<List<RestaurantApplication>> getByStatus(ApplicationStatus status);

    @Query("SELECT * FROM restaurant_applications ORDER BY appliedDate DESC")
    LiveData<List<RestaurantApplication>> getAll();

    @Query("DELETE FROM restaurant_applications WHERE applicationId = :applicationId")
    void deleteById(String applicationId);
}
