package com.licenceproject.carpoolingapp.profile;

import static com.android.volley.VolleyLog.TAG;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.businessobjects.User;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;

import java.util.HashMap;
import java.util.Map;

//class for the name-change page
public class ChangeNameActivity extends ErrorHandlingAppCompatActivity {

    //first name textview
    private TextView firstName;
    //last name textview
    private TextView lastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name);

        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);

        MaterialButton updateButton = findViewById(R.id.change_name_button);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkRequirements()) {
                    updateProfile();
                }
            }
        });

        // Set the existing first and last names in the input fields
        firstName.setText(User.getSessionUser().getFirstName());
        lastName.setText(User.getSessionUser().getLastName());

    }

    //function for updating the name change
    private void updateProfile() {
        String updatedFirstName = firstName.getText().toString();
        String updatedLastName = lastName.getText().toString();

        if (!User.getSessionUser().getFirstName().equals(updatedFirstName) || !User.getSessionUser().getLastName().equals(updatedLastName)) {
            // At least one of the names has changed, proceed with updating

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Query query = db.collection("users")
                    .whereEqualTo("username", User.getSessionUser().getUsername());

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Retrieve the document reference for the current user
                            DocumentReference userDocRef = task.getResult().getDocuments().get(0).getReference();

                            Map<String, Object> updatedData = new HashMap<>();
                            updatedData.put("firstname", updatedFirstName);
                            updatedData.put("lastname", updatedLastName);

                            // Update the user document with the new first and last names
                            userDocRef.update(updatedData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(ChangeNameActivity.this, "Profile updated successfully.", Toast.LENGTH_LONG).show();
                                            Log.d(TAG, "User profile updated!");

                                            // Update the first and last names in the currentUser object as well
                                            User.getSessionUser().setFirstName(updatedFirstName);
                                            User.getSessionUser().setLastName(updatedLastName);

                                            waitTwoSeconds();
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ChangeNameActivity.this, "Couldn't update user profile.", Toast.LENGTH_LONG).show();
                                            Log.w(TAG, "Error updating user profile", e);
                                        }
                                    });
                        } else {
                            Toast.makeText(ChangeNameActivity.this, "User not found.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(ChangeNameActivity.this, "Couldn't update user profile.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            // Both first and last names are the same, no need to update
            Toast.makeText(ChangeNameActivity.this, "No changes made to the profile.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    //function for waiting two seconds
    private void waitTwoSeconds() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // yourMethod();
            }
        }, 2000);
    }

    //function for checking the requirements for the names
    private boolean checkRequirements() {
        removeErrors();
        return checkName(firstName) && checkName(lastName);
    }

    //function for checking if the name textview fulfills the requirements
    private boolean checkName(TextView nameView) {
        CharSequence name = nameView.getText();
        if (name.length() < 3 || name.length() > 20) {
            nameView.setError("The name should have between 3 and 20 characters");
            return false;
        }
        if (!isLetter(name.charAt(0))) {
            nameView.setError("The name should start with a letter");
            return false;
        }
        for (int i = 1; i < name.length(); i++) {
            if (!isLetter(name.charAt(i)) && !isSpaceOrMinus(name.charAt(i))) {
                nameView.setError("The name should only contain 'A-Z', 'a-z', space, or '-'.");
                return false;
            }
        }
        return true;
    }

    //function for removing errors
    private void removeErrors() {
        firstName.setError(null);
        lastName.setError(null);
    }

    //checks if the character is a letter
    private boolean isLetter(char c) {
        return (c >= 65 && c <= 90) || (c >= 97 && c <= 122);
    }

    //checks if the character is a space or a minus
    private boolean isSpaceOrMinus(char c) {
        return c == ' ' || c == '-';
    }
}