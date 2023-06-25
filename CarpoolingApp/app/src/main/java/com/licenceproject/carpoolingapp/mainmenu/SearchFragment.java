package com.licenceproject.carpoolingapp.mainmenu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.util.Consumer;
import com.google.gson.Gson;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.businessobjects.Ride;
import com.licenceproject.carpoolingapp.search.SearchResultsActivity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

//Fragment for searching rides
public class SearchFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    //departure place
    private static Place departurePlace;
    //destination place
    private static Place destinationPlace;
    //date of departure
    private static Date departureDate;
    //date picker
    private DatePicker datePicker;
    //search button
    private Button searchButton;

    //list of rides
    public static ArrayList<Ride> rides = new ArrayList<>();

    public SearchFragment() {
        // Required empty public constructor
    }

    //creates a new instance of the fragment
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize the Places SDK
        Places.initialize(getContext(), "AIzaSyCAEl4PZZ2KDNBLJQdLrgKTqyFK6rcVJoA");

        // Configure the departure search FragmentContainerView
        FragmentContainerView departureSearch = rootView.findViewById(R.id.departure_search);
        setupAutocompleteSupportFragment(departureSearch, "Select departure", place -> departurePlace = place);

        // Configure the destination search FragmentContainerView
        FragmentContainerView destinationSearch = rootView.findViewById(R.id.destination_search);
        setupAutocompleteSupportFragment(destinationSearch, "Select destination", place -> destinationPlace = place);

        DatePicker datePicker = rootView.findViewById(R.id.date_picker);
        datePicker.init(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), (view, year, monthOfYear, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, monthOfYear, dayOfMonth);
            departureDate = calendar.getTime();
        });

        Button searchButton = rootView.findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> searchRides());

        return rootView;
    }

    //function that performs the setup of the search fragments
    private void setupAutocompleteSupportFragment(FragmentContainerView fragmentContainerView, String hint, Consumer<Place> onPlaceSelected) {
        AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(fragmentContainerView.getId());
        autocompleteSupportFragment.setHint(hint);
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteSupportFragment.setTypeFilter(TypeFilter.CITIES);
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                onPlaceSelected.accept(place);
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getContext(), "Error: " + status, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //function that performs the search of the rides
    private void searchRides() {
        if (departurePlace == null || destinationPlace == null || departureDate == null) {
            Toast.makeText(getContext(), "Please fill in all the fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rides")
                .orderBy("startDate")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            rides.clear();
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

                                if (startDate == null || !isSameDay(startDate, departureDate)) {
                                    continue;
                                }

                                String startLocationJson = document.getString("startLocation");
                                String endLocationJson = document.getString("endLocation");
                                LatLng startLocation = gson.fromJson(startLocationJson, LatLng.class);
                                LatLng endLocation = gson.fromJson(endLocationJson, LatLng.class);

                                if (distanceBetween(departurePlace.getLatLng(), startLocation) <= 50 &&
                                        distanceBetween(destinationPlace.getLatLng(), endLocation) <= 50) {
                                    rides.add(new Ride(document.getId(), document.getString("driverID"), document.getString("driverFirstName"),
                                            Integer.parseInt(document.getString("nrPassengers")), Integer.parseInt(document.getString("currentNrOfPassengers")), startLocation, endLocation, startDate,
                                            document.getString("price"), document.getString("currency")));
                                }
                            }
                            openResultsPage();
                        }
                    } else {
                        Toast.makeText(getContext(), "Error searching rides.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //function that shows the results of the search
    private void openResultsPage() {
        Intent intent = new Intent(getActivity(), SearchResultsActivity.class);
        startActivity(intent);
    }

    //function that compares two dates and checks if the day coincides
    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    //function for calculating the distance between two places on the map using their latitude and longitude
    private double distanceBetween(LatLng latLng1, LatLng latLng2) {
        // Radius of the earth in km
        double earthRadius = 6371;

        // Haversine formula for calculating the great-circle distance between two points on a sphere
        double dLat = Math.toRadians(latLng2.latitude - latLng1.latitude);
        double dLng = Math.toRadians(latLng2.longitude - latLng1.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(latLng1.latitude)) *
                Math.cos(Math.toRadians(latLng2.latitude)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Multiply the angular distance by the radius of the earth to get the actual distance
        return earthRadius * c;
    }

    //returns the departure place
    public static Place getDeparturePlace() {
        return departurePlace;
    }

    //returns the destination place
    public static Place getDestinationPlace() {
        return destinationPlace;
    }

    //returns the date of the departure
    public static Date getDepartureDate() {
        return departureDate;
    }

}


