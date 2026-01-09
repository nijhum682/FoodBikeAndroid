package com.example.foodbikeandroid.ui.dashboard;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.ReviewWithUserInfo;
import com.example.foodbikeandroid.databinding.ItemReviewBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<ReviewWithUserInfo> reviews = new ArrayList<>();
    private final Context context;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public ReviewAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReviewBinding binding = ItemReviewBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ReviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public void setReviews(List<ReviewWithUserInfo> newReviews) {
        this.reviews = newReviews != null ? newReviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addReview(ReviewWithUserInfo review) {
        reviews.add(0, review); // Add at top
        notifyItemInserted(0);
    }

    public void clearReviews() {
        int size = reviews.size();
        reviews.clear();
        notifyItemRangeRemoved(0, size);
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final ItemReviewBinding binding;

        ReviewViewHolder(ItemReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ReviewWithUserInfo reviewWithUserInfo) {
            // Set user avatar/initial
            String initial = reviewWithUserInfo.getUserInitial();
            binding.ivUserAvatar.setText(initial);
            
            // Set avatar background color based on initial
            int color = getColorForInitial(initial);
            binding.ivUserAvatar.setBackgroundColor(color);

            // Set username
            binding.tvUsername.setText(reviewWithUserInfo.getUsername());

            binding.rbRating.setRating(reviewWithUserInfo.getRating());

            if (reviewWithUserInfo.hasComment()) {
                binding.tvComment.setText(reviewWithUserInfo.getComment());
                binding.tvComment.setVisibility(View.VISIBLE);
            } else {
                binding.tvComment.setVisibility(View.GONE);
            }

            String dateStr = dateFormat.format(new Date(reviewWithUserInfo.getCreatedAt()));
            binding.tvDate.setText(dateStr);

            binding.tvRatingText.setText(String.valueOf(reviewWithUserInfo.getRating()));
        }

        private int getColorForInitial(String initial) {
            // Generate a consistent color based on the first character
            if (initial == null || initial.isEmpty()) {
                return ContextCompat.getColor(context, R.color.primary);
            }

            char c = initial.charAt(0);
            int[] colors = {
                    Color.parseColor("#FF6B6B"), // Red
                    Color.parseColor("#4ECDC4"), // Teal
                    Color.parseColor("#45B7D1"), // Blue
                    Color.parseColor("#FFA07A"), // Light Salmon
                    Color.parseColor("#98D8C8"), // Mint
                    Color.parseColor("#F7DC6F"), // Gold
                    Color.parseColor("#BB8FCE"), // Purple
                    Color.parseColor("#85C1E2")  // Light Blue
            };

            return colors[Math.abs(c) % colors.length];
        }
    }
}
