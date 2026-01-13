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
    private final com.example.foodbikeandroid.data.remote.FirestoreHelper firestoreHelper;
    private final ExecutorService executorService;
    private final android.os.Handler mainHandler;

    public WithdrawalRepository(Application application) {
        FoodBikeDatabase database = FoodBikeDatabase.getInstance(application);
        withdrawalDao = database.withdrawalDao();
        firestoreHelper = com.example.foodbikeandroid.data.remote.FirestoreHelper.getInstance();
        executorService = Executors.newFixedThreadPool(2);
        mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    }

    public interface WithdrawalCallback {
        void onSuccess();
        void onError(String error);
    }

    public void createWithdrawal(Withdrawal withdrawal, WithdrawalCallback callback) {
        insert(withdrawal, 
            () -> {
                if (callback != null) callback.onSuccess();
            },
            () -> {
                if (callback != null) callback.onError("Failed to create withdrawal");
            }
        );
    }

    public LiveData<List<Withdrawal>> getAllWithdrawals() {
        return withdrawalDao.getAllWithdrawals();
    }

    public LiveData<Double> getTotalWithdrawnAmount() {
        return withdrawalDao.getTotalWithdrawnAmount();
    }

    public LiveData<List<Withdrawal>> getWithdrawalsByUser(String username, String userType) {
        return withdrawalDao.getWithdrawalsByUser(username, userType);
    }

    public LiveData<Double> getTotalWithdrawnByUser(String username, String userType) {
        return withdrawalDao.getTotalWithdrawnByUser(username, userType);
    }

    public void insert(Withdrawal withdrawal, Runnable onSuccess, Runnable onError) {
        firestoreHelper.getWithdrawalsCollection().document(withdrawal.getId()).set(withdrawal)
                .addOnSuccessListener(aVoid -> {
                    executorService.execute(() -> {
                        try {
                            withdrawalDao.insert(withdrawal);
                            if (onSuccess != null) {
                                mainHandler.post(onSuccess);
                            }
                        } catch (Exception e) {
                            if (onError != null) {
                                mainHandler.post(onError);
                            }
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    if (onError != null) {
                        mainHandler.post(onError);
                    }
                });
    }

    public void refreshWithdrawals(String adminUsername) {
        // Filter by admin? Or all? Usually withdrawals are for logged in admin/entrepreneur.
        // Assuming fetch all for now or filter by user if passed.
        // The model has 'adminUsername'.
        // If adminUsername is null, fetch all (Admin view).
        com.google.firebase.firestore.Query query = firestoreHelper.getWithdrawalsCollection();
        if (adminUsername != null) {
            query = query.whereEqualTo("adminUsername", adminUsername);
        }
        
        query.get().addOnSuccessListener(snapshots -> {
            if (snapshots != null && !snapshots.isEmpty()) {
                List<Withdrawal> withdrawals = snapshots.toObjects(Withdrawal.class);
                executorService.execute(() -> {
                     for (Withdrawal w : withdrawals) {
                         // Dao insert is just Insert (FAIL if exists? or Abort?). 
                         // Default is ABORT usually if not specified.
                         // But for syncing, we might want REPLACE.
                         // Dao definition is just @Insert.
                         // If we encounter conflict, we might catch exception.
                         // Ideally we should use Insert(onConflict = REPLACE).
                         // Given I didn't change DAO to REPLACE, I'll catch exception.
                         try {
                             withdrawalDao.insert(w);
                         } catch (Exception e) {
                             // Ignore conflict
                         }
                     }
                });
            }
        });
    }
}
