package com.licenceproject.carpoolingapp.profile;

import androidx.appcompat.app.AppCompatActivity;
import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;

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
import com.licenceproject.carpoolingapp.parsing.Encryption;

//class for the password-change page
public class ChangePasswordActivity extends ErrorHandlingAppCompatActivity {

    //textview for current password
    private TextView currentPassword;
    //textview for new password
    private TextView newPassword;
    //textview for confirm password
    private TextView confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        currentPassword = findViewById(R.id.old_pass);
        newPassword = findViewById(R.id.new_pass);
        confirmPassword = findViewById(R.id.repeat_pass);

        MaterialButton changePasswordButton = findViewById(R.id.change_password_button);

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkRequirements()) {
                    changePassword();
                }
            }
        });
    }

    //function for changing the password
    private void changePassword() {
        String currentPasswordValue = currentPassword.getText().toString();
        String newPasswordValue = newPassword.getText().toString();
        String confirmPasswordValue = confirmPassword.getText().toString();

        if (!User.getSessionUser().getPassword().equals(currentPasswordValue)) {
            currentPassword.setError("Incorrect current password");
            return;
        }

        if (!newPasswordValue.equals(confirmPasswordValue)) {
            confirmPassword.setError("Passwords do not match");
            return;
        }

        if (newPasswordValue.equals(currentPasswordValue)) {
            newPassword.setError("New password should be different from the current password");
            return;
        }

        // At this point, the password change is valid, proceed with updating

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

                        // Update the password field in the user document
                        userDocRef.update("password", Encryption.hashPassword(newPasswordValue))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(ChangePasswordActivity.this, "Password changed successfully.", Toast.LENGTH_LONG).show();
                                        Log.d(TAG, "User password changed!");

                                        // Update the password in the currentUser object as well
                                        User.getSessionUser().setPassword(newPasswordValue);

                                        waitTwoSeconds();
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error updating user password", e);
                                    }
                                });
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "User not found.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
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

    //function for checking the requirements
    private boolean checkRequirements() {
        removeErrors();
        return checkCurrentPassword() && checkNewPassword() && checkConfirmPassword();
    }

    //function for checking the requirements of the current password
    private boolean checkCurrentPassword() {
        CharSequence password = currentPassword.getText();
        if (password.length() == 0) {
            currentPassword.setError("Current password is required");
            return false;
        }
        return true;
    }

    //function for checking the requirements of the new password
    private boolean checkNewPassword() {
        boolean hasUpperCase = false, hasNumber = false, hasSpecial = false;
        CharSequence password = newPassword.getText();
        if (password.length() < 5 || password.length() > 20) {
            newPassword.setError("New password should have between 5 and 20 characters");
            return false;
        }
        if (!isLetter(password.charAt(0))) {
            newPassword.setError("New password should start with a letter");
            return false;
        }
        for(int i = 0; i < password.length(); i++){
            if(!isLetter(password.charAt(i)) && !isNumber(password.charAt(i)) &&
                    !isUnderspaceOrPoint(password.charAt(i))){
                newPassword.setError("Password should only contain 'A-Z', 'a-z', '0-9', '.' or '_'");
                return false;
            }
            if(password.charAt(i) >= 65 && password.charAt(i) <= 90){
                hasUpperCase = true;
            } else if(isNumber(password.charAt(i))){
                hasNumber = true;
            } else if(isUnderspaceOrPoint(password.charAt(i))){
                hasSpecial = true;
            }
        }
        if(!(hasUpperCase && hasNumber && hasSpecial)){
            newPassword.setError("Password should contain at least a uppercase character, " +
                    "a number or a special character ('_', '.').");
            return false;
        }
        return true;
    }

    //function for checking the requirements of the confirmed password
    private boolean checkConfirmPassword() {
        CharSequence password = confirmPassword.getText();
        if (password.length() == 0) {
            confirmPassword.setError("Confirm password is required");
            return false;
        }
        return true;
    }

    //function that removes the errors
    private void removeErrors() {
        currentPassword.setError(null);
        newPassword.setError(null);
        confirmPassword.setError(null);
    }

    //function that checks if the character is a letter
    private boolean isLetter(char c) {
        return (c >= 65 && c <= 90) || (c >= 97 && c <= 122);
    }

    //function that checks if the character is a number
    private boolean isNumber(char c) {
        return c >= 48 && c <= 57;
    }

    //function that checks if the character is an underspace or point
    private boolean isUnderspaceOrPoint(char c) {
        return c == '.' || c == '_';
    }

    //function that checks if the password contains an uppercase character
    private boolean containsUppercase(String password) {
        for (int i = 0; i < password.length(); i++) {
            if (isLetter(password.charAt(i)) && Character.isUpperCase(password.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    //function that checks if the password contains a number character
    private boolean containsNumber(String password) {
        for (int i = 0; i < password.length(); i++) {
            if (isNumber(password.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    //function that checks if the password contains a special character
    private boolean containsSpecialCharacter(String password) {
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (!isLetter(c) && !isNumber(c) && !isUnderspaceOrPoint(c)) {
                return true;
            }
        }
        return false;
    }
}