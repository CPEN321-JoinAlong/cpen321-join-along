package com.joinalongapp.viewmodel;

import org.json.JSONException;

public interface IDetailsModel {
    String toJsonString() throws JSONException;
    IDetailsModel populateDetailsFromJson(String jsonBody) throws JSONException;
}
