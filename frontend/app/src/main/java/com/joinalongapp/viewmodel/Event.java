package com.joinalongapp.viewmodel;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Event implements Serializable {

    private UUID eventId;
    private UUID eventOwnerId;
    private String ownerName;
    private String title;
    private String location;
    private Date beginningDate;
    private Date endDate;
    private Boolean publicVisibility;
    private int numberOfPeople;
    private List<String> tags;

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

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private List<String> friends;
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

    public List<String> getTags() {
        return tags;
    }

    public List<String> getFriends() {
        return friends;
    }

    public String getDescription() {
        return description;
    }
}
