package com.joinalongapp.joinalong;

import android.app.Application;

import com.joinalongapp.LocationUtils;
import com.joinalongapp.viewmodel.IDetailsModel;
import com.joinalongapp.viewmodel.Tag;
import com.joinalongapp.viewmodel.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserApplicationInfo extends Application implements IDetailsModel {
    private String userToken;
    private UserProfile profile = new UserProfile();

    // Default constructor
    public UserApplicationInfo() {
        super();
    }

    public void updateApplicationInfo(UserApplicationInfo userApplicationInfo) {
        this.userToken = userApplicationInfo.getUserToken();
        this.profile = userApplicationInfo.getProfile();
    }

    public UserApplicationInfo(UserProfile profile) {
        this.profile = profile;
    }


    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public String tokenToJsonStringForLogin() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("Token", getUserToken());
        return json.toString();
    }

    public String tokenToJsonString() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("token", getUserToken());
        return json.toString();
    }

    public void reset() {
        setUserToken("");
        setProfile(new UserProfile());
    }

    @Override
    public String toJsonString() throws JSONException {

        JSONObject json = new JSONObject();
        json.put("token", getUserToken());
        json.put("id", profile.getId());
        json.put("name", profile.getFirstName() + " " + profile.getLastName());
        json.put("location", profile.getLocation());

        JSONArray interests = new JSONArray(profile.getStringListOfTags());

        json.put("interests", interests);
        json.put("description", profile.getDescription());
        json.put("profilePicture", profile.getProfilePicture());
        if (!profile.getFriends().isEmpty()) {
            json.put("friends", new JSONArray(profile.getFriends()));
        }

        json.put("coordinates", LocationUtils.getLatLngAsString(profile.getCoordinates()));

        return json.toString();
    }

    public UserApplicationInfo populateDetailsFromJson(String jsonBody) throws JSONException {
        JSONObject json = new JSONObject(jsonBody);

        profile.setId(json.getString("_id"));

        String name = json.getString("name");
        profile.setFirstName(getFirstNameFromFull(name));
        profile.setLastName(getLastNameFromFull(name));

        profile.setLocation(json.getString("location"));

        JSONArray tags = json.getJSONArray("interests");
        for (int i = 0; i < tags.length(); i++) {
            profile.addTagToInterests(new Tag(tags.getString(i)));
        }

        profile.setDescription(json.getString("description"));
        profile.setProfilePicture(json.getString("profilePicture"));

        JSONArray friends = json.getJSONArray("friends");
        for (int i = 0; i < friends.length(); i++) {
            profile.addFriendToList(friends.getString(i));
        }

        setUserToken(json.getString("token"));

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
