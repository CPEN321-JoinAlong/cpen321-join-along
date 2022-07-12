package com.joinalongapp.viewmodel;

import org.json.JSONException;

public interface IDetailsModel {
    public String toJsonString() throws JSONException;
    public IDetailsModel populateDetailsFromJson(String jsonBody) throws JSONException;
}
