package com.example.foodbikeandroid.work;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class WorkManagerInitializer {
    
    private static final String AUTO_CANCEL_WORK_NAME = "auto_cancel_orders";
    
    public static void initialize(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();
        
        PeriodicWorkRequest autoCancelRequest = new PeriodicWorkRequest.Builder(
                AutoCancelOrderWorker.class,
                1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                AUTO_CANCEL_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                autoCancelRequest
        );
    }
}
