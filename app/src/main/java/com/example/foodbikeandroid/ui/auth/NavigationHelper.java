package com.example.foodbikeandroid.ui.auth;

import android.content.Context;
import android.content.Intent;

import com.example.foodbikeandroid.data.model.UserType;
import com.example.foodbikeandroid.ui.dashboard.AdminDashboardActivity;
import com.example.foodbikeandroid.ui.dashboard.BikerDashboardActivity;
import com.example.foodbikeandroid.ui.dashboard.EntrepreneurDashboardActivity;
import com.example.foodbikeandroid.ui.dashboard.UserDashboardActivity;

/**
 * Helper class for navigation based on user roles.
 */
public class NavigationHelper {

    /**
     * Get the appropriate dashboard intent based on user type.
     */
    public static Intent getDashboardIntent(Context context, UserType userType) {
        if (userType == null) {
            return new Intent(context, SignInActivity.class);
        }

        switch (userType) {
            case ADMIN:
                return new Intent(context, AdminDashboardActivity.class);
            case ENTREPRENEUR:
                return new Intent(context, EntrepreneurDashboardActivity.class);
            case DELIVERY_BIKER:
                return new Intent(context, BikerDashboardActivity.class);
            case REGULAR_USER:
            default:
                return new Intent(context, UserDashboardActivity.class);
        }
    }
}
