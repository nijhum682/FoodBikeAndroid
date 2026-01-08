package com.example.foodbikeandroid.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.foodbikeandroid.data.database.Converters;

import java.util.List;

@Entity(tableName = "orders")
@TypeConverters(Converters.class)
public class Order {

    @PrimaryKey
    @NonNull
    private String orderId;

    @NonNull
    private String userId;

    @NonNull
    private String restaurantId;

    @NonNull
    private String district;

    private List<CartItem> items;

    private double totalPrice;

    @NonNull
    private OrderStatus status;

    private long createdAt;

    private String bikerId;

    @NonNull
    private PaymentMethod paymentMethod;

    public Order(@NonNull String userId, @NonNull String restaurantId, @NonNull String district,
                 List<CartItem> items, double totalPrice, @NonNull PaymentMethod paymentMethod) {
        this.orderId = "ORD_" + System.currentTimeMillis();
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.district = district;
        this.items = items;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.PENDING;
        this.createdAt = System.currentTimeMillis();
        this.paymentMethod = paymentMethod;
    }

    @NonNull
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(@NonNull String orderId) {
        this.orderId = orderId;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(@NonNull String restaurantId) {
        this.restaurantId = restaurantId;
    }

    @NonNull
    public String getDistrict() {
        return district;
    }

    public void setDistrict(@NonNull String district) {
        this.district = district;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    @NonNull
    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(@NonNull OrderStatus status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getBikerId() {
        return bikerId;
    }

    public void setBikerId(String bikerId) {
        this.bikerId = bikerId;
    }

    @NonNull
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(@NonNull PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isPending() {
        return status == OrderStatus.PENDING;
    }

    public boolean shouldAutoCancelCheck() {
        long oneHourInMillis = 60 * 60 * 1000;
        return isPending() && (System.currentTimeMillis() - createdAt) > oneHourInMillis;
    }
}
