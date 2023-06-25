package com.licenceproject.carpoolingapp.parsing;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//class for parsing data
public class ParseToData {
    //context
    private Context context;

    //constructor
    public ParseToData(Context context) {
        this.context = context;
    }

    //returns the city as a string from latitude and longitude
    public String getCityFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && !addresses.isEmpty()) {
            Address address = addresses.get(0);
            return address.getLocality(); // get the city name
        } else {
            return "Unknown location";
        }
    }

    //returns the formatted address as a string from latitude and longitude
    public String getFormattedAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && !addresses.isEmpty()) {
            Address address = addresses.get(0);
            String city = address.getLocality();
            String street = address.getThoroughfare();
            String streetNumber = address.getSubThoroughfare();
            StringBuilder formattedAddress = new StringBuilder();

            if (city != null) {
                formattedAddress.append(city);
                formattedAddress.append(", ");
            }
            if (street != null) {
                formattedAddress.append(street);
                if (streetNumber != null) {
                    formattedAddress.append(" ");
                    formattedAddress.append(streetNumber);
                }
            }

            if (formattedAddress.length() > 0) {
                return formattedAddress.toString();
            } else {
                return "Unknown location";
            }
        } else {
            return "Unknown location";
        }
    }

    //returns the time from a Date
    public String getTimeFromDate(Date date){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

            String formattedTime = timeFormat.format(date);
            return formattedTime;
        }

        return date.toString();
    }

    //returns only the date from a Date
    public String getOnlyDateFromDate(Date date){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            String formattedDate = dateFormat.format(date);
            return formattedDate;
        }

        return date.toString();
    }
}
