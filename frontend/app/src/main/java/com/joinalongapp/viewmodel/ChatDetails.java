package com.joinalongapp.viewmodel;

import java.util.ArrayList;
import java.util.List;

public class ChatDetails {
    private String title;
    private List<String> tags;
    private String description;
    private List<UserProfile> people;

    public ChatDetails(){

    }

    public String getTitle() {
        return title;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getDescription() {
        return description;
    }

    public List<UserProfile> getPeople() {
        return people;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPeople(List<UserProfile> people) {
        this.people = people;
    }

    public List<String> getListPeople(){
        List<String> result = new ArrayList<>();
        for(UserProfile user : people){
            result.add(user.getFullName());
        }
        return result;
    }
}
