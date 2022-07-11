package com.joinalongapp.joinalong;

import android.app.Application;

import com.joinalongapp.viewmodel.IDetailsModel;
import com.joinalongapp.viewmodel.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserApplicationInfo extends Application implements IDetailsModel {
    private String userToken;
    private UserProfile profile;

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

    public String tokenToJsonString() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("Token", getUserToken());
        return json.toString();
    }

    @Override
    public String toJsonString() throws JSONException {
        List<String> friendId = new ArrayList<>();
        for(UserProfile friend : profile.getFriends()){
            friendId.add(friend.getId().toString());
        }
        JSONObject json = new JSONObject();
        json.put("Token", getUserToken());
        json.put("id", profile.getId());
        json.put("firstName", profile.getFirstName());
        json.put("lastName", profile.getLastName());
        json.put("location", profile.getLocation());
        json.put("interests", profile.getStringListOfTags());
        json.put("description", profile.getDescription());
        json.put("profilePicture", profile.getProfilePicture());
        json.put("friends", friendId);

        return json.toString();
    }
}
