package com.example.foodbikeandroid.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.foodbikeandroid.data.database.FoodBikeDatabase;
import com.example.foodbikeandroid.data.database.UserDao;
import com.example.foodbikeandroid.data.model.User;
import com.example.foodbikeandroid.data.model.UserType;
import com.example.foodbikeandroid.data.session.SessionManager;

import com.example.foodbikeandroid.data.remote.FirestoreHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class UserRepository {

    private final UserDao userDao;
    private final FirestoreHelper firestoreHelper;
    private final SessionManager sessionManager;
    private final ExecutorService executorService;

    private static volatile UserRepository INSTANCE;

    private UserRepository(Context context) {
        FoodBikeDatabase database = FoodBikeDatabase.getInstance(context);
        userDao = database.userDao();
        sessionManager = SessionManager.getInstance(context);
        firestoreHelper = FirestoreHelper.getInstance();
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
                            String phoneNumber, UserType userType, String address, AuthCallback callback) {
        // Check if username exists in Firestore
        firestoreHelper.getUsersCollection().document(username).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onError("Username already exists");
                    } else {
                        // Check if email exists
                        firestoreHelper.getUsersCollection().whereEqualTo("email", email).get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        callback.onError("Email already registered");
                                    } else {
                                        // Create user
                                        User user = new User(username, password, email, phoneNumber, userType, address);
                                        
                                        // Save to Firestore
                                        firestoreHelper.getUsersCollection().document(username).set(user)
                                                .addOnSuccessListener(aVoid -> {
                                                    // Save to local Room DB and Session
                                                    executorService.execute(() -> {
                                                        userDao.insertUser(user);
                                                        sessionManager.createLoginSession(username, email, phoneNumber, userType);
                                                    });
                                                    callback.onSuccess(user);
                                                })
                                                .addOnFailureListener(e -> callback.onError("Registration failed: " + e.getMessage()));
                                    }
                                })
                                .addOnFailureListener(e -> callback.onError("Error checking email: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onError("Error checking username: " + e.getMessage()));
    }
    
    public void loginUser(String username, String password, AuthCallback callback) {
        loginUser(username, password, false, callback);
    }
    
    public void loginUser(String username, String password, boolean rememberMe, AuthCallback callback) {
        firestoreHelper.getUsersCollection().document(username).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getPassword().equals(password)) {
                            // Sync to local Room DB
                            executorService.execute(() -> {
                                if (userDao.isUsernameExists(username)) {
                                    userDao.updateUser(user);
                                } else {
                                    userDao.insertUser(user);
                                }
                                sessionManager.createLoginSession(
                                        user.getUsername(),
                                        user.getEmail(),
                                        user.getPhoneNumber(),
                                        user.getUserType(),
                                        rememberMe
                                );
                            });
                            callback.onSuccess(user);
                        } else {
                            callback.onError("Invalid username or password");
                        }
                    } else {
                        callback.onError("User not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError("Login failed: " + e.getMessage()));
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
        firestoreHelper.getUsersCollection().document(username).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            // Update local cache
                            executorService.execute(() -> {
                                if (userDao.isUsernameExists(username)) {
                                    userDao.updateUser(user);
                                } else {
                                    userDao.insertUser(user);
                                }
                            });
                            callback.onSuccess(user);
                        } else {
                            callback.onError("Data parsing error");
                        }
                    } else {
                        // Fallback to local if not found online? Or just return error?
                        // For now, if not in Firestore, check local (offline mode support can be better, but this is simple migration)
                        executorService.execute(() -> {
                            User localUser = userDao.getUserByUsername(username);
                            if (localUser != null) {
                                callback.onSuccess(localUser);
                            } else {
                                callback.onError("User not found");
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback to local on network error
                    executorService.execute(() -> {
                        User localUser = userDao.getUserByUsername(username);
                        if (localUser != null) {
                            callback.onSuccess(localUser);
                        } else {
                            callback.onError("Error fetching user: " + e.getMessage());
                        }
                    });
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
        firestoreHelper.getUsersCollection().document(user.getUsername()).set(user)
                .addOnSuccessListener(aVoid -> {
                    executorService.execute(() -> userDao.updateUser(user));
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onError("Update failed: " + e.getMessage()));
    }
    public void deleteUser(User user, SimpleCallback callback) {
        firestoreHelper.getUsersCollection().document(user.getUsername()).delete()
                .addOnSuccessListener(aVoid -> {
                    executorService.execute(() -> userDao.deleteUser(user));
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onError("Delete failed: " + e.getMessage()));
    }
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void createDefaultAdmin() {
        String adminUsername = "admin";
        firestoreHelper.getUsersCollection().document(adminUsername).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Create default admin
                        User admin = new User(adminUsername, "admin123", "admin@foodbike.com", "01700000000", UserType.ADMIN, "Headquarters");
                        firestoreHelper.getUsersCollection().document(adminUsername).set(admin)
                                .addOnSuccessListener(aVoid -> {
                                    executorService.execute(() -> userDao.insertUser(admin));
                                });
                    }
                });
    }
}
