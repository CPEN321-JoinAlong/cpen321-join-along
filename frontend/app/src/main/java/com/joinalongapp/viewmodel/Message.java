package com.joinalongapp.viewmodel;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Message implements IDetailsModel {
    public Message() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }

    private String message;
    private String name;
    //todo: change to string
    private String id;
    private long createdAt;
    private boolean isOwner;

    public Message(String message, String name, String id, long createdAt) {
        this.message = message;
        this.name = name;
        this.id = id;
        this.createdAt = createdAt;
    }

//TODO: make sure json keys match with backend
    @Override
    public String toJsonString() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("message", getMessage());
        json.put("name", getName());
        json.put("id", getId());
        json.put("createdAt", createdAt);
        json.put("isOwner", isOwner());
        Log.w("MessageModel", "this may produce unintended results if json keys dont match");
        return json.toString();
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("message", getMessage());
        json.put("name", getName());
        json.put("id", getId());
        json.put("createdAt", createdAt);
        json.put("isOwner", isOwner());
        Log.w("MessageModel", "this may produce unintended results if json keys dont match");
        return json;
    }

    @Override
    public IDetailsModel populateDetailsFromJson(String jsonBody) throws JSONException {
        return null;
    }
}
