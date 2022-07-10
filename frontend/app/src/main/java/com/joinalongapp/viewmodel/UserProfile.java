package com.joinalongapp.viewmodel;

import android.graphics.Bitmap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserProfile implements Serializable {
    private UUID id;
    private String firstName;
    private String lastName;
    private String location;
    private List<String> interests; //TODO: it might be good to make this have its own datatype, or maybe a list of ENUM's
    private String description;
    private Bitmap profilePicture;
    private List<UserProfile> friends;

    public UserProfile(UUID id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getId() {
        return id;
    }

    public List<UserProfile> getFriends() {
        return friends;
    }

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
        return location;
    }

    public void setLocation(String location) {
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

    public String getFullName(){
        return firstName + " " + lastName;
    }

    public String[] getFriendsStringArray(){
        String[] result = new String[friends.size()];
        for(int i = 0; i < friends.size(); i++){
            result[i] = friends.get(i).getName();
        }
        return result;
    }

    public String toJsonString() throws JSONException {
        List<String> friendId = new ArrayList<>();
        for(UserProfile friend : friends){
            friendId.add(friend.getId().toString());
        }
        JSONObject json = new JSONObject();
        json.put("id", getId());
        json.put("firstName", getFirstName());
        json.put("lastName", getLastName());
        json.put("location", getLocation());
        json.put("interests", getInterests());
        json.put("description", getDescription());
        json.put("profilePicture", getProfilePicture());
        json.put("friends", friendId);

        return json.toString();
    }
}
