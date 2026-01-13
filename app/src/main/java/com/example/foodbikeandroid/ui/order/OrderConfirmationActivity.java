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
        binding.rvOrderItems.setNestedScrollingEnabled(false);
    }

    private void displayOrderDetails() {
        binding.tvRestaurantName.setText(cartManager.getCurrentRestaurantName());
        binding.tvDeliveryLocation.setText(sessionManager.getUserDistrict());

        List<CartItem> items = cartManager.getCartItems();
        summaryAdapter.setItems(items);

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


        if (sessionManager.getUsername() == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();

            return;
        }
        if (cartManager.getCurrentRestaurantId() == null) {
            Toast.makeText(this, "Restaurant information missing. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        PaymentMethod paymentMethod;
        if (binding.rbCashOnDelivery.isChecked()) {
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY;
            processOrder(paymentMethod, null);
        } else if (binding.rbBkash.isChecked()) {
            paymentMethod = PaymentMethod.BKASH;
            showMobilePaymentDialog(paymentMethod);
        } else if (binding.rbNagad.isChecked()) {
            paymentMethod = PaymentMethod.NAGAD;
            showMobilePaymentDialog(paymentMethod);
        } else {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnPlaceOrder.setEnabled(false);
        new android.os.Handler().postDelayed(() -> binding.btnPlaceOrder.setEnabled(true), 2000);
    }

    private void showMobilePaymentDialog(PaymentMethod paymentMethod) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(paymentMethod == PaymentMethod.BKASH ? "Bkash Payment" : "Nagad Payment");
        
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        input.setHint("01XXXXXXXXX");
        input.setGravity(android.view.Gravity.CENTER);
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {

        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        android.app.AlertDialog dialog = builder.create();
        dialog.show();


        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String phoneNumber = input.getText().toString().trim();
            if (isValidPhoneNumber(phoneNumber)) {
                dialog.dismiss();
                showOtpDialog(paymentMethod, phoneNumber);
            } else {
                input.setError("Enter valid 11-digit mobile number starting with 01");
            }
        });
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("^01\\d{9}$");
    }

    private void showOtpDialog(PaymentMethod paymentMethod, String phoneNumber) {
        int otpValue = (int) (Math.random() * 8000) + 1000;
        String randomOtp = String.valueOf(otpValue);
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("OTP Verification");
        builder.setMessage("Enter the OTP sent to " + phoneNumber + "\n\nYour OTP is: " + randomOtp);
        
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter 4-digit OTP");
        input.setGravity(android.view.Gravity.CENTER);
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        android.app.AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String enteredOtp = input.getText().toString().trim();
            if (enteredOtp.equals(randomOtp)) {
                dialog.dismiss();
                showPinDialog(paymentMethod, phoneNumber);
            } else {
                input.setError("Invalid OTP");
                Toast.makeText(this, "Incorrect OTP. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPinDialog(PaymentMethod paymentMethod, String phoneNumber) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Enter PIN");
        builder.setMessage("Enter your " + (paymentMethod == PaymentMethod.BKASH ? "Bkash" : "Nagad") + " PIN to confirm payment");
        
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint("Enter PIN");
        input.setGravity(android.view.Gravity.CENTER);
        builder.setView(input);

        builder.setPositiveButton("Confirm Payment", (dialog, which) -> {
            // Keep open if fails - handled below
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        android.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Override Positive Button
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String pin = input.getText().toString().trim();
            if (pin.length() >= 4) {
                dialog.dismiss();
                processOrder(paymentMethod, phoneNumber);
            } else {
                input.setError("Invalid PIN");
                Toast.makeText(this, "Please enter a valid PIN", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processOrder(PaymentMethod paymentMethod, String paymentSourceAccount) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnPlaceOrder.setEnabled(false);

        String userId = sessionManager.getUsername();
        String restaurantId = cartManager.getCurrentRestaurantId();
        String district = sessionManager.getUserDistrict();
        
        String deliveryAddress = binding.etDeliveryAddress.getText().toString().trim();
        if (deliveryAddress.isEmpty()) {
            binding.etDeliveryAddress.setError("Address required");
            binding.etDeliveryAddress.requestFocus();
            binding.progressBar.setVisibility(View.GONE);
            binding.btnPlaceOrder.setEnabled(true);
            return;
        }

        List<CartItem> items = cartManager.getCartItems();
        double totalPrice = cartManager.getTotalPrice() + DELIVERY_FEE;

        Order order = new Order(userId, restaurantId, district, deliveryAddress, items, totalPrice, paymentMethod, paymentSourceAccount);

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
