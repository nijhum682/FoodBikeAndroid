package com.example.foodbikeandroid.ui.dashboard;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.database.AdminActionDao;
import com.example.foodbikeandroid.data.database.FoodBikeDatabase;
import com.example.foodbikeandroid.data.model.ActionType;
import com.example.foodbikeandroid.data.model.AdminAction;
import com.example.foodbikeandroid.data.model.MenuItem;
import com.example.foodbikeandroid.data.model.Restaurant;
import com.example.foodbikeandroid.data.repository.RestaurantRepository;
import com.example.foodbikeandroid.databinding.ActivityAdminEditMenuBinding;
import com.example.foodbikeandroid.databinding.DialogMenuItemFormBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AdminEditMenuActivity extends AppCompatActivity implements ManageMenuItemAdapter.OnMenuItemActionListener {

    private ActivityAdminEditMenuBinding binding;
    private AuthViewModel authViewModel;
    private RestaurantRepository restaurantRepository;
    private AdminActionDao adminActionDao;
    private ManageMenuItemAdapter adapter;

    private String restaurantId;
    private Restaurant currentRestaurant;
    private List<MenuItem> menuItems = new ArrayList<>();

    public static final String EXTRA_RESTAURANT_ID = "restaurant_id";
    public static final int MINIMUM_MENU_ITEMS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminEditMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        restaurantRepository = RestaurantRepository.getInstance(this);
        
        FoodBikeDatabase database = FoodBikeDatabase.getInstance(this);
        adminActionDao = database.adminActionDao();

        restaurantId = getIntent().getStringExtra(EXTRA_RESTAURANT_ID);
        
        if (restaurantId == null || restaurantId.isEmpty()) {
            Toast.makeText(this, R.string.error_restaurant_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new ManageMenuItemAdapter();
        adapter.setOnMenuItemActionListener(this);
        
        binding.rvMenuItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMenuItems.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.fabAddMenuItem.setOnClickListener(v -> showMenuItemDialog(null, -1));
        binding.btnSaveAllChanges.setOnClickListener(v -> saveAllChanges());
    }

    private void loadData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        restaurantRepository.getRestaurantById(restaurantId).observe(this, restaurant -> {
            binding.progressBar.setVisibility(View.GONE);
            if (restaurant != null) {
                currentRestaurant = restaurant;
                menuItems = restaurant.getMenuItems() != null ? 
                        new ArrayList<>(restaurant.getMenuItems()) : new ArrayList<>();
                updateUI();
            } else {
                Toast.makeText(this, R.string.error_restaurant_not_found, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUI() {
        if (currentRestaurant != null) {
            binding.tvRestaurantName.setText(currentRestaurant.getName());
            binding.tvRestaurantLocation.setText(
                    currentRestaurant.getDivision() + ", " + currentRestaurant.getDistrict());
            binding.tvRestaurantId.setText(getString(R.string.restaurant_id_label, currentRestaurant.getId()));
        }

        updateMenuItemCount();
        updateMenuStatus();
        
        if (menuItems.isEmpty()) {
            showEmptyState();
        } else {
            showMenuItems();
            adapter.submitList(new ArrayList<>(menuItems));
        }
    }

    private void updateMenuItemCount() {
        int count = menuItems.size();
        binding.tvMenuItemCount.setText(getString(R.string.menu_item_count, count));
    }

    private void updateMenuStatus() {
        int count = menuItems.size();
        boolean isReady = count >= MINIMUM_MENU_ITEMS;
        
        binding.tvMenuStatus.setText(isReady ? 
                getString(R.string.menu_ready) : 
                getString(R.string.menu_needs_items, MINIMUM_MENU_ITEMS - count));
        
        int colorRes = isReady ? R.color.success : R.color.warning;
        binding.tvMenuStatus.setTextColor(getColor(R.color.white));
        
        GradientDrawable background = (GradientDrawable) binding.tvMenuStatus.getBackground().mutate();
        background.setColor(getColor(colorRes));
    }

    private void showEmptyState() {
        binding.rvMenuItems.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.VISIBLE);
    }

    private void showMenuItems() {
        binding.rvMenuItems.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);
    }

    private void showMenuItemDialog(MenuItem existingItem, int position) {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
        DialogMenuItemFormBinding dialogBinding = DialogMenuItemFormBinding.inflate(
                LayoutInflater.from(this));
        builder.setView(dialogBinding.getRoot());

        boolean isEditing = existingItem != null;
        dialogBinding.tvDialogTitle.setText(isEditing ? R.string.edit_menu_item : R.string.add_menu_item);

        if (isEditing) {
            dialogBinding.etItemName.setText(existingItem.getName());
            dialogBinding.etItemDescription.setText(existingItem.getDescription());
            dialogBinding.etItemPrice.setText(String.valueOf(existingItem.getPrice()));
            dialogBinding.switchItemAvailable.setChecked(existingItem.isAvailable());
        }

        AlertDialog dialog = builder.create();

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnSave.setOnClickListener(v -> {
            if (validateMenuItemForm(dialogBinding)) {
                String name = dialogBinding.etItemName.getText().toString().trim();
                String description = dialogBinding.etItemDescription.getText().toString().trim();
                double price = Double.parseDouble(dialogBinding.etItemPrice.getText().toString().trim());
                boolean available = dialogBinding.switchItemAvailable.isChecked();

                if (isEditing) {
                    String oldName = existingItem.getName();
                    double oldPrice = existingItem.getPrice();
                    
                    existingItem.setName(name);
                    existingItem.setDescription(description);
                    existingItem.setPrice(price);
                    existingItem.setAvailable(available);
                    menuItems.set(position, existingItem);
                    
                    // Log the edit action
                    logAdminAction(ActionType.EDITED_MENU_ITEM, currentRestaurant.getName(),
                            "Item: " + name + ", Price: ৳" + price + " (was: " + oldName + ", ৳" + oldPrice + ")");
                } else {
                    MenuItem newItem = new MenuItem(name, description, price, "General");
                    newItem.setAvailable(available);
                    menuItems.add(newItem);
                    
                    // Log the add action
                    logAdminAction(ActionType.ADDED_MENU_ITEM, currentRestaurant.getName(),
                            "Item: " + name + ", Price: ৳" + price);
                }

                saveMenuChanges();
                updateUI();
                dialog.dismiss();

                Toast.makeText(this, 
                        isEditing ? R.string.menu_item_updated : R.string.menu_item_added, 
                        Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private boolean validateMenuItemForm(DialogMenuItemFormBinding dialogBinding) {
        boolean isValid = true;

        String name = dialogBinding.etItemName.getText().toString().trim();
        if (name.isEmpty()) {
            dialogBinding.tilItemName.setError(getString(R.string.error_item_name_required));
            isValid = false;
        } else if (name.length() < 2) {
            dialogBinding.tilItemName.setError(getString(R.string.error_item_name_min_length));
            isValid = false;
        } else {
            dialogBinding.tilItemName.setError(null);
        }

        String description = dialogBinding.etItemDescription.getText().toString().trim();
        if (description.isEmpty()) {
            dialogBinding.tilItemDescription.setError(getString(R.string.error_item_description_required));
            isValid = false;
        } else {
            dialogBinding.tilItemDescription.setError(null);
        }

        String priceStr = dialogBinding.etItemPrice.getText().toString().trim();
        if (priceStr.isEmpty()) {
            dialogBinding.tilItemPrice.setError(getString(R.string.error_item_price_required));
            isValid = false;
        } else {
            try {
                double price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    dialogBinding.tilItemPrice.setError(getString(R.string.error_item_price_positive));
                    isValid = false;
                } else {
                    dialogBinding.tilItemPrice.setError(null);
                }
            } catch (NumberFormatException e) {
                dialogBinding.tilItemPrice.setError(getString(R.string.error_item_price_invalid));
                isValid = false;
            }
        }

        return isValid;
    }

    private void showDeleteConfirmationDialog(MenuItem item, int position) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_menu_item)
                .setMessage(getString(R.string.delete_menu_item_confirm, item.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    String itemName = item.getName();
                    double itemPrice = item.getPrice();
                    
                    menuItems.remove(position);
                    saveMenuChanges();
                    updateUI();
                    
                    // Log the delete action
                    logAdminAction(ActionType.DELETED_MENU_ITEM, currentRestaurant.getName(),
                            "Item: " + itemName + ", Price: ৳" + itemPrice);
                    
                    Toast.makeText(this, R.string.menu_item_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void saveMenuChanges() {
        if (currentRestaurant != null) {
            currentRestaurant.setMenuItems(new ArrayList<>(menuItems));
            restaurantRepository.update(currentRestaurant, new RestaurantRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    // Update will trigger LiveData observers
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> 
                        Toast.makeText(AdminEditMenuActivity.this, 
                            "Error saving: " + message, Toast.LENGTH_SHORT).show()
                    );
                }
            });
        }
    }
    
    private void saveAllChanges() {
        if (currentRestaurant != null) {
            currentRestaurant.setMenuItems(new ArrayList<>(menuItems));
            restaurantRepository.update(currentRestaurant, new RestaurantRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> 
                        Toast.makeText(AdminEditMenuActivity.this, 
                            R.string.all_changes_saved, Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> 
                        Toast.makeText(AdminEditMenuActivity.this, 
                            "Error: " + message, Toast.LENGTH_SHORT).show()
                    );
                }
            });
        }
    }
    
    private void logAdminAction(ActionType actionType, String targetName, String details) {
        String adminUsername = authViewModel.getCurrentUsername();
        if (adminUsername == null) adminUsername = "Admin";
        
        final String finalAdminUsername = adminUsername;
        Executors.newSingleThreadExecutor().execute(() -> {
            AdminAction action = new AdminAction(finalAdminUsername, actionType, targetName, details);
            adminActionDao.insert(action);
        });
    }

    @Override
    public void onEditClick(MenuItem item, int position) {
        showMenuItemDialog(item, position);
    }

    @Override
    public void onDeleteClick(MenuItem item, int position) {
        showDeleteConfirmationDialog(item, position);
    }

    @Override
    public void onAvailabilityChanged(MenuItem item, int position, boolean isAvailable) {
        String previousStatus = item.isAvailable() ? "Available" : "Unavailable";
        String newStatus = isAvailable ? "Available" : "Unavailable";
        
        item.setAvailable(isAvailable);
        menuItems.set(position, item);
        saveMenuChanges();
        adapter.notifyItemChanged(position);
        
        // Log the availability toggle action
        logAdminAction(ActionType.TOGGLED_MENU_ITEM_AVAILABILITY, currentRestaurant.getName(),
                "Item: " + item.getName() + " - " + previousStatus + " → " + newStatus);
    }
}
