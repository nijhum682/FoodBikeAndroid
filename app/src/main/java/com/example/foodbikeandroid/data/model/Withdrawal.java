package com.example.foodbikeandroid.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "withdrawals")
public class Withdrawal {
    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String username; // Admin or Biker username

    @NonNull
    private String userType; // "ADMIN" or "BIKER"

    private double amount;

    @NonNull
    private String method; // Bank, Bkash, Nagad

    @NonNull
    private String accountNumber;

    private long timestamp;

    public Withdrawal(@NonNull String username, @NonNull String userType, double amount, @NonNull String method, @NonNull String accountNumber) {
        this.id = "W_" + System.currentTimeMillis();
        this.username = username;
        this.userType = userType;
        this.amount = amount;
        this.method = method;
        this.accountNumber = accountNumber;
        this.timestamp = System.currentTimeMillis();
    }

    public Withdrawal() {
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getUserType() {
        return userType;
    }

    public void setUserType(@NonNull String userType) {
        this.userType = userType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @NonNull
    public String getMethod() {
        return method;
    }

    public void setMethod(@NonNull String method) {
        this.method = method;
    }

    @NonNull
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(@NonNull String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
