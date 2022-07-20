package com.joinalongapp.viewmodel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChatDetails implements Serializable, IDetailsModel {

    //TODO: change to String (including for all other models)
    private String id;
    private String title;
    private List<Tag> tags;
    private String description;
    private List<String> people;
    private int maxNumPeople = Integer.MAX_VALUE;
    //chat has at least one person, so that is the default
    private int numPeople = 1;

    public String getId(){
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getPeople() {
        return people;
    }

    public void setId(String id){
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPeople(List<String> people) {
        this.people = people;
    }

    public int getMaxNumPeople() {
        return maxNumPeople;
    }

    public void setMaxNumPeople(int maxNumPeople) {
        this.maxNumPeople = maxNumPeople;
    }

    public int getNumPeople() {
        return numPeople;
    }

    public void setNumPeople(int numPeople) {
        this.numPeople = numPeople;
    }

    public List<String> getStringListOfTags(){
        List<String> result = new ArrayList<>();
        for(Tag tag : tags){
            result.add(tag.getName());
        }
        return result;
    }

    @Override
    public String toJsonString() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("title", getTitle());

        JSONArray tags = new JSONArray(getStringListOfTags());
        json.put("tags", tags);

        json.put("numberOfPeople", getMaxNumPeople());
        json.put("description", getDescription());
        json.put("capacity", getNumPeople());
        json.put("participants", new JSONArray(getPeople()));

        return json.toString();
    }


    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("title", getTitle());

        JSONArray tags = new JSONArray(getStringListOfTags());
        json.put("tags", tags);

        json.put("numberOfPeople", getMaxNumPeople());
        json.put("description", getDescription());
        json.put("capacity", getNumPeople());
        json.put("participants", new JSONArray(getPeople()));

        return json;
    }

    @Override
    public IDetailsModel populateDetailsFromJson(String jsonBody) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonBody);
        setId(jsonObject.getString("_id"));
        setTitle(jsonObject.getString("title"));

        JSONArray tagJson = jsonObject.getJSONArray("tags");
        for (int i = 0; i < tagJson.length(); i++) {
            addTagToInterests(new Tag(tagJson.getString(i)));
        }

        setMaxNumPeople(jsonObject.getInt("numberOfPeople"));
        setDescription(jsonObject.getString("description"));
        setNumPeople(jsonObject.getInt("currCapacity"));

        JSONArray friendsJson = jsonObject.getJSONArray("participants");
        for (int i = 0; i < friendsJson.length(); i++) {
            addToFriends(friendsJson.getString(i));
        }

        return this;
    }

    private void addToFriends(String string) {
        if(people == null){
            people = new ArrayList<>();
        }
        people.add(string);
    }

    private void addTagToInterests(Tag tag) {
        if(tags == null){
            tags = new ArrayList<>();
        }
        tags.add(tag);
    }
}
