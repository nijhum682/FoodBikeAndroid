package com.example.foodbikeandroid.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.foodbikeandroid.data.database.FoodBikeDatabase;
import com.example.foodbikeandroid.data.database.OrderDao;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.model.OrderStatus;

import java.util.List;

public class AutoCancelOrderWorker extends Worker {
    private static final String TAG = "AutoCancelOrderWorker";
    private static final long ONE_HOUR_MILLIS = 60 * 60 * 1000;
    public AutoCancelOrderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "Starting auto-cancel job");
            
            FoodBikeDatabase database = FoodBikeDatabase.getInstance(getApplicationContext());
            OrderDao orderDao = database.orderDao();
            
            List<Order> pendingOrders = orderDao.getOrdersByStatusSync(OrderStatus.PENDING);
            long currentTime = System.currentTimeMillis();
            int cancelledCount = 0;
            
            for (Order order : pendingOrders) {
                long orderAge = currentTime - order.getCreatedAt();
                
                if (orderAge > ONE_HOUR_MILLIS) {
                    order.setStatus(OrderStatus.AUTO_CANCELLED);
                    orderDao.updateOrder(order);
                    cancelledCount++;
                    Log.d(TAG, "Auto-cancelled order: " + order.getOrderId());
                }
            }
            
            Log.d(TAG, "Auto-cancel job completed. Cancelled " + cancelledCount + " orders");
            return Result.success();
            
        } catch (Exception e) {
            Log.e(TAG, "Error during auto-cancel job", e);
            return Result.retry();
        }
    }
}
