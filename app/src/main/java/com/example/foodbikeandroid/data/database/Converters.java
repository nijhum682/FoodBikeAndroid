package com.example.foodbikeandroid.data.database;

import androidx.room.TypeConverter;

import com.example.foodbikeandroid.data.model.MenuItem;
import com.example.foodbikeandroid.data.model.UserType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Converters {

    @TypeConverter
    public static String fromUserType(UserType userType) {
        return userType == null ? null : userType.name();
    }

    @TypeConverter
    public static UserType toUserType(String value) {
        return value == null ? null : UserType.valueOf(value);
    }

    @TypeConverter
    public static String fromMenuItemList(List<MenuItem> menuItems) {
        if (menuItems == null) return null;
        JSONArray jsonArray = new JSONArray();
        for (MenuItem item : menuItems) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", item.getName());
                jsonObject.put("description", item.getDescription());
                jsonObject.put("price", item.getPrice());
                jsonObject.put("category", item.getCategory());
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
                        jsonObject.getString("name"),
                        jsonObject.getString("description"),
                        jsonObject.getDouble("price"),
                        jsonObject.getString("category")
                );
                menuItems.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return menuItems;
    }
}
