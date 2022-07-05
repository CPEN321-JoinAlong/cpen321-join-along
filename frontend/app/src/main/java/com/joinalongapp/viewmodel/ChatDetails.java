package com.joinalongapp.viewmodel;

import java.util.ArrayList;
import java.util.List;

public class ChatDetails {
    private String title;
    private List<String> tags;
    private String description;
    private List<User> people;

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

    public List<User> getPeople() {
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

    public void setPeople(List<User> people) {
        this.people = people;
    }

    public List<String> getListPeople(){
        List<String> result = new ArrayList<>();
        for(User user : people){
            result.add(user.getName());
        }
        return result;
    }
}
