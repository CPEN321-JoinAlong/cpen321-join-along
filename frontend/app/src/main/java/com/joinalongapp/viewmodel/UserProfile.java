package com.joinalongapp.viewmodel;

import android.graphics.Bitmap;
import android.location.Address;

import java.util.List;

public class UserProfile {
    String firstName;
    String lastName;
    Address location;
    List<String> interests; //TODO: it might be good to make this have its own datatype, or maybe a list of ENUM's
    String description;
    Bitmap profilePicture;

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

    public String getLocation() {
        return lastName;
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
}
