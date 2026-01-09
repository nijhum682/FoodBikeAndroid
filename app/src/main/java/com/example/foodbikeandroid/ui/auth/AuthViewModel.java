package com.example.foodbikeandroid.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodbikeandroid.data.model.User;
import com.example.foodbikeandroid.data.model.UserType;
import com.example.foodbikeandroid.data.repository.UserRepository;

/**
 * ViewModel for handling authentication logic.
 * Manages login and registration state.
 */
public class AuthViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    private final MutableLiveData<AuthState> authState = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public AuthViewModel(@NonNull Application application) {
        super(application);
        userRepository = UserRepository.getInstance(application);
    }

    /**
     * Enum representing different authentication states.
     */
    public enum AuthState {
        IDLE,
        AUTHENTICATED,
        UNAUTHENTICATED
    }

    /**
     * Get authentication state LiveData.
     */
    public LiveData<AuthState> getAuthState() {
        return authState;
    }

    /**
     * Get error message LiveData.
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Get loading state LiveData.
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Attempt to login with username and password.
     */
    public void login(String username, String password) {
        login(username, password, false);
    }
    
    /**
     * Attempt to login with username, password, and remember me option.
     */
    public void login(String username, String password, boolean rememberMe) {
        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            errorMessage.setValue("Username is required");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            errorMessage.setValue("Password is required");
            return;
        }

        isLoading.setValue(true);
        userRepository.loginUser(username.trim(), password, rememberMe, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                isLoading.postValue(false);
                authState.postValue(AuthState.AUTHENTICATED);
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
                authState.postValue(AuthState.UNAUTHENTICATED);
            }
        });
    }

    /**
     * Register a new user.
     */
    public void register(String username, String password, String confirmPassword,
                        String email, String phoneNumber, UserType userType) {
        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            errorMessage.setValue("Username is required");
            return;
        }
        if (username.trim().length() < 3) {
            errorMessage.setValue("Username must be at least 3 characters");
            return;
        }
        if (password == null || password.isEmpty()) {
            errorMessage.setValue("Password is required");
            return;
        }
        if (password.length() < 6) {
            errorMessage.setValue("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            errorMessage.setValue("Passwords do not match");
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Email is required");
            return;
        }
        if (!isValidEmail(email.trim())) {
            errorMessage.setValue("Please enter a valid email");
            return;
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            errorMessage.setValue("Phone number is required");
            return;
        }
        if (!isValidPhoneNumber(phoneNumber.trim())) {
            errorMessage.setValue("Please enter a valid phone number");
            return;
        }
        if (userType == null) {
            errorMessage.setValue("Please select a user type");
            return;
        }

        isLoading.setValue(true);
        userRepository.registerUser(
                username.trim(),
                password,
                email.trim(),
                phoneNumber.trim(),
                userType,
                new UserRepository.AuthCallback() {
                    @Override
                    public void onSuccess(User user) {
                        isLoading.postValue(false);
                        authState.postValue(AuthState.AUTHENTICATED);
                    }

                    @Override
                    public void onError(String message) {
                        isLoading.postValue(false);
                        errorMessage.postValue(message);
                        authState.postValue(AuthState.UNAUTHENTICATED);
                    }
                }
        );
    }

    /**
     * Logout the current user.
     */
    public void logout() {
        userRepository.logout();
        authState.setValue(AuthState.UNAUTHENTICATED);
    }

    /**
     * Check if user is logged in.
     */
    public boolean isLoggedIn() {
        return userRepository.isLoggedIn();
    }

    /**
     * Get current user type.
     */
    public UserType getCurrentUserType() {
        return userRepository.getCurrentUserType();
    }

    /**
     * Get current username.
     */
    public String getCurrentUsername() {
        return userRepository.getCurrentUsername();
    }

    /**
     * Clear error message.
     */
    public void clearError() {
        errorMessage.setValue(null);
    }

    /**
     * Validate email format.
     */
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validate phone number format.
     */
    private boolean isValidPhoneNumber(String phone) {
        // Basic phone validation - at least 10 digits
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        return digitsOnly.length() >= 10;
    }
}
