package com.licenceproject.carpoolingapp.setride;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;

//class for the page for confirming the route
public class ConfirmMapsActivity extends ErrorHandlingAppCompatActivity implements OnMapReadyCallback {

    //map view
    private MapView mapView;
    //google map
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_maps);
        mapView = findViewById(R.id.mapView_confirm);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

//        TextView textView = findViewById(R.id.text_confirm);
//        textView.setText(getConfirmText());
        MaterialButton button = findViewById(R.id.confirm_map_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNextPage();
            }
        });
    }

    //Function that adds the visual markers and lines to the map
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        MarkerOptions markerOptionDepart = new MarkerOptions()
                .position(MapsActivity.departingPlace.getLatLng())
                .title(MapsActivity.departingPlace.getName());

        MarkerOptions markerOptionArrive = new MarkerOptions()
                .position(MapsActivity.arrivingPlace.getLatLng())
                .title(MapsActivity.arrivingPlace.getName());

        Marker markerDepart = map.addMarker(markerOptionDepart);
        Marker markerArrive = map.addMarker(markerOptionArrive);

        PolylineOptions polylineOptions = new PolylineOptions()
                .add(markerDepart.getPosition())
                .add(markerArrive.getPosition())
                .color(Color.RED)
                .width(5);
        Polyline polyline = map.addPolyline(polylineOptions);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : polyline.getPoints()) {
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();
        int padding = 100; // padding in pixels
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        map.animateCamera(cameraUpdate);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
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

    //opens the next page in the process of setting a ride
    private void openNextPage() {
        Intent intent = new Intent(this, SetRideDetailsActivity.class);
        startActivity(intent);
        finish();
    }
}