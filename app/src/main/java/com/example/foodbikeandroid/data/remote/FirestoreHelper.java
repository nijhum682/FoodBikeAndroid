package com.example.foodbikeandroid.data.remote;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreHelper {

    private static volatile FirestoreHelper INSTANCE;
    private final FirebaseFirestore db;

    // Collection Constants
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_RESTAURANTS = "restaurants";
    private static final String COLLECTION_ORDERS = "orders";
    private static final String COLLECTION_REVIEWS = "reviews";
    private static final String COLLECTION_WITHDRAWALS = "withdrawals";
    private static final String COLLECTION_APPLICATIONS = "restaurant_applications";
    private static final String COLLECTION_ADMIN_ACTIONS = "admin_actions";
    
    // Sub-collection Constants
    private static final String SUB_COLLECTION_MENU = "menu";

    private FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public static FirestoreHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (FirestoreHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FirestoreHelper();
                }
            }
        }
        return INSTANCE;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public CollectionReference getUsersCollection() {
        return db.collection(COLLECTION_USERS);
    }

    public CollectionReference getRestaurantsCollection() {
        return db.collection(COLLECTION_RESTAURANTS);
    }
    
    public CollectionReference getMenuCollection(String restaurantId) {
        return getRestaurantsCollection().document(restaurantId).collection(SUB_COLLECTION_MENU);
    }

    public CollectionReference getOrdersCollection() {
        return db.collection(COLLECTION_ORDERS);
    }

    public CollectionReference getReviewsCollection() {
        return db.collection(COLLECTION_REVIEWS);
    }

    public CollectionReference getWithdrawalsCollection() {
        return db.collection(COLLECTION_WITHDRAWALS);
    }

    public CollectionReference getApplicationsCollection() {
        return db.collection(COLLECTION_APPLICATIONS);
    }
    
    public CollectionReference getAdminActionsCollection() {
        return db.collection(COLLECTION_ADMIN_ACTIONS);
    }
}
