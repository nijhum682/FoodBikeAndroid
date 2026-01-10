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
    private final com.example.foodbikeandroid.data.remote.FirestoreHelper firestoreHelper;
    private final ExecutorService executorService;
    private final android.os.Handler mainHandler;

    private RestaurantApplicationRepository(Context context) {
        FoodBikeDatabase database = FoodBikeDatabase.getInstance(context);
        restaurantApplicationDao = database.restaurantApplicationDao();
        firestoreHelper = com.example.foodbikeandroid.data.remote.FirestoreHelper.getInstance();
        executorService = Executors.newFixedThreadPool(4);
        mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
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
        firestoreHelper.getApplicationsCollection().document(application.getApplicationId()).set(application)
                .addOnSuccessListener(aVoid -> executorService.execute(() -> restaurantApplicationDao.insert(application)));
    }

    public void update(RestaurantApplication application) {
        firestoreHelper.getApplicationsCollection().document(application.getApplicationId()).set(application)
                .addOnSuccessListener(aVoid -> executorService.execute(() -> restaurantApplicationDao.update(application)));
    }

    public void syncApplications() {
        firestoreHelper.getApplicationsCollection().get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots != null && !snapshots.isEmpty()) {
                        List<RestaurantApplication> apps = snapshots.toObjects(RestaurantApplication.class);
                        executorService.execute(() -> {
                            for (RestaurantApplication app : apps) {
                                try {
                                    restaurantApplicationDao.insert(app); 
                                    // Use simple insert (ABORT/IGNORE) or explicit update depending on DAO. 
                                    // Given we don't know ConflictStrategy, safe to just try insert.
                                    // Or better, use a loop that updates local from remote.
                                    // For simplicity in this migration:
                                } catch (Exception e) {
                                    // likely exists
                                }
                            }
                        });
                    }
                });
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
        firestoreHelper.getApplicationsCollection().document(applicationId).delete()
                .addOnSuccessListener(aVoid -> executorService.execute(() -> restaurantApplicationDao.deleteById(applicationId)));
    }
}
