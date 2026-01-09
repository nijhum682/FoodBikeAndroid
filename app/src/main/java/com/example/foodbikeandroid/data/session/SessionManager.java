package com.example.foodbikeandroid.data.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.foodbikeandroid.data.model.UserType;
public class SessionManager {

    private static final String PREF_NAME = "FoodBikeSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE_NUMBER = "phoneNumber";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_LOGIN_TIME = "loginTime";
    private static final String KEY_USER_DISTRICT = "userDistrict";
    private static final String KEY_LAST_ACTIVITY = "lastActivity";
    private static final String KEY_REMEMBER_ME = "rememberMe";
    
    private static final long INACTIVITY_TIMEOUT = 30 * 60 * 1000; // 30 minutes
    
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private static volatile SessionManager INSTANCE;

    private SessionManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    public static SessionManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SessionManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SessionManager(context);
                }
            }
        }
        return INSTANCE;
    }
    public void createLoginSession(String username, String email, 
                                   String phoneNumber, UserType userType) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE_NUMBER, phoneNumber);
        editor.putString(KEY_USER_TYPE, userType.name());
        long currentTime = System.currentTimeMillis();
        editor.putLong(KEY_LOGIN_TIME, currentTime);
        editor.putLong(KEY_LAST_ACTIVITY, currentTime);
        editor.apply();
    }
    
    public void createLoginSession(String username, String email, 
                                   String phoneNumber, UserType userType, boolean rememberMe) {
        createLoginSession(username, email, phoneNumber, userType);
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.apply();
    }
    
    public boolean isLoggedIn() {
        boolean loggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        if (!loggedIn) return false;
        
        boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
        if (rememberMe) return true;
        
        return !isSessionExpired();
    }
    
    public boolean isSessionExpired() {
        long lastActivity = sharedPreferences.getLong(KEY_LAST_ACTIVITY, 0);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastActivity) > INACTIVITY_TIMEOUT;
    }
    
    public void updateLastActivity() {
        editor.putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis());
        editor.apply();
    }
    
    public boolean getRememberMe() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }
    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }
    public String getPhoneNumber() {
        return sharedPreferences.getString(KEY_PHONE_NUMBER, null);
    }
    public UserType getUserType() {
        String userTypeStr = sharedPreferences.getString(KEY_USER_TYPE, null);
        if (userTypeStr != null) {
            try {
                return UserType.valueOf(userTypeStr);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
    public long getLoginTime() {
        return sharedPreferences.getLong(KEY_LOGIN_TIME, 0);
    }
    public void logout() {
        editor.clear();
        editor.apply();
    }
    public void updateSession(String email, String phoneNumber) {
        if (email != null) {
            editor.putString(KEY_EMAIL, email);
        }
        if (phoneNumber != null) {
            editor.putString(KEY_PHONE_NUMBER, phoneNumber);
        }
        editor.apply();
    }

    public void setUserDistrict(String district) {
        editor.putString(KEY_USER_DISTRICT, district);
        editor.apply();
    }

    public String getUserDistrict() {
        return sharedPreferences.getString(KEY_USER_DISTRICT, "Unknown");
    }
}
