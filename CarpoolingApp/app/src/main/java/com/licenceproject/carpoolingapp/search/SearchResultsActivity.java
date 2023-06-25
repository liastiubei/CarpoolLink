package com.licenceproject.carpoolingapp.search;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.businessobjects.RideAdapter;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;
import com.licenceproject.carpoolingapp.businessobjects.Ride;
import com.licenceproject.carpoolingapp.mainmenu.SearchFragment;
import com.licenceproject.carpoolingapp.parsing.ParseToData;

//Class for the implementation of the search results page
public class SearchResultsActivity extends ErrorHandlingAppCompatActivity implements RideAdapter.OnItemClickListener{

    //recycler view
    private RecyclerView recyclerView;
    //ride adapter
    private RideAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        TextView locations = findViewById(R.id.locations_search_result);
        TextView date = findViewById(R.id.date_search_result);
        ParseToData parser = new ParseToData(this.getApplicationContext());
        locations.setText(parser.getCityFromLatLng(SearchFragment.getDeparturePlace().getLatLng())
                + " - " + parser.getCityFromLatLng(SearchFragment.getDestinationPlace().getLatLng()));
        date.setText(parser.getOnlyDateFromDate(SearchFragment.getDepartureDate()));

        recyclerView = findViewById(R.id.recycler_search_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RideAdapter(SearchFragment.rides, this.getApplicationContext(), "search");
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(Ride ride) {
        if(ride.getCurrentNrOfPassengers() < ride.getNrPassengers()) {
            RideViewerActivity.setRide(ride);
            Intent intent = new Intent(this, RideViewerActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        SearchFragment.rides.clear();
        super.onDestroy();
        getDelegate().onDestroy();
    }
}