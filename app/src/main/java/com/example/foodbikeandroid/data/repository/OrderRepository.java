package com.example.foodbikeandroid.data.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.foodbikeandroid.data.database.FoodBikeDatabase;
import com.example.foodbikeandroid.data.database.OrderDao;
import com.example.foodbikeandroid.data.database.UserDao;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.model.OrderStatus;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderRepository {

    private final OrderDao orderDao;
    private final UserDao userDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private static final long AUTO_CANCEL_CHECK_INTERVAL = 5 * 60 * 1000;
    private static final long ONE_HOUR_MILLIS = 60 * 60 * 1000;

    public OrderRepository(Application application) {
        FoodBikeDatabase database = FoodBikeDatabase.getInstance(application);
        orderDao = database.orderDao();
        userDao = database.userDao();
        executorService = Executors.newFixedThreadPool(4);
        mainHandler = new Handler(Looper.getMainLooper());
        startAutoCancelChecker();
    }

    public void insertOrder(Order order, OrderCallback callback) {
        executorService.execute(() -> {
            try {
                orderDao.insertOrder(order);
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSuccess(order);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError("Failed to save order: " + e.getMessage());
                    }
                });
            }
        });
    }

    public void updateOrder(Order order) {
        executorService.execute(() -> orderDao.updateOrder(order));
    }

    public void updateOrderStatus(String orderId, OrderStatus status) {
        executorService.execute(() -> orderDao.updateOrderStatus(orderId, status));
    }

    public void updateOrderStatus(String orderId, OrderStatus status, StatusUpdateCallback callback) {
        executorService.execute(() -> {
            try {
                if (status == OrderStatus.READY) {
                    orderDao.clearBikerAndSetReady(orderId);
                } else {
                    orderDao.updateOrderStatus(orderId, status);
                }
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void assignBiker(String orderId, String bikerId) {
        executorService.execute(() -> orderDao.assignBiker(orderId, bikerId));
    }

    public void tryAcceptOrder(String orderId, String bikerId, AcceptOrderCallback callback) {
        executorService.execute(() -> {
            try {
                long timestamp = System.currentTimeMillis();
                int rowsUpdated = orderDao.tryAcceptOrderAtomic(orderId, bikerId, OrderStatus.PREPARING, timestamp);
                
                if (rowsUpdated > 0) {
                    callback.onSuccess();
                } else {
                    callback.onAlreadyTaken();
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public LiveData<Order> getOrderById(String orderId) {
        return orderDao.getOrderById(orderId);
    }
    
    public void getOrderById(String orderId, OrderCallback callback) {
        executorService.execute(() -> {
            try {
                Order order = orderDao.getOrderByIdSync(orderId);
                if (order != null) {
                    mainHandler.post(() -> callback.onSuccess(order));
                } else {
                    mainHandler.post(() -> callback.onError("Order not found"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public LiveData<List<Order>> getOrdersByUserId(String userId) {
        return orderDao.getOrdersByUserId(userId);
    }

    public LiveData<List<Order>> getOrdersByRestaurantId(String restaurantId) {
        return orderDao.getOrdersByRestaurantId(restaurantId);
    }

    public LiveData<List<Order>> getOrdersByBikerId(String bikerId) {
        return orderDao.getOrdersByBikerId(bikerId);
    }

    public LiveData<List<Order>> getActiveOrdersByBiker(String bikerId) {
        return orderDao.getActiveOrdersByBiker(bikerId);
    }

    public LiveData<List<Order>> getCompletedOrdersByBiker(String bikerId) {
        return orderDao.getCompletedOrdersByBiker(bikerId);
    }

    public void updateOrderStatusToReady(String orderId, StatusUpdateCallback callback) {
        executorService.execute(() -> {
            try {
                Order order = orderDao.getOrderByIdSync(orderId);
                orderDao.updateOrderStatusToDelivered(orderId, OrderStatus.DELIVERED, System.currentTimeMillis());
                
                if (order != null && order.getBikerId() != null) {
                    userDao.addEarnings(order.getBikerId(), 50.0);
                }
                
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void updateOrderStatusToDelivered(String orderId, StatusUpdateCallback callback) {
        executorService.execute(() -> {
            try {
                Order order = orderDao.getOrderByIdSync(orderId);
                orderDao.updateOrderStatusToDelivered(orderId, OrderStatus.DELIVERED, System.currentTimeMillis());
                
                if (order != null && order.getBikerId() != null) {
                    userDao.addEarnings(order.getBikerId(), 50.0);
                }
                
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public LiveData<List<Order>> getOrdersByStatus(OrderStatus status) {
        return orderDao.getOrdersByStatus(status);
    }

    public LiveData<List<Order>> getOrdersByDistrictAndStatus(String district, OrderStatus status) {
        return orderDao.getOrdersByDistrictAndStatus(district, status);
    }

    public LiveData<List<Order>> getAllOrders() {
        return orderDao.getAllOrders();
    }

    public LiveData<Integer> getTodayOrderCount(String restaurantId) {
        return orderDao.getTodayOrderCountByRestaurant(restaurantId, getStartOfDay());
    }

    public LiveData<Double> getTodayRevenue(String restaurantId) {
        return orderDao.getTodayRevenueByRestaurant(restaurantId, getStartOfDay());
    }

    public LiveData<Integer> getTodayDeliveryCount(String bikerId) {
        return orderDao.getTodayDeliveryCountByBiker(bikerId, getStartOfDay());
    }

    public LiveData<List<Order>> getCompletedOrdersByBikerAfter(String bikerId, long startTime) {
        return orderDao.getCompletedOrdersByBikerAfter(bikerId, startTime);
    }

    public LiveData<Integer> getTotalDeliveryCount(String bikerId) {
        return orderDao.getTotalDeliveryCountByBiker(bikerId);
    }

    public LiveData<Integer> getDeliveryCountAfter(String bikerId, long startTime) {
        return orderDao.getDeliveryCountByBikerAfter(bikerId, startTime);
    }

    public LiveData<Long> getAverageDeliveryTime(String bikerId) {
        return orderDao.getAverageDeliveryTimeByBiker(bikerId);
    }

    public LiveData<Double> getTotalDeliveryValue(String bikerId) {
        return orderDao.getTotalDeliveryValueByBiker(bikerId);
    }

    public LiveData<Double> getDeliveryValueAfter(String bikerId, long startTime) {
        return orderDao.getDeliveryValueByBikerAfter(bikerId, startTime);
    }

    public void exportDeliveryHistory(String bikerId, ExportCallback callback) {
        executorService.execute(() -> {
            try {
                List<Order> orders = orderDao.getCompletedOrdersByBikerSync(bikerId);
                mainHandler.post(() -> callback.onSuccess(orders));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public long getStartOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public long getStartOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private void startAutoCancelChecker() {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAndAutoCancelOrders();
                mainHandler.postDelayed(this, AUTO_CANCEL_CHECK_INTERVAL);
            }
        }, AUTO_CANCEL_CHECK_INTERVAL);
    }

    private void checkAndAutoCancelOrders() {
        executorService.execute(() -> {
            long threshold = System.currentTimeMillis() - ONE_HOUR_MILLIS;
            List<Order> pendingOrders = orderDao.getPendingOrdersOlderThan(threshold);
            for (Order order : pendingOrders) {
                orderDao.updateOrderStatus(order.getOrderId(), OrderStatus.AUTO_CANCELLED);
            }
        });
    }

    public interface OrderCallback {
        void onSuccess(Order order);
        void onError(String error);
    }

    public interface AcceptOrderCallback {
        void onSuccess();
        void onAlreadyTaken();
        void onError(String error);
    }

    public interface StatusUpdateCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface ExportCallback {
        void onSuccess(List<Order> orders);
        void onError(String error);
    }
}
