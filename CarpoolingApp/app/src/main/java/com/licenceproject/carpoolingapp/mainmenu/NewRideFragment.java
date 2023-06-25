package com.licenceproject.carpoolingapp.mainmenu;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.button.MaterialButton;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.setride.MapsActivity;

//Fragment for creating a new ride
public class NewRideFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public NewRideFragment() {
        // Required empty public constructor
    }

    //creates a new instance of the fragment
    public static NewRideFragment newInstance(String param1, String param2) {
        NewRideFragment fragment = new NewRideFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_ride, container, false);
        if (view != null) {
            MaterialButton button = view.findViewById(R.id.new_ride_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openMaps();
                }
            });
        }

        return view;
    }

    //opens the maps page
    private void openMaps(){
        Intent intent = new Intent(getActivity(), MapsActivity.class);
        startActivity(intent);
    }
}