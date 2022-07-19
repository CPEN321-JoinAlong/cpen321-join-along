package com.joinalongapp.viewmodel;

import org.json.JSONException;
import org.json.JSONObject;

public class Message implements IDetailsModel {
    private String message;
    private String name;
    private String id;
    private long createdAt;
    private boolean isOwner;

    public Message(String message, String name, String id, long createdAt) {
        this.message = message;
        this.name = name;
        this.id = id;
        this.createdAt = createdAt;
    }

    public Message() {
        // empty default constructor
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

    @Override
    public String toJsonString() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("text", getMessage());
        json.put("participantName", getName());
        json.put("participantID", getId());
        json.put("timeStamp", createdAt);
        json.put("isOwner", isOwner());

        return json.toString();
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("text", getMessage());
        json.put("participantName", getName());
        json.put("participantID", getId());
        json.put("timeStamp", createdAt);
        json.put("isOwner", isOwner());

        return json;
    }

    @Override
    public IDetailsModel populateDetailsFromJson(String jsonBody) throws JSONException {
        JSONObject json = new JSONObject(jsonBody);

        setName((String) json.get("participantName"));
        setId((String) json.get("participantID"));
        setMessage((String) json.get("text"));
        setCreatedAt((Long) Long.valueOf((String) json.get("timeStamp")));

        return this;
    }
}
