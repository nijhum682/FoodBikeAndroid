package com.example.foodbikeandroid.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.foodbikeandroid.data.database.Converters;
@Entity(tableName = "users")
@TypeConverters(Converters.class)
public class User {

    @PrimaryKey
    @NonNull
    private String username;

    @NonNull
    private String password;

    @NonNull
    private String email;

    @NonNull
    private String phoneNumber;

    @NonNull
    private UserType userType;

    private long createdAt;

    public User(@NonNull String username, @NonNull String password, 
                @NonNull String email, @NonNull String phoneNumber, 
                @NonNull UserType userType) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userType = userType;
        this.createdAt = System.currentTimeMillis();
    }
    @NonNull
    public String getUsername() {
        return username;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    @NonNull
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @NonNull
    public UserType getUserType() {
        return userType;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    public void setPhoneNumber(@NonNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setUserType(@NonNull UserType userType) {
        this.userType = userType;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
