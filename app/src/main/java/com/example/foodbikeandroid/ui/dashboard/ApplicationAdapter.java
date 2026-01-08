package com.example.foodbikeandroid.ui.dashboard;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.ApplicationStatus;
import com.example.foodbikeandroid.data.model.RestaurantApplication;
import com.google.android.material.card.MaterialCardView;

import java.time.format.DateTimeFormatter;

public class ApplicationAdapter extends ListAdapter<RestaurantApplication, ApplicationAdapter.ApplicationViewHolder> {

    private OnApplicationClickListener listener;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");

    public interface OnApplicationClickListener {
        void onApplicationClick(RestaurantApplication application);
    }

    public ApplicationAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<RestaurantApplication> DIFF_CALLBACK = 
            new DiffUtil.ItemCallback<RestaurantApplication>() {
        @Override
        public boolean areItemsTheSame(@NonNull RestaurantApplication oldItem, 
                                        @NonNull RestaurantApplication newItem) {
            return oldItem.getApplicationId().equals(newItem.getApplicationId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull RestaurantApplication oldItem, 
                                           @NonNull RestaurantApplication newItem) {
            return oldItem.getRestaurantName().equals(newItem.getRestaurantName()) &&
                    oldItem.getStatus() == newItem.getStatus() &&
                    oldItem.isMessageViewed() == newItem.isMessageViewed() &&
                    (oldItem.getAdminMessage() == null ? newItem.getAdminMessage() == null : 
                            oldItem.getAdminMessage().equals(newItem.getAdminMessage()));
        }
    };

    public void setOnApplicationClickListener(OnApplicationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_application, parent, false);
        return new ApplicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationViewHolder holder, int position) {
        RestaurantApplication application = getItem(position);
        holder.bind(application);
    }

    class ApplicationViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardView;
        private final TextView tvRestaurantName;
        private final TextView tvStatus;
        private final TextView tvLocation;
        private final TextView tvAppliedDate;
        private final TextView tvMenuItemCount;
        private final FrameLayout unreadBadge;
        private final LinearLayout adminMessagePreview;
        private final TextView tvAdminMessagePreview;

        ApplicationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardApplication);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvAppliedDate = itemView.findViewById(R.id.tvAppliedDate);
            tvMenuItemCount = itemView.findViewById(R.id.tvMenuItemCount);
            unreadBadge = itemView.findViewById(R.id.unreadBadge);
            adminMessagePreview = itemView.findViewById(R.id.adminMessagePreview);
            tvAdminMessagePreview = itemView.findViewById(R.id.tvAdminMessagePreview);
        }

        void bind(RestaurantApplication application) {
            tvRestaurantName.setText(application.getRestaurantName());
            
            String location = application.getDivision() + ", " + application.getDistrict();
            tvLocation.setText(location);
            
            if (application.getAppliedDate() != null) {
                String dateText = itemView.getContext().getString(R.string.applied_on) + " " + 
                        application.getAppliedDate().format(DATE_FORMATTER);
                tvAppliedDate.setText(dateText);
            }

            int menuCount = application.getMenuItems() != null ? application.getMenuItems().size() : 0;
            tvMenuItemCount.setText(itemView.getContext().getString(R.string.menu_item_count, menuCount));

            setStatusBadge(application.getStatus());

            boolean hasUnreadMessage = application.getAdminMessage() != null && 
                    !application.getAdminMessage().isEmpty() && 
                    !application.isMessageViewed();
            unreadBadge.setVisibility(hasUnreadMessage ? View.VISIBLE : View.GONE);

            if (application.getAdminMessage() != null && !application.getAdminMessage().isEmpty()) {
                adminMessagePreview.setVisibility(View.VISIBLE);
                tvAdminMessagePreview.setText(application.getAdminMessage());
            } else {
                adminMessagePreview.setVisibility(View.GONE);
            }

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onApplicationClick(application);
                }
            });
        }

        private void setStatusBadge(ApplicationStatus status) {
            int colorRes;
            int textColorRes = R.color.white;
            String statusText;

            switch (status) {
                case APPROVED:
                    colorRes = R.color.success;
                    statusText = itemView.getContext().getString(R.string.status_approved);
                    break;
                case REJECTED:
                    colorRes = R.color.error;
                    statusText = itemView.getContext().getString(R.string.status_rejected);
                    break;
                case PENDING:
                default:
                    colorRes = R.color.warning;
                    statusText = itemView.getContext().getString(R.string.status_pending);
                    break;
            }

            tvStatus.setText(statusText);
            tvStatus.setTextColor(itemView.getContext().getColor(textColorRes));
            
            // Set background color dynamically
            GradientDrawable background = (GradientDrawable) tvStatus.getBackground().mutate();
            background.setColor(itemView.getContext().getColor(colorRes));
        }
    }
}
