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
    public static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "pseudo";

    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";

    // <user Id (make variable public to access from outside)
    public static final String KEY_ID = "id";

    // user number spot
    public static final String KEY_SPOT = "nbspot";

    // usern number respot
    public static final String KEY_RESPOT = "nbrespot";

    // user number of friends
    public static final String KEY_FRIENDS = "nbfriends";

    // user photo profile
    public static final String KEY_PHOTO = "photo";

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

        //storing spot number in pref
        editor.putString(KEY_SPOT, String.valueOf(0));

        //storing respot number in pref
        editor.putString(KEY_RESPOT, String.valueOf(0));

        //storing friend's number in pref
        editor.putString(KEY_FRIENDS, String.valueOf(0));

        //storing user profile
        editor.putString(KEY_PHOTO, "");

        // commit changes
        editor.commit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(String name, String email, int id, int spot, int respot, int friends, String photo){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_NAME, name);

        // Storing email in pref
        editor.putString(KEY_EMAIL, email);

        //storing id in pref
        editor.putString(KEY_ID, String.valueOf(id));

        //storing spot number in pref
        editor.putString(KEY_SPOT, String.valueOf(spot));

        //storing respot number in pref
        editor.putString(KEY_RESPOT, String.valueOf(respot));

        //storing friend's number in pref
        editor.putString(KEY_FRIENDS, String.valueOf(friends));

        //storing user profile
        editor.putString(KEY_PHOTO, photo);

        //storing offset in pref
        editor.putString(KEY_OFFSET, String.valueOf(0));

        // commit changes
        editor.commit();
    }

    /**
     * storing photo profile
     * @param photo represent the user's photo
     */
    public void store_photo_profile(String photo){
        //storing user profile
        editor.putString(KEY_PHOTO, photo);

        // commit changes
        editor.commit();
    }

    /**
     * storing friend number
     * @param number
     */
    public void store_friend_number(int number){
        //storing friend's number in pref
        editor.putString(KEY_FRIENDS, String.valueOf(number));

        // commit changes
        editor.commit();
    }

    /**
     * fonction qui incrémente de 1 le nombre de spots
     */
    public void increment_nbspot(){
        int nb = Integer.valueOf(pref.getString(KEY_SPOT, null)) + 1;
        editor.putString(KEY_SPOT, String.valueOf(nb));

        // commit changes
        editor.commit();
    }

    /**
     * fonction qui incrémente de 1 le nombre de respots
     */
    public void increment_nbrespot(){
        int nb = Integer.valueOf(pref.getString(KEY_RESPOT, null)) + 1;
        editor.putString(KEY_RESPOT, String.valueOf(nb));

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

        // user email
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        // user id
        user.put(KEY_ID, pref.getString(KEY_ID, null));

        // user spot
        user.put(KEY_SPOT, pref.getString(KEY_SPOT, null));

        // user respot
        user.put(KEY_RESPOT, pref.getString(KEY_RESPOT, null));

        // user friends
        user.put(KEY_FRIENDS, pref.getString(KEY_FRIENDS, null));

        // user photo profile
        user.put(KEY_PHOTO, pref.getString(KEY_PHOTO, null));

        // user offset to download spot
        user.put(KEY_OFFSET, pref.getString(KEY_OFFSET, null));

        // return user
        return user;
    }

    /**
     * test if an user is connected
     * @return true or false depending of the status of user
     */
    public boolean isLogin(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }
}
