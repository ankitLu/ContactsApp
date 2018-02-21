package com.example.hi5an.inclass11;

import java.io.Serializable;

/**
 * Created by hi5an on 11/18/2017.
 */

public class User implements Serializable {
    private String username;
    private String userKey;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String status;
    private String imagePath;

    public String getUsername() {
        return username;
    }


    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
