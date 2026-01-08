package com.example.foodbikeandroid.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.cart.CartManager;
import com.example.foodbikeandroid.data.model.CartItem;
import com.example.foodbikeandroid.data.model.Order;
import com.example.foodbikeandroid.data.model.PaymentMethod;
import com.example.foodbikeandroid.data.repository.OrderRepository;
import com.example.foodbikeandroid.data.session.SessionManager;
import com.example.foodbikeandroid.databinding.ActivityOrderConfirmationBinding;

import java.util.List;

public class OrderConfirmationActivity extends AppCompatActivity {

    private ActivityOrderConfirmationBinding binding;
    private OrderSummaryAdapter summaryAdapter;
    private CartManager cartManager;
    private OrderRepository orderRepository;
    private SessionManager sessionManager;

    private static final double DELIVERY_FEE = 50.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderConfirmationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartManager = CartManager.getInstance();
        orderRepository = new OrderRepository(getApplication());
        sessionManager = SessionManager.getInstance(this);

        setupToolbar();
        setupRecyclerView();
        setupPlaceOrderButton();
        displayOrderDetails();
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
        summaryAdapter = new OrderSummaryAdapter();
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrderItems.setAdapter(summaryAdapter);
    }

    private void displayOrderDetails() {
        binding.tvRestaurantName.setText(cartManager.getCurrentRestaurantName());
        binding.tvDeliveryLocation.setText(sessionManager.getUserDistrict());

        List<CartItem> items = cartManager.getCartItems();
        summaryAdapter.submitList(items);

        double subtotal = cartManager.getTotalPrice();
        double total = subtotal + DELIVERY_FEE;

        binding.tvSubtotal.setText(String.format("৳%.0f", subtotal));
        binding.tvDeliveryFee.setText(String.format("৳%.0f", DELIVERY_FEE));
        binding.tvTotal.setText(String.format("৳%.0f", total));
    }

    private void setupPlaceOrderButton() {
        binding.btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        if (cartManager.isEmpty()) {
            Toast.makeText(this, R.string.cart_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnPlaceOrder.setEnabled(false);

        PaymentMethod paymentMethod = binding.rbCashOnDelivery.isChecked() 
                ? PaymentMethod.CASH_ON_DELIVERY 
                : PaymentMethod.MOBILE_BANKING;

        String userId = sessionManager.getUsername();
        String restaurantId = cartManager.getCurrentRestaurantId();
        String district = sessionManager.getUserDistrict();
        List<CartItem> items = cartManager.getCartItems();
        double totalPrice = cartManager.getTotalPrice() + DELIVERY_FEE;

        Order order = new Order(userId, restaurantId, district, items, totalPrice, paymentMethod);

        orderRepository.insertOrder(order, new OrderRepository.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                binding.progressBar.setVisibility(View.GONE);
                cartManager.clearCart();
                
                Toast.makeText(OrderConfirmationActivity.this, 
                        getString(R.string.order_placed_success, order.getOrderId()), 
                        Toast.LENGTH_LONG).show();
                
                Intent intent = new Intent(OrderConfirmationActivity.this, OrderSuccessActivity.class);
                intent.putExtra(OrderSuccessActivity.EXTRA_ORDER_ID, order.getOrderId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnPlaceOrder.setEnabled(true);
                Toast.makeText(OrderConfirmationActivity.this, 
                        getString(R.string.order_failed, error), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
