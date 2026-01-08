package com.example.foodbikeandroid.ui.dashboard;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.LocationData;
import com.example.foodbikeandroid.data.model.MenuItem;
import com.example.foodbikeandroid.data.model.RestaurantApplication;
import com.example.foodbikeandroid.data.repository.RestaurantApplicationRepository;
import com.example.foodbikeandroid.databinding.ActivityRestaurantApplicationBinding;
import com.example.foodbikeandroid.databinding.DialogMenuItemFormBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class RestaurantApplicationActivity extends AppCompatActivity implements ManageMenuItemAdapter.OnMenuItemActionListener {

    private ActivityRestaurantApplicationBinding binding;
    private AuthViewModel authViewModel;
    private RestaurantApplicationRepository applicationRepository;
    private ManageMenuItemAdapter menuAdapter;
    
    private List<String> divisions;
    private List<String> districts;
    private ArrayAdapter<String> divisionAdapter;
    private ArrayAdapter<String> districtAdapter;
    
    private String selectedDivision = "";
    private String selectedDistrict = "";
    
    private List<MenuItem> menuItems = new ArrayList<>();
    private static final int MINIMUM_MENU_ITEMS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRestaurantApplicationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        applicationRepository = RestaurantApplicationRepository.getInstance(this);

        setupToolbar();
        setupSpinners();
        setupMenuRecyclerView();
        setupClickListeners();
        updateMenuStatus();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupSpinners() {
        divisions = getDivisionsWithoutAll();
        districts = new ArrayList<>();
        districts.add(getString(R.string.select_district));

        divisionAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, divisions);
        divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDivision.setAdapter(divisionAdapter);

        districtAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, districts);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDistrict.setAdapter(districtAdapter);

        binding.spinnerDivision.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String division = divisions.get(position);
                if (position == 0) {
                    selectedDivision = "";
                    updateDistrictSpinner(null);
                } else {
                    selectedDivision = division;
                    updateDistrictSpinner(division);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDivision = "";
            }
        });

        binding.spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedDistrict = "";
                } else {
                    selectedDistrict = districts.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDistrict = "";
            }
        });
    }

    private List<String> getDivisionsWithoutAll() {
        List<String> divs = new ArrayList<>();
        divs.add(getString(R.string.select_division));
        divs.add("Dhaka");
        divs.add("Chittagong");
        divs.add("Sylhet");
        divs.add("Rajshahi");
        divs.add("Khulna");
        divs.add("Barisal");
        divs.add("Rangpur");
        divs.add("Mymensingh");
        return divs;
    }

    private void updateDistrictSpinner(String division) {
        districts.clear();
        districts.add(getString(R.string.select_district));
        
        if (division != null && !division.isEmpty()) {
            List<String> divisionDistricts = LocationData.getDistrictsForDivision(division);
            for (String district : divisionDistricts) {
                if (!district.equals("All Districts")) {
                    districts.add(district);
                }
            }
        }
        
        districtAdapter.notifyDataSetChanged();
        binding.spinnerDistrict.setSelection(0);
        selectedDistrict = "";
    }

    private void setupClickListeners() {
        binding.btnSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                submitApplication();
            }
        });

        binding.btnAddMenuItem.setOnClickListener(v -> showMenuItemDialog(null, -1));
    }

    private void setupMenuRecyclerView() {
        menuAdapter = new ManageMenuItemAdapter();
        menuAdapter.setOnMenuItemActionListener(this);
        
        binding.rvMenuItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMenuItems.setAdapter(menuAdapter);
        binding.rvMenuItems.setNestedScrollingEnabled(false);
    }

    private void updateMenuUI() {
        if (menuItems.isEmpty()) {
            binding.rvMenuItems.setVisibility(View.GONE);
            binding.tvNoMenuItems.setVisibility(View.VISIBLE);
        } else {
            binding.rvMenuItems.setVisibility(View.VISIBLE);
            binding.tvNoMenuItems.setVisibility(View.GONE);
            menuAdapter.submitList(new ArrayList<>(menuItems));
        }
        updateMenuStatus();
    }

    private void updateMenuStatus() {
        int count = menuItems.size();
        int remaining = MINIMUM_MENU_ITEMS - count;
        
        binding.tvMenuItemCount.setText(getString(R.string.menu_item_count, count));
        
        if (remaining > 0) {
            binding.tvMenuStatus.setText(getString(R.string.menu_needs_items, remaining));
            binding.tvMenuStatus.setTextColor(getColor(R.color.white));
            GradientDrawable bg = (GradientDrawable) binding.tvMenuStatus.getBackground().mutate();
            bg.setColor(getColor(R.color.warning));
        } else {
            binding.tvMenuStatus.setText(getString(R.string.menu_ready));
            binding.tvMenuStatus.setTextColor(getColor(R.color.white));
            GradientDrawable bg = (GradientDrawable) binding.tvMenuStatus.getBackground().mutate();
            bg.setColor(getColor(R.color.success));
        }
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
                    existingItem.setName(name);
                    existingItem.setDescription(description);
                    existingItem.setPrice(price);
                    existingItem.setAvailable(available);
                    menuItems.set(position, existingItem);
                } else {
                    MenuItem newItem = new MenuItem(name, description, price, "General");
                    newItem.setAvailable(available);
                    menuItems.add(newItem);
                }

                updateMenuUI();
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
                    menuItems.remove(position);
                    updateMenuUI();
                    Toast.makeText(this, R.string.menu_item_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
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
        item.setAvailable(isAvailable);
        menuItems.set(position, item);
        menuAdapter.notifyItemChanged(position);
    }

    private boolean validateForm() {
        boolean isValid = true;

        String restaurantName = binding.etRestaurantName.getText().toString().trim();
        if (restaurantName.isEmpty()) {
            binding.tilRestaurantName.setError(getString(R.string.error_restaurant_name_required));
            isValid = false;
        } else if (restaurantName.length() < 3) {
            binding.tilRestaurantName.setError(getString(R.string.error_restaurant_name_min_length));
            isValid = false;
        } else {
            binding.tilRestaurantName.setError(null);
        }

        if (selectedDivision.isEmpty()) {
            showError(getString(R.string.error_division_required));
            isValid = false;
        }

        if (selectedDistrict.isEmpty()) {
            showError(getString(R.string.error_district_required));
            isValid = false;
        }

        String address = binding.etAddress.getText().toString().trim();
        if (address.isEmpty()) {
            binding.tilAddress.setError(getString(R.string.error_address_required));
            isValid = false;
        } else {
            binding.tilAddress.setError(null);
        }

        if (menuItems.size() < MINIMUM_MENU_ITEMS) {
            showError(getString(R.string.error_minimum_menu_items, MINIMUM_MENU_ITEMS));
            isValid = false;
        }

        return isValid;
    }

    private void showError(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.validation_error)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void submitApplication() {
        String restaurantName = binding.etRestaurantName.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String username = authViewModel.getCurrentUsername();

        RestaurantApplication application = new RestaurantApplication(
                username != null ? username : "entrepreneur",
                restaurantName,
                selectedDivision,
                selectedDistrict,
                address,
                new ArrayList<>(menuItems)
        );

        applicationRepository.insert(application);
        showSuccessDialog(application.getApplicationId());
    }

    private void showSuccessDialog(String applicationId) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.application_submitted)
                .setMessage(getString(R.string.application_success_message, applicationId))
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    navigateBackToDashboard();
                })
                .setCancelable(false)
                .show();
    }

    private void navigateBackToDashboard() {
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
