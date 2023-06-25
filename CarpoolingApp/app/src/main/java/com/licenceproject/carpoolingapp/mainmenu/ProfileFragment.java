package com.licenceproject.carpoolingapp.mainmenu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.licenceproject.carpoolingapp.loginregistration.LoginPage;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.businessobjects.User;
import com.licenceproject.carpoolingapp.profile.PersonalInformationActivity;
import com.licenceproject.carpoolingapp.profile.UserDetailsActivity;


//Fragment for the profile options
public class ProfileFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    //creates a new instance of the fragment
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        MaterialButton personalInformation = view.findViewById(R.id.personalinformationbutton);
        MaterialButton myReviews = view.findViewById(R.id.myreviewsbutton);
        MaterialButton logout = view.findViewById(R.id.logoutbutton);

        personalInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPersonalInformationActivity();
            }
        });

        myReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUserDetailsActivity();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLogout();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    //opens the personal information page
    private void openPersonalInformationActivity(){
        Intent intent = new Intent(getActivity(), PersonalInformationActivity.class);
        startActivity(intent);
    }

    //opens the user details page
    private void openUserDetailsActivity(){
        Intent intent = new Intent(getActivity(), UserDetailsActivity.class);
        UserDetailsActivity.user = User.getSessionUser();
        startActivity(intent);
    }

    //the user is logged out, and the user information is removed from the device
    private void performLogout() {
        // Clear the login status and username from SharedPreferences
        SharedPreferences prefs = getActivity().getSharedPreferences(
                LoginPage.PREFS_NAME, LoginPage.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove(LoginPage.PREFS_KEY_LOGIN);
        editor.remove(LoginPage.PREFS_USERNAME);
        editor.remove(LoginPage.PREFS_PASSWORD);
        editor.remove(LoginPage.PREFS_FIRSTNAME);
        editor.remove(LoginPage.PREFS_LASTNAME);
        editor.apply();

        // Close all activities and open the login page
        Intent intent = new Intent(getActivity(), LoginPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.getActivity().finish();
    }
}