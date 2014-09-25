package com.kreyos.kreyosandroid.utilities;

import org.json.JSONException;
import org.json.JSONObject;

import com.kreyos.kreyosandroid.managers.PreferencesManager;
import com.kreyos.kreyosandroid.utilities.Constants;
import android.content.SharedPreferences;
import android.util.Log;

public class Profile 
{
	public String EMAIL 		= "";
	public String FB_TOKEN 		= "";
	public String KREYOS_TOKEN  = "";
	public String FIRSTNAME 	= "";
	public String LASTNAME 		= "";
	public String BIRTHDAY 		= "";
	public String GENDER 		= "";
	public String WEIGHT 		= "";
	public String HEIGHT 		= "";
	
	public Profile()
	{

        EMAIL 			= "";
		FB_TOKEN 		= "";
		KREYOS_TOKEN  	= "";
		FIRSTNAME 		= "";
		LASTNAME 		= "";
		BIRTHDAY 		= "";
		GENDER 			= "";
		WEIGHT 			= "";
		HEIGHT 			= "";
	}
	
	public boolean isValid()
	{
		return true;
	}

    public void loadDataFromPreferences() {

        if (PreferencesManager.getInstance() != null) {
            EMAIL	 		= PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_USER_EMAIL );
            FB_TOKEN 		= PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_USER_FB_TOKEN );
            KREYOS_TOKEN 	= PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_USER_KREYOS_TOKEN );
            FIRSTNAME 		= PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_USER_FIRST_NAME );
            LASTNAME 		= PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_USER_LAST_NAME );
            BIRTHDAY 		= PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_USER_BIRTHDAY );
            GENDER 			= PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_USER_GENDER );
            WEIGHT 			= PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_USER_WEIGHT );
            HEIGHT 			= PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_USER_HEIGHT );

            Log.d("Profile", "Load Profile");
        }
    }

    public void saveDataToPreferences() {

        if (PreferencesManager.getInstance() == null) {
            return;
        }

        if (!EMAIL.equals("")) {
            PreferencesManager.getInstance().saveDataString(Constants.PREFKEY_USER_EMAIL, EMAIL); }

        if (!FB_TOKEN.equals("")) {
            PreferencesManager.getInstance().saveDataString(Constants.PREFKEY_USER_FB_TOKEN, FB_TOKEN); }

        if (!KREYOS_TOKEN.equals("")) {
            PreferencesManager.getInstance().saveDataString(Constants.PREFKEY_USER_KREYOS_TOKEN, KREYOS_TOKEN); }

        if (!FIRSTNAME.equals("")) {
            PreferencesManager.getInstance().saveDataString(Constants.PREFKEY_USER_FIRST_NAME, FIRSTNAME); }

        if (!LASTNAME.equals("")) {
            PreferencesManager.getInstance().saveDataString(Constants.PREFKEY_USER_LAST_NAME, LASTNAME); }

        if (!BIRTHDAY.equals("")) {
                PreferencesManager.getInstance().saveDataString(Constants.PREFKEY_USER_BIRTHDAY, BIRTHDAY); }

        if (!GENDER.equals("")) {
                PreferencesManager.getInstance().saveDataString(Constants.PREFKEY_USER_GENDER, GENDER); }

        if (!WEIGHT.equals("")) {
                PreferencesManager.getInstance().saveDataString(Constants.PREFKEY_USER_WEIGHT, WEIGHT); }

        if (!HEIGHT.equals("")) {
                PreferencesManager.getInstance().saveDataString(Constants.PREFKEY_USER_HEIGHT, HEIGHT); }

            Log.d("Profile", "Saving Profile");
    }



	/****************************************************************
	 * Save profile from json object
	 **/

    public boolean save(JSONObject p_json) {
        try {

            JSONObject user 	= p_json.getJSONObject("user");
            this.EMAIL			= user.getString("email");
            this.KREYOS_TOKEN 	= user.getString("auth_token");
            this.FIRSTNAME 		= user.getString("first_name");
            this.LASTNAME 		= user.getString("last_name");
            this.BIRTHDAY 		= user.getString("birthday");
            this.GENDER 		= user.getString("gender");

            saveDataToPreferences();
            PreferencesManager.getInstance().saveDataString( Constants.PREFKEY_USER_IMAGE, "" );
            return true;

        } catch(JSONException ex) {

            ex.printStackTrace();
            return false;
        }
    }
}
