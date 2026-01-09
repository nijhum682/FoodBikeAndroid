package com.example.foodbikeandroid.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.foodbikeandroid.data.model.ActionType;
import com.example.foodbikeandroid.data.model.AdminAction;

import java.util.List;

@Dao
public interface AdminActionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AdminAction action);

    @Query("SELECT * FROM admin_actions ORDER BY timestamp DESC")
    LiveData<List<AdminAction>> getAll();

    @Query("SELECT * FROM admin_actions WHERE adminUsername = :adminUsername ORDER BY timestamp DESC")
    LiveData<List<AdminAction>> getByAdmin(String adminUsername);

    @Query("SELECT * FROM admin_actions WHERE timestamp BETWEEN :startTimestamp AND :endTimestamp ORDER BY timestamp DESC")
    LiveData<List<AdminAction>> getByDateRange(long startTimestamp, long endTimestamp);

    @Query("SELECT * FROM admin_actions WHERE actionType = :actionType ORDER BY timestamp DESC")
    LiveData<List<AdminAction>> getByActionType(ActionType actionType);
        @Query("SELECT COUNT(*) FROM admin_actions")
        LiveData<Integer> getActionCount();
}
