package test.pprivera.com.testapplicationtraccar3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

class SessionManager {

    static final String KEY_NAME = "name";
    static final String KEY_EMAIL = "email";
    static final String KEY_PASS = "pass";
    static final String KEY_CRED_BASE64 = "credBase64";
    static final String KEY_ID = "id";

    private static final String PREF_NAME = "SessionManager";
    private static final String IS_LOGIN = "IsLoggedIn";
    private static final int PRIVATE_MODE = 0;

    private SharedPreferences pref;
    private Context context;

    SessionManager(Context context) {
        this.context = context;
        pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
    }

    void createLoginSession(String name, String email, String pass, String credBase64, String id) {
        // Storing login value as TRUE, name in pref and email in pref
        pref.edit().putBoolean(IS_LOGIN, true)
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putString(KEY_PASS, pass)
                .putString(KEY_CRED_BASE64, credBase64)
                .putString(KEY_ID, id)
                .apply();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     **/
    void checkLoginStatus() {
        if(!this.isLoggedIn()){
            // User is not logged in, redirect him to Main Activity
            Intent intent = new Intent(context, MainActivity.class);

            // Closing all the Activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            context.startActivity(intent);
        }
    }

    HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<>();

        // User name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        // User email id
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        // User password
        user.put(KEY_PASS, pref.getString(KEY_PASS, null));

        // User credentials (Base64)
        user.put(KEY_CRED_BASE64, pref.getString(KEY_CRED_BASE64, null));

        // User ID
        user.put(KEY_ID, pref.getString(KEY_ID, null));

        return user;
    }

    /**
     * Clear session details
     **/
    void logoutUser(){
        // Clearing all data from Shared Preferences
        pref.edit().clear().apply();

        // After logout redirect user to Main Activity
        Intent intent = new Intent(context, MainActivity.class);
        // Closing all the Activities
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        context.startActivity(intent);
    }

    /**
     * Quick check for login
     **/
    boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
}
