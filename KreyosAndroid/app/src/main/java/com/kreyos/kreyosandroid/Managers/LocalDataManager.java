package com.kreyos.kreyosandroid.managers;

import android.app.Activity;
import android.util.Log;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.activities.MainActivity;
import com.kreyos.kreyosandroid.activities.SetupWatchActivity;
import com.kreyos.kreyosandroid.utilities.Constants;
import com.kreyos.kreyosandroid.utilities.KreyosUtility;

import java.io.File;
import java.io.IOException;


public class LocalDataManager {

//--------------------------------------------------------------------------------------------------- Variables
    private static LocalDataManager     mInstance       = null;
    private Activity                    mActivity       = null;
    private DialogManager               mDialog         = null;

    private static String               FILE_NAME       = "KreyosLocalUserData";


//--------------------------------------------------------------------------------------------------- Constructor
    protected LocalDataManager() {

    }

    public static LocalDataManager getInstance() {
        if ( mInstance == null ) {
            mInstance = new LocalDataManager();
        }
        return mInstance;
    }


//--------------------------------------------------------------------------------------------------- Init & Reset
    public void init(Activity pActivity) {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:LocalData ) - Init");

        mActivity   = pActivity;
        mDialog     = new DialogManager(mActivity);
    }

    public void resetSetup() {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:LocalData ) - RESET SETUP");

        mActivity   = null;
        mDialog     = null;
    }


//--------------------------------------------------------------------------------------------------- Check File
    private File getFile() {
        return new File(mActivity.getExternalFilesDir(null) + "/" + FILE_NAME + ".txt");
    }

    public boolean doesFileExists() {

        File file = getFile();

        if ( file.exists() ) {
            return true;

        } else {
            try {
                file.createNewFile();
                return true;

            } catch (IOException e) {
                Log.d(Constants.TAG_DEBUG, "( MANAGER:LocalData ) - ERROR: Can't create local file");
                e.printStackTrace();
                return false;
            }
        }
    }

    private void createFile() {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:LocalData ) - Create File");

        if ( !doesFileExists() ) {
            KreyosUtility.showErrorMessage( mActivity,
                                            mActivity.getString(R.string.dialog_title_error),
                                            mActivity.getString(R.string.dialog_msg_error_file_create));
        }
    }

//--------------------------------------------------------------------------------------------------- Read & Write File
}

