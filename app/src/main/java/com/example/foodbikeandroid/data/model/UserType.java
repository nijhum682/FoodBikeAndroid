package com.example.foodbikeandroid.data.model;

public enum UserType {
    ADMIN("Admin"),
    REGULAR_USER("Regular User"),
    ENTREPRENEUR("Entrepreneur"),
    DELIVERY_BIKER("Delivery Biker");

    private final String displayName;

    UserType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    public static UserType fromDisplayName(String displayName) {
        for (UserType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        return REGULAR_USER;
    }
}
