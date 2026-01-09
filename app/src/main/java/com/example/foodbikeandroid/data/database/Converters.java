package com.example.foodbikeandroid.data.database;

import androidx.room.TypeConverter;

import com.example.foodbikeandroid.data.model.ActionType;
import com.example.foodbikeandroid.data.model.ApplicationStatus;
import com.example.foodbikeandroid.data.model.CartItem;
import com.example.foodbikeandroid.data.model.MenuItem;
import com.example.foodbikeandroid.data.model.OrderStatus;
import com.example.foodbikeandroid.data.model.PaymentMethod;
import com.example.foodbikeandroid.data.model.UserType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class Converters {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @TypeConverter
    public static String fromLocalDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(formatter);
    }

    @TypeConverter
    public static LocalDateTime toLocalDateTime(String value) {
        return value == null ? null : LocalDateTime.parse(value, formatter);
    }

    @TypeConverter
    public static String fromApplicationStatus(ApplicationStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static ApplicationStatus toApplicationStatus(String value) {
        return value == null ? null : ApplicationStatus.valueOf(value);
    }

    @TypeConverter
    public static String fromUserType(UserType userType) {
        return userType == null ? null : userType.name();
    }

    @TypeConverter
    public static UserType toUserType(String value) {
        return value == null ? null : UserType.valueOf(value);
    }

    @TypeConverter
    public static String fromActionType(ActionType actionType) {
        return actionType == null ? null : actionType.name();
    }

    @TypeConverter
    public static ActionType toActionType(String value) {
        return value == null ? null : ActionType.valueOf(value);
    }

    @TypeConverter
    public static String fromOrderStatus(OrderStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static OrderStatus toOrderStatus(String value) {
        return value == null ? null : OrderStatus.valueOf(value);
    }

    @TypeConverter
    public static String fromPaymentMethod(PaymentMethod method) {
        return method == null ? null : method.name();
    }

    @TypeConverter
    public static PaymentMethod toPaymentMethod(String value) {
        return value == null ? null : PaymentMethod.valueOf(value);
    }

    @TypeConverter
    public static String fromMenuItemList(List<MenuItem> menuItems) {
        if (menuItems == null) return null;
        JSONArray jsonArray = new JSONArray();
        for (MenuItem item : menuItems) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", item.getId());
                jsonObject.put("name", item.getName());
                jsonObject.put("description", item.getDescription());
                jsonObject.put("price", item.getPrice());
                jsonObject.put("category", item.getCategory());
                jsonObject.put("available", item.isAvailable());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray.toString();
    }

    @TypeConverter
    public static List<MenuItem> toMenuItemList(String value) {
        if (value == null) return null;
        List<MenuItem> menuItems = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(value);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                MenuItem item = new MenuItem(
                        jsonObject.optString("id", "ITEM_" + System.currentTimeMillis() + i),
                        jsonObject.getString("name"),
                        jsonObject.getString("description"),
                        jsonObject.getDouble("price"),
                        jsonObject.getString("category"),
                        jsonObject.optBoolean("available", true)
                );
                menuItems.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return menuItems;
    }

    @TypeConverter
    public static String fromCartItemList(List<CartItem> cartItems) {
        if (cartItems == null) return null;
        JSONArray jsonArray = new JSONArray();
        for (CartItem cartItem : cartItems) {
            try {
                JSONObject jsonObject = new JSONObject();
                MenuItem item = cartItem.getMenuItem();
                jsonObject.put("id", item.getId());
                jsonObject.put("name", item.getName());
                jsonObject.put("description", item.getDescription());
                jsonObject.put("price", item.getPrice());
                jsonObject.put("category", item.getCategory());
                jsonObject.put("available", item.isAvailable());
                jsonObject.put("quantity", cartItem.getQuantity());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray.toString();
    }

    @TypeConverter
    public static List<CartItem> toCartItemList(String value) {
        if (value == null) return null;
        List<CartItem> cartItems = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(value);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                MenuItem menuItem = new MenuItem(
                        jsonObject.optString("id", "ITEM_" + System.currentTimeMillis() + i),
                        jsonObject.getString("name"),
                        jsonObject.getString("description"),
                        jsonObject.getDouble("price"),
                        jsonObject.getString("category"),
                        jsonObject.optBoolean("available", true)
                );
                int quantity = jsonObject.getInt("quantity");
                cartItems.add(new CartItem(menuItem, quantity));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cartItems;
    }
}
