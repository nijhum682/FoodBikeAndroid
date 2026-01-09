package com.example.foodbikeandroid.ui.dashboard;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.AdminAction;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminRecentActivityAdapter extends RecyclerView.Adapter<AdminRecentActivityAdapter.ViewHolder> {
    private List<AdminAction> actions = new ArrayList<>();

    public void setActions(List<AdminAction> actions) {
        this.actions = actions != null ? actions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_recent_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminAction action = actions.get(position);
        holder.tvType.setText(action.getActionType().toString());
        holder.tvDetails.setText(action.getDetails());
        holder.tvRestaurant.setText(action.getTargetName());
        holder.tvTime.setText(DateFormat.format("yyyy-MM-dd HH:mm", new Date(action.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return actions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDetails, tvRestaurant, tvTime;
        ViewHolder(View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvRestaurant = itemView.findViewById(R.id.tvRestaurant);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
