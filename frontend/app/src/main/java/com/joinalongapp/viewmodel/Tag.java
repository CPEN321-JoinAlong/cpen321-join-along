package com.joinalongapp.viewmodel;

import java.util.List;
import java.util.UUID;

public class Tag {
    private String name;
    private UUID id;

    public Tag(String name) {
        this.name = name;
    }

    public Tag(String name, UUID id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(UUID id) {
        this.id = id;
    }


}
