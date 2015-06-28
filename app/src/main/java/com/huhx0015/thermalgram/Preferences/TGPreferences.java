package com.huhx0015.thermalgram.Preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.huhx0015.flirhotornot.R;

/** ------------------------------------------------------------------------------------------------
 *  [TGPreferences] CLASS
 *  PROGRAMMER: Michael Yoon Huh (HUHX0015)
 *  DESCRIPTION: This class is a class that contains functionality that pertains to the use and
 *  manipulation of shared preferences data.
 *  ------------------------------------------------------------------------------------------------
 */
public class TGPreferences {

    /** SHARED PREFERENCES FUNCTIONALITY _______________________________________________________ **/

    // initializePreferences(): Initializes and returns the SharedPreferences object.
    public static SharedPreferences initializePreferences(String prefType, Context context) {
        return context.getSharedPreferences(prefType, Context.MODE_PRIVATE);
    }

    // setDefaultPreferences(): Sets the shared preference values to default values.
    public static void setDefaultPreferences(String prefType, Boolean isReset, Context context) {

        // Determines the appropriate resource file to use.
        int prefResource = R.xml.tg_options;

        // Resets the preference values to default values.
        if (isReset) {
            SharedPreferences preferences = initializePreferences(prefType, context);
            preferences.edit().clear().apply();
        }

        // Sets the default values for the SharedPreferences object.
        PreferenceManager.setDefaultValues(context, prefType, Context.MODE_PRIVATE, prefResource, true);
    }

    /** GET PREFERENCES FUNCTIONALITY __________________________________________________________ **/

    // getCurrentImage(): Retrieves the current image value from preferences.
    public static String getCurrentImage(SharedPreferences preferences) {
        return preferences.getString("tg_current_image", ""); // Retrieves the current image setting.
    }

    /** SET PREFERENCES FUNCTIONALITY __________________________________________________________ **/

    // setCurrentImage(): Sets the "tg_current_image" value to preferences.
    public static void setCurrentImage(String fileName, SharedPreferences preferences) {

        // Prepares the SharedPreferences object for editing.
        SharedPreferences.Editor prefEdit = preferences.edit();

        prefEdit.putString("tg_current_image", fileName); // Sets the current image setting.
        prefEdit.apply(); // Applies the changes to SharedPreferences.
    }
}
