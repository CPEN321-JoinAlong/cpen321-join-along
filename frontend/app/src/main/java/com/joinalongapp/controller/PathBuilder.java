package com.joinalongapp.controller;

public class PathBuilder {
    private StringBuilder path;
    private static final String DELIMITER = "/";
    private static final String USER = "user";
    private static final String EVENT = "event";
    private static final String CHAT = "chat";
    private static final String CREATE = "create";
    private static final String EDIT = "edit";


    public PathBuilder() {
        path = new StringBuilder();
    }

    public PathBuilder addUser() {
        return addNode(USER);
    }

    public PathBuilder addEvent() {
        return addNode(EVENT);
    }

    public PathBuilder addChat() {
        return addNode(CHAT);
    }

    public PathBuilder addCreate() {
        return addNode(CREATE);
    }

    public PathBuilder addEdit() {
        return addNode(EDIT);
    }

    public PathBuilder addNode(String node) {
        if (path.length() != 0) {
            path.append(DELIMITER);
        }
        path.append(node);
        return this;
    }

    public String constructPath(String... nodes) {
        for (String node : nodes) {
            if (path.length() != 0) {
                path.append("/");
            }
            path.append(node);
        }
        return path.toString();
    }

    public String build() {
        return path.toString();
    }
}
