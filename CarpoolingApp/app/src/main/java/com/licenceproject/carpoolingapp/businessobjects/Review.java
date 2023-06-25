package com.licenceproject.carpoolingapp.businessobjects;

/**
 * Class that defines a Review object
 */
public class Review {
    //Ride ID
    private String rideID;
    //Reviewer username
    private String reviewerID;
    //Reviewer's first name
    private String reviewerFirstName;
    //Recipient username
    private String recipientID;
    //Rating
    private String rating;
    //Message
    private String message;
    //Date
    private String date;

    //Constructor for the Review class
    public Review(String rideID, String reviewerID, String reviewerFirstName, String recipientID, String rating, String message, String date) {
        this.rideID = rideID;
        this.reviewerID = reviewerID;
        this.reviewerFirstName = reviewerFirstName;
        this.recipientID = recipientID;
        this.rating = rating;
        this.message = message;
        this.date = date;
    }

    public String getRideID() {
        return rideID;
    }

    public void setRideID(String rideID) {
        this.rideID = rideID;
    }

    public String getReviewerID() {
        return reviewerID;
    }

    public void setReviewerID(String reviewerID) {
        this.reviewerID = reviewerID;
    }

    public String getRecipientID() {
        return recipientID;
    }

    public void setRecipientID(String recipientID) {
        this.recipientID = recipientID;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReviewerFirstName() {
        return reviewerFirstName;
    }

    public void setReviewerFirstName(String reviewerFirstName) {
        this.reviewerFirstName = reviewerFirstName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
