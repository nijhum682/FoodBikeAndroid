package com.example.foodbikeandroid.data.database;

import androidx.room.TypeConverter;

import com.example.foodbikeandroid.data.model.UserType;
public class Converters {

    @TypeConverter
    public static String fromUserType(UserType userType) {
        return userType == null ? null : userType.name();
    }
    @TypeConverter
    public static UserType toUserType(String value) {
        return value == null ? null : UserType.valueOf(value);
    }
}
