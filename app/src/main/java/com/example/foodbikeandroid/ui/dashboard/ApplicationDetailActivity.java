package com.example.foodbikeandroid.ui.dashboard;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.database.AdminActionDao;
import com.example.foodbikeandroid.data.database.FoodBikeDatabase;
import com.example.foodbikeandroid.data.database.RestaurantApplicationDao;
import com.example.foodbikeandroid.data.database.RestaurantDao;
import com.example.foodbikeandroid.data.model.ActionType;
import com.example.foodbikeandroid.data.model.AdminAction;
import com.example.foodbikeandroid.data.model.ApplicationStatus;
import com.example.foodbikeandroid.data.model.MenuItem;
import com.example.foodbikeandroid.data.model.Restaurant;
import com.example.foodbikeandroid.data.model.RestaurantApplication;
import com.example.foodbikeandroid.data.repository.RestaurantRepository;
import com.example.foodbikeandroid.databinding.ActivityApplicationDetailBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.lifecycle.ViewModelProvider;

public class ApplicationDetailActivity extends AppCompatActivity {

    public static final String EXTRA_APPLICATION_ID = "extra_application_id";

    private ActivityApplicationDetailBinding binding;
    private AdminActionDao adminActionDao;
    private RestaurantApplicationDao applicationDao;
    private RestaurantDao restaurantDao;
    private RestaurantRepository restaurantRepository;
    private AuthViewModel authViewModel;
    
    private RestaurantApplication currentApplication;
    private MenuDetailAdapter menuAdapter;

    private static final DateTimeFormatter FULL_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityApplicationDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        FoodBikeDatabase database = FoodBikeDatabase.getInstance(this);
        adminActionDao = database.adminActionDao();
        applicationDao = database.restaurantApplicationDao();
        restaurantDao = database.restaurantDao();
        restaurantRepository = RestaurantRepository.getInstance(getApplication());
        
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupToolbar();
        setupRecyclerView();
        loadApplicationDetails();
        setupClickListeners();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        menuAdapter = new MenuDetailAdapter();
        binding.rvMenuItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMenuItems.setAdapter(menuAdapter);
    }

    private void loadApplicationDetails() {
        String applicationId = getIntent().getStringExtra(EXTRA_APPLICATION_ID);
        if (applicationId == null) {
            Toast.makeText(this, R.string.error_application_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            currentApplication = applicationDao.getById(applicationId);
            
            runOnUiThread(() -> {
                if (currentApplication == null) {
                    Toast.makeText(this, R.string.error_application_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                displayApplicationDetails();
            });
        });
    }

    private void displayApplicationDetails() {
        // Status badge
        setStatusBadge(currentApplication.getStatus());

        // Rating
        binding.tvRating.setText(String.format("%.1f", currentApplication.getRating()));

        // Restaurant details
        binding.tvRestaurantName.setText(currentApplication.getRestaurantName());
        binding.tvEntrepreneur.setText("@" + currentApplication.getEntrepreneurUsername());
        
        String location = currentApplication.getDivision() + " Division, " + 
                currentApplication.getDistrict() + " District";
        binding.tvLocation.setText(location);
        
        binding.tvAddress.setText(currentApplication.getAddress());

        if (currentApplication.getAppliedDate() != null) {
            binding.tvAppliedDate.setText(
                    currentApplication.getAppliedDate().format(FULL_DATE_FORMATTER));
        }

        // Menu items
        List<MenuItem> menuItems = currentApplication.getMenuItems();
        if (menuItems != null && !menuItems.isEmpty()) {
            binding.rvMenuItems.setVisibility(View.VISIBLE);
            binding.emptyMenuState.setVisibility(View.GONE);
            binding.tvMenuItemCount.setText(menuItems.size() + " " + getString(R.string.items));
            menuAdapter.setMenuItems(menuItems);
        } else {
            binding.rvMenuItems.setVisibility(View.GONE);
            binding.emptyMenuState.setVisibility(View.VISIBLE);
            binding.tvMenuItemCount.setText("0 " + getString(R.string.items));
        }

        // Show/hide action buttons based on status
        if (currentApplication.getStatus() == ApplicationStatus.PENDING) {
            binding.bottomActionBar.setVisibility(View.VISIBLE);
        } else {
            binding.bottomActionBar.setVisibility(View.GONE);
        }
    }

    private void setStatusBadge(ApplicationStatus status) {
        int colorRes;
        String statusText;

        switch (status) {
            case APPROVED:
                colorRes = R.color.success;
                statusText = getString(R.string.status_approved);
                break;
            case REJECTED:
                colorRes = R.color.error;
                statusText = getString(R.string.status_rejected);
                break;
            case PENDING:
            default:
                colorRes = R.color.warning;
                statusText = getString(R.string.status_pending);
                break;
        }

        binding.tvStatus.setText(statusText);
        binding.tvStatus.setTextColor(getColor(R.color.white));

        GradientDrawable background = (GradientDrawable) binding.tvStatus.getBackground().mutate();
        background.setColor(getColor(colorRes));
    }

    private void setupClickListeners() {
        binding.btnApprove.setOnClickListener(v -> showApproveDialog());
        binding.btnReject.setOnClickListener(v -> showRejectDialog());
    }

    private void showApproveDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_approve_application, null);
        
        TextView tvMessage = dialogView.findViewById(R.id.tvApproveMessage);
        EditText etMessage = dialogView.findViewById(R.id.etMessage);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnApprove = dialogView.findViewById(R.id.btnApprove);

        tvMessage.setText(getString(R.string.confirm_approve_application, 
                currentApplication.getRestaurantName()));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnApprove.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            dialog.dismiss();
            approveApplication(message.isEmpty() ? null : message);
        });

        dialog.show();
    }

    private void showRejectDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reject_application, null);
        
        TextView tvMessage = dialogView.findViewById(R.id.tvRejectMessage);
        TextInputLayout tilReason = dialogView.findViewById(R.id.tilReason);
        EditText etReason = dialogView.findViewById(R.id.etReason);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnReject = dialogView.findViewById(R.id.btnReject);

        tvMessage.setText(getString(R.string.confirm_reject_application,
                currentApplication.getRestaurantName()));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnReject.setOnClickListener(v -> {
            String reason = etReason.getText().toString().trim();
            if (reason.isEmpty()) {
                tilReason.setError(getString(R.string.error_rejection_reason_required));
                return;
            }
            tilReason.setError(null);
            dialog.dismiss();
            rejectApplication(reason);
        });

        dialog.show();
    }

    private void approveApplication(String optionalMessage) {
        binding.btnApprove.setEnabled(false);
        binding.btnReject.setEnabled(false);

        Executors.newSingleThreadExecutor().execute(() -> {
            // Generate unique restaurant ID with division prefix
            String divisionPrefix = getDivisionPrefix(currentApplication.getDivision());
            int count = restaurantDao.getRestaurantCountByPrefix(divisionPrefix);
            String restaurantId = divisionPrefix + String.format("%04d", count + 1);

            // Update application status
            currentApplication.setStatus(ApplicationStatus.APPROVED);
            if (optionalMessage != null) {
                currentApplication.setAdminMessage(optionalMessage);
                currentApplication.setMessageViewed(false);
            }
            applicationDao.update(currentApplication);

            // Create restaurant from application with unique ID
            Restaurant restaurant = new Restaurant(
                    restaurantId,
                    currentApplication.getRestaurantName(),
                    currentApplication.getDivision(),
                    currentApplication.getDistrict(),
                    currentApplication.getAddress()
            );
            restaurant.setRating(currentApplication.getRating());
            restaurant.setCuisineType("Mixed");
            restaurant.setOpen(true);
            restaurant.setOpeningHours("9:00 AM - 10:00 PM");
            
            // Copy menu items
            List<MenuItem> menuItems = currentApplication.getMenuItems();
            if (menuItems != null) {
                List<MenuItem> copiedMenuItems = new ArrayList<>();
                for (MenuItem item : menuItems) {
                    MenuItem copiedItem = new MenuItem(
                            item.getId(),
                            item.getName(),
                            item.getDescription(),
                            item.getPrice(),
                            item.getCategory(),
                            item.isAvailable()
                    );
                    copiedMenuItems.add(copiedItem);
                }
                restaurant.setMenuItems(copiedMenuItems);
            }
            // Insert restaurant using repository with callback
            restaurantRepository.insert(restaurant, new RestaurantRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    // Log admin action after successful restaurant creation
                    Executors.newSingleThreadExecutor().execute(() -> {
                        String adminUsername = authViewModel.getCurrentUsername();
                        if (adminUsername == null) adminUsername = "Admin";
                        AdminAction action = new AdminAction(adminUsername, ActionType.APPROVED_APPLICATION,
                                currentApplication.getRestaurantName(), 
                                "Approved and created restaurant with ID: " + restaurantId);
                        adminActionDao.insert(action);
                    });

                    runOnUiThread(() -> {
                        Toast.makeText(ApplicationDetailActivity.this, 
                            getString(R.string.application_approved) + " - " + currentApplication.getRestaurantName() + " is now visible to users", 
                            Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(ApplicationDetailActivity.this, 
                            "Error creating restaurant: " + message, Toast.LENGTH_LONG).show();
                        binding.btnApprove.setEnabled(true);
                        binding.btnReject.setEnabled(true);
                    });
                }
            });
        });
    }

    private void rejectApplication(String reason) {
        binding.btnApprove.setEnabled(false);
        binding.btnReject.setEnabled(false);

        Executors.newSingleThreadExecutor().execute(() -> {
            // Update application status and set admin message
            currentApplication.setStatus(ApplicationStatus.REJECTED);
            currentApplication.setAdminMessage(reason);
            currentApplication.setMessageViewed(false);
            applicationDao.update(currentApplication);

            // Log admin action
            String adminUsername = authViewModel.getCurrentUsername();
            if (adminUsername == null) adminUsername = "Admin";
            AdminAction action = new AdminAction(adminUsername, ActionType.REJECTED_APPLICATION,
                    currentApplication.getRestaurantName(),
                    "Rejected: " + reason);
            adminActionDao.insert(action);

            runOnUiThread(() -> {
                Toast.makeText(this, R.string.application_rejected, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    private String getDivisionPrefix(String division) {
        if (division == null || division.isEmpty()) {
            return "REST";
        }
        // Create prefix from division name (first 3 letters uppercase)
        String cleaned = division.replaceAll("[^a-zA-Z]", "").toUpperCase();
        if (cleaned.length() >= 3) {
            return cleaned.substring(0, 3);
        } else if (!cleaned.isEmpty()) {
            return cleaned;
        }
        return "REST";
    }
}
