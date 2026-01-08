package com.example.foodbikeandroid.ui.dashboard;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.ApplicationStatus;
import com.example.foodbikeandroid.data.model.RestaurantApplication;
import com.example.foodbikeandroid.data.repository.RestaurantApplicationRepository;
import com.example.foodbikeandroid.databinding.ActivityMyApplicationsBinding;
import com.example.foodbikeandroid.databinding.DialogApplicationDetailBinding;
import com.example.foodbikeandroid.ui.auth.AuthViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import java.time.format.DateTimeFormatter;

public class MyApplicationsActivity extends AppCompatActivity implements 
        ApplicationAdapter.OnApplicationClickListener {

    private ActivityMyApplicationsBinding binding;
    private AuthViewModel authViewModel;
    private RestaurantApplicationRepository applicationRepository;
    private ApplicationAdapter adapter;

    private static final DateTimeFormatter FULL_DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyApplicationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        applicationRepository = RestaurantApplicationRepository.getInstance(this);

        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadApplications();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new ApplicationAdapter();
        adapter.setOnApplicationClickListener(this);
        
        binding.rvApplications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvApplications.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnSubmitNew.setOnClickListener(v -> {
            Intent intent = new Intent(this, RestaurantApplicationActivity.class);
            startActivity(intent);
        });
    }

    private void loadApplications() {
        String username = authViewModel.getCurrentUsername();
        if (username == null) {
            showEmptyState();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        
        applicationRepository.getByEntrepreneur(username).observe(this, applications -> {
            binding.progressBar.setVisibility(View.GONE);
            
            if (applications == null || applications.isEmpty()) {
                showEmptyState();
            } else {
                showApplications();
                adapter.submitList(applications);
            }
        });
    }

    private void showEmptyState() {
        binding.rvApplications.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.VISIBLE);
    }

    private void showApplications() {
        binding.rvApplications.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);
    }

    @Override
    public void onApplicationClick(RestaurantApplication application) {
        showApplicationDetailDialog(application);
    }

    private void showApplicationDetailDialog(RestaurantApplication application) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        DialogApplicationDetailBinding dialogBinding = DialogApplicationDetailBinding.inflate(
                LayoutInflater.from(this));
        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.tvDialogRestaurantName.setText(application.getRestaurantName());

        String location = application.getDivision() + " Division, " + application.getDistrict() + " District";
        dialogBinding.tvDialogLocation.setText(location);

        dialogBinding.tvDialogAddress.setText(application.getAddress());

        if (application.getAppliedDate() != null) {
            dialogBinding.tvDialogAppliedDate.setText(
                    application.getAppliedDate().format(FULL_DATE_FORMATTER));
        }

        setDialogStatusBadge(dialogBinding.tvDialogStatus, application.getStatus());

        int menuItemCount = application.getMenuItems() != null ? application.getMenuItems().size() : 0;
        dialogBinding.tvMenuItemCount.setText(getString(R.string.menu_item_count, menuItemCount));
        dialogBinding.tvMenuItemCount.setVisibility(View.VISIBLE);

        if (application.getStatus() == ApplicationStatus.APPROVED || 
            application.getStatus() == ApplicationStatus.PENDING) {
            dialogBinding.btnManageMenu.setVisibility(View.VISIBLE);
            dialogBinding.btnManageMenu.setOnClickListener(v -> {
                dialog.dismiss();
                openManageMenu(application);
            });
        } else {
            dialogBinding.btnManageMenu.setVisibility(View.GONE);
        }

        if (application.getAdminMessage() != null && !application.getAdminMessage().isEmpty()) {
            dialogBinding.cardAdminMessage.setVisibility(View.VISIBLE);
            dialogBinding.tvDialogAdminMessage.setText(application.getAdminMessage());
            
            if (!application.isMessageViewed()) {
                markMessageAsViewed(application);
            }
        } else {
            dialogBinding.cardAdminMessage.setVisibility(View.GONE);
        }

        dialogBinding.btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void openManageMenu(RestaurantApplication application) {
        Intent intent = new Intent(this, ManageMenuActivity.class);
        intent.putExtra(ManageMenuActivity.EXTRA_APPLICATION_ID, application.getApplicationId());
        if (application.getStatus() == ApplicationStatus.APPROVED) {
            intent.putExtra(ManageMenuActivity.EXTRA_RESTAURANT_ID, application.getApplicationId());
        }
        startActivity(intent);
    }

    private void setDialogStatusBadge(TextView tvStatus, ApplicationStatus status) {
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

        tvStatus.setText(statusText);
        tvStatus.setTextColor(getColor(R.color.white));
        
        GradientDrawable background = (GradientDrawable) tvStatus.getBackground().mutate();
        background.setColor(getColor(colorRes));
    }

    private void markMessageAsViewed(RestaurantApplication application) {
        application.setMessageViewed(true);
        applicationRepository.update(application);
    }
}
