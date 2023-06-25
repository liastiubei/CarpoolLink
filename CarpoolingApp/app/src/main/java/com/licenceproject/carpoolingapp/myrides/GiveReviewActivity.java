package com.licenceproject.carpoolingapp.myrides;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.businessobjects.Passenger;
import com.licenceproject.carpoolingapp.businessobjects.Review;
import com.licenceproject.carpoolingapp.businessobjects.Ride;
import com.licenceproject.carpoolingapp.businessobjects.User;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;
import com.licenceproject.carpoolingapp.profile.UserDetailsActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

//class for the page for giving a review
public class GiveReviewActivity extends ErrorHandlingAppCompatActivity {

    //the passenger
    public static Passenger passenger;
    //the ride
    public static Ride ride;

    //the review textview
    private TextView review;
    //initial rating
    private String rating = "1";
    //calendar, for getting date
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_give_review);

        review = findViewById(R.id.reviewEditText);

        Spinner spinner = findViewById(R.id.rating_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.ratingSpinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                rating = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        MaterialButton button = findViewById(R.id.finish_set_review_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performAddReview();
            }
        });
    }

    //function that adds the review
    private void performAddReview() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String ratingField;
        String recipientUserID;
        if(User.getSessionUser().getUsername().equals(passenger.getUserId())){
            recipientUserID = ride.getDriverId();
            ratingField = "gaveRating";
            passenger.setGaveRating("true");
        } else {
            recipientUserID = passenger.getUserId();
            ratingField = "receivedRating";
            passenger.setReceivedRating("true");
        }

        // Create a new rating document with auto-generated ID
        CollectionReference  ratingRef = db.collection("users").document(recipientUserID)
                .collection("ratings");
        String reviewID = ratingRef.document().getId();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Create a rating object
        Map<String, Object> ratingMap = new HashMap<>();
        ratingMap.put("rideID", ride.getRideId());
        ratingMap.put("reviewerID", User.getSessionUser().getUsername());
        ratingMap.put("reviewerFirstName", User.getSessionUser().getFirstName());
        ratingMap.put("recipientID", recipientUserID);
        ratingMap.put("rating", rating);
        ratingMap.put("message", review.getText().toString());
        ratingMap.put("date", dateFormat.format(calendar.getTime()));

        // Set the rating document
        ratingRef.document(reviewID).set(ratingMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Get the reference to the passenger document
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference passengerRef = db.collection("passengers").document(passenger.getPassengerId());

                        // Create a map to update the rating field
                        Map<String, Object> updates = new HashMap<>();
                        updates.put(ratingField, "true");

                        // Perform the update
                        passengerRef.update(updates)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        successMessage();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        failureMessage();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        failureMessage();
                    }
                });
    }

    //shows the message in case of failure
    private void failureMessage(){
        Toast.makeText(this, "The review couldn't be set", Toast.LENGTH_LONG).show();
    }

    //shows the message in case of success
    private void successMessage(){
        Toast.makeText(this, "The review has been set", Toast.LENGTH_LONG).show();
        this.finish();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        UserDetailsActivity.passenger = passenger;
        passenger = null;
        ride = null;
    }
}