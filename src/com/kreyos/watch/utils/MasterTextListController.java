package com.kreyos.watch.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.res.AssetManager;
import android.util.Log;

import com.kreyos.watch.KreyosActivity;

/* Singleton
 * 
 * To be sure that only one class can access the 
 * master text file list.
 * 
 * Methods
 * - Open File
 * - Get Title and Message
 * 
 */

public class MasterTextListController {
	
	private static MasterTextListController mInstance = null; 
	private KreyosActivity mActivity = null;
	final String mTextFile = "mastertextlist/mastertextlist.txt";
	
	
	protected MasterTextListController () {}
	
	public static MasterTextListController getInstance() {
		if (mInstance == null) {
			mInstance = new MasterTextListController();
		}
		return mInstance;
	}
	
	// Encapsulated Methods
	private void openFile() {
		AssetManager am;
	    am = mActivity.getAssets();
	    InputStream inputStream = null ;  
        try {  
            inputStream = am.open(mTextFile);  
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while((line = br.readLine()) != null) {
            	sb.append(line + "\n");
            }
        } catch (IOException e) {
        	e.printStackTrace();
        }  
	}

	// Interface Methods
	public String[] getMessage(KreyosActivity p_aActivity) {
		mActivity = p_aActivity;
		openFile();
		return null;
	}
	
	public void test() {
		
	}
}
