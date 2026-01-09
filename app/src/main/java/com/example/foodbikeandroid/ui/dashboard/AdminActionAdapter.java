package com.example.foodbikeandroid.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbikeandroid.R;
import com.example.foodbikeandroid.data.model.ActionType;
import com.example.foodbikeandroid.data.model.AdminAction;

import java.util.ArrayList;
import java.util.List;

public class AdminActionAdapter extends RecyclerView.Adapter<AdminActionAdapter.ActionViewHolder> {

    private List<AdminAction> actions = new ArrayList<>();

    public void setActions(List<AdminAction> actions) {
        this.actions = actions != null ? actions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_action, parent, false);
        return new ActionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActionViewHolder holder, int position) {
        AdminAction action = actions.get(position);
        holder.bind(action);
    }

    @Override
    public int getItemCount() {
        return actions.size();
    }

    static class ActionViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivActionIcon;
        private final TextView tvActionDescription;
        private final TextView tvAdminName;
        private final TextView tvTimestamp;

        public ActionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivActionIcon = itemView.findViewById(R.id.ivActionIcon);
            tvActionDescription = itemView.findViewById(R.id.tvActionDescription);
            tvAdminName = itemView.findViewById(R.id.tvAdminName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        public void bind(AdminAction action) {
            tvActionDescription.setText(action.getActionDescription());
            tvAdminName.setText(action.getAdminUsername());
            tvTimestamp.setText(action.getFormattedTimestamp());

            int iconRes = getIconForActionType(action.getActionType());
            int colorRes = getColorForActionType(action.getActionType());

            ivActionIcon.setImageResource(iconRes);
            ivActionIcon.setColorFilter(itemView.getContext().getColor(colorRes));
        }

        private int getIconForActionType(ActionType actionType) {
            switch (actionType) {
                case APPROVED_APPLICATION:
                    return R.drawable.ic_check_circle;
                case REJECTED_APPLICATION:
                    return R.drawable.ic_remove;
                case ADDED_RESTAURANT:
                    return R.drawable.ic_add_circle;
                case DELETED_RESTAURANT:
                    return R.drawable.ic_delete;
                case EDITED_MENU:
                case EDITED_MENU_ITEM:
                    return R.drawable.ic_edit;
                case ADDED_MENU_ITEM:
                    return R.drawable.ic_add;
                case DELETED_MENU_ITEM:
                    return R.drawable.ic_delete;
                case TOGGLED_MENU_ITEM_AVAILABILITY:
                    return R.drawable.ic_visibility;
                default:
                    return R.drawable.ic_info;
            }
        }

        private int getColorForActionType(ActionType actionType) {
            switch (actionType) {
                case APPROVED_APPLICATION:
                case ADDED_RESTAURANT:
                case ADDED_MENU_ITEM:
                    return R.color.success;
                case REJECTED_APPLICATION:
                case DELETED_RESTAURANT:
                case DELETED_MENU_ITEM:
                    return R.color.error;
                case EDITED_MENU:
                case EDITED_MENU_ITEM:
                case TOGGLED_MENU_ITEM_AVAILABILITY:
                    return R.color.info;
                default:
                    return R.color.primary;
            }
        }
    }
}
