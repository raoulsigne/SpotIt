package techafrkix.work.com.spot.techafrkix.work.com.spot.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

import techafrkix.work.com.spot.spotit.Accueil;
import techafrkix.work.com.spot.spotit.Connexion;

/**
 * Created by techafrkix0 on 19/05/2016.
 */
public class SessionManager {

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "AndroidHivePref";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "pseudo";

    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";

    // <user Id (make variable public to access from outside)
    public static final String KEY_ID = "id";

    // <user Id (make variable public to access from outside)
    public static final String KEY_OFFSET = "offset";

    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(String name, String email, int id){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_NAME, name);

        // Storing email in pref
        editor.putString(KEY_EMAIL, email);

        //storing id in pref
        editor.putString(KEY_ID, String.valueOf(id));

        //storing offset in pref
        editor.putString(KEY_OFFSET, String.valueOf(0));

        // commit changes
        editor.commit();
    }

    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        // user email id
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        // user id
        user.put(KEY_ID, pref.getString(KEY_ID, null));

        // user offset to download spot
        user.put(KEY_OFFSET, pref.getString(KEY_OFFSET, null));

        // return user
        return user;
    }

    /**
     * set the value of the offset inside preferences
     * @param offset value to be set represent the value to use in request to get spots
     */
    public void putOffset(int offset){
        //storing offset in pref
        editor.putString(KEY_OFFSET, String.valueOf(offset));

        // commit changes
        editor.commit();
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        // Intent i = new Intent(_context, Accueil.class);
        // Closing all the Activities
        // i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        // i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        // _context.startActivity(i);
    }
}
