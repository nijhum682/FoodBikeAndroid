package com.example.foodbikeandroid.ui.dashboard;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.CartItem;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.model.OrderStatus;
import com.example.foodbikeandroid.data.model.PaymentMethod;
import com.example.foodbikeandroid.databinding.ItemUserOrderBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class UserOrderHistoryAdapter extends ListAdapter<Order, UserOrderHistoryAdapter.OrderViewHolder> {

    private static final double DELIVERY_FEE = 50.0;

    private RestaurantNameProvider restaurantNameProvider;
    private OrderClickListener orderClickListener;
    private ReviewClickListener reviewClickListener;
    private ViewReviewClickListener viewReviewClickListener;
    private final Set<String> expandedOrderIds = new HashSet<>();
    private final Set<String> reviewedOrderIds = new HashSet<>();

    public interface RestaurantNameProvider {
        String getRestaurantName(String restaurantId);
    }
    
    public interface OrderClickListener {
        void onOrderClick(Order order);
    }

    public interface ReviewClickListener {
        void onReviewClick(Order order);
    }

    public interface ViewReviewClickListener {
        void onViewReviewClick(Order order);
    }

    public UserOrderHistoryAdapter() {
        super(ORDER_DIFF_CALLBACK);
    }

    public void setRestaurantNameProvider(RestaurantNameProvider provider) {
        this.restaurantNameProvider = provider;
    }
    
    public void setOrderClickListener(OrderClickListener listener) {
        this.orderClickListener = listener;
    }

    public void setReviewClickListener(ReviewClickListener listener) {
        this.reviewClickListener = listener;
    }

    public void setViewReviewClickListener(ViewReviewClickListener listener) {
        this.viewReviewClickListener = listener;
    }

    public void setReviewedOrderIds(Set<String> reviewedOrderIds) {
        this.reviewedOrderIds.clear();
        if (reviewedOrderIds != null) {
            this.reviewedOrderIds.addAll(reviewedOrderIds);
        }
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<Order> ORDER_DIFF_CALLBACK = new DiffUtil.ItemCallback<Order>() {
        @Override
        public boolean areItemsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
            return oldItem.getOrderId().equals(newItem.getOrderId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
            return oldItem.getOrderId().equals(newItem.getOrderId())
                    && oldItem.getStatus() == newItem.getStatus()
                    && oldItem.getTotalPrice() == newItem.getTotalPrice();
        }
    };

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserOrderBinding binding = ItemUserOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = getItem(position);
        boolean isExpanded = expandedOrderIds.contains(order.getOrderId());
        holder.bind(order, isExpanded);
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserOrderBinding binding;
        private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());

        OrderViewHolder(ItemUserOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Order order, boolean isExpanded) {
            Context context = binding.getRoot().getContext();

            // Restaurant Name
            String restaurantName = restaurantNameProvider != null
                    ? restaurantNameProvider.getRestaurantName(order.getRestaurantId())
                    : order.getRestaurantId();
            binding.tvRestaurantName.setText(restaurantName != null ? restaurantName : context.getString(R.string.unknown_restaurant));

            // Order ID
            binding.tvOrderId.setText(context.getString(R.string.order_id_format, order.getOrderId()));

            // Status Badge
            setupStatusBadge(context, order.getStatus());

            // Item Summary
            int itemCount = getTotalItemCount(order.getItems());
            binding.tvItemSummary.setText(context.getResources().getQuantityString(
                    R.plurals.item_count_format, itemCount, itemCount));

            // Total Price
            binding.tvTotalPrice.setText(String.format(Locale.getDefault(), "৳%.2f", order.getTotalPrice()));

            // Date
            Date orderDate = new Date(order.getCreatedAt());
            binding.tvDate.setText(dateTimeFormat.format(orderDate));

            // Expanded state
            updateExpandedState(isExpanded, false);

            // Click to expand/collapse
            binding.layoutMain.setOnClickListener(v -> {
                boolean newExpanded = !expandedOrderIds.contains(order.getOrderId());
                if (newExpanded) {
                    expandedOrderIds.add(order.getOrderId());
                } else {
                    expandedOrderIds.remove(order.getOrderId());
                }
                updateExpandedState(newExpanded, true);
            });
            
            // Long click to view order details
            binding.layoutMain.setOnLongClickListener(v -> {
                if (orderClickListener != null) {
                    orderClickListener.onOrderClick(order);
                }
                return true;
            });

            // Populate expanded details
            if (isExpanded) {
                populateDetails(context, order);
            }

            // Show refund message for cancelled orders with digital payments
            boolean isCancelled = order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.AUTO_CANCELLED;
            if (isCancelled && order.isRefunded() && order.getPaymentSourceAccount() != null) {
                String paymentMethodName = order.getPaymentMethod() == PaymentMethod.BKASH ? "Bkash" : "Nagad";
                String refundMessage = String.format(Locale.getDefault(),
                        "Refund of ৳%.2f processed to your %s account (%s)",
                        order.getTotalPrice(),
                        paymentMethodName,
                        order.getPaymentSourceAccount());
                
                binding.tvRefundMessage.setText(refundMessage);
                binding.layoutRefundMessage.setVisibility(View.VISIBLE);
            } else {
                binding.layoutRefundMessage.setVisibility(View.GONE);
            }
        }

        private void setupStatusBadge(Context context, OrderStatus status) {
            int backgroundColor;
            int textColor;
            String statusText;

            switch (status) {
                case PENDING:
                    backgroundColor = ContextCompat.getColor(context, R.color.status_pending);
                    textColor = ContextCompat.getColor(context, R.color.status_pending_text);
                    statusText = context.getString(R.string.status_pending);
                    break;
                case CONFIRMED:
                    backgroundColor = ContextCompat.getColor(context, R.color.status_confirmed);
                    textColor = ContextCompat.getColor(context, R.color.status_confirmed_text);
                    statusText = context.getString(R.string.confirmed);
                    break;
                case PREPARING:
                    backgroundColor = ContextCompat.getColor(context, R.color.status_preparing);
                    textColor = ContextCompat.getColor(context, R.color.status_preparing_text);
                    statusText = context.getString(R.string.preparing);
                    break;
                case READY:
                    backgroundColor = ContextCompat.getColor(context, R.color.status_ready);
                    textColor = ContextCompat.getColor(context, R.color.status_ready_text);
                    statusText = context.getString(R.string.ready);
                    break;
                case DELIVERED:
                    backgroundColor = ContextCompat.getColor(context, R.color.status_delivered);
                    textColor = ContextCompat.getColor(context, R.color.status_delivered_text);
                    statusText = context.getString(R.string.delivered);
                    break;
                case CANCELLED:
                case AUTO_CANCELLED:
                    backgroundColor = ContextCompat.getColor(context, R.color.status_cancelled);
                    textColor = ContextCompat.getColor(context, R.color.status_cancelled_text);
                    statusText = context.getString(R.string.cancelled);
                    break;
                default:
                    backgroundColor = ContextCompat.getColor(context, R.color.light_gray);
                    textColor = ContextCompat.getColor(context, R.color.text_secondary);
                    statusText = status.name();
            }

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(context.getResources().getDimension(R.dimen.status_badge_radius));
            drawable.setColor(backgroundColor);

            binding.tvStatus.setBackground(drawable);
            binding.tvStatus.setTextColor(textColor);
            binding.tvStatus.setText(statusText);
        }

        private void updateExpandedState(boolean isExpanded, boolean animate) {
            if (animate) {
                // Animate rotation
                float startRotation = isExpanded ? 90f : 270f;
                float endRotation = isExpanded ? 270f : 90f;
                
                ValueAnimator rotationAnimator = ValueAnimator.ofFloat(startRotation, endRotation);
                rotationAnimator.setDuration(200);
                rotationAnimator.setInterpolator(new DecelerateInterpolator());
                rotationAnimator.addUpdateListener(animation -> 
                        binding.ivExpandIcon.setRotation((float) animation.getAnimatedValue()));
                rotationAnimator.start();
            } else {
                binding.ivExpandIcon.setRotation(isExpanded ? 270f : 90f);
            }

            // Show/hide details
            binding.divider.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            binding.layoutDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            if (isExpanded && binding.layoutOrderItems.getChildCount() == 0) {
                // Populate details if expanding
                Context context = binding.getRoot().getContext();
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Order order = getItem(position);
                    populateDetails(context, order);
                }
            }
        }

        private void populateDetails(Context context, Order order) {
            // Clear previous items
            binding.layoutOrderItems.removeAllViews();

            // Add order items
            List<CartItem> items = order.getItems();
            if (items != null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                for (CartItem item : items) {
                    View itemView = inflater.inflate(R.layout.item_order_detail_row, binding.layoutOrderItems, false);
                    
                    TextView tvQuantity = itemView.findViewById(R.id.tvItemQuantity);
                    TextView tvName = itemView.findViewById(R.id.tvItemName);
                    TextView tvPrice = itemView.findViewById(R.id.tvItemPrice);

                    tvQuantity.setText(String.format(Locale.getDefault(), "%dx", item.getQuantity()));
                    tvName.setText(item.getMenuItem() != null ? item.getMenuItem().getName() : "Unknown Item");
                    tvPrice.setText(String.format(Locale.getDefault(), "৳%.2f", item.getTotalPrice()));

                    binding.layoutOrderItems.addView(itemView);
                }
            }

            // Delivery location
            binding.tvDeliveryLocation.setText(order.getDistrict());

            // Payment method
            String paymentText = order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY
                    ? context.getString(R.string.cash_on_delivery)
                    : context.getString(R.string.mobile_banking);
            binding.tvPaymentMethod.setText(paymentText);

            // Price breakdown
            double subtotal = order.getTotalPrice() - DELIVERY_FEE;
            binding.tvSubtotal.setText(String.format(Locale.getDefault(), "৳%.2f", subtotal));
            binding.tvDeliveryFee.setText(String.format(Locale.getDefault(), "৳%.2f", DELIVERY_FEE));
            binding.tvTotalPriceDetail.setText(String.format(Locale.getDefault(), "৳%.2f", order.getTotalPrice()));
            
            // View Details button click listener
            binding.btnViewDetails.setOnClickListener(v -> {
                if (orderClickListener != null) {
                    orderClickListener.onOrderClick(order);
                }
            });

            // Leave Review / View Your Review button visibility and click listener
            boolean isDelivered = order.getStatus() == OrderStatus.DELIVERED;
            boolean hasReview = reviewedOrderIds.contains(order.getOrderId());
            
            if (isDelivered && !hasReview) {
                // Show "Leave Review" button for delivered orders without review
                binding.btnLeaveReview.setVisibility(View.VISIBLE);
                binding.btnViewReview.setVisibility(View.GONE);
                binding.btnLeaveReview.setOnClickListener(v -> {
                    if (reviewClickListener != null) {
                        reviewClickListener.onReviewClick(order);
                    }
                });
            } else if (isDelivered && hasReview) {
                // Show "View Your Review" button for delivered orders with existing review
                binding.btnLeaveReview.setVisibility(View.GONE);
                binding.btnViewReview.setVisibility(View.VISIBLE);
                binding.btnViewReview.setOnClickListener(v -> {
                    if (viewReviewClickListener != null) {
                        viewReviewClickListener.onViewReviewClick(order);
                    }
                });
            } else {
                // Hide both buttons for non-delivered orders
                binding.btnLeaveReview.setVisibility(View.GONE);
                binding.btnViewReview.setVisibility(View.GONE);
            }
        }

        private int getTotalItemCount(List<CartItem> items) {
            if (items == null) return 0;
            int count = 0;
            for (CartItem item : items) {
                count += item.getQuantity();
            }
            return count;
        }
    }

    public void collapseAll() {
        expandedOrderIds.clear();
        notifyDataSetChanged();
    }
}
