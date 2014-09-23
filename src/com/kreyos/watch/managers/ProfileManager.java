package com.kreyos.watch.managers;

import org.json.JSONObject;

import android.content.SharedPreferences;

import com.kreyos.watch.KreyosActivity;
import com.kreyos.watch.KreyosPrefKeys;
import com.kreyos.watch.dataobjects.Profile;

public class ProfileManager
{
	private static ProfileManager instance = null;
	
	protected ProfileManager() 
	{
      // Exists only to defeat instantiation.
	}
	
	public static ProfileManager instance()
	{
		if(instance == null) 
		{
			instance = new ProfileManager();
		}
		return instance;
	}
	
	public void saveProfile( Profile p_profile )
	{
		SharedPreferences.Editor editor = KreyosActivity.getPrefs().edit();
		editor.putString(KreyosPrefKeys.USER_FIRST_NAME, 	p_profile.FIRSTNAME);
		editor.putString(KreyosPrefKeys.USER_LAST_NAME, 	p_profile.LASTNAME);
		editor.putString(KreyosPrefKeys.USER_BIRTHDAY, 		p_profile.BIRTHDAY);
		editor.putString(KreyosPrefKeys.USER_GENDER, 		p_profile.GENDER);	
		editor.putString(KreyosPrefKeys.USER_WEIGHT, 		p_profile.HEIGHT);
		editor.putString(KreyosPrefKeys.USER_HEIGHT, 		p_profile.WEIGHT);
		editor.commit();
	}
	
}
