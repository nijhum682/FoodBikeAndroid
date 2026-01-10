package com.example.foodbikeandroid.ui.dashboard;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.database.FoodBikeDatabase;
import com.example.foodbikeandroid.data.model.Withdrawal;
import com.example.foodbikeandroid.data.repository.WithdrawalRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AdminEarningsActivity extends AppCompatActivity {

    private TextView tvTotalEarnings;
    private WithdrawalRepository withdrawalRepository;
    
    private double currentTotalEarnings = 0;
    private double currentTotalWithdrawn = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_earnings);

        tvTotalEarnings = findViewById(R.id.tvTotalEarnings);
        MaterialButton btnWithdraw = findViewById(R.id.btnWithdraw);
        MaterialButton btnHistory = findViewById(R.id.btnHistory);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        withdrawalRepository = new WithdrawalRepository(getApplication());

        setupToolbar(toolbar);
        setupDataObservation();
        
        btnWithdraw.setOnClickListener(v -> showWithdrawalMethodDialog());
        btnHistory.setOnClickListener(v -> {
             startActivity(new Intent(this, WithdrawalHistoryActivity.class));
        });
    }

    private void setupToolbar(MaterialToolbar toolbar) {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void setupDataObservation() {
        // Observer Total Earnings (Source)
        FoodBikeDatabase.getInstance(this).adminActionDao().getActionCount().observe(this, count -> {
            currentTotalEarnings = (count != null ? count : 0) * 10.0; // Mock logic preservered
            updateBalanceDisplay();
        });

        // Observe Total Withdrawn
        withdrawalRepository.getTotalWithdrawnAmount().observe(this, amount -> {
            currentTotalWithdrawn = amount != null ? amount : 0;
            updateBalanceDisplay();
        });
    }

    private void updateBalanceDisplay() {
        double balance = currentTotalEarnings - currentTotalWithdrawn;
        tvTotalEarnings.setText(String.format("৳%.2f", balance));
    }

    private void showWithdrawalMethodDialog() {
        String[] methods = {"Bank Account", "Bkash", "Nagad"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Withdrawal Method")
                .setItems(methods, (dialog, which) -> {
                    showAccountDetailsDialog(methods[which]);
                })
                .show();
    }

    private void showAccountDetailsDialog(String method) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etAccountNumber = new EditText(this);
        etAccountNumber.setHint("Account Number");
        layout.addView(etAccountNumber);

        final EditText etAmount = new EditText(this);
        etAmount.setHint("Amount (Max: " + (currentTotalEarnings - currentTotalWithdrawn) + ")");
        etAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etAmount);

        new MaterialAlertDialogBuilder(this)
                .setTitle(method + " Details")
                .setView(layout)
                .setPositiveButton("Next", (dialog, which) -> {
                    String account = etAccountNumber.getText().toString();
                    String amountStr = etAmount.getText().toString();
                    
                    if (account.isEmpty() || amountStr.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount = Double.parseDouble(amountStr);
                    double currentBalance = currentTotalEarnings - currentTotalWithdrawn;

                    if (amount > currentBalance) {
                        Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (amount <= 0) {
                         Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                         return;
                    }

                    showOTPDialog(method, account, amount);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showOTPDialog(String method, String account, double amount) {
        // Generate random 4-digit OTP
        String generatedOTP = String.format("%04d", new java.util.Random().nextInt(10000));
        
        EditText etOTP = new EditText(this);
        etOTP.setHint("Enter OTP");
        etOTP.setInputType(InputType.TYPE_CLASS_NUMBER);
        
        // Show the generated OTP to the user
        Toast.makeText(this, "Your OTP is: " + generatedOTP, Toast.LENGTH_LONG).show();

        new MaterialAlertDialogBuilder(this)
                .setTitle("Enter OTP")
                .setMessage("An OTP has been sent to your registered number.")
                .setView(etOTP)
                .setPositiveButton("Verify", (dialog, which) -> {
                    if (generatedOTP.equals(etOTP.getText().toString())) {
                        showPinDialog(method, account, amount);
                    } else {
                        Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showPinDialog(String method, String account, double amount) {
        // Generate random 4-digit PIN
        String generatedPIN = String.format("%04d", new java.util.Random().nextInt(10000));
        
        // Show the generated PIN to the user
        Toast.makeText(this, "Your PIN is: " + generatedPIN, Toast.LENGTH_LONG).show();
        
        EditText etPin = new EditText(this);
        etPin.setHint("Enter PIN");
        etPin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Enter PIN")
                .setMessage("Enter the PIN to confirm your withdrawal.")
                .setView(etPin)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    if (generatedPIN.equals(etPin.getText().toString())) {
                        processWithdrawal(method, account, amount);
                    } else {
                        Toast.makeText(this, "Invalid PIN", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void processWithdrawal(String method, String account, double amount) {
        // In a real app, verify PIN here
        
        Withdrawal withdrawal = new Withdrawal("admin", amount, method, account); // using "admin" as static username for now
        
        withdrawalRepository.insert(withdrawal, 
            () -> runOnUiThread(() -> {
                Toast.makeText(this, "Your Balance Withdrawal is Successful", Toast.LENGTH_LONG).show();
                // Balance display updates automatically via LiveData
            }),
            () -> runOnUiThread(() -> Toast.makeText(this, "Withdrawal Failed", Toast.LENGTH_SHORT).show())
        );
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
