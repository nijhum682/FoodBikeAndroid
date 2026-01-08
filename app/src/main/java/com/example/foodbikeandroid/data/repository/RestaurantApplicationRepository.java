package com.example.foodbikeandroid.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.foodbikeandroid.data.database.FoodBikeDatabase;
import com.example.foodbikeandroid.data.database.RestaurantApplicationDao;
import com.example.foodbikeandroid.data.model.ApplicationStatus;
import com.example.foodbikeandroid.data.model.RestaurantApplication;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RestaurantApplicationRepository {

    private static volatile RestaurantApplicationRepository INSTANCE;
    private final RestaurantApplicationDao restaurantApplicationDao;
    private final ExecutorService executorService;

    private RestaurantApplicationRepository(Context context) {
        FoodBikeDatabase database = FoodBikeDatabase.getInstance(context);
        restaurantApplicationDao = database.restaurantApplicationDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public static RestaurantApplicationRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (RestaurantApplicationRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RestaurantApplicationRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public void insert(RestaurantApplication application) {
        executorService.execute(() -> restaurantApplicationDao.insert(application));
    }

    public void update(RestaurantApplication application) {
        executorService.execute(() -> restaurantApplicationDao.update(application));
    }

    public LiveData<List<RestaurantApplication>> getByEntrepreneur(String username) {
        return restaurantApplicationDao.getByEntrepreneur(username);
    }

    public LiveData<List<RestaurantApplication>> getByStatus(ApplicationStatus status) {
        return restaurantApplicationDao.getByStatus(status);
    }

    public LiveData<List<RestaurantApplication>> getAll() {
        return restaurantApplicationDao.getAll();
    }

    public void deleteById(String applicationId) {
        executorService.execute(() -> restaurantApplicationDao.deleteById(applicationId));
    }
}
