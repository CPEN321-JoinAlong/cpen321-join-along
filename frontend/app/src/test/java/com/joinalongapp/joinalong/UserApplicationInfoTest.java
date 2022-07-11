package com.joinalongapp.joinalong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.joinalongapp.viewmodel.UserProfile;

import org.json.JSONException;
import org.junit.Test;

public class UserApplicationInfoTest {
    @Test
    public void testJsonToObject(){
        String input = "{\"interests\":[]," +
                "\"name\":\"First Last\"," +
                "\"location\":\"2336 Main Mall Vancouver BC\"," +
                "\"chats\":[]," +
                "\"events\":[]," +
                "\"profilePicture\":\"example.com\"," +
                "\"description\":\"Yes\"," +
                "\"chatInvites\":[]," +
                "\"eventInvites\":[]," +
                "\"friendRequest\":[]," +
                "\"friends\":[]," +
                "\"blockedUsers\":[]," +
                "\"blockedEvents\":[]," +
                "\"token\":\"token\"," +
                "\"_id\":\"123\"," +
                "\"__v\":0}";

        UserApplicationInfo userApplicationInfo = new UserApplicationInfo();

        try {
            userApplicationInfo.populateDetailsFromJson(input);
        } catch (JSONException e) {
            fail();
        }

        assertEquals("token", userApplicationInfo.getUserToken());

        UserProfile profile = userApplicationInfo.getProfile();

        assertEquals("123", profile.getId());
        assertEquals("First Last", profile.getFullName());
        assertTrue(profile.getFriends().isEmpty());
        //assertTrue(profile.getTags())
        assertEquals("example.com", profile.getProfilePicture());

    }
}
