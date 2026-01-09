package com.example.foodbikeandroid.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.model.OrderStatus;

import java.util.List;

@Dao
public interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrder(Order order);

    @Update
    void updateOrder(Order order);

    @Delete
    void deleteOrder(Order order);

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    LiveData<Order> getOrderById(String orderId);

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    Order getOrderByIdSync(String orderId);

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<Order>> getOrdersByUserId(String userId);

    @Query("SELECT * FROM orders WHERE restaurantId = :restaurantId ORDER BY createdAt DESC")
    LiveData<List<Order>> getOrdersByRestaurantId(String restaurantId);

    @Query("SELECT * FROM orders WHERE bikerId = :bikerId ORDER BY createdAt DESC")
    LiveData<List<Order>> getOrdersByBikerId(String bikerId);

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY createdAt DESC")
    LiveData<List<Order>> getOrdersByStatus(OrderStatus status);

    @Query("SELECT * FROM orders WHERE district = :district AND status = :status ORDER BY createdAt DESC")
    LiveData<List<Order>> getOrdersByDistrictAndStatus(String district, OrderStatus status);

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    LiveData<List<Order>> getAllOrders();

    @Query("SELECT * FROM orders WHERE status = 'PENDING' AND createdAt < :threshold")
    List<Order> getPendingOrdersOlderThan(long threshold);

    @Query("UPDATE orders SET status = :status WHERE orderId = :orderId")
    void updateOrderStatus(String orderId, OrderStatus status);

    @Query("UPDATE orders SET bikerId = :bikerId WHERE orderId = :orderId")
    void assignBiker(String orderId, String bikerId);

    @Query("SELECT COUNT(*) FROM orders WHERE restaurantId = :restaurantId AND createdAt >= :startOfDay")
    LiveData<Integer> getTodayOrderCountByRestaurant(String restaurantId, long startOfDay);

    @Query("SELECT SUM(totalPrice) FROM orders WHERE restaurantId = :restaurantId AND createdAt >= :startOfDay AND status != 'CANCELLED' AND status != 'AUTO_CANCELLED'")
    LiveData<Double> getTodayRevenueByRestaurant(String restaurantId, long startOfDay);

    @Query("SELECT COUNT(*) FROM orders WHERE bikerId = :bikerId AND createdAt >= :startOfDay AND status = 'DELIVERED'")
    LiveData<Integer> getTodayDeliveryCountByBiker(String bikerId, long startOfDay);

    @Query("SELECT COUNT(*) FROM orders WHERE createdAt >= :timestamp")
    int getOrderCountAfterTimestamp(long timestamp);

    @Query("SELECT * FROM orders WHERE bikerId = :bikerId AND (status = 'PREPARING' OR status = 'READY') ORDER BY createdAt DESC")
    LiveData<List<Order>> getActiveOrdersByBiker(String bikerId);

    @Query("SELECT * FROM orders WHERE bikerId = :bikerId AND status = 'DELIVERED' ORDER BY deliveredAt DESC")
    LiveData<List<Order>> getCompletedOrdersByBiker(String bikerId);

    @Query("UPDATE orders SET status = :status, readyAt = :timestamp WHERE orderId = :orderId")
    void updateOrderStatusToReady(String orderId, OrderStatus status, long timestamp);

    @Query("UPDATE orders SET status = :status, deliveredAt = :timestamp WHERE orderId = :orderId")
    void updateOrderStatusToDelivered(String orderId, OrderStatus status, long timestamp);

    @Query("SELECT * FROM orders WHERE bikerId = :bikerId AND status = 'DELIVERED' AND deliveredAt >= :startTime ORDER BY deliveredAt DESC")
    LiveData<List<Order>> getCompletedOrdersByBikerAfter(String bikerId, long startTime);

    @Query("SELECT COUNT(*) FROM orders WHERE bikerId = :bikerId AND status = 'DELIVERED'")
    LiveData<Integer> getTotalDeliveryCountByBiker(String bikerId);

    @Query("SELECT COUNT(*) FROM orders WHERE bikerId = :bikerId AND status = 'DELIVERED' AND deliveredAt >= :startTime")
    LiveData<Integer> getDeliveryCountByBikerAfter(String bikerId, long startTime);

    @Query("SELECT AVG(deliveredAt - acceptedAt) FROM orders WHERE bikerId = :bikerId AND status = 'DELIVERED' AND acceptedAt > 0 AND deliveredAt > 0")
    LiveData<Long> getAverageDeliveryTimeByBiker(String bikerId);

    @Query("SELECT SUM(totalPrice) FROM orders WHERE bikerId = :bikerId AND status = 'DELIVERED'")
    LiveData<Double> getTotalDeliveryValueByBiker(String bikerId);

    @Query("SELECT SUM(totalPrice) FROM orders WHERE bikerId = :bikerId AND status = 'DELIVERED' AND deliveredAt >= :startTime")
    LiveData<Double> getDeliveryValueByBikerAfter(String bikerId, long startTime);

    @Query("SELECT * FROM orders WHERE bikerId = :bikerId AND status = 'DELIVERED' ORDER BY deliveredAt DESC")
    List<Order> getCompletedOrdersByBikerSync(String bikerId);
}
