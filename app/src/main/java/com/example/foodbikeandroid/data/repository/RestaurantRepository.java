package com.example.foodbikeandroid.data.repository;

import android.content.Context;

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

    private RestaurantRepository(Context context) {
        FoodBikeDatabase database = FoodBikeDatabase.getInstance(context);
        restaurantDao = database.restaurantDao();
        executorService = Executors.newFixedThreadPool(4);
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
            if (restaurantDao.getRestaurantCount() == 0) {
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

    public void insert(Restaurant restaurant) {
        executorService.execute(() -> restaurantDao.insert(restaurant));
    }

    public void update(Restaurant restaurant) {
        executorService.execute(() -> restaurantDao.update(restaurant));
    }

    public void delete(Restaurant restaurant) {
        executorService.execute(() -> restaurantDao.delete(restaurant));
    }

    private List<Restaurant> createSampleRestaurants() {
        List<Restaurant> restaurants = new ArrayList<>();

        restaurants.add(createRestaurant("DH001", "Kacchi Bhai", "Dhaka", "Dhaka", "Dhanmondi Road 27", 4.7, "Bangladeshi", createBangladeshiMenu()));
        restaurants.add(createRestaurant("DH002", "Pizza Hut Dhaka", "Dhaka", "Dhaka", "Gulshan Avenue", 4.5, "Pizza", createPizzaMenu()));
        restaurants.add(createRestaurant("DH003", "Sultan's Dine", "Dhaka", "Dhaka", "Uttara Sector 11", 4.8, "Bangladeshi", createBangladeshiMenu()));
        restaurants.add(createRestaurant("DH004", "Chillox", "Dhaka", "Dhaka", "Banani Road 11", 4.3, "Fast Food", createFastFoodMenu()));
        restaurants.add(createRestaurant("DH005", "Nando's Dhaka", "Dhaka", "Dhaka", "Dhanmondi Road 2", 4.6, "Grilled", createGrilledMenu()));
        restaurants.add(createRestaurant("DH006", "Star Kabab", "Dhaka", "Gazipur", "Tongi Bazar", 4.4, "Bangladeshi", createBangladeshiMenu()));
        restaurants.add(createRestaurant("DH007", "Burger King", "Dhaka", "Narayanganj", "Chashara", 4.2, "Burger", createBurgerMenu()));
        restaurants.add(createRestaurant("DH008", "Cafe Mango", "Dhaka", "Tangail", "Tangail Sadar", 4.1, "Cafe", createCafeMenu()));

        restaurants.add(createRestaurant("CH001", "Mezban Restaurant", "Chittagong", "Chittagong", "GEC Circle", 4.6, "Bangladeshi", createBangladeshiMenu()));
        restaurants.add(createRestaurant("CH002", "Pizza Inn CTG", "Chittagong", "Chittagong", "Agrabad C/A", 4.4, "Pizza", createPizzaMenu()));
        restaurants.add(createRestaurant("CH003", "Sea Pearl", "Chittagong", "Cox's Bazar", "Marine Drive", 4.8, "Seafood", createSeafoodMenu()));
        restaurants.add(createRestaurant("CH004", "Beach Cafe", "Chittagong", "Cox's Bazar", "Laboni Beach", 4.5, "Cafe", createCafeMenu()));
        restaurants.add(createRestaurant("CH005", "Comilla Sweets", "Chittagong", "Comilla", "Kandirpar", 4.3, "Dessert", createDessertMenu()));
        restaurants.add(createRestaurant("CH006", "Hill View Restaurant", "Chittagong", "Rangamati", "Reserve Bazar", 4.2, "Bangladeshi", createBangladeshiMenu()));

        restaurants.add(createRestaurant("SY001", "Panshi Restaurant", "Sylhet", "Sylhet", "Zindabazar", 4.7, "Bangladeshi", createBangladeshiMenu()));
        restaurants.add(createRestaurant("SY002", "Seven Star", "Sylhet", "Sylhet", "Amberkhana", 4.5, "Bangladeshi", createBangladeshiMenu()));
        restaurants.add(createRestaurant("SY003", "Tea Valley", "Sylhet", "Moulvibazar", "Srimangal", 4.6, "Cafe", createCafeMenu()));
        restaurants.add(createRestaurant("SY004", "Habiganj Food Court", "Sylhet", "Habiganj", "Habiganj Sadar", 4.1, "Fast Food", createFastFoodMenu()));

        restaurants.add(createRestaurant("RJ001", "Rajshahi Kitchen", "Rajshahi", "Rajshahi", "Saheb Bazar", 4.5, "Bangladeshi", createBangladeshiMenu()));
        restaurants.add(createRestaurant("RJ002", "Mango Garden", "Rajshahi", "Rajshahi", "Padma Garden", 4.4, "Cafe", createCafeMenu()));
        restaurants.add(createRestaurant("RJ003", "Bogra Doi Ghor", "Rajshahi", "Bogra", "Satmatha", 4.8, "Dessert", createDessertMenu()));
        restaurants.add(createRestaurant("RJ004", "Pabna Restaurant", "Rajshahi", "Pabna", "Pabna Sadar", 4.2, "Bangladeshi", createBangladeshiMenu()));

        restaurants.add(createRestaurant("KH001", "Khulna Food Plaza", "Khulna", "Khulna", "Shibbari More", 4.4, "Bangladeshi", createBangladeshiMenu()));
        restaurants.add(createRestaurant("KH002", "Sundarbans Kitchen", "Khulna", "Khulna", "Khalishpur", 4.3, "Seafood", createSeafoodMenu()));
        restaurants.add(createRestaurant("KH003", "Jessore Biryani House", "Khulna", "Jessore", "Monihar", 4.6, "Bangladeshi", createBangladeshiMenu()));
        restaurants.add(createRestaurant("KH004", "Satkhira Sweets", "Khulna", "Satkhira", "Satkhira Sadar", 4.1, "Dessert", createDessertMenu()));

        restaurants.add(createRestaurant("BA001", "Barisal River View", "Barisal", "Barisal", "Sadar Road", 4.5, "Bangladeshi", createBangladeshiMenu()));
        restaurants.add(createRestaurant("BA002", "Bhola Fish Corner", "Barisal", "Bhola", "Bhola Sadar", 4.6, "Seafood", createSeafoodMenu()));
        restaurants.add(createRestaurant("BA003", "Patuakhali Kitchen", "Barisal", "Patuakhali", "Patuakhali Sadar", 4.2, "Bangladeshi", createBangladeshiMenu()));

        restaurants.add(createRestaurant("RP001", "Rangpur Royal Dine", "Rangpur", "Rangpur", "Dhap", 4.5, "Bangladeshi", createBangladeshiMenu()));
        restaurants.add(createRestaurant("RP002", "Dinajpur Delights", "Rangpur", "Dinajpur", "Munshipara", 4.4, "Bangladeshi", createBangladeshiMenu()));
        restaurants.add(createRestaurant("RP003", "Northern Pizza", "Rangpur", "Rangpur", "Jahaj Company More", 4.3, "Pizza", createPizzaMenu()));
        restaurants.add(createRestaurant("RP004", "Nilphamari Cafe", "Rangpur", "Nilphamari", "Nilphamari Sadar", 4.1, "Cafe", createCafeMenu()));

        restaurants.add(createRestaurant("MY001", "Mymensingh Food Hub", "Mymensingh", "Mymensingh", "Ganginar Par", 4.4, "Bangladeshi", createBangladeshiMenu()));
        restaurants.add(createRestaurant("MY002", "BAU Cafeteria", "Mymensingh", "Mymensingh", "BAU Campus", 4.2, "Cafe", createCafeMenu()));
        restaurants.add(createRestaurant("MY003", "Jamalpur Kitchen", "Mymensingh", "Jamalpur", "Jamalpur Sadar", 4.3, "Bangladeshi", createBangladeshiMenu()));
        restaurants.add(createRestaurant("MY004", "Sherpur Sweets", "Mymensingh", "Sherpur", "Sherpur Sadar", 4.5, "Dessert", createDessertMenu()));

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
}
