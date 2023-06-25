package com.licenceproject.carpoolingapp.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.businessobjects.Passenger;
import com.licenceproject.carpoolingapp.businessobjects.Review;
import com.licenceproject.carpoolingapp.businessobjects.ReviewAdapter;
import com.licenceproject.carpoolingapp.businessobjects.Ride;
import com.licenceproject.carpoolingapp.businessobjects.User;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;
import com.licenceproject.carpoolingapp.messaging.MessageThreadActivity;
import com.licenceproject.carpoolingapp.myrides.GiveReviewActivity;

import java.util.ArrayList;
import java.util.List;

//Class for the implementation of the User details and reviews page
public class UserDetailsActivity extends ErrorHandlingAppCompatActivity {

    //recycler view
    private RecyclerView recyclerView;
    //review adapter
    private ReviewAdapter reviewAdapter;
    //list of reviews
    private List<Review> reviewList;
    //button for leaving a review
    private MaterialButton reviewButton;
    //user's average rating
    private double averageRating;
    //text view for title
    private TextView titleTextView;
    //text view for rating
    private TextView ratingTextView;

    //The ride
    public static Ride ride;
    //The passenger
    public static Passenger passenger;
    //The user
    public static User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        // Initialize RecyclerView and review list
        recyclerView = findViewById(R.id.reviewRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewList = new ArrayList<>();

        titleTextView = findViewById(R.id.profile);
        ratingTextView = findViewById(R.id.rating);

        // Initialize review adapter
        reviewAdapter = new ReviewAdapter(reviewList);
        recyclerView.setAdapter(reviewAdapter);

        // Add reviews
        addReviews();

        // Button for messaging the user
        MaterialButton messageButton = findViewById(R.id.messageButton);

        // Button for giving a review
        reviewButton = findViewById(R.id.reviewButton);
        if(passenger == null || ride == null){
            reviewButton.setVisibility(View.INVISIBLE);
            messageButton.setVisibility(View.INVISIBLE);
        }
        else{
            if(User.getSessionUser().getUsername().equals(passenger.getUserId()) && passenger.getGaveRating().equals("true")){
                reviewButton.setVisibility(View.INVISIBLE);
            } else if((!User.getSessionUser().getUsername().equals(passenger.getUserId())) && passenger.getReceivedRating().equals("true")){
                reviewButton.setVisibility(View.INVISIBLE);
            }
            else {
                reviewButton.setVisibility(View.VISIBLE);
                reviewButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openGiveReviewActivity();
                    }
                });
            }

            messageButton.setVisibility(View.VISIBLE);
            messageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openMessageThreadActivity(passenger);
                }
            });
        }

    }

    //function that opens the page to give a review
    private void openGiveReviewActivity() {
        // Create an intent to open the MessageThreadActivity
        Intent intent = new Intent(this, GiveReviewActivity.class);
        GiveReviewActivity.passenger = passenger;
        GiveReviewActivity.ride = ride;

        // Start the MessageThreadActivity
        startActivity(intent);
    }

    //function that opens the message thread with the user
    private void openMessageThreadActivity(Passenger passenger) {
        // Create an intent to open the MessageThreadActivity
        Intent intent = new Intent(this, MessageThreadActivity.class);
        MessageThreadActivity.setRide(ride);
        MessageThreadActivity.setPassenger(passenger);

        // Start the MessageThreadActivity
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ride = null;
        passenger = null;
        user = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(passenger != null){
            if((passenger.getReceivedRating().equals("true") && ride.getDriverId().equals(User.getSessionUser().getUsername())) ||
                    (passenger.getGaveRating().equals("true") && passenger.getUserId().equals(User.getSessionUser().getUsername()))){
                reviewButton.setVisibility(View.INVISIBLE);
            }
        }
        addReviews();
    }

    //function for getting the reviews from the database and adding them to the page
    private void addReviews() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String recipientUserID;
        if(passenger != null && ride != null){
            if(User.getSessionUser().getUsername().equals(passenger.getUserId())){
                recipientUserID = ride.getDriverId();
                titleTextView.setText(ride.getDriverFirstName() + "'s profile");
            } else {
                recipientUserID = passenger.getUserId();
                titleTextView.setText(passenger.getFirstName() + "'s profile");
            }
        } else if(passenger == null && ride != null){
            recipientUserID = ride.getDriverId();
            titleTextView.setText(ride.getDriverFirstName() + "'s profile");
        } else{
            recipientUserID = user.getUsername();
            titleTextView.setText(user.getFirstName() + "'s profile");
        }


        db.collection("users").document(recipientUserID)
                .collection("ratings")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            reviewList.clear();
                            int totalRatings = 0;
                            int sumRatings = 0;

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Get the rating value from each document
                                int rating = Integer.parseInt(document.getString("rating"));

                                // Update the total ratings count and sum of ratings
                                totalRatings++;
                                sumRatings += rating;

                                //add the document to reviewList
                                reviewList.add(new Review(document.getString("rideID"), document.getString("reviewerID"), document.getString("reviewerFirstName"),
                                        document.getString("recipientID"), document.getString("rating"), document.getString("message"), document.getString("date")));
                            }

                            // Calculate the average rating
                            averageRating = totalRatings > 0 ? (double) sumRatings / totalRatings : 0.0;
                            String formattedAverageRating = String.format("%.2f", averageRating);
                            ratingTextView.setText("Average rating: " + formattedAverageRating + "/5");

                            // Notify the adapter about the data set change
                            reviewAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(UserDetailsActivity.this, "Couldn't retrieve reviews from database", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}