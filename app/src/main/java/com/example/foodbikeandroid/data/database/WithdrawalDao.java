package com.example.foodbikeandroid.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.foodbikeandroid.data.model.Withdrawal;

import java.util.List;

@Dao
public interface WithdrawalDao {
    @Insert
    void insert(Withdrawal withdrawal);

    @Query("SELECT * FROM withdrawals ORDER BY timestamp DESC")
    LiveData<List<Withdrawal>> getAllWithdrawals();

    @Query("SELECT * FROM withdrawals WHERE username = :username AND userType = :userType ORDER BY timestamp DESC")
    LiveData<List<Withdrawal>> getWithdrawalsByUser(String username, String userType);

    @Query("SELECT SUM(amount) FROM withdrawals")
    LiveData<Double> getTotalWithdrawnAmount();

    @Query("SELECT SUM(amount) FROM withdrawals WHERE username = :username AND userType = :userType")
    LiveData<Double> getTotalWithdrawnByUser(String username, String userType);
}
