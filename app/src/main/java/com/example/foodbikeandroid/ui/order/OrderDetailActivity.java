package com.example.foodbikeandroid.ui.order;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.cart.CartManager;
import com.example.foodbikeandroid.data.model.CartItem;
import com.example.foodbikeandroid.data.model.MenuItem;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.model.OrderStatus;
import com.example.foodbikeandroid.data.model.Restaurant;
import com.example.foodbikeandroid.data.model.User;
import com.example.foodbikeandroid.data.repository.OrderRepository;
import com.example.foodbikeandroid.data.repository.RestaurantRepository;
import com.example.foodbikeandroid.data.repository.UserRepository;
import com.example.foodbikeandroid.data.session.SessionManager;
import com.example.foodbikeandroid.databinding.ActivityOrderDetailBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {

    private ActivityOrderDetailBinding binding;
    private OrderRepository orderRepository;
    private RestaurantRepository restaurantRepository;
    private UserRepository userRepository;
    private SessionManager sessionManager;
    private CartManager cartManager;
    
    private OrderDetailAdapter itemAdapter;
    private Order currentOrder;
    private Restaurant currentRestaurant;
    
    private static final double DELIVERY_FEE = 50.0;
    private static final String ORDER_ID_KEY = "orderId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeRepositories();
        setupToolbar();
        setupRecyclerView();
        setupButtons();
        loadOrderDetails();
    }

    private void initializeRepositories() {
        orderRepository = new OrderRepository(getApplication());
        restaurantRepository = RestaurantRepository.getInstance(getApplication());
        userRepository = UserRepository.getInstance(getApplication());
        sessionManager = SessionManager.getInstance(this);
        cartManager = CartManager.getInstance();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        itemAdapter = new OrderDetailAdapter();
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrderItems.setAdapter(itemAdapter);
    }

    private void setupButtons() {
        binding.btnCancelOrder.setOnClickListener(v -> showCancelConfirmation());
    }

    private void loadOrderDetails() {
        String orderId = getIntent().getStringExtra(ORDER_ID_KEY);
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        
        orderRepository.getOrderById(orderId, new OrderRepository.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                currentOrder = order;
                loadRestaurantAndBikerInfo();
                displayOrderDetails();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderDetailActivity.this, "Error loading order: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadRestaurantAndBikerInfo() {
        restaurantRepository.getRestaurantById(currentOrder.getRestaurantId(), new RestaurantRepository.RestaurantCallback() {
            @Override
            public void onSuccess(Restaurant restaurant) {
                currentRestaurant = restaurant;
                displayRestaurantInfo();
            }

            @Override
            public void onError(String error) {
                // Continue without restaurant info
            }
        });

        // Load biker info if assigned
        if (currentOrder.getBikerId() != null && !currentOrder.getBikerId().isEmpty()) {
            userRepository.getUserByUsername(currentOrder.getBikerId(), new UserRepository.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    displayBikerInfo(user);
                }

                @Override
                public void onError(String error) {
                    // Continue without biker info
                }
            });
        }
    }

    private void displayOrderDetails() {
        // Set order header info
        binding.tvOrderId.setText("Order #" + currentOrder.getOrderId().substring(4));
        binding.tvOrderDate.setText(formatDate(currentOrder.getCreatedAt()));
        binding.tvOrderTime.setText(formatTime(currentOrder.getCreatedAt()));
        
        // Display order status
        displayOrderStatus();
        
        // Display timeline
        displayStatusTimeline();
        
        // Display items
        if (currentOrder.getItems() != null && !currentOrder.getItems().isEmpty()) {
            itemAdapter.submitList(currentOrder.getItems());
        }
        
        // Display price breakdown
        displayPriceBreakdown();
        
        // Display payment method
        binding.tvPaymentMethod.setText(currentOrder.getPaymentMethod().toString());
        
        // Update button visibility based on status
        updateButtonVisibility();
    }

    private void displayRestaurantInfo() {
        if (currentRestaurant != null) {
            binding.tvRestaurantName.setText(currentRestaurant.getName());
        }
    }

    private void displayBikerInfo(User biker) {
        binding.layoutBikerInfo.setVisibility(View.VISIBLE);
        binding.tvBikerName.setText(biker.getUsername());
        binding.tvBikerPhone.setText(biker.getPhoneNumber());
    }

    private void displayOrderStatus() {
        int statusColor;
        int statusBgColor;
        String statusText = currentOrder.getStatus().toString();
        
        switch (currentOrder.getStatus()) {
            case PENDING:
                statusColor = getColor(R.color.status_pending_text);
                statusBgColor = getColor(R.color.status_pending);
                break;
            case CONFIRMED:
                statusColor = getColor(R.color.status_confirmed_text);
                statusBgColor = getColor(R.color.status_confirmed);
                break;
            case PREPARING:
                statusColor = getColor(R.color.status_preparing_text);
                statusBgColor = getColor(R.color.status_preparing);
                break;
            case READY:
                statusColor = getColor(R.color.status_ready_text);
                statusBgColor = getColor(R.color.status_ready);
                break;
            case DELIVERED:
                statusColor = getColor(R.color.status_delivered_text);
                statusBgColor = getColor(R.color.status_delivered);
                break;
            case CANCELLED:
            case AUTO_CANCELLED:
                statusColor = getColor(R.color.status_cancelled_text);
                statusBgColor = getColor(R.color.status_cancelled);
                statusText = "CANCELLED";
                break;
            default:
                statusColor = getColor(R.color.text_primary);
                statusBgColor = getColor(R.color.background);
        }
        
        binding.tvOrderStatus.setText(statusText);
        binding.tvOrderStatus.setTextColor(statusColor);
        binding.tvOrderStatus.setBackgroundColor(statusBgColor);
    }

    private void displayStatusTimeline() {
        OrderStatus status = currentOrder.getStatus();
        boolean isCancelled = status == OrderStatus.CANCELLED || status == OrderStatus.AUTO_CANCELLED;
        
        // Stage 1: Order Placed - Always completed (the order was placed successfully)
        binding.iconStage1.setImageResource(R.drawable.ic_check_circle);
        binding.iconStage1.setColorFilter(getColor(R.color.success));
        binding.tvStage1.setTextColor(getColor(R.color.text_primary));
        
        if (isCancelled) {
            // For cancelled orders, show all remaining stages (2-5) with red X marks
            // Stage 2
            binding.iconStage2.setImageResource(R.drawable.ic_cancel_circle);
            binding.iconStage2.setColorFilter(getColor(R.color.error));
            binding.tvStage2.setTextColor(getColor(R.color.error));
            binding.lineStage1.setBackgroundColor(getColor(R.color.error));
            
            // Stage 3
            binding.iconStage3.setImageResource(R.drawable.ic_cancel_circle);
            binding.iconStage3.setColorFilter(getColor(R.color.error));
            binding.tvStage3.setTextColor(getColor(R.color.error));
            binding.lineStage2.setBackgroundColor(getColor(R.color.error));
            
            // Stage 4
            binding.iconStage4.setImageResource(R.drawable.ic_cancel_circle);
            binding.iconStage4.setColorFilter(getColor(R.color.error));
            binding.tvStage4.setTextColor(getColor(R.color.error));
            binding.lineStage3.setBackgroundColor(getColor(R.color.error));
            
            // Stage 5
            binding.iconStage5.setImageResource(R.drawable.ic_cancel_circle);
            binding.iconStage5.setColorFilter(getColor(R.color.error));
            binding.tvStage5.setTextColor(getColor(R.color.error));
            binding.lineStage4.setBackgroundColor(getColor(R.color.error));
        } else {
            // Normal order flow
            // Stage 2: Confirmed by Restaurant
            if (status.ordinal() >= OrderStatus.CONFIRMED.ordinal()) {
                binding.iconStage2.setImageResource(R.drawable.ic_check_circle);
                binding.iconStage2.setColorFilter(getColor(R.color.success));
                binding.tvStage2.setTextColor(getColor(R.color.text_primary));
                binding.lineStage1.setBackgroundColor(getColor(R.color.success));
            } else {
                binding.iconStage2.setImageResource(R.drawable.ic_empty_state);
                binding.iconStage2.setColorFilter(getColor(R.color.text_hint));
                binding.tvStage2.setTextColor(getColor(R.color.text_secondary));
                binding.lineStage1.setBackgroundColor(getColor(R.color.text_hint));
            }
            
            // Stage 3: Food Ready
            if (status.ordinal() >= OrderStatus.READY.ordinal()) {
                binding.iconStage3.setImageResource(R.drawable.ic_check_circle);
                binding.iconStage3.setColorFilter(getColor(R.color.success));
                binding.tvStage3.setTextColor(getColor(R.color.text_primary));
                binding.lineStage2.setBackgroundColor(getColor(R.color.success));
            } else {
                binding.iconStage3.setImageResource(R.drawable.ic_empty_state);
                binding.iconStage3.setColorFilter(getColor(R.color.text_hint));
                binding.tvStage3.setTextColor(getColor(R.color.text_secondary));
                binding.lineStage2.setBackgroundColor(getColor(R.color.text_hint));
            }
            
            // Stage 4: Out for Delivery (when biker accepts - PREPARING status)
            if (status.ordinal() >= OrderStatus.PREPARING.ordinal()) {
                binding.iconStage4.setImageResource(R.drawable.ic_check_circle);
                binding.iconStage4.setColorFilter(getColor(R.color.success));
                binding.tvStage4.setTextColor(getColor(R.color.text_primary));
                binding.lineStage3.setBackgroundColor(getColor(R.color.success));
            } else {
                binding.iconStage4.setImageResource(R.drawable.ic_empty_state);
                binding.iconStage4.setColorFilter(getColor(R.color.text_hint));
                binding.tvStage4.setTextColor(getColor(R.color.text_secondary));
                binding.lineStage3.setBackgroundColor(getColor(R.color.text_hint));
            }
            
            // Stage 5: Delivered
            if (status == OrderStatus.DELIVERED) {
                binding.iconStage5.setImageResource(R.drawable.ic_check_circle);
                binding.iconStage5.setColorFilter(getColor(R.color.success));
                binding.tvStage5.setTextColor(getColor(R.color.text_primary));
                binding.lineStage4.setBackgroundColor(getColor(R.color.success));
            } else {
                binding.iconStage5.setImageResource(R.drawable.ic_empty_state);
                binding.iconStage5.setColorFilter(getColor(R.color.text_hint));
                binding.tvStage5.setTextColor(getColor(R.color.text_secondary));
                binding.lineStage4.setBackgroundColor(getColor(R.color.text_hint));
            }
        }
    }

    private void displayPriceBreakdown() {
        double subtotal = currentOrder.getTotalPrice() - DELIVERY_FEE;
        double deliveryFee = DELIVERY_FEE;
        double total = currentOrder.getTotalPrice();

        binding.tvSubtotal.setText(String.format("৳%.0f", subtotal));
        binding.tvDeliveryFee.setText(String.format("৳%.0f", deliveryFee));
        binding.tvTotal.setText(String.format("৳%.0f", total));
    }

    private void updateButtonVisibility() {
        if (currentOrder.getStatus() == OrderStatus.PENDING) {
            binding.btnCancelOrder.setVisibility(View.VISIBLE);
        } else {
            binding.btnCancelOrder.setVisibility(View.GONE);
        }
    }

    private void handleReorder() {
        if (currentOrder == null || currentOrder.getItems() == null || currentOrder.getItems().isEmpty()) {
            Toast.makeText(this, R.string.no_items_to_reorder, Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear cart and switch to the same restaurant
        cartManager.clearCart();

        List<CartItem> unavailableItems = new ArrayList<>();
        boolean hasAvailableItems = false;

        // Try to add all items back to cart with their original quantities
        for (CartItem item : currentOrder.getItems()) {
            MenuItem menuItem = item.getMenuItem();
            
            if (menuItem != null) {
                if (menuItem.isAvailable()) {
                    // Add item with quantity
                    for (int i = 0; i < item.getQuantity(); i++) {
                        cartManager.addItem(menuItem, currentOrder.getRestaurantId(), 
                            currentRestaurant != null ? currentRestaurant.getName() : "Restaurant");
                    }
                    hasAvailableItems = true;
                } else {
                    unavailableItems.add(item);
                }
            }
        }

        // Show warning if some items are unavailable
        if (!unavailableItems.isEmpty()) {
            showUnavailableItemsWarning(unavailableItems, hasAvailableItems);
        } else {
            // All items available, proceed to cart
            navigateToCart();
        }
    }

    private void showUnavailableItemsWarning(List<CartItem> unavailableItems, boolean hasAvailableItems) {
        StringBuilder itemNames = new StringBuilder();
        for (int i = 0; i < unavailableItems.size(); i++) {
            itemNames.append("• ").append(unavailableItems.get(i).getMenuItem().getName());
            if (i < unavailableItems.size() - 1) {
                itemNames.append("\n");
            }
        }

        String message = "The following items are not available:\n\n" + itemNames.toString();
        if (hasAvailableItems) {
            message += "\n\nAvailable items have been added to your cart. Would you like to continue?";
        }

        new AlertDialog.Builder(this)
            .setTitle("Items Unavailable")
            .setMessage(message)
            .setPositiveButton("OK", (dialog, which) -> {
                if (hasAvailableItems) {
                    navigateToCart();
                }
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
            .show();
    }

    private void navigateToCart() {
        Toast.makeText(this, "Items added to cart", Toast.LENGTH_SHORT).show();
        Intent cartIntent = new Intent(this, CartActivity.class);
        startActivity(cartIntent);
    }

    private void showCancelConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Cancel Order")
            .setMessage("Are you sure you want to cancel this order?")
            .setPositiveButton("Cancel Order", (dialog, which) -> {
                cancelOrder();
                dialog.dismiss();
            })
            .setNegativeButton("Keep Order", (dialog, which) -> dialog.dismiss())
            .show();
    }

    private void cancelOrder() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        orderRepository.updateOrderStatus(currentOrder.getOrderId(), OrderStatus.CANCELLED);
        currentOrder.setStatus(OrderStatus.CANCELLED);
        
        // Update UI
        displayOrderStatus();
        displayStatusTimeline(); // Update timeline to show red X marks
        updateButtonVisibility();
        
        binding.progressBar.setVisibility(View.GONE);
        Toast.makeText(this, "Order cancelled successfully", Toast.LENGTH_SHORT).show();
    }

    private String formatDate(long timestamp) {
        return DateFormat.format("MMM dd, yyyy", new Date(timestamp)).toString();
    }

    private String formatTime(long timestamp) {
        return DateFormat.format("hh:mm a", new Date(timestamp)).toString();
    }

    // Helper class for timeline items
    static class StatusTimelineItem {
        String label;
        long timestamp;
        boolean completed;

        StatusTimelineItem(String label, long timestamp, boolean completed) {
            this.label = label;
            this.timestamp = timestamp;
            this.completed = completed;
        }
    }
}
