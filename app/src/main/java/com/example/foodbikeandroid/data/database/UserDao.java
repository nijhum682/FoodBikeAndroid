package com.example.foodbikeandroid.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.foodbikeandroid.data.model.User;

import java.util.List;
@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertUser(User user);
    @Update
    void updateUser(User user);

    @Delete
    void deleteUser(User user);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    LiveData<User> getUserByUsernameLive(String username);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User authenticateUser(String username, String password);

    @Query("SELECT * FROM users")
    LiveData<List<User>> getAllUsers();

    @Query("SELECT * FROM users")
    List<User> getAllUsersSync();

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    boolean isUsernameExists(String username);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    boolean isEmailExists(String email);

    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();

    @Query("UPDATE users SET earnings = earnings + :amount WHERE username = :username")
    void addEarnings(String username, double amount);

    @Query("DELETE FROM users")
    void deleteAllUsers();
}
