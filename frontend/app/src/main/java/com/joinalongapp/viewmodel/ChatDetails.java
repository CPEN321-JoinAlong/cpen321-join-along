package com.joinalongapp.viewmodel;

import android.graphics.Bitmap;

import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatDetails implements Serializable, IDetailsModel {
    private UUID id;
    private String title;
    private List<Tag> tags;
    private String description;
    private List<UserProfile> people;
    private Bitmap groupPhoto;

    public ChatDetails(){

    }

    public UUID getId(){
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

    public List<UserProfile> getPeople() {
        return people;
    }

    public void setId(UUID id){
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

    public void setPeople(List<UserProfile> people) {
        this.people = people;
    }

    public List<String> getStringListOfPeople(){
        List<String> result = new ArrayList<>();
        for(UserProfile user : people){
            result.add(user.getFullName());
        }
        return result;
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
        //TODO
        return null;
    }
}
