package com.joinalongapp.viewmodel;

import java.util.UUID;

public class Message {
    private String message;
    private String name;
    private UUID id;
    private long createdAt;
    private boolean isOwner;

    public Message(String message, String name, UUID id, long createdAt) {
        this.message = message;
        this.name = name;
        this.id = id;
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isOwner(){
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }
}
