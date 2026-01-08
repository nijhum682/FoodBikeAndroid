package com.example.foodbikeandroid.data.model;
import java.util.List;

public class Division {

    private String name;
    private String prefix;
    private List<String> districts;

    public Division(String name, String prefix, List<String> districts) {
        this.name = name;
        this.prefix = prefix;
        this.districts = districts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<String> getDistricts() {
        return districts;
    }

    public void setDistricts(List<String> districts) {
        this.districts = districts;
    }
}
