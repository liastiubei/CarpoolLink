package com.licenceproject.carpoolingapp.myrides;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.businessobjects.Passenger;
import com.licenceproject.carpoolingapp.businessobjects.Ride;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;
import com.licenceproject.carpoolingapp.mainmenu.MyRidesFragment;
import com.licenceproject.carpoolingapp.businessobjects.RideAdapter;

import java.util.ArrayList;
import java.util.List;

//class for implementing the ride list for my rides
public class MyRidesListActivity extends ErrorHandlingAppCompatActivity implements RideAdapter.OnItemClickListener {
    //recycler view for rides
    private RecyclerView recyclerView;
    //ride adapter
    private RideAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rides_list);

        TextView textView = findViewById(R.id.my_rides_text_view);

        textView.setText("My " + getIntent().getExtras().get("situation") + " rides");
        recyclerView = findViewById(R.id.recycler_rides_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RideAdapter(MyRidesFragment.rides, this.getApplicationContext(), "com/licenceproject/carpoolingapp/myrides");
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    //function that when you click on a ride it opens said ride
    @Override
    public void onItemClick(Ride ride) {
        if(getIntent().getExtras().get("situation").equals("reserved")){
            openRideReservedViewer(ride);
        }
        else{
            getPassengers(ride);
        }
    }

    private void openRideReservedViewer(Ride ride){
        RidePageActivity.setRide(ride);
        Intent intent = new Intent(this, RidePageActivity.class);
        startActivity(intent);
    }

    //function that opens the page for a published ride
    private void openRidePublishedViewer(){
        Intent intent = new Intent(this, RidePublishedViewerActivity.class);
        startActivity(intent);
    }

    //function that gets the passengers for the published ride
    public void getPassengers(Ride ride) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final List<Passenger> passengers = new ArrayList<>();

        // Query the "passengers" collection to fetch the passengers for this ride
        db.collection("passengers")
                .whereEqualTo("rideID", ride.getRideId())
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

                            Passenger passenger = new Passenger(passengerId, userId, rideId,
                                    firstName, receivedRating, gaveRating);
                            passengers.add(passenger);
                        }
                        RidePublishedViewerActivity.setRide(ride);
                        RidePublishedViewerActivity.setPassengers(passengers);
                        openRidePublishedViewer();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {  }
                });
    }

    @Override
    protected void onDestroy() {
        MyRidesFragment.rides.clear();
        super.onDestroy();
        getDelegate().onDestroy();
    }
}