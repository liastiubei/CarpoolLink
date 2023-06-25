package com.licenceproject.carpoolingapp.businessobjects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.parsing.ParseToData;

import java.util.ArrayList;
import java.util.Calendar;
/**
 * Adapter for the implementation of the Ride List
 */
public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

    //List of rides
    private ArrayList<Ride> rideList;
    //Context
    private Context context;
    //on click listener
    private OnItemClickListener listener;
    //type of situation
    private String situation;

    //constructor
    public RideAdapter(ArrayList<Ride> rideList, Context applicationContext, String situation) {
        this.rideList = rideList;
        this.situation = situation;
        context = applicationContext;
    }

    //sets on click listener for the ride
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ride_card_layout, parent, false);
        return new RideViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = rideList.get(position);
        Calendar currentCalendar = Calendar.getInstance();
        holder.driverName.setText(ride.getDriverFirstName());
        ParseToData parser = new ParseToData(context);
        try {
            holder.startLocation.setText("Start: " + parser.getFormattedAddressFromLatLng(ride.getStartLocation()));
            holder.endLocation.setText("End: " + parser.getFormattedAddressFromLatLng(ride.getEndLocation()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.departureTime.setText(parser.getTimeFromDate(ride.getDate()));
        holder.price.setText(ride.getPrice().toString() + " "+ ride.getCurrency());
        if((situation.equals("search") && ride.getCurrentNrOfPassengers() >= ride.getNrPassengers())
            || (situation.equals("com/licenceproject/carpoolingapp/myrides") && currentCalendar.getTime().compareTo(ride.getDate()) > 0)){
            holder.cardView.setAlpha(0.5F);
        }
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    //interface for the on click listener
    public interface OnItemClickListener {
        void onItemClick(Ride ride);
    }

    // ViewHolder for the individual items in the RecyclerView
    public class RideViewHolder extends RecyclerView.ViewHolder {
        public TextView driverName, startLocation, endLocation, departureTime, price;
        public CardView cardView;

        //Constructor for the RideViewHolder
        public RideViewHolder(@NonNull View view) {
            super(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(rideList.get(position));
                    }
                }
            });

            driverName = view.findViewById(R.id.driver_name);
            startLocation = view.findViewById(R.id.start_location);
            endLocation = view.findViewById(R.id.end_location);
            departureTime = view.findViewById(R.id.departure_time);
            price = view.findViewById(R.id.price_of_ride);
            cardView = view.findViewById(R.id.cardView);
        }
    }
}
