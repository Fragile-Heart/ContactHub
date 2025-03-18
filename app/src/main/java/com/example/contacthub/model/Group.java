package com.example.contacthub.model;

import java.util.List;

public class Group {
    private int id;
    private String name;

    private boolean expanded;


    public Group(int id, List<Integer> contactIds, boolean expanded, String name) {
        this.id = id;
        this.expanded = expanded;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}