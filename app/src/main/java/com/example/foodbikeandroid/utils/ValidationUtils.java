package com.example.foodbikeandroid.utils;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

import com.example.foodbikeandroid.R;
import com.google.android.material.textfield.TextInputLayout;

public class ValidationUtils {
    
    public static boolean validateRequired(TextInputLayout layout, String errorMessage) {
        EditText editText = layout.getEditText();
        if (editText == null) return false;
        
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            layout.setError(errorMessage);
            return false;
        }
        
        layout.setError(null);
        return true;
    }
    
    public static boolean validateEmail(TextInputLayout layout) {
        EditText editText = layout.getEditText();
        if (editText == null) return false;
        
        String email = editText.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            layout.setError(layout.getContext().getString(R.string.field_required));
            return false;
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layout.setError(layout.getContext().getString(R.string.invalid_email));
            return false;
        }
        
        layout.setError(null);
        return true;
    }
    
    public static boolean validatePhone(TextInputLayout layout) {
        EditText editText = layout.getEditText();
        if (editText == null) return false;
        
        String phone = editText.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            layout.setError(layout.getContext().getString(R.string.field_required));
            return false;
        }
        
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        if (digitsOnly.length() < 10) {
            layout.setError(layout.getContext().getString(R.string.invalid_phone));
            return false;
        }
        
        layout.setError(null);
        return true;
    }
    
    public static boolean validatePassword(TextInputLayout layout, int minLength) {
        EditText editText = layout.getEditText();
        if (editText == null) return false;
        
        String password = editText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            layout.setError(layout.getContext().getString(R.string.field_required));
            return false;
        }
        
        if (password.length() < minLength) {
            layout.setError(layout.getContext().getString(R.string.password_too_short));
            return false;
        }
        
        layout.setError(null);
        return true;
    }
    
    public static boolean validatePasswordMatch(TextInputLayout passwordLayout, TextInputLayout confirmLayout) {
        EditText passwordEdit = passwordLayout.getEditText();
        EditText confirmEdit = confirmLayout.getEditText();
        
        if (passwordEdit == null || confirmEdit == null) return false;
        
        String password = passwordEdit.getText().toString();
        String confirm = confirmEdit.getText().toString();
        
        if (!password.equals(confirm)) {
            confirmLayout.setError(confirmLayout.getContext().getString(R.string.passwords_dont_match));
            return false;
        }
        
        confirmLayout.setError(null);
        return true;
    }
    
    public static boolean validatePrice(TextInputLayout layout) {
        EditText editText = layout.getEditText();
        if (editText == null) return false;
        
        String priceStr = editText.getText().toString().trim();
        if (TextUtils.isEmpty(priceStr)) {
            layout.setError(layout.getContext().getString(R.string.field_required));
            return false;
        }
        
        try {
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                layout.setError(layout.getContext().getString(R.string.price_must_be_positive));
                return false;
            }
        } catch (NumberFormatException e) {
            layout.setError(layout.getContext().getString(R.string.invalid_price));
            return false;
        }
        
        layout.setError(null);
        return true;
    }
    
    public static boolean validateMinLength(TextInputLayout layout, int minLength, String fieldName) {
        EditText editText = layout.getEditText();
        if (editText == null) return false;
        
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            layout.setError(layout.getContext().getString(R.string.field_required));
            return false;
        }
        
        if (text.length() < minLength) {
            layout.setError(fieldName + " must be at least " + minLength + " characters");
            return false;
        }
        
        layout.setError(null);
        return true;
    }
}
