package com.example.foodbikeandroid.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.LocationData;
import com.example.foodbikeandroid.data.database.AdminActionDao;
import com.example.foodbikeandroid.data.database.FoodBikeDatabase;
import com.example.foodbikeandroid.data.database.RestaurantDao;
import com.example.foodbikeandroid.data.model.ActionType;
import com.example.foodbikeandroid.data.model.AdminAction;
import com.example.foodbikeandroid.data.model.Restaurant;
import com.example.foodbikeandroid.data.repository.RestaurantRepository;
import com.example.foodbikeandroid.databinding.ActivityAdminManageRestaurantsBinding;
import com.example.foodbikeandroid.databinding.DialogAddRestaurantBinding;
import com.example.foodbikeandroid.databinding.DialogEditRestaurantBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AdminManageRestaurantsActivity extends AppCompatActivity 
        implements AdminRestaurantAdapter.OnRestaurantActionListener {

    private ActivityAdminManageRestaurantsBinding binding;
    private AuthViewModel authViewModel;
    private RestaurantRepository restaurantRepository;
    private RestaurantDao restaurantDao;
    private AdminActionDao adminActionDao;
    private AdminRestaurantAdapter adapter;

    private List<Restaurant> allRestaurants = new ArrayList<>();
    private String currentSearchQuery = "";
    private String currentDivisionFilter = "All Divisions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminManageRestaurantsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        restaurantRepository = RestaurantRepository.getInstance(this);
        
        FoodBikeDatabase database = FoodBikeDatabase.getInstance(this);
        restaurantDao = database.restaurantDao();
        adminActionDao = database.adminActionDao();

        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupDivisionFilter();
        setupFab();
        observeRestaurants();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new AdminRestaurantAdapter();
        adapter.setOnRestaurantActionListener(this);
        
        binding.rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRestaurants.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new AdminRestaurantAdapter.SwipeToDeleteCallback(adapter, binding.rvRestaurants));
        itemTouchHelper.attachToRecyclerView(binding.rvRestaurants);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchQuery = s.toString().trim();
                filterRestaurants();
            }
        });
    }

    private void setupDivisionFilter() {
        List<String> divisions = LocationData.getAllDivisions();
        ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, divisions);
        binding.actvDivisionFilter.setAdapter(divisionAdapter);
        binding.actvDivisionFilter.setText("All Divisions", false);

        binding.actvDivisionFilter.setOnItemClickListener((parent, view, position, id) -> {
            currentDivisionFilter = divisions.get(position);
            filterRestaurants();
        });
    }

    private void setupFab() {
        binding.fabAddRestaurant.setOnClickListener(v -> showAddRestaurantDialog());
    }

    private void observeRestaurants() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        restaurantRepository.getAllRestaurants().observe(this, restaurants -> {
            binding.progressBar.setVisibility(View.GONE);
            
            if (restaurants != null) {
                allRestaurants = new ArrayList<>(restaurants);
                filterRestaurants();
            } else {
                showEmptyState();
            }
        });
    }

    private void filterRestaurants() {
        List<Restaurant> filteredList = new ArrayList<>();

        for (Restaurant restaurant : allRestaurants) {
            boolean matchesSearch = currentSearchQuery.isEmpty() || 
                    restaurant.getName().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    restaurant.getId().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    restaurant.getAddress().toLowerCase().contains(currentSearchQuery.toLowerCase());

            boolean matchesDivision = currentDivisionFilter.equals("All Divisions") ||
                    restaurant.getDivision().equals(currentDivisionFilter);

            if (matchesSearch && matchesDivision) {
                filteredList.add(restaurant);
            }
        }

        updateUI(filteredList);
    }

    private void updateUI(List<Restaurant> restaurants) {
        binding.tvRestaurantCount.setText(getString(R.string.restaurant_count, restaurants.size()));

        if (restaurants.isEmpty()) {
            showEmptyState();
        } else {
            showRestaurants();
            adapter.submitList(new ArrayList<>(restaurants));
        }
    }

    private void showEmptyState() {
        binding.rvRestaurants.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.VISIBLE);
    }

    private void showRestaurants() {
        binding.rvRestaurants.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
    }

    @Override
    public void onRestaurantClick(Restaurant restaurant) {
        showEditRestaurantDialog(restaurant);
    }

    @Override
    public void onRestaurantDelete(Restaurant restaurant, int position) {
        showDeleteConfirmationDialog(restaurant, position);
    }
    
    @Override
    public void onEditMenuClick(Restaurant restaurant) {
        Intent intent = new Intent(this, AdminEditMenuActivity.class);
        intent.putExtra(AdminEditMenuActivity.EXTRA_RESTAURANT_ID, restaurant.getId());
        startActivity(intent);
    }

    private void showDeleteConfirmationDialog(Restaurant restaurant, int position) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_restaurant)
                .setMessage(getString(R.string.delete_restaurant_confirm, restaurant.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    deleteRestaurant(restaurant);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    adapter.notifyItemChanged(position);
                })
                .setOnCancelListener(dialog -> {
                    adapter.notifyItemChanged(position);
                })
                .show();
    }

    private void deleteRestaurant(Restaurant restaurant) {
        String adminUsername = authViewModel.getCurrentUsername();
        
        restaurantRepository.delete(restaurant, new RestaurantRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                Executors.newSingleThreadExecutor().execute(() -> {
                    AdminAction action = new AdminAction(
                            adminUsername,
                            ActionType.DELETED_RESTAURANT,
                            restaurant.getName(),
                            "Deleted restaurant: " + restaurant.getName() + " (ID: " + restaurant.getId() + ")"
                    );
                    adminActionDao.insert(action);
                });

                runOnUiThread(() -> 
                    Toast.makeText(AdminManageRestaurantsActivity.this, 
                        getString(R.string.restaurant_deleted, restaurant.getName()), 
                        Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> 
                    Toast.makeText(AdminManageRestaurantsActivity.this, 
                        "Error deleting: " + message, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void showAddRestaurantDialog() {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
        DialogAddRestaurantBinding dialogBinding = DialogAddRestaurantBinding.inflate(
                LayoutInflater.from(this));
        builder.setView(dialogBinding.getRoot());

        AlertDialog dialog = builder.create();

        setupDivisionDropdown(dialogBinding.actvDivision, dialogBinding.actvDistrict);

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnSave.setOnClickListener(v -> {
            if (validateAddForm(dialogBinding)) {
                addRestaurant(dialogBinding);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void setupDivisionDropdown(AutoCompleteTextView divisionDropdown, 
            AutoCompleteTextView districtDropdown) {
        List<String> divisions = LocationData.getAllDivisions();
        divisions.remove(0);
        
        ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, divisions);
        divisionDropdown.setAdapter(divisionAdapter);

        divisionDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDivision = divisions.get(position);
            List<String> districts = LocationData.getDistrictsForDivision(selectedDivision);
            districts.remove(0);
            
            ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, districts);
            districtDropdown.setAdapter(districtAdapter);
            districtDropdown.setText("", false);
        });
    }

    private boolean validateAddForm(DialogAddRestaurantBinding dialogBinding) {
        boolean isValid = true;

        String name = dialogBinding.etRestaurantName.getText().toString().trim();
        if (name.isEmpty()) {
            dialogBinding.tilRestaurantName.setError(getString(R.string.error_restaurant_name_required));
            isValid = false;
        } else if (name.length() < 3) {
            dialogBinding.tilRestaurantName.setError(getString(R.string.error_restaurant_name_min_length));
            isValid = false;
        } else {
            dialogBinding.tilRestaurantName.setError(null);
        }

        String division = dialogBinding.actvDivision.getText().toString().trim();
        if (division.isEmpty()) {
            dialogBinding.tilDivision.setError(getString(R.string.error_division_required));
            isValid = false;
        } else {
            dialogBinding.tilDivision.setError(null);
        }

        String district = dialogBinding.actvDistrict.getText().toString().trim();
        if (district.isEmpty()) {
            dialogBinding.tilDistrict.setError(getString(R.string.error_district_required));
            isValid = false;
        } else {
            dialogBinding.tilDistrict.setError(null);
        }

        String address = dialogBinding.etAddress.getText().toString().trim();
        if (address.isEmpty()) {
            dialogBinding.tilAddress.setError(getString(R.string.error_address_required));
            isValid = false;
        } else {
            dialogBinding.tilAddress.setError(null);
        }

        String ratingStr = dialogBinding.etInitialRating.getText().toString().trim();
        try {
            double rating = Double.parseDouble(ratingStr);
            if (rating < 0 || rating > 5) {
                dialogBinding.tilInitialRating.setError(getString(R.string.error_rating_range));
                isValid = false;
            } else {
                dialogBinding.tilInitialRating.setError(null);
            }
        } catch (NumberFormatException e) {
            dialogBinding.tilInitialRating.setError(getString(R.string.error_rating_invalid));
            isValid = false;
        }

        return isValid;
    }

    private void addRestaurant(DialogAddRestaurantBinding dialogBinding) {
        String name = dialogBinding.etRestaurantName.getText().toString().trim();
        String division = dialogBinding.actvDivision.getText().toString().trim();
        String district = dialogBinding.actvDistrict.getText().toString().trim();
        String address = dialogBinding.etAddress.getText().toString().trim();
        double rating = Double.parseDouble(dialogBinding.etInitialRating.getText().toString().trim());

        String adminUsername = authViewModel.getCurrentUsername();

        Executors.newSingleThreadExecutor().execute(() -> {
            int count = restaurantDao.getRestaurantCountByPrefix(LocationData.getDivisionPrefix(division));
            String restaurantId = LocationData.generateRestaurantId(division, count + 1);

            Restaurant restaurant = new Restaurant(restaurantId, name, division, district, address);
            restaurant.setRating(rating);
            restaurant.setOpen(true);
            restaurant.setMenuItems(new ArrayList<>());

            restaurantDao.insert(restaurant);

            AdminAction action = new AdminAction(
                    adminUsername,
                    ActionType.ADDED_RESTAURANT,
                    name,
                    "Added restaurant: " + name + " (ID: " + restaurantId + ")"
            );
            adminActionDao.insert(action);

            runOnUiThread(() -> {
                Toast.makeText(this, getString(R.string.restaurant_added, name), 
                        Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void showEditRestaurantDialog(Restaurant restaurant) {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
        DialogEditRestaurantBinding dialogBinding = DialogEditRestaurantBinding.inflate(
                LayoutInflater.from(this));
        builder.setView(dialogBinding.getRoot());

        AlertDialog dialog = builder.create();

        dialogBinding.tvRestaurantId.setText(getString(R.string.restaurant_id_label, restaurant.getId()));
        dialogBinding.etRestaurantName.setText(restaurant.getName());
        dialogBinding.etAddress.setText(restaurant.getAddress());
        dialogBinding.etRating.setText(String.valueOf(restaurant.getRating()));

        setupDivisionDropdown(dialogBinding.actvDivision, dialogBinding.actvDistrict);
        
        dialogBinding.actvDivision.setText(restaurant.getDivision(), false);
        
        List<String> districts = LocationData.getDistrictsForDivision(restaurant.getDivision());
        districts.remove(0);
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, districts);
        dialogBinding.actvDistrict.setAdapter(districtAdapter);
        dialogBinding.actvDistrict.setText(restaurant.getDistrict(), false);

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnSave.setOnClickListener(v -> {
            if (validateEditForm(dialogBinding)) {
                updateRestaurant(restaurant, dialogBinding);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean validateEditForm(DialogEditRestaurantBinding dialogBinding) {
        boolean isValid = true;

        String name = dialogBinding.etRestaurantName.getText().toString().trim();
        if (name.isEmpty()) {
            dialogBinding.tilRestaurantName.setError(getString(R.string.error_restaurant_name_required));
            isValid = false;
        } else if (name.length() < 3) {
            dialogBinding.tilRestaurantName.setError(getString(R.string.error_restaurant_name_min_length));
            isValid = false;
        } else {
            dialogBinding.tilRestaurantName.setError(null);
        }

        String division = dialogBinding.actvDivision.getText().toString().trim();
        if (division.isEmpty()) {
            dialogBinding.tilDivision.setError(getString(R.string.error_division_required));
            isValid = false;
        } else {
            dialogBinding.tilDivision.setError(null);
        }

        String district = dialogBinding.actvDistrict.getText().toString().trim();
        if (district.isEmpty()) {
            dialogBinding.tilDistrict.setError(getString(R.string.error_district_required));
            isValid = false;
        } else {
            dialogBinding.tilDistrict.setError(null);
        }

        String address = dialogBinding.etAddress.getText().toString().trim();
        if (address.isEmpty()) {
            dialogBinding.tilAddress.setError(getString(R.string.error_address_required));
            isValid = false;
        } else {
            dialogBinding.tilAddress.setError(null);
        }

        String ratingStr = dialogBinding.etRating.getText().toString().trim();
        try {
            double rating = Double.parseDouble(ratingStr);
            if (rating < 0 || rating > 5) {
                dialogBinding.tilRating.setError(getString(R.string.error_rating_range));
                isValid = false;
            } else {
                dialogBinding.tilRating.setError(null);
            }
        } catch (NumberFormatException e) {
            dialogBinding.tilRating.setError(getString(R.string.error_rating_invalid));
            isValid = false;
        }

        return isValid;
    }

    private void updateRestaurant(Restaurant restaurant, DialogEditRestaurantBinding dialogBinding) {
        restaurant.setName(dialogBinding.etRestaurantName.getText().toString().trim());
        restaurant.setDivision(dialogBinding.actvDivision.getText().toString().trim());
        restaurant.setDistrict(dialogBinding.actvDistrict.getText().toString().trim());
        restaurant.setAddress(dialogBinding.etAddress.getText().toString().trim());
        restaurant.setRating(Double.parseDouble(dialogBinding.etRating.getText().toString().trim()));

        restaurantRepository.update(restaurant);

        Toast.makeText(this, getString(R.string.restaurant_updated, restaurant.getName()), 
                Toast.LENGTH_SHORT).show();
    }
}
