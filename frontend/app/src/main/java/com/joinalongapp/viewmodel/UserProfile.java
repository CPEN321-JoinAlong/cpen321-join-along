package com.joinalongapp.viewmodel;

import android.graphics.Bitmap;
import android.location.Address;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

public class UserProfile implements Serializable {
    private String firstName;
    private String lastName;
    private Address location;
    private List<String> interests; //TODO: it might be good to make this have its own datatype, or maybe a list of ENUM's
    private String description;
    private Bitmap profilePicture;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Address getLocation() {
        return location;
    }

    public void setLocation(Address location) {
        this.location = location;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Bitmap getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Bitmap profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String toJsonString() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("firstName", getFirstName());
        json.put("lastName", getLastName());
        json.put("location", getLocation());
        json.put("interests", getInterests());
        json.put("description", getDescription());
        json.put("profilePicture", getProfilePicture());

        return json.toString();
    }
}
