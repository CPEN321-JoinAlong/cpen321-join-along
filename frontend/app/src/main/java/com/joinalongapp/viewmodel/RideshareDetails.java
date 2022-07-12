package com.joinalongapp.viewmodel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;

/**
 * May be changed a lot in future. Marking deprecated for now
 */
@Deprecated
public class RideshareDetails implements Serializable, IDetailsModel {
    private String title;
    private String pickupLocation;
    private String destination;
    private Calendar pickUpDateTime;
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

    public Calendar getPickUpDate() {
        return pickUpDateTime;
    }

    public void setPickUpDate(Calendar pickUpDate) {
        if (pickUpDateTime == null) {
            pickUpDateTime = Calendar.getInstance();
        }
        pickUpDateTime.set(Calendar.YEAR, pickUpDate.get(Calendar.YEAR));
        pickUpDateTime.set(Calendar.MONTH, pickUpDate.get(Calendar.MONTH));
        pickUpDateTime.set(Calendar.DAY_OF_MONTH, pickUpDate.get(Calendar.DAY_OF_MONTH));
    }

    public void setPickUpTime(Calendar pickUpTime) {
        if (pickUpDateTime == null) {
            pickUpDateTime = Calendar.getInstance();
        }
        pickUpDateTime.set(Calendar.HOUR_OF_DAY, pickUpTime.get(Calendar.HOUR_OF_DAY));
        pickUpDateTime.set(Calendar.MINUTE, pickUpTime.get(Calendar.MINUTE));
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

    @Override
    public IDetailsModel populateDetailsFromJson(String jsonString) throws JSONException {
        return null;
    }

}