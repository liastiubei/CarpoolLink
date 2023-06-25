package com.licenceproject.carpoolingapp.mainmenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;

//Class for the implementation of the main menu fragments
public class MainMenuPage extends ErrorHandlingAppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    //view for the navigation between fragments
    BottomNavigationView bottomNavigationView;

    //Fragments
    MyRidesFragment myRidesFragment = new MyRidesFragment();
    NewRideFragment newRideFragment = new NewRideFragment();
    SearchFragment searchFragment = new SearchFragment();
    MessagesFragment messagesFragment = new MessagesFragment();
    ProfileFragment profileFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_page);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.search_menu);
    }

    //function for choosing and opening the menu fragment
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.my_rides_menu:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, myRidesFragment).commit();
                return true;
            case R.id.new_ride_menu:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, newRideFragment).commit();
                return true;
            case R.id.search_menu:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, searchFragment).commit();
                return true;
            case R.id.messages_menu:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, messagesFragment).commit();
                return true;
            case R.id.profile_menu:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, profileFragment).commit();
                return true;
        }
        return false;
    }
}