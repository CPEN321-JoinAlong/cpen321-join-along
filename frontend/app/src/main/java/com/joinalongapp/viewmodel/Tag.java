package com.joinalongapp.viewmodel;

import java.io.Serializable;

public class Tag implements Serializable {
    private String name;
    private String id;

    public Tag(String name) {
        this.name = name;
    }

    public Tag(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }


}
