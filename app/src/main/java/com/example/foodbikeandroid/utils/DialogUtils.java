package com.example.foodbikeandroid.utils;

import android.content.Context;

import com.example.foodbikeandroid.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DialogUtils {
    
    public interface ConfirmationCallback {
        void onConfirm();
        void onCancel();
    }
    
    public static void showConfirmationDialog(
            Context context,
            String title,
            String message,
            ConfirmationCallback callback
    ) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    if (callback != null) callback.onConfirm();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    if (callback != null) callback.onCancel();
                })
                .setCancelable(false)
                .show();
    }
    
    public static void showLogoutConfirmation(Context context, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.confirm_logout)
                .setMessage(R.string.confirm_logout_message)
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    if (onConfirm != null) onConfirm.run();
                })
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .show();
    }
    
    public static void showDeleteConfirmation(Context context, String itemName, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.confirm_delete)
                .setMessage(context.getString(R.string.confirm_delete_message))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    if (onConfirm != null) onConfirm.run();
                })
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .show();
    }
    
    public static void showCancelOrderConfirmation(Context context, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.confirm_cancel_order)
                .setMessage(R.string.confirm_cancel_order_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    if (onConfirm != null) onConfirm.run();
                })
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .show();
    }
    
    public static void showErrorDialog(Context context, String message) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.error_occurred)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }
    
    public static void showErrorDialogWithRetry(Context context, String message, Runnable onRetry) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.error_occurred)
                .setMessage(message)
                .setPositiveButton(R.string.retry, (dialog, which) -> {
                    if (onRetry != null) onRetry.run();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    public static void showSuccessDialog(Context context, String title, String message) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }
}
