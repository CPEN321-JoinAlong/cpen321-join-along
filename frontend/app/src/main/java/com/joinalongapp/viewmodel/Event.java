package com.joinalongapp.viewmodel;

import android.location.Location;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class Event implements Serializable {

    private UUID eventId;
    private UUID eventOwnerId;
    private String title;
    private transient Location location;
    private Date beginningDate;
    private Date endDate;
    private Boolean publicVisibility;
    private int numberOfPeople;

    private String description;

    public Event() {
    }

    public Event(UUID eventId, UUID eventOwnerId, String title, Location location, Date beginningDate, Date endDate, Boolean publicVisibility, int numberOfPeople, String description) {
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

    public String getTitle() {
        return title;
    }

    public Location getLocation() {
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

    public String getDescription() {
        return description;
    }
}
