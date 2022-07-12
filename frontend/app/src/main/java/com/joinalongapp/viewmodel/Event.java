package com.joinalongapp.viewmodel;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private int numberOfPeopleAllowed;
    private List<Tag> tags = new ArrayList<>();
    private List<String> members = new ArrayList<>();
    private String description;
    private int currentNumPeopleRegistered;

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
        this.numberOfPeopleAllowed = numberOfPeople;
        this.description = description;
    }

    public int getCurrentNumPeopleRegistered() {
        return currentNumPeopleRegistered;
    }

    public void setCurrentNumPeopleRegistered(int currentNumPeopleRegistered) {
        this.currentNumPeopleRegistered = currentNumPeopleRegistered;
    }

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

    public void setNumberOfPeopleAllowed(int numberOfPeopleAllowed) {
        this.numberOfPeopleAllowed = numberOfPeopleAllowed;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    private void addTagToInterests(Tag tag) {
        tags.add(tag);
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void addMemberToList(String member) {
        this.members.add(member);
    }

    public void setDescription(String description) {
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

    public int getNumberOfPeopleAllowed() {
        return numberOfPeopleAllowed;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public List<String> getMembers() {
        return members;
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
        return toJson().toString();
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("title", getTitle());

        JSONArray tags = new JSONArray(getStringListOfTags());
        json.put("tags", tags);

        json.put("location", getLocation());
        json.put("numberOfPeople", getNumberOfPeopleAllowed());
        json.put("description", getDescription());
        json.put("beginningDate", getBeginningDate());
        json.put("endDate", getEndDate());
        json.put("publicVisibility", getPublicVisibility());
        json.put("eventOwnerID", getEventOwnerId());
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

        SimpleDateFormat serverSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
//        "2022-07-11T07:00:00.000Z"

        String beginDateString = json.getString("beginningDate");
        try {
            Date date = serverSDF.parse(beginDateString.substring(0, beginDateString.length()-2));
            setBeginningDate(date);
        } catch (ParseException e){
            Log.d("Event", "helpppppppppppppp");
        }

        //todo: convert to date object, need to know the storage format first
        //setBeginningDate();

        String endDateString = json.getString("endDate");
        try {
            Date date = serverSDF.parse(endDateString.substring(0, endDateString.length()-2));
            setEndDate(date);
        } catch (ParseException e){
            Log.d("Event", "helpppppppppppppp");
        }
        //todo: convert to date object, need to know the storage format first
        //setEndDate();

        setPublicVisibility(json.getBoolean("publicVisibility"));
        setNumberOfPeopleAllowed(json.getInt("numberOfPeople"));
        setLocation(json.getString("location"));
        setDescription(json.getString("description"));

        JSONArray members = json.getJSONArray("participants");
        for (int i = 0; i < members.length(); i++) {
            addMemberToList(members.getString(i));
        }

        setCurrentNumPeopleRegistered(json.getInt("currCapacity"));

        //todo?: capacity, event image, event chat


        return this;
    }

}
