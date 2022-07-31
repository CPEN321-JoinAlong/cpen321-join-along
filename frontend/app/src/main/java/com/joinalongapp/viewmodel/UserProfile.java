package com.joinalongapp.viewmodel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserProfile implements Serializable, IDetailsModel {
    private String id;
    private String firstName;
    private String lastName;
    private String location;
    private List<Tag> tags = new ArrayList<>();
    private String description;
    private String profilePictureUrl;
    //TODO: probably should make constructor instantiate these, or modify the existing setters to null check them
    private List<String> friends = new ArrayList<>();
    private boolean isAdmin;

    public UserProfile(String id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UserProfile() {
        //Default Constructor
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void addTagToInterests(Tag tag) {
        tags.add(tag);
    }

    public void addAllTagsToInterests(List<Tag> tags) {
        this.tags.addAll(tags);
    }

    public void setFriends(List<String> friends) {
        this.friends.addAll(friends);
    }

    public void addFriendToList(String friend) {
        friends.add(friend);
    }

    public String getId() {
        return id;
    }

    public List<String> getFriends() {
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

    public List<Tag> getInterests() {
        return tags;
    }

    public void setInterests(List<Tag> interests) {
        this.tags = interests;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfilePicture() {
        return profilePictureUrl;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePictureUrl = profilePicture;
    }

    public String getFullName(){
        return firstName + " " + lastName;
    }

    public String toJsonString() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", getId());
        json.put("name", getFullName());
        json.put("location", getLocation());
        json.put("interests", getStringListOfTags());
        json.put("description", getDescription());
        json.put("profilePicture", getProfilePicture());

        JSONArray jsonArray = new JSONArray(friends);
        json.put("friends", jsonArray);

        return json.toString();
    }

    public List<String> getStringListOfTags(){
        List<String> result = new ArrayList<>();
        for(Tag tag : tags){
            result.add(tag.getName());
        }
        return result;
    }

    public UserProfile populateDetailsFromJson(String jsonBody) throws JSONException {
        JSONObject json = new JSONObject(jsonBody);

        setId(json.getString("_id"));
        setFirstName(getFirstNameFromFull(json.getString("name")));
        setLastName(getLastNameFromFull(json.getString("name")));
        setLocation(json.getString("location"));

        JSONArray tags = json.getJSONArray("interests");
        for (int i = 0; i < tags.length(); i++) {
            addTagToInterests(new Tag(tags.getString(i)));
        }

        setDescription(json.getString("description"));
        setProfilePicture(json.getString("profilePicture"));

        JSONArray jsonFriendsList = json.getJSONArray("friends");
        for (int i = 0; i < jsonFriendsList.length(); i++) {
            addFriendToList(jsonFriendsList.getString(i));
        }

        // TODO: add parsing for admin

        return this;
    }

    private boolean validateName(String name) {
        return name.contains(" ") && name.indexOf(" ") != (name.length() - 1);
    }

    private String getFirstNameFromFull(String fullName) {
        if (validateName(fullName)) {
            return fullName.substring(0, fullName.indexOf(" "));
        } else {
            return null;
        }
    }

    private String getLastNameFromFull(String fullName) {
        if (validateName(fullName)) {
            return fullName.substring(fullName.indexOf(" ") + 1);
        } else {
            return null;
        }
    }
}
