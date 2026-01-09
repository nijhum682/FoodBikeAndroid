# Developer Quick Start Guide

## Using the New Features

### 1. Add Loading State to Your Activity

```java
public class YourActivity extends BaseActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your);
        
        // Include loading overlay in your layout
        // <include layout="@layout/layout_loading_overlay" />
        
        loadData();
    }
    
    private void loadData() {
        showLoading();
        
        repository.getData(new Callback() {
            @Override
            public void onSuccess(Data data) {
                hideLoading();
                // Update UI
            }
            
            @Override
            public void onError(String error) {
                hideLoading();
                showErrorWithRetry(error, this::loadData);
            }
        });
    }
}
```

### 2. Add Empty State to RecyclerView

```xml
<!-- In your layout -->
<FrameLayout>
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    
    <include
        android:id="@+id/emptyState"
        layout="@layout/layout_empty_state" />
</FrameLayout>
```

```java
// In your activity/fragment
private void updateUI(List<Item> items) {
    if (items.isEmpty()) {
        recyclerView.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.VISIBLE);
        emptyStateTitle.setText(R.string.no_items);
        emptyStateMessage.setText(R.string.no_items_message);
    } else {
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateContainer.setVisibility(View.GONE);
        adapter.submitList(items);
    }
}
```

### 3. Validate Form Input

```java
private boolean validateForm() {
    boolean isValid = true;
    
    isValid &= ValidationUtils.validateRequired(tilName, 
        getString(R.string.field_required));
    isValid &= ValidationUtils.validateEmail(tilEmail);
    isValid &= ValidationUtils.validatePhone(tilPhone);
    isValid &= ValidationUtils.validatePrice(tilPrice);
    
    return isValid;
}

private void submitForm() {
    if (validateForm()) {
        // Submit data
    }
}
```

### 4. Show Confirmation Before Destructive Action

```java
// Logout
binding.btnLogout.setOnClickListener(v -> 
    showLogoutConfirmation(() -> {
        authViewModel.logout();
        navigateToLogin();
    })
);

// Delete
binding.btnDelete.setOnClickListener(v -> 
    DialogUtils.showDeleteConfirmation(this, itemName, () -> {
        deleteItem();
    })
);

// Cancel order
binding.btnCancel.setOnClickListener(v -> 
    DialogUtils.showCancelOrderConfirmation(this, () -> {
        cancelOrder();
    })
);
```

### 5. Extend BaseActivity for Auto-Session Management

```java
// Instead of extending AppCompatActivity
public class YourActivity extends BaseActivity {
    // Session is automatically managed
    // Auto-logout on inactivity
    // Session expiration checks on resume
    
    @Override
    protected void onResume() {
        super.onResume(); // Important: call super
        // Your code
    }
}
```

### 6. Apply Material Design Styles

```xml
<!-- Button styles -->
<Button
    style="@style/Widget.App.Button.Primary"
    android:text="Submit" />

<Button
    style="@style/Widget.App.Button.Secondary"
    android:text="Cancel" />

<Button
    style="@style/Widget.App.Button.Danger"
    android:text="Delete" />

<!-- TextInputLayout -->
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.App.TextInputLayout"
    android:hint="Email">
    
    <com.google.android.material.textfield.TextInputEditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</com.google.android.material.textfield.TextInputLayout>

<!-- CardView -->
<com.google.android.material.card.MaterialCardView
    style="@style/Widget.App.CardView">
    <!-- Content -->
</com.google.android.material.card.MaterialCardView>
```

### 7. Handle Errors Gracefully

```java
try {
    // Database operation
    dao.insertItem(item);
} catch (Exception e) {
    runOnUiThread(() -> 
        showErrorWithRetry(
            getString(R.string.error_try_again),
            this::retryOperation
        )
    );
}
```

### 8. Use Pull-to-Refresh

```xml
<androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    android:id="@+id/swipeRefresh"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
        
</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
```

```java
binding.swipeRefresh.setOnRefreshListener(() -> {
    loadData();
});

private void loadData() {
    // Load data
    binding.swipeRefresh.setRefreshing(false);
}
```

## Color Usage Guidelines

```xml
<!-- Primary actions -->
android:backgroundTint="@color/primary"

<!-- Success states -->
android:backgroundTint="@color/success"

<!-- Warning states -->
android:backgroundTint="@color/warning"

<!-- Error/Danger states -->
android:backgroundTint="@color/error"

<!-- Text colors -->
android:textColor="@color/text_primary"      <!-- Main text -->
android:textColor="@color/text_secondary"    <!-- Secondary text -->
android:textColor="@color/text_hint"         <!-- Hints/placeholders -->

<!-- Backgrounds -->
android:background="@color/background"       <!-- App background -->
android:background="@color/surface"          <!-- Card/surface background -->
```

## Common Patterns

### Pattern 1: Load -> Show Loading -> Hide Loading -> Show Data/Error

```java
private void loadOrders() {
    showLoading();
    
    orderRepository.getOrders(userId, new Callback() {
        @Override
        public void onSuccess(List<Order> orders) {
            hideLoading();
            if (orders.isEmpty()) {
                showEmptyState();
            } else {
                showOrders(orders);
            }
        }
        
        @Override
        public void onError(String error) {
            hideLoading();
            showErrorWithRetry(error, this::loadOrders);
        }
    });
}
```

### Pattern 2: Validate -> Confirm -> Execute -> Show Result

```java
private void deleteMenuItem() {
    if (!validateSelection()) return;
    
    DialogUtils.showDeleteConfirmation(this, itemName, () -> {
        showLoading();
        
        repository.deleteItem(itemId, new Callback() {
            @Override
            public void onSuccess() {
                hideLoading();
                Toast.makeText(this, R.string.item_deleted, 
                    Toast.LENGTH_SHORT).show();
                finish();
            }
            
            @Override
            public void onError(String error) {
                hideLoading();
                showError(error);
            }
        });
    });
}
```

## Tips and Best Practices

1. **Always extend BaseActivity** for activities that require authentication
2. **Use ValidationUtils** before submitting forms
3. **Show loading states** for all async operations
4. **Use confirmation dialogs** for destructive actions
5. **Implement empty states** for all list views
6. **Handle errors gracefully** with retry options
7. **Apply Material Design styles** consistently
8. **Use string resources** instead of hardcoded text
9. **Test session timeout** by waiting 30 minutes
10. **Check remember me** functionality on login

## Testing Checklist

- [ ] Loading states appear and disappear correctly
- [ ] Empty states show when lists are empty
- [ ] Validation prevents invalid form submission
- [ ] Confirmation dialogs appear for destructive actions
- [ ] Session expires after 30 minutes of inactivity
- [ ] Remember me keeps user logged in
- [ ] Pull-to-refresh works on all list screens
- [ ] Error dialogs show with retry option
- [ ] Auto-cancel works for old pending orders
- [ ] Material Design is consistent throughout

## Need Help?

Check the implementation in existing activities:
- `BikerAvailableOrdersActivity` - Pull-to-refresh, empty states
- `UserOrderHistoryActivity` - Loading, filtering, empty states
- `SignInActivity` - Validation, remember me
- `BaseActivity` - Session management template
