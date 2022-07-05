package com.joinalongapp.viewmodel;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class User implements Serializable {

    private UUID id;
    private String name;
    private List<User> friends;

    public User(UUID id, String name){
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<User> getFriends() {
        return friends;
    }

    public String[] getFriendsStringArray(){
        String[] result = new String[friends.size()];
        for(int i = 0; i < friends.size(); i++){
            result[i] = friends.get(i).getName();
        }
        return result;
    }
}
