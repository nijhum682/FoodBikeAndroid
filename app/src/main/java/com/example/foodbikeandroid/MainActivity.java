package com.example.foodbikeandroid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodbikeandroid.data.model.UserType;
import com.example.foodbikeandroid.data.repository.UserRepository;
import com.example.foodbikeandroid.ui.auth.NavigationHelper;
import com.example.foodbikeandroid.ui.auth.SignInActivity;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthAndNavigate, SPLASH_DELAY);
    }

    private void checkAuthAndNavigate() {
        UserRepository userRepository = UserRepository.getInstance(this);

        Intent intent;
        if (userRepository.isLoggedIn()) {
            UserType userType = userRepository.getCurrentUserType();
            intent = NavigationHelper.getDashboardIntent(this, userType);
        } else {
            intent = new Intent(this, SignInActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}