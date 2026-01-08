package com.example.foodbikeandroid.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.foodbikeandroid.data.database.Converters;

import java.util.List;

@Entity(tableName = "restaurants")
@TypeConverters(Converters.class)
public class Restaurant {

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String name;

    @NonNull
    private String division;

    @NonNull
    private String district;

    @NonNull
    private String address;

    private double rating;

    private List<MenuItem> menuItems;

    private String cuisineType;

    private String imageUrl;

    private boolean isOpen;

    private String openingHours;

    private long createdAt;

    public Restaurant(@NonNull String id, @NonNull String name, @NonNull String division,
                      @NonNull String district, @NonNull String address) {
        this.id = id;
        this.name = name;
        this.division = division;
        this.district = district;
        this.address = address;
        this.rating = 4.5;
        this.isOpen = true;
        this.openingHours = "9:00 AM - 10:00 PM";
        this.createdAt = System.currentTimeMillis();
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getDivision() {
        return division;
    }

    public void setDivision(@NonNull String division) {
        this.division = division;
    }

    @NonNull
    public String getDistrict() {
        return district;
    }

    public void setDistrict(@NonNull String district) {
        this.district = district;
    }

    @NonNull
    public String getAddress() {
        return address;
    }

    public void setAddress(@NonNull String address) {
        this.address = address;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getFullLocation() {
        return district + ", " + division;
    }
}
