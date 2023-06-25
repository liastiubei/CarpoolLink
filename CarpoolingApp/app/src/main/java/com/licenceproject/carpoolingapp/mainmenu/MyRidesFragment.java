package com.licenceproject.carpoolingapp.mainmenu;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.businessobjects.Ride;
import com.licenceproject.carpoolingapp.businessobjects.User;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.licenceproject.carpoolingapp.myrides.MyRidesListActivity;

//Fragment for the my rides list
public class MyRidesFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    //The list of rides
    public static ArrayList<Ride> rides = new ArrayList<>();

    //number of repetitions for getting the rides
    private static int numberOfRepetitions = 0;

    public MyRidesFragment() {
        // Required empty public constructor
    }

    //creates a new instance of the fragment
    public static MyRidesFragment newInstance(String param1, String param2) {
        MyRidesFragment fragment = new MyRidesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_rides, container, false);
        MaterialButton publishedButton = rootView.findViewById(R.id.buttonPublishedRides);
        MaterialButton reservedButton = rootView.findViewById(R.id.buttonReservedRides);

        publishedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRides("published");
            }
        });

        reservedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRides("reserved");
            }
        });
        return rootView;
    }

    //gets the rides depending on the type of situation
    private void getRides(String situation) {
        switch(situation){
            case "published":
                getPublishedRides(situation);
                break;
            case "reserved":
                getReservedRides(situation);
                break;
        }
    }

    //opens the page of the list of rides
    private void openRidesPage(String situation){
        Intent intent = new Intent(getActivity(), MyRidesListActivity.class);
        intent.putExtra("situation", situation);
        startActivity(intent);
    }

    //gets the reserved rides
    private void getReservedRides(String situation) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        rides.clear();
        numberOfRepetitions = 0;

        db.collection("passengers")
                .whereEqualTo("userID", User.getSessionUser().getUsername())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null) {
                                List<DocumentSnapshot> passengerDocuments = querySnapshot.getDocuments();
                                List<Ride> rideList = new ArrayList<>();
                                for (DocumentSnapshot passengerDocument : passengerDocuments) {
                                    String rideId = passengerDocument.getString("rideID");
                                    if (rideId != null) {
                                        db.collection("rides")
                                                .document(rideId)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot rideDocument = task.getResult();
                                                            numberOfRepetitions++;
                                                            if (rideDocument != null && rideDocument.exists()) {
                                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                                                    Gson gson = new Gson();
                                                                    DocumentSnapshot document = task.getResult();
                                                                        String startDateString = document.getString("startDate");
                                                                        Date startDate = new Date();
                                                                        try {
                                                                            startDate = sdf.parse(startDateString);
                                                                        } catch (ParseException e) {
                                                                            e.printStackTrace();
                                                                        }

                                                                        String startLocationJson = document.getString("startLocation");
                                                                        String endLocationJson = document.getString("endLocation");
                                                                        LatLng startLocation = gson.fromJson(startLocationJson, LatLng.class);
                                                                        LatLng endLocation = gson.fromJson(endLocationJson, LatLng.class);

                                                                        rides.add(new Ride(document.getId(), document.getString("driverID"), document.getString("driverFirstName"),
                                                                                Integer.parseInt(document.getString("nrPassengers")), Integer.parseInt(document.getString("currentNrOfPassengers")), startLocation, endLocation, startDate,
                                                                                document.getString("price"), document.getString("currency")));
                                                                    if(numberOfRepetitions == passengerDocuments.size()){
                                                                        openRidesPage(situation);
                                                                    }

                                                                }
                                                            }
                                                        }
                                                        if (task.isComplete()) {
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        } else {
                            databaseError();
                        }
                    }
                });
    }

    //gets the published rides
    private void getPublishedRides(String situation) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        rides.clear();

        db.collection("rides")
                .whereEqualTo("driverID", User.getSessionUser().getUsername())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            Gson gson = new Gson();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String startDateString = document.getString("startDate");
                                Date startDate;
                                try {
                                    startDate = sdf.parse(startDateString);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    continue;
                                }

                                if (!document.getString("driverID").equals(User.getSessionUser().getUsername())) {
                                    continue;
                                }

                                String startLocationJson = document.getString("startLocation");
                                String endLocationJson = document.getString("endLocation");
                                LatLng startLocation = gson.fromJson(startLocationJson, LatLng.class);
                                LatLng endLocation = gson.fromJson(endLocationJson, LatLng.class);

                                rides.add(new Ride(document.getId(), document.getString("driverID"), document.getString("driverFirstName"),
                                        Integer.parseInt(document.getString("nrPassengers")), Integer.parseInt(document.getString("currentNrOfPassengers")), startLocation, endLocation, startDate,
                                        document.getString("price"), document.getString("currency")));
                            }
                            openRidesPage(situation);
                        }
                    } else {
                        databaseError();
                    }
                });
    }

    //function for handling of a database error
    private void databaseError(){
        Toast.makeText(getContext(), "Database error.", Toast.LENGTH_LONG);
    }
}