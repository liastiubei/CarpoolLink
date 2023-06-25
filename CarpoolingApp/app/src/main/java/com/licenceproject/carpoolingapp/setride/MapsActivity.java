package com.licenceproject.carpoolingapp.setride;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.button.MaterialButton;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;

import java.util.Arrays;

//class that implements the beginning of setting a ride, choosing the destination and departure places on the map
public class MapsActivity extends ErrorHandlingAppCompatActivity implements OnMapReadyCallback {

    //map view
    private MapView mapView;
    //departing place
    static public Place departingPlace;
    //arriving place
    static public Place arrivingPlace;
    //google map
    private GoogleMap map;
    //boolean to check if the button has been pressed
    private boolean wasTheButtonPressed = false;
    //button for choosing the destination
    private MaterialButton destinationbutton;
    //button for going to the next step
    private MaterialButton nextbutton;
    //map marker
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        departingPlace = null;
        arrivingPlace = null;
        // Set up the Autocomplete widget
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setHint("Choose departure");

        destinationbutton = findViewById(R.id.choose_destination_button);
        nextbutton = findViewById(R.id.maps_next_button);
        destinationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(departingPlace == null){
                    Toast.makeText(MapsActivity.this, "You haven't selected a place.", Toast.LENGTH_LONG).show();
                }
                else if(departingPlace != null && arrivingPlace == null){
                    destinationbutton.setVisibility(View.INVISIBLE);
                    autocompleteFragment.setText("");
                    autocompleteFragment.setHint("Choose destination");
                    wasTheButtonPressed = true;
                    destinationbutton.setVisibility(View.INVISIBLE);
                }
            }
        });

        nextbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(departingPlace != null && arrivingPlace != null){
                    openConfirmPage();
                }
            }
        });

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Initialize the Places SDK
        Places.initialize(getApplicationContext(), "AIzaSyCAEl4PZZ2KDNBLJQdLrgKTqyFK6rcVJoA");

        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(this);


        // Specify the types of place data to return
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG, Place.Field.ADDRESS));
        // Set up a PlaceSelectionListener to handle the user's selection
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // Handle the selected place
                if(!wasTheButtonPressed){
                    departingPlace = place;
                    destinationbutton.setVisibility(View.VISIBLE);
                }
                else {
                    arrivingPlace = place;
                    nextbutton.setVisibility(View.VISIBLE);
                }
                LatLng latlng = place.getLatLng();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latlng);
                markerOptions.title(place.getAddress());
                //markerOptions.snippet(place.getAddress());
                if(marker != null){
                    marker.remove();
                }
                marker = map.addMarker(markerOptions);
            }

            @Override
            public void onError(Status status) {
                // Handle the error
            }
        });
    }

    //function that opens the confirmation page for the route
    private void openConfirmPage() {
        Intent intent = new Intent(this, ConfirmMapsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}