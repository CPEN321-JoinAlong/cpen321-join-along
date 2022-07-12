package com.joinalongapp.joinalong;

import android.app.Application;

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

    public void updateApplicaitonInfo(UserApplicationInfo userApplicationInfo) {
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

        return json.toString();
    }

    String string = "{\"interests\":[]," +
            "\"location\":\"2336 Main Mall Vancouver BC\"," +
            "\"chats\":[]," +
            "\"events\":[]," +
            "\"profilePicture\":\"https://lh3.googleusercontent.com/a/AItbvmnLZR2PnBwNbO6OnjVppOvReOM0Yp9WQPLnuZB0=s96-c\"," +
            "\"description\":\"Yes\"," +
            "\"chatInvites\":[]," +
            "\"eventInvites\":[]," +
            "\"friendRequest\":[]," +
            "\"friends\":[]," +
            "\"blockedUsers\":[]," +
            "\"blockedEvents\":[]," +
            "\"token\":\"shortToken\"," +
            "\"_id\":\"62cbc9fcdd72460310e15459\"," +
            "\"__v\":0}";

    public UserApplicationInfo populateDetailsFromJson(String jsonBody) throws JSONException {
        JSONObject json = new JSONObject(jsonBody);

        profile.setId(json.getString("_id"));
        profile.setFirstName(getFirstNameFromFull(json.getString("name")));
        profile.setLastName(getLastNameFromFull(json.getString("name")));
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

    private String getFirstNameFromFull(String fullName) {
        return fullName.substring(0, fullName.indexOf(" "));
    }

    private String getLastNameFromFull(String fullName) {
        return fullName.substring(fullName.indexOf(" ") + 1);
    }
}
