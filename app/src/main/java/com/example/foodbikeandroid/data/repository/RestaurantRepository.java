package com.example.foodbikeandroid.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.foodbikeandroid.data.LocationData;
import com.example.foodbikeandroid.data.database.FoodBikeDatabase;
import com.example.foodbikeandroid.data.database.RestaurantDao;
import com.example.foodbikeandroid.data.model.MenuItem;
import com.example.foodbikeandroid.data.model.Restaurant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RestaurantRepository {

    private static volatile RestaurantRepository INSTANCE;
    private final RestaurantDao restaurantDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    private RestaurantRepository(Context context) {
        FoodBikeDatabase database = FoodBikeDatabase.getInstance(context);
        restaurantDao = database.restaurantDao();
        executorService = Executors.newFixedThreadPool(4);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static RestaurantRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (RestaurantRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RestaurantRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public void initializeSampleData() {
        executorService.execute(() -> {
            int currentCount = restaurantDao.getRestaurantCount();
            // Only initialize sample data if database is completely empty
            if (currentCount == 0) {
                restaurantDao.insertAll(createSampleRestaurants());
            }
        });
    }

    public LiveData<List<Restaurant>> getAllRestaurants() {
        return restaurantDao.getAllRestaurants();
    }

    public LiveData<Restaurant> getRestaurantById(String id) {
        return restaurantDao.getRestaurantById(id);
    }
    
    public void getRestaurantById(String id, RestaurantCallback callback) {
        executorService.execute(() -> {
            try {
                Restaurant restaurant = restaurantDao.getRestaurantByIdSync(id);
                if (restaurant != null) {
                    mainHandler.post(() -> callback.onSuccess(restaurant));
                } else {
                    mainHandler.post(() -> callback.onError("Restaurant not found"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public LiveData<List<Restaurant>> getRestaurantsByDivision(String division) {
        if (division == null || division.equals("All Divisions")) {
            return restaurantDao.getAllRestaurants();
        }
        return restaurantDao.getRestaurantsByDivision(division);
    }

    public LiveData<List<Restaurant>> getRestaurantsByLocation(String division, String district) {
        if (division == null || division.equals("All Divisions")) {
            return restaurantDao.getAllRestaurants();
        }
        if (district == null || district.equals("All Districts")) {
            return restaurantDao.getRestaurantsByDivision(division);
        }
        return restaurantDao.getRestaurantsByLocation(division, district);
    }

    public LiveData<List<Restaurant>> searchRestaurants(String query, String division, String district) {
        if (query == null || query.isEmpty()) {
            return getRestaurantsByLocation(division, district);
        }
        if (division == null || division.equals("All Divisions")) {
            return restaurantDao.searchRestaurants(query);
        }
        if (district == null || district.equals("All Districts")) {
            return restaurantDao.searchRestaurantsInDivision(query, division);
        }
        return restaurantDao.searchRestaurantsInLocation(query, division, district);
    }

    public LiveData<List<Restaurant>> getRestaurantsByCuisine(String cuisineType) {
        return restaurantDao.getRestaurantsByCuisine(cuisineType);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String message);
    }

    public void insert(Restaurant restaurant) {
        executorService.execute(() -> restaurantDao.insert(restaurant));
    }

    public void insert(Restaurant restaurant, OperationCallback callback) {
        executorService.execute(() -> {
            try {
                restaurantDao.insert(restaurant);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void update(Restaurant restaurant) {
        executorService.execute(() -> restaurantDao.update(restaurant));
    }

    public void update(Restaurant restaurant, OperationCallback callback) {
        executorService.execute(() -> {
            try {
                restaurantDao.update(restaurant);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void delete(Restaurant restaurant) {
        executorService.execute(() -> restaurantDao.delete(restaurant));
    }

    public void delete(Restaurant restaurant, OperationCallback callback) {
        executorService.execute(() -> {
            try {
                restaurantDao.delete(restaurant);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    private List<Restaurant> createSampleRestaurants() {
        List<Restaurant> restaurants = new ArrayList<>();
        int id = 1;

        // Dhaka Division - 13 districts
        restaurants.addAll(createDistrictRestaurants(id, "Dhaka", "Dhaka")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Dhaka", "Gazipur")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Dhaka", "Narayanganj")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Dhaka", "Tangail")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Dhaka", "Kishoreganj")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Dhaka", "Manikganj")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Dhaka", "Munshiganj")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Dhaka", "Narsingdi")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Dhaka", "Rajbari")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Dhaka", "Madaripur")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Dhaka", "Shariatpur")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Dhaka", "Faridpur")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Dhaka", "Gopalganj")); id += 4;

        // Chittagong Division - 11 districts
        restaurants.addAll(createDistrictRestaurants(id, "Chittagong", "Chittagong")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Chittagong", "Cox's Bazar")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Chittagong", "Comilla")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Chittagong", "Rangamati")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Chittagong", "Bandarban")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Chittagong", "Khagrachari")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Chittagong", "Feni")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Chittagong", "Lakshmipur")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Chittagong", "Noakhali")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Chittagong", "Brahmanbaria")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Chittagong", "Chandpur")); id += 4;

        // Sylhet Division - 4 districts
        restaurants.addAll(createDistrictRestaurants(id, "Sylhet", "Sylhet")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Sylhet", "Moulvibazar")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Sylhet", "Habiganj")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Sylhet", "Sunamganj")); id += 4;

        // Rajshahi Division - 8 districts
        restaurants.addAll(createDistrictRestaurants(id, "Rajshahi", "Rajshahi")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Rajshahi", "Bogra")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Rajshahi", "Pabna")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Rajshahi", "Sirajganj")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Rajshahi", "Natore")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Rajshahi", "Naogaon")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Rajshahi", "Chapainawabganj")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Rajshahi", "Joypurhat")); id += 4;

        // Khulna Division - 10 districts
        restaurants.addAll(createDistrictRestaurants(id, "Khulna", "Khulna")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Khulna", "Jessore")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Khulna", "Satkhira")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Khulna", "Bagerhat")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Khulna", "Narail")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Khulna", "Chuadanga")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Khulna", "Kushtia")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Khulna", "Meherpur")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Khulna", "Magura")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Khulna", "Jhenaidah")); id += 4;

        // Barisal Division - 6 districts
        restaurants.addAll(createDistrictRestaurants(id, "Barisal", "Barisal")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Barisal", "Bhola")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Barisal", "Patuakhali")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Barisal", "Pirojpur")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Barisal", "Jhalokathi")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Barisal", "Barguna")); id += 4;

        // Rangpur Division - 8 districts
        restaurants.addAll(createDistrictRestaurants(id, "Rangpur", "Rangpur")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Rangpur", "Dinajpur")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Rangpur", "Nilphamari")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Rangpur", "Thakurgaon")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Rangpur", "Panchagarh")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Rangpur", "Lalmonirhat")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Rangpur", "Kurigram")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Rangpur", "Gaibandha")); id += 4;

        // Mymensingh Division - 4 districts
        restaurants.addAll(createDistrictRestaurants(id, "Mymensingh", "Mymensingh")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Mymensingh", "Jamalpur")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Mymensingh", "Sherpur")); id += 4;
        restaurants.addAll(createDistrictRestaurants(id, "Mymensingh", "Netrokona")); id += 4;

        return restaurants;
    }

    private List<Restaurant> createDistrictRestaurants(int startId, String division, String district) {
        List<Restaurant> restaurants = new ArrayList<>();
        String prefix = division.substring(0, 2).toUpperCase();
        
        restaurants.add(createRestaurant(
            String.format("%s%03d", prefix, startId),
            district + " Kitchen",
            division, district, district + " Sadar",
            4.5, "Bangladeshi", createBangladeshiMenu()));
        
        restaurants.add(createRestaurant(
            String.format("%s%03d", prefix, startId + 1),
            district + " Pizza House",
            division, district, district + " Center",
            4.3, "Pizza", createPizzaMenu()));
        
        restaurants.add(createRestaurant(
            String.format("%s%03d", prefix, startId + 2),
            district + " Cafe",
            division, district, district + " Plaza",
            4.4, "Cafe", createCafeMenu()));
        
        restaurants.add(createRestaurant(
            String.format("%s%03d", prefix, startId + 3),
            district + " Grill & BBQ",
            division, district, district + " Road",
            4.6, "Grilled", createGrilledMenu()));
        
        return restaurants;
    }

    private Restaurant createRestaurant(String id, String name, String division, String district,
                                        String address, double rating, String cuisineType, List<MenuItem> menu) {
        Restaurant restaurant = new Restaurant(id, name, division, district, address);
        restaurant.setRating(rating);
        restaurant.setCuisineType(cuisineType);
        restaurant.setMenuItems(menu);
        return restaurant;
    }

    private List<MenuItem> createBangladeshiMenu() {
        return Arrays.asList(
                new MenuItem("Kacchi Biryani", "Aromatic rice with tender mutton", 350.0, "Main"),
                new MenuItem("Beef Tehari", "Spiced rice with beef", 200.0, "Main"),
                new MenuItem("Chicken Roast", "Whole roasted chicken with spices", 450.0, "Main"),
                new MenuItem("Beef Bhuna", "Slow-cooked beef curry", 280.0, "Main"),
                new MenuItem("Borhani", "Traditional yogurt drink", 50.0, "Drinks")
        );
    }

    private List<MenuItem> createPizzaMenu() {
        return Arrays.asList(
                new MenuItem("Margherita Pizza", "Classic tomato and mozzarella", 550.0, "Pizza"),
                new MenuItem("Pepperoni Pizza", "Spicy pepperoni with cheese", 650.0, "Pizza"),
                new MenuItem("BBQ Chicken Pizza", "Grilled chicken with BBQ sauce", 700.0, "Pizza"),
                new MenuItem("Veggie Supreme", "Loaded with fresh vegetables", 600.0, "Pizza"),
                new MenuItem("Garlic Bread", "Crispy bread with garlic butter", 180.0, "Sides")
        );
    }

    private List<MenuItem> createBurgerMenu() {
        return Arrays.asList(
                new MenuItem("Classic Burger", "Beef patty with fresh vegetables", 250.0, "Burger"),
                new MenuItem("Cheese Burger", "Double cheese with beef", 300.0, "Burger"),
                new MenuItem("Chicken Burger", "Crispy chicken fillet", 280.0, "Burger"),
                new MenuItem("French Fries", "Crispy golden fries", 120.0, "Sides"),
                new MenuItem("Onion Rings", "Crispy fried onion rings", 150.0, "Sides")
        );
    }

    private List<MenuItem> createFastFoodMenu() {
        return Arrays.asList(
                new MenuItem("Fried Chicken", "Crispy fried chicken pieces", 350.0, "Main"),
                new MenuItem("Chicken Wings", "Spicy buffalo wings", 280.0, "Main"),
                new MenuItem("Fish & Chips", "Battered fish with fries", 320.0, "Main"),
                new MenuItem("Nachos", "Tortilla chips with cheese", 200.0, "Snacks"),
                new MenuItem("Soft Drink", "Chilled carbonated beverage", 60.0, "Drinks")
        );
    }

    private List<MenuItem> createGrilledMenu() {
        return Arrays.asList(
                new MenuItem("Grilled Chicken", "Peri-peri grilled chicken", 450.0, "Main"),
                new MenuItem("Beef Steak", "Juicy grilled steak", 650.0, "Main"),
                new MenuItem("BBQ Ribs", "Tender BBQ pork ribs", 550.0, "Main"),
                new MenuItem("Grilled Fish", "Fresh fish with herbs", 480.0, "Main"),
                new MenuItem("Coleslaw", "Fresh cabbage salad", 100.0, "Sides")
        );
    }

    private List<MenuItem> createSeafoodMenu() {
        return Arrays.asList(
                new MenuItem("Grilled Prawns", "Jumbo prawns with garlic butter", 650.0, "Main"),
                new MenuItem("Fish Curry", "Traditional Bengali fish curry", 350.0, "Main"),
                new MenuItem("Crab Masala", "Spicy crab curry", 550.0, "Main"),
                new MenuItem("Fried Hilsha", "Fried Hilsa fish", 450.0, "Main"),
                new MenuItem("Prawn Biryani", "Rice with prawns", 400.0, "Main")
        );
    }

    private List<MenuItem> createCafeMenu() {
        return Arrays.asList(
                new MenuItem("Cappuccino", "Espresso with steamed milk", 180.0, "Coffee"),
                new MenuItem("Latte", "Smooth espresso latte", 200.0, "Coffee"),
                new MenuItem("Club Sandwich", "Triple-decker sandwich", 280.0, "Snacks"),
                new MenuItem("Pasta Alfredo", "Creamy white sauce pasta", 350.0, "Main"),
                new MenuItem("Chocolate Brownie", "Warm chocolate brownie", 150.0, "Dessert")
        );
    }

    private List<MenuItem> createDessertMenu() {
        return Arrays.asList(
                new MenuItem("Mishti Doi", "Sweet yogurt", 80.0, "Dessert"),
                new MenuItem("Rasgulla", "Soft cottage cheese balls in syrup", 100.0, "Dessert"),
                new MenuItem("Gulab Jamun", "Deep-fried milk solids in syrup", 120.0, "Dessert"),
                new MenuItem("Firni", "Rice pudding", 90.0, "Dessert"),
                new MenuItem("Ice Cream", "Various flavors", 150.0, "Dessert")
        );
    }
    
    public interface RestaurantCallback {
        void onSuccess(Restaurant restaurant);
        void onError(String error);
    }
}
