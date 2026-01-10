package com.example.foodbikeandroid.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.Withdrawal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WithdrawalAdapter extends RecyclerView.Adapter<WithdrawalAdapter.WithdrawalViewHolder> {

    private List<Withdrawal> withdrawals = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy - hh:mm a", Locale.getDefault());

    public void setWithdrawals(List<Withdrawal> withdrawals) {
        this.withdrawals = withdrawals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WithdrawalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_withdrawal, parent, false);
        return new WithdrawalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WithdrawalViewHolder holder, int position) {
        Withdrawal withdrawal = withdrawals.get(position);
        holder.bind(withdrawal);
    }

    @Override
    public int getItemCount() {
        return withdrawals.size();
    }

    class WithdrawalViewHolder extends RecyclerView.ViewHolder {
        TextView tvMethod, tvAmount, tvAccountNumber, tvDate;

        WithdrawalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMethod = itemView.findViewById(R.id.tvMethod);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvAccountNumber = itemView.findViewById(R.id.tvAccountNumber);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        void bind(Withdrawal withdrawal) {
            tvMethod.setText(withdrawal.getMethod());
            tvAmount.setText(String.format("৳%.2f", withdrawal.getAmount()));
            tvAccountNumber.setText(withdrawal.getAccountNumber());
            tvDate.setText(dateFormat.format(new Date(withdrawal.getTimestamp())));
        }
    }
}
