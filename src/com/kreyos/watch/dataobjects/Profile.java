package com.kreyos.watch.dataobjects;

import org.json.JSONException;
import org.json.JSONObject;

import com.kreyos.watch.KreyosActivity;
import com.kreyos.watch.KreyosPrefKeys;

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
		EMAIL 			= "Kreyos";
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
	
	public void loadfromPrefs()
	{
		SharedPreferences prefs = KreyosActivity.getPrefs();
		if( prefs != null )
		{
			// EMAIL	 		= prefs.getString(KreyosPrefKeys.USER_EMAIL, 		"");
			FB_TOKEN 		= prefs.getString(KreyosPrefKeys.USER_FB_TOKEN, 	"");
			KREYOS_TOKEN 	= prefs.getString(KreyosPrefKeys.USER_KREYOS_TOKEN, "");
			FIRSTNAME 		= prefs.getString(KreyosPrefKeys.USER_FIRST_NAME, 	"");
			LASTNAME 		= prefs.getString(KreyosPrefKeys.USER_LAST_NAME, 	"");
			BIRTHDAY 		= prefs.getString(KreyosPrefKeys.USER_BIRTHDAY, 	"");
			GENDER 			= prefs.getString(KreyosPrefKeys.USER_GENDER, 		"");
			WEIGHT 			= prefs.getString(KreyosPrefKeys.USER_WEIGHT, 		"");		
			HEIGHT 			= prefs.getString(KreyosPrefKeys.USER_HEIGHT, 		"");
			
			Log.d("Profile", "Load Profile");
		}
	}
	
	public void saveToPrefs()
	{
		SharedPreferences.Editor editor = KreyosActivity.getPrefs().edit();
		if( editor != null )
		{
			/*
			if(EMAIL != "")
			{
				editor.putString(KreyosPrefKeys.USER_EMAIL, 		EMAIL);
			}
			*/
			
			if(FB_TOKEN != "")
			{
				editor.putString(KreyosPrefKeys.USER_FB_TOKEN, 		FB_TOKEN);
			}
			if(KREYOS_TOKEN != "")
			{
				editor.putString(KreyosPrefKeys.USER_KREYOS_TOKEN, 	KREYOS_TOKEN);
			}
			if(FIRSTNAME != "")
			{
				editor.putString(KreyosPrefKeys.USER_FIRST_NAME, 	FIRSTNAME);
			}
			if(LASTNAME != "")
			{
				editor.putString(KreyosPrefKeys.USER_LAST_NAME, 	LASTNAME);
			}
			if(BIRTHDAY != "")
			{
				editor.putString(KreyosPrefKeys.USER_BIRTHDAY, 		BIRTHDAY);
			}
			if(GENDER != "")
			{
				editor.putString(KreyosPrefKeys.USER_GENDER, 		GENDER);
			}
			if(WEIGHT != "")
			{
				editor.putString(KreyosPrefKeys.USER_WEIGHT, 		WEIGHT);
			}
			if(HEIGHT != "")
			{
				editor.putString(KreyosPrefKeys.USER_HEIGHT, 		HEIGHT);
			}
			
			editor.commit();
			
			Log.d("Profile", "Saving Profile");
		}
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
			
			saveToPrefs();
			SharedPreferences.Editor editor = KreyosActivity.getPrefs().edit();
	        editor.putString(KreyosPrefKeys.USER_IMAGE, "");
	        editor.commit();
	        return true;
		} catch(JSONException ex) {
			ex.printStackTrace();
			return false;
		}
	}
}
