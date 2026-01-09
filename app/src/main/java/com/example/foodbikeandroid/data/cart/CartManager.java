package com.example.foodbikeandroid.data.cart;

import com.example.foodbikeandroid.data.model.CartItem;
import com.example.foodbikeandroid.data.model.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static CartManager instance;
    private final List<CartItem> cartItems;
    private String currentRestaurantId;
    private String currentRestaurantName;
    private CartUpdateListener listener;

    public interface CartUpdateListener {
        void onCartUpdated(int itemCount, double totalPrice);
    }

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void setCartUpdateListener(CartUpdateListener listener) {
        this.listener = listener;
    }

    public void addItem(MenuItem menuItem, String restaurantId, String restaurantName) {
        if (currentRestaurantId != null && !currentRestaurantId.equals(restaurantId)) {
            clearCart();
        }
        
        currentRestaurantId = restaurantId;
        currentRestaurantName = restaurantName;

        for (CartItem item : cartItems) {
            if (item.getMenuItem().getId().equals(menuItem.getId())) {
                item.incrementQuantity();
                notifyCartUpdated();
                return;
            }
        }

        cartItems.add(new CartItem(menuItem, 1));
        notifyCartUpdated();
    }

    public void removeItem(MenuItem menuItem) {
        cartItems.removeIf(item -> item.getMenuItem().getId().equals(menuItem.getId()));
        if (cartItems.isEmpty()) {
            currentRestaurantId = null;
            currentRestaurantName = null;
        }
        notifyCartUpdated();
    }

    public void updateItemQuantity(MenuItem menuItem, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getMenuItem().getId().equals(menuItem.getId())) {
                if (quantity <= 0) {
                    removeItem(menuItem);
                } else {
                    item.setQuantity(quantity);
                    notifyCartUpdated();
                }
                return;
            }
        }
    }

    public void incrementItem(MenuItem menuItem) {
        for (CartItem item : cartItems) {
            if (item.getMenuItem().getId().equals(menuItem.getId())) {
                item.incrementQuantity();
                notifyCartUpdated();
                return;
            }
        }
    }

    public void decrementItem(MenuItem menuItem) {
        for (CartItem item : cartItems) {
            if (item.getMenuItem().getId().equals(menuItem.getId())) {
                if (item.getQuantity() > 1) {
                    item.decrementQuantity();
                } else {
                    removeItem(menuItem);
                    return;
                }
                notifyCartUpdated();
                return;
            }
        }
    }

    public List<CartItem> getCartItems() {
        List<CartItem> copies = new ArrayList<>();
        for (CartItem item : cartItems) {
            copies.add(new CartItem(item.getMenuItem(), item.getQuantity()));
        }
        return copies;
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public String getCurrentRestaurantId() {
        return currentRestaurantId;
    }

    public String getCurrentRestaurantName() {
        return currentRestaurantName;
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    public void clearCart() {
        cartItems.clear();
        currentRestaurantId = null;
        currentRestaurantName = null;
        notifyCartUpdated();
    }

    private void notifyCartUpdated() {
        if (listener != null) {
            listener.onCartUpdated(getItemCount(), getTotalPrice());
        }
    }

    public int getQuantityForItem(String itemId) {
        for (CartItem item : cartItems) {
            if (item.getMenuItem().getId().equals(itemId)) {
                return item.getQuantity();
            }
        }
        return 0;
    }
}
