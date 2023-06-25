package com.licenceproject.carpoolingapp.parsing;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Class that utilizes BCrypt for the encryption of passwords
 */
public class Encryption {

    //Function that hashes the password using BCrypt
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    //Function that verifies if the input password matches with the hashed
    public static boolean checkPassword(String candidate, String hashed) {
        return BCrypt.checkpw(candidate, hashed);
    }
}
