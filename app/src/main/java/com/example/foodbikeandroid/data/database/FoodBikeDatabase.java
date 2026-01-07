package com.example.foodbikeandroid.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.foodbikeandroid.data.model.Restaurant;
import com.example.foodbikeandroid.data.model.User;

@Database(entities = {User.class, Restaurant.class}, version = 2, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class FoodBikeDatabase extends RoomDatabase {

    private static volatile FoodBikeDatabase INSTANCE;
    private static final String DATABASE_NAME = "foodbike_database";

    public abstract UserDao userDao();

    public abstract RestaurantDao restaurantDao();

    public static FoodBikeDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (FoodBikeDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            FoodBikeDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
