package com.kreyos.kreyosandroid.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.utilities.Constants;


public class PreferencesManager {

    //----------------------------------------------------------------------------------------------> Variables
    private static PreferencesManager   mInstance       = null;
    private SharedPreferences           mPreferences    = null;


    //----------------------------------------------------------------------------------------------> Initialize singleton
    // initial call
    public static void initializePrefInstance(Context pContext) {
        if (mInstance == null) {
            mInstance = new PreferencesManager(pContext);
        }
    }

    // initialize xml file
    private PreferencesManager(Context pContext) {
        String filename = pContext.getResources().getString(R.string.shared_preferences);       // "shared_preferences"
        mPreferences = pContext.getSharedPreferences(filename, Context.MODE_PRIVATE);
    }


    //----------------------------------------------------------------------------------------------> Get instance
    public static PreferencesManager getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException(PreferencesManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return mInstance;
    }


    //----------------------------------------------------------------------------------------------> Save/Write data
    public void saveDataString(String pKey, String pValue) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(pKey, pValue);
        editor.commit();

        Log.d(Constants.TAG_DEBUG, "( MANAGER:preferences ) - saved!    (\"" + pKey + "\" = " + pValue + ")");
    }

    public void saveDataBoolean(String pKey, boolean pValue) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(pKey, pValue);
        editor.commit();

        Log.d(Constants.TAG_DEBUG, "( MANAGER:preferences ) - saved!    (\"" + pKey + "\" = boolean " + pValue + " )");
    }

    public void saveDataInt(String pKey, int pValue) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt( pKey, pValue );
        editor.commit();

        Log.d(Constants.TAG_DEBUG, "( MANAGER:preferences ) - saved!    (\"" + pKey + "\" = int " + pValue + " )");
    }

    public void saveDataLong(String pKey, long pValue) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong( pKey, pValue );
        editor.commit();

        Log.d(Constants.TAG_DEBUG, "( MANAGER:preferences ) - saved!    (\"" + pKey + "\" = long " + pValue + " )");
    }


    //----------------------------------------------------------------------------------------------> Get data
    public boolean containsKey(String pKey) {
        return mPreferences.contains(pKey);
    }

    public String retrieveDataForString(String pKey) {
        return mPreferences.getString(pKey, "NO VALUE");
    }

    public boolean retrieveDataForBoolean(String pKey, boolean pDefValue) {
        return mPreferences.getBoolean(pKey, pDefValue);
    }

    public int retrieveDataForInt(String pKey, int pDefValue) {
        return mPreferences.getInt( pKey, pDefValue );
    }

    public long retrieveDataForLong(String pKey, long pDefValue) {
        return mPreferences.getLong( pKey, pDefValue );
    }


    //----------------------------------------------------------------------------------------------> Remove specific values
    public void deleteDataForKey(String pKey) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(pKey);
        editor.commit();
    }


    //----------------------------------------------------------------------------------------------> Remove ALL
    public void deleteAllData() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.clear();
        editor.commit();
    }
}