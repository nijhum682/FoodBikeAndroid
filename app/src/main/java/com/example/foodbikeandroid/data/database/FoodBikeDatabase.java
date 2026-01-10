package com.example.foodbikeandroid.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.model.Restaurant;
import com.example.foodbikeandroid.data.model.RestaurantApplication;
import com.example.foodbikeandroid.data.model.User;
import com.example.foodbikeandroid.data.model.AdminAction;
import com.example.foodbikeandroid.data.model.Review;

import com.example.foodbikeandroid.data.model.Withdrawal;

@Database(entities = {User.class, Restaurant.class, Order.class, RestaurantApplication.class, AdminAction.class, Review.class, Withdrawal.class}, version = 13, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class FoodBikeDatabase extends RoomDatabase {

    private static volatile FoodBikeDatabase INSTANCE;
    private static final String DATABASE_NAME = "foodbike_database";

    public abstract UserDao userDao();

    public abstract RestaurantDao restaurantDao();

    public abstract OrderDao orderDao();

    public abstract RestaurantApplicationDao restaurantApplicationDao();
    public abstract AdminActionDao adminActionDao();
    public abstract ReviewDao reviewDao();
    public abstract WithdrawalDao withdrawalDao();

    public static FoodBikeDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (FoodBikeDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            FoodBikeDatabase.class,
                            DATABASE_NAME
                    )
                    .addMigrations(MIGRATION_8_9, MIGRATION_12_13)
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    public static final androidx.room.migration.Migration MIGRATION_8_9 = new androidx.room.migration.Migration(8, 9) {
        @Override
        public void migrate(androidx.sqlite.db.SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users ADD COLUMN address TEXT NOT NULL DEFAULT ''");
        }
    };
    
    public static final androidx.room.migration.Migration MIGRATION_12_13 = new androidx.room.migration.Migration(12, 13) {
        @Override
        public void migrate(androidx.sqlite.db.SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `withdrawals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `adminUsername` TEXT NOT NULL, `amount` REAL NOT NULL, `method` TEXT NOT NULL, `accountNumber` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)");
        }
    };
}
