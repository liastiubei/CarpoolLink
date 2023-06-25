package com.licenceproject.carpoolingapp.businessobjects;

/**
 * Class that defines a Conversation object
 */
public class Conversation {
    //Conversation ID
    private String id;
    //Driver ID
    private String driverId;
    //Passenger ID
    private String passengerId;
    //Passenger Username
    private String passengerUserId;

    //Default constructor required for Firebase
    public Conversation() {
        // Empty
    }

    //Main constructor for Conversation
    public Conversation(String id, String driverId, String passengerId, String passengerUserId) {
        this.id = id;
        this.driverId = driverId;
        this.passengerId = passengerId;
        this.passengerUserId = passengerUserId;
    }

    //Returns conversation ID
    public String getId() {
        return id;
    }

    //Returns Driver ID
    public String getDriverId() {
        return driverId;
    }

    //Gets Passenger ID
    public String getPassengerId() {
        return passengerId;
    }

    //Gets the Passenger Username
    public String getPassengerUserId() {
        return passengerUserId;
    }

    //Sets Driver ID
    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    //Sets Passenger ID
    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    //Sets Passenger Username
    public void setPassengerUserId(String passengerUserId) {
        this.passengerUserId = passengerUserId;
    }

    //Sets conversation ID
    public void setId(String id) {
        this.id = id;
    }
}
