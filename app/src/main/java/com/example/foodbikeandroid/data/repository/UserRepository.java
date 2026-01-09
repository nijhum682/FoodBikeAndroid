package com.example.foodbikeandroid.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.foodbikeandroid.data.database.FoodBikeDatabase;
import com.example.foodbikeandroid.data.database.UserDao;
import com.example.foodbikeandroid.data.model.User;
import com.example.foodbikeandroid.data.model.UserType;
import com.example.foodbikeandroid.data.session.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class UserRepository {

    private final UserDao userDao;
    private final SessionManager sessionManager;
    private final ExecutorService executorService;

    private static volatile UserRepository INSTANCE;

    private UserRepository(Context context) {
        FoodBikeDatabase database = FoodBikeDatabase.getInstance(context);
        userDao = database.userDao();
        sessionManager = SessionManager.getInstance(context);
        executorService = Executors.newFixedThreadPool(2);
    }
    public static UserRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (UserRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UserRepository(context);
                }
            }
        }
        return INSTANCE;
    }
    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }
    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }
    public void registerUser(String username, String password, String email,
                            String phoneNumber, UserType userType, AuthCallback callback) {
        executorService.execute(() -> {
            try {
                if (userDao.isUsernameExists(username)) {
                    callback.onError("Username already exists");
                    return;
                }
                if (userDao.isEmailExists(email)) {
                    callback.onError("Email already registered");
                    return;
                }
                User user = new User(username, password, email, phoneNumber, userType);
                userDao.insertUser(user);
                sessionManager.createLoginSession(username, email, phoneNumber, userType);

                callback.onSuccess(user);
            } catch (Exception e) {
                callback.onError("Registration failed: " + e.getMessage());
            }
        });
    }
    
    public void loginUser(String username, String password, AuthCallback callback) {
        loginUser(username, password, false, callback);
    }
    
    public void loginUser(String username, String password, boolean rememberMe, AuthCallback callback) {
        executorService.execute(() -> {
            try {
                User user = userDao.authenticateUser(username, password);
                if (user != null) {
                    sessionManager.createLoginSession(
                            user.getUsername(),
                            user.getEmail(),
                            user.getPhoneNumber(),
                            user.getUserType(),
                            rememberMe
                    );
                    callback.onSuccess(user);
                } else {
                    callback.onError("Invalid username or password");
                }
            } catch (Exception e) {
                callback.onError("Login failed: " + e.getMessage());
            }
        });
    }
    public void logout() {
        sessionManager.logout();
    }
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public UserType getCurrentUserType() {
        return sessionManager.getUserType();
    }
    public String getCurrentUsername() {
        return sessionManager.getUsername();
    }

    public void getUserByUsername(String username, AuthCallback callback) {
        executorService.execute(() -> {
            try {
                User user = userDao.getUserByUsername(username);
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onError("User not found");
                }
            } catch (Exception e) {
                callback.onError("Error fetching user: " + e.getMessage());
            }
        });
    }

    public LiveData<User> getUserByUsername(String username) {
        return userDao.getUserByUsernameLive(username);
    }

    public String getUsernameSync(String userId) {
        try {
            User user = userDao.getUserByUsername(userId);
            return user != null ? user.getUsername() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<List<User>> getAllUsers() {
        return userDao.getAllUsers();
    }

    public void updateUser(User user, SimpleCallback callback) {
        executorService.execute(() -> {
            try {
                userDao.updateUser(user);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError("Update failed: " + e.getMessage());
            }
        });
    }
    public void deleteUser(User user, SimpleCallback callback) {
        executorService.execute(() -> {
            try {
                userDao.deleteUser(user);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError("Delete failed: " + e.getMessage());
            }
        });
    }
    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
