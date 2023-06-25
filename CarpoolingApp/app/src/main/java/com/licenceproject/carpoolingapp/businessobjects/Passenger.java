package com.licenceproject.carpoolingapp.businessobjects;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Class that defines a Passenger object
 */
public class Passenger {
    //Passenger ID
    private String passengerId;
    //Username of the passenger
    private String userId;
    //ID of the ride
    private String rideId;
    //First name of the passenger
    private String firstName;
    //String for receiving from database whether the passenger has received a rating for the ride
    private String receivedRating;
    //String for receiving from database whether the passenger has given a rating for the ride
    private String gaveRating;

    //Constructor for the Passenger class
    public Passenger(String passengerId, String userId, String rideId, String firstName, String receivedRating, String gaveRating) {
        this.passengerId = passengerId;
        this.userId = userId;
        this.rideId = rideId;
        this.firstName = firstName;
        this.receivedRating = receivedRating;
        this.gaveRating = gaveRating;
    }

    //Reserves the ride for the current passenger
    public void performReserveRide(ErrorHandlingAppCompatActivity activity) {
        // Create a new map to store the data
        Map<String, Object> data = new HashMap<>();
        data.put("userID", userId);
        data.put("rideID", rideId);
        data.put("firstName", firstName);
        data.put("receivedRating", receivedRating);
        data.put("gaveRating", gaveRating);

        // Add the data to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("passengers")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        try {
                            Ride.incrementCurrentNumberOfPassengers(rideId);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                        activity.setResultText(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        activity.setResultText(false);
                    }
                });
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getReceivedRating() {
        return receivedRating;
    }

    public void setReceivedRating(String receivedRating) {
        this.receivedRating = receivedRating;
    }

    public String getGaveRating() {
        return gaveRating;
    }

    public void setGaveRating(String gaveRating) {
        this.gaveRating = gaveRating;
    }
}
