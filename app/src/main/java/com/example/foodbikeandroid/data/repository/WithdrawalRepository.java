package com.example.foodbikeandroid.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.foodbikeandroid.data.database.FoodBikeDatabase;
import com.example.foodbikeandroid.data.database.WithdrawalDao;
import com.example.foodbikeandroid.data.model.Withdrawal;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WithdrawalRepository {

    private final WithdrawalDao withdrawalDao;
    private final ExecutorService executorService;

    public WithdrawalRepository(Application application) {
        FoodBikeDatabase database = FoodBikeDatabase.getInstance(application);
        withdrawalDao = database.withdrawalDao();
        executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<Withdrawal>> getAllWithdrawals() {
        return withdrawalDao.getAllWithdrawals();
    }

    public LiveData<Double> getTotalWithdrawnAmount() {
        return withdrawalDao.getTotalWithdrawnAmount();
    }

    public void insert(Withdrawal withdrawal, Runnable onSuccess, Runnable onError) {
        executorService.execute(() -> {
            try {
                withdrawalDao.insert(withdrawal);
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } catch (Exception e) {
                if (onError != null) {
                    onError.run();
                }
            }
        });
    }
}
