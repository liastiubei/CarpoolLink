package com.licenceproject.carpoolingapp.myrides;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.businessobjects.Passenger;
import com.licenceproject.carpoolingapp.businessobjects.Ride;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;
import com.licenceproject.carpoolingapp.profile.UserDetailsActivity;
import com.licenceproject.carpoolingapp.parsing.ParseToData;

import java.util.List;

//class for the implementation of the published ride page
public class RidePublishedViewerActivity extends ErrorHandlingAppCompatActivity {

    //the ride
    private static Ride ride;
    //the list of passengers
    private static List<Passenger> passengers;
    //the textviews
    private TextView startLocation, endLocation, departureTime, departureDate, price;

    //function to set the ride
    public static void setRide(Ride setRide) {
        ride = setRide;
    }

    //function to set the passengers
    public static void setPassengers(List<Passenger> passengers) {
        RidePublishedViewerActivity.passengers = passengers;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_published_viewer);
        ParseToData parser = new ParseToData(getApplicationContext());

        // Set the ride details
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

        LinearLayout passengerLayout = findViewById(R.id.passengerLayout);

        if (passengers != null) {
            for (Passenger passenger : passengers) {
                View passengerView = LayoutInflater.from(this).inflate(R.layout.passenger_card_view, passengerLayout, false);
                TextView passengerNameTextView = passengerView.findViewById(R.id.userNameTextView);
                passengerNameTextView.setText(passenger.getFirstName());
                passengerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openUserDetailsActivity(passenger);
                    }
                });

                passengerLayout.addView(passengerView);
            }
        }
    }

    //Opens the user page
    private void openUserDetailsActivity(Passenger passenger) {
        // Create an intent to open the UserDetailsActivity
        Intent intent = new Intent(this, UserDetailsActivity.class);
        UserDetailsActivity.ride = ride;
        UserDetailsActivity.passenger = passenger;

        // Start the UserDetailsActivity
        startActivity(intent);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        passengers = null;
        ride = null;
    }
}
