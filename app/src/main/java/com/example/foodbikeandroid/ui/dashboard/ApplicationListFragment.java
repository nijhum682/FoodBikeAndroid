package com.example.foodbikeandroid.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.ApplicationStatus;
import com.example.foodbikeandroid.data.model.RestaurantApplication;
import com.example.foodbikeandroid.data.repository.RestaurantApplicationRepository;
import com.example.foodbikeandroid.databinding.FragmentApplicationListBinding;

public class ApplicationListFragment extends Fragment implements 
        AdminApplicationAdapter.OnApplicationClickListener {

    private static final String ARG_STATUS = "status";

    private FragmentApplicationListBinding binding;
    private RestaurantApplicationRepository applicationRepository;
    private AdminApplicationAdapter adapter;
    private ApplicationStatus status;

    public static ApplicationListFragment newInstance(ApplicationStatus status) {
        ApplicationListFragment fragment = new ApplicationListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            status = ApplicationStatus.valueOf(getArguments().getString(ARG_STATUS));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentApplicationListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        applicationRepository = RestaurantApplicationRepository.getInstance(requireContext());
        
        setupRecyclerView();
        setupSwipeRefresh();
        loadApplications();
    }

    private void setupRecyclerView() {
        adapter = new AdminApplicationAdapter();
        adapter.setOnApplicationClickListener(this);
        binding.rvApplications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvApplications.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(
                R.color.primary, R.color.secondary, R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(this::loadApplications);
    }

    private void loadApplications() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        applicationRepository.getByStatus(status).observe(getViewLifecycleOwner(), applications -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            
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
        
        String emptyMessage;
        switch (status) {
            case PENDING:
                emptyMessage = getString(R.string.no_pending_applications);
                break;
            case APPROVED:
                emptyMessage = getString(R.string.no_approved_applications);
                break;
            case REJECTED:
                emptyMessage = getString(R.string.no_rejected_applications);
                break;
            default:
                emptyMessage = getString(R.string.no_applications_in_category);
        }
        binding.tvEmptyMessage.setText(emptyMessage);
    }

    private void showApplications() {
        binding.rvApplications.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);
    }

    @Override
    public void onApplicationClick(RestaurantApplication application) {
        Intent intent = new Intent(requireContext(), ApplicationDetailActivity.class);
        intent.putExtra(ApplicationDetailActivity.EXTRA_APPLICATION_ID, application.getApplicationId());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
