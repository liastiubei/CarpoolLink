package com.licenceproject.carpoolingapp.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.businessobjects.User;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;

//Class for the implementation of the personal information page
public class PersonalInformationActivity extends ErrorHandlingAppCompatActivity {
    //username textview
    TextView username;
    //firstname textview
    TextView firstname;
    //lastname textview
    TextView lastname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_information);

        username = findViewById(R.id.username);
        firstname = findViewById(R.id.firstName);
        lastname = findViewById(R.id.last_name);

        username.setText("Username: " + User.getSessionUser().getUsername());
        firstname.setText("First name: " + User.getSessionUser().getFirstName());
        lastname.setText("Last name: " + User.getSessionUser().getLastName());

        MaterialButton nameButton = findViewById(R.id.change_name);
        MaterialButton passButton = findViewById(R.id.change_pass);

        nameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openChangeNameActivity();
            }
        });

        passButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openChangePasswordActivity();
            }
        });
    }

    //opens the password changing activity
    private void openChangePasswordActivity(){
        Intent intent = new Intent(PersonalInformationActivity.this, ChangePasswordActivity.class);
        startActivity(intent);
    }

    //opens the name changing activity
    private void openChangeNameActivity(){
        Intent intent = new Intent(PersonalInformationActivity.this, ChangeNameActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateInformation();
    }

    //function that updates the information in the textviews
    private void updateInformation() {
        username.setText("Username: " + User.getSessionUser().getUsername());
        firstname.setText("First name: " + User.getSessionUser().getFirstName());
        lastname.setText("Last name: " + User.getSessionUser().getLastName());
    }
}