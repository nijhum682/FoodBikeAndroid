package com.example.foodbikeandroid.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.foodbikeandroid.data.database.AdminActionDao;
import com.example.foodbikeandroid.data.database.FoodBikeDatabase;
import com.example.foodbikeandroid.data.model.AdminAction;
import com.example.foodbikeandroid.data.remote.FirestoreHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminActionRepository {

    private static volatile AdminActionRepository INSTANCE;
    private final AdminActionDao adminActionDao;
    private final FirestoreHelper firestoreHelper;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    private AdminActionRepository(Context context) {
        FoodBikeDatabase database = FoodBikeDatabase.getInstance(context);
        adminActionDao = database.adminActionDao();
        firestoreHelper = FirestoreHelper.getInstance();
        executorService = Executors.newFixedThreadPool(4);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static AdminActionRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AdminActionRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AdminActionRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public void insert(AdminAction action) {
        firestoreHelper.getAdminActionsCollection().document(action.getActionId()).set(action)
                .addOnSuccessListener(aVoid -> executorService.execute(() -> adminActionDao.insert(action)));
    }

    public void syncAdminActions() {
        firestoreHelper.getAdminActionsCollection().orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(100).get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots != null && !snapshots.isEmpty()) {
                        List<AdminAction> actions = snapshots.toObjects(AdminAction.class);
                        executorService.execute(() -> {
                            for (AdminAction a : actions) {
                                try {
                                    adminActionDao.insert(a);
                                } catch (Exception e) {
                                    // Ignore
                                }
                            }
                        });
                    }
                });
    }

    public LiveData<List<AdminAction>> getAllActions() {
        return adminActionDao.getAll();
    }

    public LiveData<List<AdminAction>> getRecentActions() {
        return adminActionDao.getAll();
    }
}
