package com.licenceproject.carpoolingapp.businessobjects;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.parsing.ParseToData;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for the implementation of the Conversation List
 */

public class ConversationAdapter extends ArrayAdapter<Conversation> {
    //context
    private Context context;

    //Constructor
    public ConversationAdapter(Context context, List<Conversation> conversations) {
        super(context, 0, conversations);
        this.context = context;
    }

    //Creates and returns the view of each conversation, while also requesting the necessary information from the database
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Conversation conversation = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        }

        TextView nameTextView = convertView.findViewById(R.id.nameTextView);
        TextView dateTextView = convertView.findViewById(R.id.rideDateTextView);
        TextView locationsTextView = convertView.findViewById(R.id.rideLocationsTextView);

        FirebaseFirestore.getInstance().collection("passengers").document(conversation.getPassengerId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot != null && documentSnapshot.exists()){
                    Passenger passenger = new Passenger(documentSnapshot.getId(), documentSnapshot.getString("userID"), documentSnapshot.getString("rideID"), documentSnapshot.getString("firstName"), documentSnapshot.getString("receivedRating"), documentSnapshot.getString("gaveRating"));
                    FirebaseFirestore.getInstance().collection("rides").document(passenger.getRideId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot snapshot) {
                            if(snapshot != null && snapshot.exists()){
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                    Gson gson = new Gson();
                                    String startDateString = snapshot.getString("startDate");
                                    Date startDate = new Date();
                                    try {
                                        startDate = sdf.parse(startDateString);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    String startLocationJson = snapshot.getString("startLocation");
                                    String endLocationJson = snapshot.getString("endLocation");
                                    LatLng startLocation = gson.fromJson(startLocationJson, LatLng.class);
                                    LatLng endLocation = gson.fromJson(endLocationJson, LatLng.class);

                                    Ride ride = new Ride(snapshot.getId(), snapshot.getString("driverID"), snapshot.getString("driverFirstName"),
                                            Integer.parseInt(snapshot.getString("nrPassengers")), Integer.parseInt(snapshot.getString("currentNrOfPassengers")), startLocation, endLocation, startDate,
                                            snapshot.getString("price"), snapshot.getString("currency"));

                                    Calendar currentCalendar = Calendar.getInstance();
                                    ParseToData parser = new ParseToData(context);
                                    try {
                                        locationsTextView.setText(parser.getCityFromLatLng(ride.getStartLocation()) + " - " + parser.getCityFromLatLng(ride.getEndLocation()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    dateTextView.setText(parser.getOnlyDateFromDate(ride.getDate()));

                                    if(ride.getDriverId().equals(User.getSessionUser().getUsername())){
                                        nameTextView.setText(passenger.getFirstName());
                                    } else {
                                        nameTextView.setText(ride.getDriverFirstName());
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });

        return convertView;
    }
}
