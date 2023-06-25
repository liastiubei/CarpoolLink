package com.licenceproject.carpoolingapp.myrides;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

/*import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;*/
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;
import com.licenceproject.carpoolingapp.profile.UserDetailsActivity;
import com.licenceproject.carpoolingapp.businessobjects.Passenger;
import com.licenceproject.carpoolingapp.businessobjects.Ride;
import com.licenceproject.carpoolingapp.businessobjects.User;
import com.licenceproject.carpoolingapp.parsing.ParseToData;

//class for the implementation of the reserved ride page
public class RidePageActivity extends ErrorHandlingAppCompatActivity {
    //text views
    private TextView driverName, startLocation, endLocation, departureTime, departureDate, price;
    //the passenger of the ride
    private Passenger passenger;
    //the ride
    private static Ride ride;

    //function that sets the ride
    public static void setRide(Ride setRide) {
        ride = setRide;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_page);

        ParseToData parser = new ParseToData(getApplicationContext());

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
                                for (DocumentSnapshot passengerDocument : querySnapshot.getDocuments()){
                                    passenger = new Passenger(passengerDocument.getId(), passengerDocument.getString("userID"), passengerDocument.getString("rideID"), passengerDocument.getString("firstName"), passengerDocument.getString("receivedRating"), passengerDocument.getString("gaveRating"));
                                }
                            }
                        }
                    }
                });


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

        driverName = findViewById(R.id.user_text);

        driverName.setText("   " + ride.getDriverFirstName());

        CardView cardView = findViewById(R.id.cardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUserDetailsActivity();
            }
        });
    }

    //Opens the user page
    private void openUserDetailsActivity() {
        // Create an intent to open the UserDetailsActivity
        Intent intent = new Intent(this, UserDetailsActivity.class);
        UserDetailsActivity.ride = ride;
        UserDetailsActivity.passenger = passenger;

        // Start the UserDetailsActivity
        startActivity(intent);
    }
}
