package com.example.foodbikeandroid.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodbikeandroid.data.model.User;
import com.example.foodbikeandroid.data.repository.UserRepository;

public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<String> updateMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUpdateSuccess = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        userRepository = UserRepository.getInstance(application);
    }

    public LiveData<User> getCurrentUser() {
        String username = userRepository.getCurrentUsername();
        return userRepository.getUserByUsername(username);
    }

    public LiveData<String> getUpdateMessage() {
        return updateMessage;
    }

    public LiveData<Boolean> getIsUpdateSuccess() {
        return isUpdateSuccess;
    }

    public void updateUser(User user, String newEmail, String newPhone, String newAddress, String newPassword) {
        // Basic validation
        if (newEmail.isEmpty() || newPhone.isEmpty() || newAddress.isEmpty() || newPassword.isEmpty()) {
            updateMessage.setValue("All fields are required");
            isUpdateSuccess.setValue(false);
            return;
        }

        user.setEmail(newEmail);
        user.setPhoneNumber(newPhone);
        user.setAddress(newAddress);
        user.setPassword(newPassword);

        userRepository.updateUser(user, new UserRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                updateMessage.postValue("Profile updated successfully");
                isUpdateSuccess.postValue(true);
            }

            @Override
            public void onError(String message) {
                updateMessage.postValue(message);
                isUpdateSuccess.postValue(false);
            }
        });
    }

    public void clearMessage() {
        updateMessage.setValue(null);
    }
}
