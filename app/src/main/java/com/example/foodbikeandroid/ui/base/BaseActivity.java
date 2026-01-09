package com.example.foodbikeandroid.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.session.SessionManager;
import com.example.foodbikeandroid.ui.auth.SignInActivity;
import com.example.foodbikeandroid.utils.DialogUtils;

public abstract class BaseActivity extends AppCompatActivity {
    
    protected SessionManager sessionManager;
    private FrameLayout loadingOverlay;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = SessionManager.getInstance(this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        checkSession();
        sessionManager.updateLastActivity();
    }
    
    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        sessionManager.updateLastActivity();
    }
    
    private void checkSession() {
        if (!sessionManager.isLoggedIn()) {
            if (sessionManager.isSessionExpired()) {
                showSessionExpiredDialog();
            } else {
                navigateToLogin();
            }
        }
    }
    
    private void showSessionExpiredDialog() {
        DialogUtils.showErrorDialog(
                this,
                getString(R.string.session_expired_message)
        );
        navigateToLogin();
    }
    
    private void navigateToLogin() {
        sessionManager.logout();
        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    protected void showLoading() {
        if (loadingOverlay == null) {
            loadingOverlay = findViewById(R.id.loadingOverlay);
        }
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
        }
    }
    
    protected void hideLoading() {
        if (loadingOverlay == null) {
            loadingOverlay = findViewById(R.id.loadingOverlay);
        }
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        }
    }
    
    protected void showError(String message) {
        DialogUtils.showErrorDialog(this, message);
    }
    
    protected void showErrorWithRetry(String message, Runnable onRetry) {
        DialogUtils.showErrorDialogWithRetry(this, message, onRetry);
    }
    
    protected void showLogoutConfirmation(Runnable onConfirm) {
        DialogUtils.showLogoutConfirmation(this, onConfirm);
    }
}
