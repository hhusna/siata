package com.siata.client.model;

import java.time.LocalDateTime;

public class Activity {
    private String id;
    private String user;
    private String actionType; // Create, Approve, Reject, Delete, Update
    private String description;
    private String target;
    private String details;
    private LocalDateTime timestamp;

    public Activity() {
    }

    public Activity(String id, String user, String actionType, String description, String target, String details, LocalDateTime timestamp) {
        this.id = id;
        this.user = user;
        this.actionType = actionType;
        this.description = description;
        this.target = target;
        this.details = details;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

