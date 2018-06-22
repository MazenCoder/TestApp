package com.codedroid.testapp.model;

import java.io.Serializable;

public class Category implements Serializable {

    private String id;
    private String title;
    private String description;
    private String image_url;
    private String image_path;

    public Category() { }

    public Category(String title, String description, String image_url, String image_path) {
        this.title = title;
        this.description = description;
        this.image_url = image_url;
        this.image_path = image_path;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitel() {
        return title;
    }

    public void setTitel(String titel) {
        this.title = titel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }
}
