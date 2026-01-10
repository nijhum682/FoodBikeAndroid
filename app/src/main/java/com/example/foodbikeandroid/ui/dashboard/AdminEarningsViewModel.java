package com.example.foodbikeandroid.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.foodbikeandroid.data.database.FoodBikeDatabase;
import com.example.foodbikeandroid.data.repository.WithdrawalRepository;
import com.example.foodbikeandroid.data.model.Withdrawal;

public class AdminEarningsViewModel extends AndroidViewModel {

    private final WithdrawalRepository withdrawalRepository;
    private final MediatorLiveData<Double> currentBalance = new MediatorLiveData<>();
    private final LiveData<Double> totalEarnings; // From AdminActionDao (proxy)
    private final LiveData<Double> totalWithdrawn;

    public AdminEarningsViewModel(@NonNull Application application) {
        super(application);
        withdrawalRepository = new WithdrawalRepository(application);
        
        // Simulating Total Earnings from Admin Actions (as per previous logic)
        // In a real app, this would come from an OrderRepository or similar
        totalEarnings = new MediatorLiveData<>();
        // Note: The previous logic calculated earnings = actionCount * 10
        // We will keep using AdminActionDao for the base "Earnings" source
        LiveData<Integer> actionCount = FoodBikeDatabase.getInstance(application).adminActionDao().getActionCount();
        
        // We need to transform actionCount to earnings double
        // Since we can't easily transform inside ViewModel constructor without Transformations.map
        // We will observe it in Activity and set value, OR use a custom repository method.
        // For simplicity, let's assume we can get it.
        
        // Better approach: Let Activity observe both and calculate balance?
        // Or do it here using MediatorLiveData.
        
        totalWithdrawn = withdrawalRepository.getTotalWithdrawnAmount();
    }
    
    public WithdrawalRepository getRepository() {
        return withdrawalRepository;
    }
    
    public LiveData<Double> getTotalWithdrawn() {
        return totalWithdrawn;
    }
}
