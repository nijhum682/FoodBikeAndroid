package com.example.foodbikeandroid.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.foodbikeandroid.data.database.Converters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "admin_actions")
@TypeConverters(Converters.class)
public class AdminAction {

    @PrimaryKey
    @NonNull
    private String actionId;
    private String adminUsername;
    private ActionType actionType;
    private String targetName;
    private String details;
    private long timestamp;

    public AdminAction(@NonNull String actionId, String adminUsername, ActionType actionType,
                       String targetName, String details, long timestamp) {
        this.actionId = actionId;
        this.adminUsername = adminUsername;
        this.actionType = actionType;
        this.targetName = targetName;
        this.details = details;
        this.timestamp = timestamp;
    }

    @Ignore
    public AdminAction(String adminUsername, ActionType actionType, String targetName, String details) {
        this("ACT_" + System.currentTimeMillis(), adminUsername, actionType, targetName, details,
                System.currentTimeMillis());
    }

    // Required for Firebase
    public AdminAction() {
    }

    @NonNull
    public String getActionId() {
        return actionId;
    }

    public void setActionId(@NonNull String actionId) {
        this.actionId = actionId;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getActionDescription() {
        String actionText;
        switch (actionType) {
            case APPROVED_APPLICATION:
                actionText = "Approved application for " + targetName;
                break;
            case REJECTED_APPLICATION:
                actionText = "Rejected application for " + targetName;
                break;
            case ADDED_RESTAURANT:
                actionText = "Added restaurant " + targetName;
                break;
            case DELETED_RESTAURANT:
                actionText = "Deleted restaurant " + targetName;
                break;
            case EDITED_MENU:
                actionText = "Edited menu for " + targetName;
                break;
            case ADDED_MENU_ITEM:
                actionText = "Added menu item to " + targetName;
                break;
            case EDITED_MENU_ITEM:
                actionText = "Edited menu item for " + targetName;
                break;
            case DELETED_MENU_ITEM:
                actionText = "Deleted menu item from " + targetName;
                break;
            case TOGGLED_MENU_ITEM_AVAILABILITY:
                actionText = "Toggled availability for " + targetName;
                break;
            default:
                actionText = "Updated " + targetName;
                break;
        }
        if (details != null && !details.isEmpty()) {
            actionText += " (" + details + ")";
        }
        return actionText;
    }

    public String getFormattedTimestamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return formatter.format(new Date(timestamp));
    }
}
