package com.licenceproject.carpoolingapp.businessobjects;

import android.os.Build;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/**
 * Class that defines a Ride object
 */
public class Ride {

    //ride ID
    private String rideId;
    //driver username
    private String driverId;
    //driver first name
    private String driverFirstName;
    //number of passengers
    private int nrPassengers;
    //latitude and longitude for start location
    private LatLng startLocation;
    //latitude and longitude for end location
    private LatLng endLocation;
    //date of the ride
    private Date date;
    //price of the ride
    private String price;
    //currency of the price
    private String currency;
    //current number of passengers that reserved the ride
    private int currentNrOfPassengers;

    //current number of passengers from the database
    private static Integer currentPassengersFromDb = null;

    //constructor
    public Ride(String rideId, String driverId, String driverName, int nrPassengers, int currentNrOfPassengers, LatLng startLocation, LatLng endLocation, Date date, String price, String currency) {
        this.rideId = rideId;
        this.driverId = driverId;
        this.driverFirstName = driverName;
        this.nrPassengers = nrPassengers;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.date = date;
        this.price = price;
        this.currency = currency;
        this.currentNrOfPassengers = currentNrOfPassengers;
    }

    //Function that returns the list of passengers for the ride
    public List<Passenger> getPassengers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final List<Passenger> passengers = new ArrayList<>();

        // Create a CountDownLatch with a count of 1
        final CountDownLatch latch = new CountDownLatch(1);

        // Query the "passengers" collection to fetch the passengers for this ride
        db.collection("passengers")
                .whereEqualTo("rideID", rideId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String passengerId = documentSnapshot.getId();
                            String userId = documentSnapshot.getString("userID");
                            String rideId = documentSnapshot.getString("rideID");
                            String firstName = documentSnapshot.getString("firstName");
                            String receivedRating = documentSnapshot.getString("receivedRating");
                            String gaveRating = documentSnapshot.getString("gaveRating");

                            Passenger passenger = new Passenger(passengerId, userId, rideId, firstName, receivedRating, gaveRating);
                            passengers.add(passenger);
                        }

                        // Passengers retrieval successful, release the latch
                        latch.countDown();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Release the latch in case of failure to avoid blocking
                        latch.countDown();
                    }
                });

        try {
            // Wait for the latch to count down to 0, which means the thread can continue
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return passengers;
    }

    //function that sets the ride in the database
    public void performSetRide(ErrorHandlingAppCompatActivity activity) {
        // Create a new map to store the data
        Map<String, Object> data = new HashMap<>();
        data.put("driverID", driverId);
        data.put("driverFirstName", driverFirstName);
        data.put("nrPassengers", String.valueOf(nrPassengers));
        data.put("currentNrOfPassengers", String.valueOf(currentNrOfPassengers));
        data.put("price", price);
        data.put("currency", currency);

        // Convert startLocation and endLocation to JSON strings
        Gson gson = new Gson();
        String startLocationJson = gson.toJson(startLocation);
        String endLocationJson = gson.toJson(endLocation);

        // Add the JSON strings to the data map
        data.put("startLocation", startLocationJson);
        data.put("endLocation", endLocationJson);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateString = sdf.format(date);

        data.put("startDate", dateString);

        // Add the data to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rides")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
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

    //function that increases the current number of passengers in the database
    public static void incrementCurrentNumberOfPassengers(String rideId) throws RuntimeException{
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the current number of passengers
        getCurrentNrOfPassengersFromDb(rideId);

        if(currentPassengersFromDb == null){
            throw new RuntimeException("Problem with current passengers of database");
        }
        // Increment the current number of passengers by 1
        int updatedNrOfPassengers = currentPassengersFromDb + 1;

        // Create a new map to store the updated data
        Map<String, Object> data = new HashMap<>();
        data.put("currentNrOfPassengers", String.valueOf(updatedNrOfPassengers));

        // Update the document in Firestore
        db.collection("rides")
                .document(rideId)
                .update(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Update successful
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Update failed
                    }
                });
        currentPassengersFromDb = null;
    }

    //function that gets from the database the current number of passengers
    private static void getCurrentNrOfPassengersFromDb(String rideId){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference rideRef = db.collection("rides").document(rideId);

        rideRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Integer currentNrOfPassengers = Integer.parseInt(document.get("currentNrOfPassengers").toString());
                                currentPassengersFromDb = new Integer(currentNrOfPassengers);
                            } else {
                                currentPassengersFromDb = null;
                            }
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                currentPassengersFromDb = null;
                            }
                        }
                    }
                });
    }


    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverFirstName() {
        return driverFirstName;
    }

    public void setDriverFirstName(String driverFirstName) {
        this.driverFirstName = driverFirstName;
    }

    public int getNrPassengers() {
        return nrPassengers;
    }

    public void setNrPassengers(int nrPassengers) {
        this.nrPassengers = nrPassengers;
    }

    public int getCurrentNrOfPassengers() {
        return currentNrOfPassengers;
    }

    public void setTotalNrOfPassengers(int currentNrOfPassengers) {
        this.currentNrOfPassengers = currentNrOfPassengers;
    }

    public LatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LatLng endLocation) {
        this.endLocation = endLocation;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
