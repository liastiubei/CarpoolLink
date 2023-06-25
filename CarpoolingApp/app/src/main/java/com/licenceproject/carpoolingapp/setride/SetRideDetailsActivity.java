package com.licenceproject.carpoolingapp.setride;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;
import com.licenceproject.carpoolingapp.businessobjects.Ride;
import com.licenceproject.carpoolingapp.businessobjects.User;

import java.util.Calendar;
import java.util.Date;

//class for the implementation of the finalisation of the setting of the ride
public class SetRideDetailsActivity extends ErrorHandlingAppCompatActivity {
    //number of passengers textview
    private TextView nrPassengers;
    //price textview
    private TextView price;
    //selected departure date and time
    private Date selectedDateAndTime;
    //selected currency string
    private String selectedCurrency;
    //calendar instance
    private Calendar selectedCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_ride_details);

        nrPassengers = findViewById(R.id.numberOfPassengersEditText);
        price = findViewById(R.id.pricePerSeatEditText);

        DatePicker datePicker = findViewById(R.id.date_picker);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                    selectedCalendar.set(year, monthOfYear, dayOfMonth);
                    selectedDateAndTime = selectedCalendar.getTime();
                }
            });
        }

        TimePicker timePicker = findViewById(R.id.timePicker);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedCalendar.set(Calendar.MINUTE, minute);
                selectedDateAndTime = selectedCalendar.getTime();
            }
        });

        Spinner spinner = findViewById(R.id.currency_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currencySpinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCurrency = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        MaterialButton setRideButton = findViewById(R.id.finish_set_ride_button);

        setRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(areFieldsValid()){
                    performSetRide();
                }
            }
        });
    }

    //function to check if the fields are valid
    private boolean areFieldsValid() {
        Calendar currentCalendar = Calendar.getInstance();
        if (currentCalendar.compareTo(selectedCalendar) > 0) {
            Toast.makeText(SetRideDetailsActivity.this, "The selected date and time are no longer available", Toast.LENGTH_LONG).show();
            return false;
        }
        if(nrPassengers.getText().toString().length() < 1 || price.getText().toString().length() < 1){
            Toast.makeText(SetRideDetailsActivity.this, "Please answer all fields", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    //function to perform setting the ride
    private void performSetRide() {
        Ride newRide = new Ride(null, User.getSessionUser().getUsername(), User.getSessionUser().getFirstName(),
                Integer.parseInt(nrPassengers.getText().toString()), 0, MapsActivity.departingPlace.getLatLng(), MapsActivity.arrivingPlace.getLatLng(),
                selectedDateAndTime, price.getText().toString(), selectedCurrency);

        newRide.performSetRide(this);
    }

    @Override
    public void setResultText(boolean performSetRide) {
        if(performSetRide){
            Toast.makeText(SetRideDetailsActivity.this, "Ride set successfully.", Toast.LENGTH_LONG).show();
            finish();
        }
        else{
            Toast.makeText(SetRideDetailsActivity.this, "ERROR: Ride couldn't be set.", Toast.LENGTH_LONG).show();
        }
    }
}