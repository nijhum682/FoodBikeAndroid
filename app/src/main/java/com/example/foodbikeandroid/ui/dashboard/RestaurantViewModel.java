package com.example.foodbikeandroid.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodbikeandroid.data.LocationData;
import com.example.foodbikeandroid.data.model.Restaurant;
import com.example.foodbikeandroid.data.repository.RestaurantRepository;

import java.util.List;

public class RestaurantViewModel extends AndroidViewModel {

    private final RestaurantRepository repository;
    private final MutableLiveData<String> selectedDivision = new MutableLiveData<>("All Divisions");
    private final MutableLiveData<String> selectedDistrict = new MutableLiveData<>("All Districts");
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MediatorLiveData<List<Restaurant>> restaurants = new MediatorLiveData<>();
    private LiveData<List<Restaurant>> currentSource;

    public RestaurantViewModel(@NonNull Application application) {
        super(application);
        repository = RestaurantRepository.getInstance(application);
        repository.initializeSampleData();
        updateRestaurants();
    }

    public LiveData<List<Restaurant>> getRestaurants() {
        return restaurants;
    }

    public MutableLiveData<String> getSelectedDivision() {
        return selectedDivision;
    }

    public MutableLiveData<String> getSelectedDistrict() {
        return selectedDistrict;
    }

    public void setSelectedDivision(String division) {
        selectedDivision.setValue(division);
        selectedDistrict.setValue("All Districts");
        updateRestaurants();
    }

    public void setSelectedDistrict(String district) {
        selectedDistrict.setValue(district);
        updateRestaurants();
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
        updateRestaurants();
    }

    public List<String> getDivisions() {
        return LocationData.getAllDivisions();
    }

    public List<String> getDistrictsForCurrentDivision() {
        String division = selectedDivision.getValue();
        return LocationData.getDistrictsForDivision(division);
    }

    private void updateRestaurants() {
        if (currentSource != null) {
            restaurants.removeSource(currentSource);
        }

        String division = selectedDivision.getValue();
        String district = selectedDistrict.getValue();
        String query = searchQuery.getValue();

        currentSource = repository.searchRestaurants(query, division, district);
        restaurants.addSource(currentSource, restaurants::setValue);
    }

    public LiveData<Restaurant> getRestaurantById(String id) {
        return repository.getRestaurantById(id);
    }
}
