package com.example.foodbikeandroid.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.cart.CartManager;
import com.example.foodbikeandroid.data.model.CartItem;
import com.example.foodbikeandroid.databinding.ActivityCartBinding;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {

    private ActivityCartBinding binding;
    private CartAdapter cartAdapter;
    private CartManager cartManager;

    private static final double DELIVERY_FEE = 50.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartManager = CartManager.getInstance();

        setupToolbar();
        setupRecyclerView();
        setupCheckoutButton();
        updateUI();
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
        cartAdapter = new CartAdapter();
        cartAdapter.setCartItemListener(this);
        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCartItems.setAdapter(cartAdapter);
    }

    private void setupCheckoutButton() {
        binding.btnCheckout.setOnClickListener(v -> {
            if (!cartManager.isEmpty()) {
                Intent intent = new Intent(this, OrderConfirmationActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateUI() {
        List<CartItem> items = cartManager.getCartItems();
        
        if (items.isEmpty()) {
            binding.layoutEmptyCart.setVisibility(View.VISIBLE);
            binding.rvCartItems.setVisibility(View.GONE);
            binding.cardCheckout.setVisibility(View.GONE);
            binding.tvRestaurantName.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyCart.setVisibility(View.GONE);
            binding.rvCartItems.setVisibility(View.VISIBLE);
            binding.cardCheckout.setVisibility(View.VISIBLE);
            binding.tvRestaurantName.setVisibility(View.VISIBLE);
            
            binding.tvRestaurantName.setText(getString(R.string.from_restaurant, 
                    cartManager.getCurrentRestaurantName()));
            
            cartAdapter.submitList(items);
            updatePrices();
        }
    }

    private void updatePrices() {
        double subtotal = cartManager.getTotalPrice();
        double total = subtotal + DELIVERY_FEE;
        
        binding.tvSubtotal.setText(String.format("৳%.0f", subtotal));
        binding.tvDeliveryFee.setText(String.format("৳%.0f", DELIVERY_FEE));
        binding.tvTotal.setText(String.format("৳%.0f", total));
    }

    @Override
    public void onIncreaseQuantity(CartItem item) {
        cartManager.incrementItem(item.getMenuItem());
        cartAdapter.submitList(new ArrayList<>(cartManager.getCartItems()));
        updatePrices();
    }

    @Override
    public void onDecreaseQuantity(CartItem item) {
        cartManager.decrementItem(item.getMenuItem());
        cartAdapter.submitList(new ArrayList<>(cartManager.getCartItems()));
        updatePrices();
    }

    @Override
    public void onRemoveItem(CartItem item) {
        cartManager.removeItem(item.getMenuItem());
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}
