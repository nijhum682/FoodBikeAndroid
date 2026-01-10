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
    private String adminUsername;

    private double amount;

    @NonNull
    private String method; // Bank, Bkash, Nagad

    @NonNull
    private String accountNumber;

    private long timestamp;

    public Withdrawal(@NonNull String adminUsername, double amount, @NonNull String method, @NonNull String accountNumber) {
        this.id = "W_" + System.currentTimeMillis();
        this.adminUsername = adminUsername;
        this.amount = amount;
        this.method = method;
        this.accountNumber = accountNumber;
        this.timestamp = System.currentTimeMillis();
    }

    // Required for Firebase
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
    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(@NonNull String adminUsername) {
        this.adminUsername = adminUsername;
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
