package com.joinalongapp.viewmodel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Event implements Serializable, IDetailsModel {

    //TODO; capacity and number of people confusion
    private String eventId;
    private String eventOwnerId;
    private String ownerName;
    private String title;
    private String location;
    private Date beginningDate;
    private Date endDate;
    private Boolean publicVisibility;
    private int numberOfPeople;
    private List<Tag> tags;

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setEventOwnerId(String eventOwnerId) {
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

    private void addTagToInterests(Tag tag) {
        tags.add(tag);
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

    public Event(String eventId, String eventOwnerId, String title, String location, Date beginningDate, Date endDate, Boolean publicVisibility, int numberOfPeople, String description) {
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

    public String getEventId() {
        return eventId;
    }

    public String getEventOwnerId() {
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
        json.put("tags", tags);

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

    public Event populateDetailsFromJson(String jsonBody) throws JSONException {
        JSONObject json = new JSONObject(jsonBody);

        setTitle(json.getString("title"));
        setEventOwnerId(json.getString("eventOwnerID"));

        JSONArray tags = json.getJSONArray("tags");
        for (int i = 0; i < tags.length(); i++) {
            addTagToInterests(new Tag(tags.getString(i)));
        }

        String beginDateString = json.getString("beginningDate");
        //todo: convert to date object, need to know the storage format first
        //setBeginningDate();

        String endDateString = json.getString("endDate");
        //todo: convert to date object, need to know the storage format first
        //setEndDate();

        setPublicVisibility(json.getBoolean("publicVisibility"));
        setNumberOfPeople(json.getInt("numberOfPeople"));
        setLocation(json.getString("location"));
        setDescription(json.getString("description"));

        //todo?: participant list, capacity, event image, event chat


        return this;
    }

}
