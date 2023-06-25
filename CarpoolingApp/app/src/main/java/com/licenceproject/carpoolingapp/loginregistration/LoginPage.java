package com.licenceproject.carpoolingapp.loginregistration;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.businessobjects.User;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;
import com.licenceproject.carpoolingapp.mainmenu.MainMenuPage;
import com.licenceproject.carpoolingapp.parsing.Encryption;

import java.io.IOException;
import java.io.InputStream;

//Class for the implementation of the login page
public class LoginPage extends ErrorHandlingAppCompatActivity {

    //username textview
    private TextView username;
    //password textview
    private TextView password;


    //key preferences for a permanent login
    public static final String PREFS_NAME = "MyPrefs";
    public static final String PREFS_KEY_LOGIN = "isLoggedIn";
    public static final String PREFS_USERNAME ="username";
    public static final String PREFS_PASSWORD ="password";
    public static final String PREFS_FIRSTNAME ="firstname";
    public static final String PREFS_LASTNAME ="lastname";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        // Check if the user is already logged in
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(PREFS_KEY_LOGIN, false);

        if (isLoggedIn) {
            // User is already logged in, open the main menu page
            User.createSessionUser(new User(prefs.getString(PREFS_USERNAME, null), prefs.getString(PREFS_PASSWORD, null),
                    prefs.getString(PREFS_FIRSTNAME, null), prefs.getString(PREFS_LASTNAME, null)));
            openMainMenuPage();
            return;
        }

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        MaterialButton loginbutton = findViewById(R.id.loginbutton);

        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLogin();
            }
        });
        MaterialButton registrationButton = findViewById(R.id.register);

        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRegistrationPage();
            }
        });

        showTermsAndConditions();
    }

    //function for showing the terms and conditions only if the user is not logged in
    private void showTermsAndConditions() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(PREFS_KEY_LOGIN, false);

        if (!isLoggedIn) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Terms and Conditions")
                    .setMessage(loadAssetText("terms_conditions.txt"))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .setCancelable(false);

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    //function to perform the login
    private void performLogin() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("users")
                .whereEqualTo("username", username.getText().toString());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult().size() == 0){
                        Toast.makeText(LoginPage.this, "FAILURE: Incorrect username or password.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(task.getResult().size() > 1){
                        Toast.makeText(LoginPage.this, "FAILURE: Too many identical usernames.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String pass = document.get("password").toString();
                        if(Encryption.checkPassword(password.getText().toString(), pass)){
                            Toast.makeText(LoginPage.this, "LOGIN SUCCESSFUL", Toast.LENGTH_LONG).show();
                            User.createSessionUser(new User(username.getText().toString(), password.getText().toString(), document.get("firstname").toString(), document.get("lastname").toString()));

                            // Mark the user as logged in
                            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean(PREFS_KEY_LOGIN, true);
                            editor.putString(PREFS_USERNAME, User.getSessionUser().getUsername());
                            editor.putString(PREFS_FIRSTNAME, User.getSessionUser().getFirstName());
                            editor.putString(PREFS_LASTNAME, User.getSessionUser().getLastName());
                            editor.putString(PREFS_PASSWORD, User.getSessionUser().getPassword());
                            editor.apply();

                            openMainMenuPage();
                        }else{
                            Toast.makeText(LoginPage.this, "FAILURE: Incorrect username or password.", Toast.LENGTH_LONG).show();
                        }
                    }

                }else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                    Toast.makeText(LoginPage.this, "FAILURE: Error while connecting to database.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //function for opening the registration page
    private void openRegistrationPage() {
        Intent intent = new Intent(this, RegistrationPage.class);
        startActivity(intent);
    }

    //function for opening the main menu page
    private void openMainMenuPage() {
        Intent intent = new Intent(this, MainMenuPage.class);
        startActivity(intent);
        finish();
    }

    //function for loading the asset text for the terms and conditions
    private String loadAssetText(String filename) {
        String assetText = "";

        try {
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            assetText = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return assetText;
    }
}