package com.joinalongapp.viewmodel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Event implements Serializable, IDetailsModel {

    //TODO; capacity and number of people confusion
    private UUID eventId;
    private UUID eventOwnerId;
    private String ownerName;
    private String title;
    private String location;
    private Date beginningDate;
    private Date endDate;
    private Boolean publicVisibility;
    private int numberOfPeople;
    private List<Tag> tags;

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public void setEventOwnerId(UUID eventOwnerId) {
        this.eventOwnerId = eventOwnerId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setBeginningDate(Date beginningDate) {
        this.beginningDate = beginningDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setPublicVisibility(Boolean publicVisibility) {
        this.publicVisibility = publicVisibility;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void setFriends(List<UserProfile> friends) {
        this.friends = friends;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private List<UserProfile> friends;
    private String description;

    public Event() {
    }

    public Event(UUID eventId, UUID eventOwnerId, String title, String location, Date beginningDate, Date endDate, Boolean publicVisibility, int numberOfPeople, String description) {
        this.eventId = eventId;
        this.eventOwnerId = eventOwnerId;
        this.title = title;
        this.location = location;
        this.beginningDate = beginningDate;
        this.endDate = endDate;
        this.publicVisibility = publicVisibility;
        this.numberOfPeople = numberOfPeople;
        this.description = description;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getEventOwnerId() {
        return eventOwnerId;
    }

    //TODO: this is a get request with owner id
    public String getOwnerName() {
        return ownerName;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public Date getBeginningDate() {
        return beginningDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Boolean getPublicVisibility() {
        return publicVisibility;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public List<UserProfile> getFriends() {
        return friends;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getStringListOfTags(){
        List<String> result = new ArrayList<>();
        for(Tag tag : tags){
            result.add(tag.getName());
        }
        return result;
    }

    @Override
    public String toJsonString() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("title", getTitle());

        JSONArray tags = new JSONArray(getStringListOfTags());
        json.put("tags", tags); //TODO fix array in request use jsonarray

        json.put("location", getLocation());
        json.put("numberOfPeople", getNumberOfPeople());
        json.put("description", getDescription());
        json.put("beginningDate", getBeginningDate());
        json.put("endDate", getEndDate());
        json.put("publicVisibility", getPublicVisibility());
        return json.toString();
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("title", getTitle());
        json.put("tags", getStringListOfTags());
        json.put("location", getLocation());
        json.put("numberOfPeople", getNumberOfPeople());
        json.put("description", getDescription());
        json.put("beginningDate", getBeginningDate());
        json.put("endDate", getEndDate());
        json.put("publicVisibility", getPublicVisibility());
        return json;
    }
}
