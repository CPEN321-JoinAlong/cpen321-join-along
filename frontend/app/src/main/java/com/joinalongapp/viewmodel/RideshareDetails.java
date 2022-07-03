package com.joinalongapp.viewmodel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.time.LocalDateTime;

public class RideshareDetails implements Serializable {
    private String title;
    private String pickupLocation;
    private String destination;
    private LocalDateTime pickUpDate;
    private int numPeople;
    private boolean shareCost;
    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDateTime getPickUpDate() {
        return pickUpDate;
    }

    public void setPickUpDate(LocalDateTime pickUpDate) {
        this.pickUpDate = pickUpDate;
    }

    public int getNumPeople() {
        return numPeople;
    }

    public void setNumPeople(int numPeople) {
        this.numPeople = numPeople;
    }

    public boolean isShareCost() {
        return shareCost;
    }

    public void setShareCost(boolean shareCost) {
        this.shareCost = shareCost;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toJsonString() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("title", getTitle());
        json.put("pickUpLocation", getPickupLocation());
        json.put("destination", getDestination());
        json.put("pickUpDate", getPickUpDate());
        json.put("numPeople", getNumPeople());
        json.put("shareCost", isShareCost());
        json.put("description", getDescription());

        return json.toString();
    }

}