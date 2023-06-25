package com.licenceproject.carpoolingapp.loginregistration;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;
import com.licenceproject.carpoolingapp.parsing.Encryption;

import java.util.HashMap;
import java.util.Map;

//Class for the implementation of the registration page
public class RegistrationPage extends ErrorHandlingAppCompatActivity {

    //textview for username
    private TextView username;
    //textview for password
    private TextView password;
    //textview for the repeating of password
    private TextView passwordRepeat;
    //textview for the first name
    private TextView firstName;
    //textview for the last name
    private TextView lastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_page);

        username = findViewById(R.id.usernameRegister);
        password = findViewById(R.id.passwordRegister);
        passwordRepeat = findViewById(R.id.repeatPasswordRegister);
        firstName = findViewById(R.id.firstNameRegister);
        lastName = findViewById(R.id.lastNameRegister);

        MaterialButton loginbutton = findViewById(R.id.register);

        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkRequirements()){
                    registerUser();
                }
            }
        });
    }

    //function for registering the user
    private void registerUser(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("users")
                .whereEqualTo("username", username.getText().toString());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult().isEmpty()){
                                Map<String, Object> city = new HashMap<>();
                                city.put("username", username.getText().toString());
                                city.put("firstname", firstName.getText().toString());
                                city.put("lastname", lastName.getText().toString());
                                city.put("password", Encryption.hashPassword(password.getText().toString()));
                                db.collection("users").document(username.getText().toString())
                                        .set(city)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(RegistrationPage.this, "Registration successful. Please log in.", Toast.LENGTH_LONG).show();
                                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                                waitFiveSeconds();
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error writing document", e);
                                            }
                                        });
                            }
                            else Toast.makeText(RegistrationPage.this, "FAILURE: This username already exists.", Toast.LENGTH_LONG).show();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    //function for a thread to wait 5 seconds
    private void waitFiveSeconds(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // yourMethod();
            }
        }, 5000);
    }

    //function that returns whether the textviews fullfill the requirements
    private boolean checkRequirements() {
        removeErrors();
        return checkUsername() && checkPassword() && checkFirstName() && checkLastName();
    }

    //function for checking the requirements of the last name textview
    private boolean checkLastName(){
        CharSequence name = lastName.getText();
        if(name.length() < 3 || name.length()>20){
            lastName.setError("Last name should have between 3 and 20 characters");
            return false;
        }
        if(!isLetter(name.charAt(0))){
            lastName.setError("Last name should start with a letter");
            return false;
        }
        for(int i = 1; i < name.length(); i++){
            if(!isLetter(name.charAt(i)) && !isSpaceOrMinus(name.charAt(i))){
                lastName.setError("Last name should only contain 'A-Z', 'a-z', space, or '-'.");
                return false;
            }
        }
        return true;
    }

    //function for checking the requirements of the first name textview
    private boolean checkFirstName(){
        CharSequence name = firstName.getText();
        if(name.length() < 3 || name.length()>20){
            firstName.setError("First name should have between 3 and 20 characters");
            return false;
        }
        if(!isLetter(name.charAt(0))){
            firstName.setError("First name should start with a letter");
            return false;
        }
        for(int i = 1; i < name.length(); i++){
            if(!isLetter(name.charAt(i)) && !isSpaceOrMinus(name.charAt(i))){
                firstName.setError("First name should only contain 'A-Z', 'a-z', space, or '-'.");
                return false;
            }
        }
        return true;
    }

    //function for checking the requirements of the password
    private boolean checkPassword(){
        CharSequence pass = password.getText();
        CharSequence passRep = passwordRepeat.getText();
        boolean hasUpperCase = false, hasNumber = false, hasSpecial = false;
        if(pass.length() < 5 || pass.length()>20){
            password.setError("Password should have between 5 and 20 characters");
            return false;
        }
        if(!isLetter(pass.charAt(0))){
            password.setError("Password should start with a letter");
            return false;
        }
        for(int i = 0; i < pass.length(); i++){
            if(!isLetter(pass.charAt(i)) && !isNumber(pass.charAt(i)) && !isUnderspaceOrPoint(pass.charAt(i))){
                password.setError("Password should only contain 'A-Z', 'a-z', '0-9', '.' or '_'");
                return false;
            }
            if(pass.charAt(i) >= 65 && pass.charAt(i) <= 90){
                hasUpperCase = true;
            } else if(isNumber(pass.charAt(i))){
                hasNumber = true;
            } else if(isUnderspaceOrPoint(pass.charAt(i))){
                hasSpecial = true;
            }
        }
        if(!(hasUpperCase && hasNumber && hasSpecial)){
            password.setError("Password should contain at least a uppercase character, a number or a special character ('_', '.').");
            return false;
        }
        if(!pass.toString().equals(passRep.toString())){
            passwordRepeat.setError("The passwords must match.");
            return false;
        }
        return true;

    }

    //function for checking the requirements of the username
    private boolean checkUsername(){
        CharSequence name = username.getText();
        if(name.length() < 5 || name.length()>20){
            username.setError("Username should have between 5 and 20 characters");
            return false;
        }
        if(!isLetter(name.charAt(0))){
            username.setError("Username should start with a letter");
            return false;
        }
        for(int i = 1; i < name.length(); i++){
            if(!isLetter(name.charAt(i)) && !isNumber(name.charAt(i)) && !isUnderspaceOrPoint(name.charAt(i))){
                username.setError("Username should only contain 'A-Z', 'a-z', '0-9', '.' or '_'");
                return false;
            }
        }
        return true;
    }

    //function for removing the errors
    private void removeErrors() {
        username.setError(null);
        password.setError(null);
        firstName.setError(null);
        lastName.setError(null);
        passwordRepeat.setError(null);
    }

    //function for checking if the character is a letter
    private boolean isLetter(char c){
        return (c >= 65 && c <= 90) || (c >= 97 && c <= 122);
    }

    //function for checking if the character is a number
    private boolean isNumber(char c){
        return c >=48 && c <= 57;
    }

    //function for checking if the character is an underspace or a point
    private boolean isUnderspaceOrPoint(char c){
        return c == '.' || c == '_';
    }

    //function for checking if the character is a space or a minus
    private boolean isSpaceOrMinus(char c){
        return c == ' ' || c == '-';
    }

}