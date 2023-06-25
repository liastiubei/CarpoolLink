package com.licenceproject.carpoolingapp.businessobjects;

/**
 * Class that defines a User object
 */

public class User {
    //The logged in user singleton
    private static User sessionUser;
    //Username
    private String username;
    //Password
    private String password;
    //First name
    private String firstName;
    //Last name
    private String lastName;

    //Constructor of an User object
    public User(String username, String password, String firstName, String lastName){
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    //Function that creates the session user
    public static void createSessionUser(User user){
        sessionUser = user;
    }

    public static User getSessionUser() {
        return sessionUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
