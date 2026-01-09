package com.example.foodbikeandroid.ui.dashboard;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.database.AdminActionDao;
import com.example.foodbikeandroid.data.database.FoodBikeDatabase;
import com.example.foodbikeandroid.data.database.RestaurantApplicationDao;
import com.example.foodbikeandroid.data.model.ActionType;
import com.example.foodbikeandroid.data.model.AdminAction;
import com.example.foodbikeandroid.data.model.ApplicationStatus;
import com.example.foodbikeandroid.data.model.MenuItem;
import com.example.foodbikeandroid.data.model.Restaurant;
import com.example.foodbikeandroid.data.model.RestaurantApplication;
import com.example.foodbikeandroid.data.repository.RestaurantApplicationRepository;
import com.example.foodbikeandroid.data.repository.RestaurantRepository;
import com.example.foodbikeandroid.databinding.ActivityApplicationDetailBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.lifecycle.ViewModelProvider;

public class ApplicationDetailActivity extends AppCompatActivity {

    public static final String EXTRA_APPLICATION_ID = "extra_application_id";

    private ActivityApplicationDetailBinding binding;
    private RestaurantApplicationRepository applicationRepository;
    private RestaurantRepository restaurantRepository;
    private AdminActionDao adminActionDao;
    private RestaurantApplicationDao applicationDao;
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

        applicationRepository = RestaurantApplicationRepository.getInstance(this);
        restaurantRepository = RestaurantRepository.getInstance(this);
        
        FoodBikeDatabase database = FoodBikeDatabase.getInstance(this);
        adminActionDao = database.adminActionDao();
        applicationDao = database.restaurantApplicationDao();
        
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
        binding.btnApprove.setOnClickListener(v -> showApproveConfirmation());
        binding.btnReject.setOnClickListener(v -> showRejectConfirmation());
    }

    private void showApproveConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.approve_application)
                .setMessage(getString(R.string.confirm_approve_application, 
                        currentApplication.getRestaurantName()))
                .setPositiveButton(R.string.approve, (dialog, which) -> approveApplication())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showRejectConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reject_application)
                .setMessage(getString(R.string.confirm_reject_application,
                        currentApplication.getRestaurantName()))
                .setPositiveButton(R.string.reject, (dialog, which) -> rejectApplication())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void approveApplication() {
        currentApplication.setStatus(ApplicationStatus.APPROVED);
        applicationRepository.update(currentApplication);

        // Create restaurant from application
        Restaurant restaurant = new Restaurant(
                currentApplication.getApplicationId(),
                currentApplication.getRestaurantName(),
                currentApplication.getDivision(),
                currentApplication.getDistrict(),
                currentApplication.getAddress()
        );
        restaurant.setRating(currentApplication.getRating());
        restaurant.setMenuItems(currentApplication.getMenuItems());
        restaurantRepository.insert(restaurant);

        // Log admin action
        logAdminAction(ActionType.APPROVED_APPLICATION, currentApplication.getRestaurantName(),
                "Approved restaurant application");

        Toast.makeText(this, R.string.application_approved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void rejectApplication() {
        currentApplication.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.update(currentApplication);

        // Log admin action
        logAdminAction(ActionType.REJECTED_APPLICATION, currentApplication.getRestaurantName(),
                "Rejected restaurant application");

        Toast.makeText(this, R.string.application_rejected, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void logAdminAction(ActionType actionType, String targetName, String details) {
        String adminUsername = authViewModel.getCurrentUsername();
        if (adminUsername == null) adminUsername = "Admin";
        
        AdminAction action = new AdminAction(adminUsername, actionType, targetName, details);
        
        Executors.newSingleThreadExecutor().execute(() -> adminActionDao.insert(action));
    }
}
