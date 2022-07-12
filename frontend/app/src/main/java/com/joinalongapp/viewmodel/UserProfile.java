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
    private List<UserProfile> friends = new ArrayList<UserProfile>();


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

    public void setFriends(List<UserProfile> friends) {
        this.friends.addAll(friends);
    }

    public void addFriendToList(UserProfile friend) {
        friends.add(friend);
    }

    public UserProfile(String id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UserProfile() {
    }

    public String getId() {
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

    public List<Tag> getInterests() {
        return tags;
    }

    public void setInterests(List<Tag> interests) {
        this.tags = interests;
    }

    public String getDescription() {
        return description;
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

    public String[] getFriendsStringArray(){
        String[] result = new String[friends.size()];
        for(int i = 0; i < friends.size(); i++){
            result[i] = friends.get(i).getFullName();
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
        json.put("name", getFullName());
        json.put("location", getLocation());
        json.put("interests", getStringListOfTags());
        json.put("description", getDescription());
        json.put("profilePicture", getProfilePicture());
        json.put("friends", friendId);

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

        JSONArray friends = json.getJSONArray("friends");
        for (int i = 0; i < friends.length(); i++) {
            addFriendToList((UserProfile) friends.get(i));
        }

        return this;
    }

    private String getFirstNameFromFull(String fullName) {
        return fullName.substring(0, fullName.indexOf(" "));
    }

    private String getLastNameFromFull(String fullName) {
        return fullName.substring(fullName.indexOf(" ") + 1);
    }
}
