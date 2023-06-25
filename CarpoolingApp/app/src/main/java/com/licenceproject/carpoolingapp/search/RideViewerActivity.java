package com.licenceproject.carpoolingapp.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;
import com.licenceproject.carpoolingapp.businessobjects.Passenger;
import com.licenceproject.carpoolingapp.businessobjects.Ride;
import com.licenceproject.carpoolingapp.businessobjects.User;
import com.licenceproject.carpoolingapp.parsing.ParseToData;
import com.licenceproject.carpoolingapp.profile.UserDetailsActivity;

public class RideViewerActivity extends ErrorHandlingAppCompatActivity {

    private static Ride ride;
    private TextView driverName, startLocation, endLocation, departureTime, departureDate, price;
    private MaterialButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_viewer);
        ParseToData parser = new ParseToData(getApplicationContext());

        driverName = findViewById(R.id.user_text);
        driverName.setText("   " + ride.getDriverFirstName());

        CardView cardView = findViewById(R.id.cardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUserDetailsActivity();
            }
        });

        startLocation = findViewById(R.id.start_location_text);
        startLocation.setText("Start location: " + parser.getFormattedAddressFromLatLng(ride.getStartLocation()));

        endLocation = findViewById(R.id.end_location_text);
        endLocation.setText("End location: " + parser.getFormattedAddressFromLatLng(ride.getEndLocation()));

        departureTime = findViewById(R.id.departure_time_text);
        departureTime.setText("Departure time: " + parser.getTimeFromDate(ride.getDate()));

        departureDate = findViewById(R.id.departure_date_text);
        departureDate.setText("Departure date: " + parser.getOnlyDateFromDate(ride.getDate()));

        price = findViewById(R.id.price_text);
        price.setText(ride.getPrice().toString() + " "+ ride.getCurrency());
        
        button = findViewById(R.id.reserve_ride_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("passengers")
                        .whereEqualTo("rideID", ride.getRideId())
                        .whereEqualTo("userID", User.getSessionUser().getUsername())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot != null){
                                        if(querySnapshot.getDocuments().size() > 0){
                                            tooManyPassengersError();
                                        }
                                        else{
                                            performReserveRide();
                                        }
                                    }
                                }
                            }
                        });
            }
        });
    }

    private void performReserveRide() {
        Passenger passenger = new Passenger(null, User.getSessionUser().getUsername(), ride.getRideId(), User.getSessionUser().getFirstName(), "false", "false");


        passenger.performReserveRide(this);
    }

    //Opens the user page
    private void openUserDetailsActivity() {
        // Create an intent to open the UserDetailsActivity
        Intent intent = new Intent(this, UserDetailsActivity.class);
        UserDetailsActivity.ride = ride;

        // Start the UserDetailsActivity
        startActivity(intent);
    }

    @Override
    public void setResultText(boolean performSetRide) {
        if(performSetRide){
            Toast.makeText(RideViewerActivity.this, "Ride reserved successfully.", Toast.LENGTH_LONG).show();
            finish();
        }
        else{
            Toast.makeText(RideViewerActivity.this, "ERROR: Ride couldn't be reserved.", Toast.LENGTH_LONG).show();
        }
    }

    public static Ride getRide() {
        return ride;
    }

    public static void setRide(Ride ride) {
        RideViewerActivity.ride = ride;
    }

    private void tooManyPassengersError(){
        Toast.makeText(this, "This ride is already reserved by you.", Toast.LENGTH_LONG).show();
    }
}