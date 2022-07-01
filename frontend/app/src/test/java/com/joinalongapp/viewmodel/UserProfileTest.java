package com.joinalongapp.viewmodel;

import static org.junit.Assert.assertEquals;

import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserProfileTest {

    @Test
    public void testToJsonString() throws IOException, JSONException{
        UserProfile userProfile = new UserProfile();
        userProfile.setFirstName("First");
        userProfile.setLastName("Last");

        List<String> interests = new ArrayList<>();
        interests.add("hiking");
        interests.add("food");
        userProfile.setInterests(interests);

        userProfile.setDescription("i'm interesting :)");

        String jsonResult = userProfile.toJsonString();
        String expected = "{\"firstName\":\"First\",\"lastName\":\"Last\",\"description\":\"i'm interesting :)\"," +
                "\"interests\":[\"hiking\",\"food\"]}";
        assertEquals(expected, jsonResult);
    }
}
