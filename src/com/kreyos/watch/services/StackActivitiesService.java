package com.kreyos.watch.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kreyos.watch.db.DatabaseManager;
import com.kreyos.watch.managers.AppHelper;
import com.kreyos.watch.utils.RequestManager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

public class StackActivitiesService extends Service {

	
	Timer mStackUpdateTimer = null;
	TimerTask mStackUpdateTask = null;
	
	final int MILLI = 1000;
	final int mUpdateDelay = 5;
	
	final boolean mIsLogEnable = true;
	
	ArrayList<HashMap<String, JSONObject>> mStacks;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		LogMessage("Created");
		storedStackActivities();
	}
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		LogMessage("Destroyed");
	}
	
	
	private void storedStackActivities() {
		
		mStacks = new ArrayList<HashMap<String,JSONObject>>();
		
		Cursor queriedData = DatabaseManager.instance((Context) this).queryData("SELECT * FROM Stack_Activities");
		if(queriedData != null && queriedData.moveToFirst()) {
			
			// Set key index
			final int EPOCH = 0;
			final int REQUEST_KEY = 1;
			final int JSONDATA = 2;
			
			mStacks.clear();
			
			try {
				
				do {
					
					String requestKey = queriedData.getString(1);
					JSONObject jsonActivity = new JSONObject(queriedData.getString(2));
					HashMap<String, JSONObject> dictionary = new HashMap<String, JSONObject>();
					dictionary.put(requestKey, jsonActivity);
					mStacks.add(dictionary);
					
					LogMessage("JsonData:" + jsonActivity.toString());
					
				} while(queriedData.moveToNext());
				
			} catch (JSONException e) {
				
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Start sending request
			startRequest();
			
		} else {
			
			LogMessage("No Available Stack Activities to be send");
			
		}
	}
	
	
	public void startRequest() {
		
		// Create instance of timer
		mStackUpdateTimer = new Timer();
		// Setup the task 
		mStackUpdateTask = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				// Checking of there activity that is not saved on the web
				checkStack();
			}
		};
		
		// Start update
		mStackUpdateTimer.schedule(mStackUpdateTask, 0, mUpdateDelay * MILLI);
	}
	
	
	private void checkStack() {
		
		LogMessage("Check Stack:" + mStacks.size());
		LogMessage("Internet Connected = " + isInternetConnected());
		
		// Check if connected to internet
		if(isInternetConnected()) {
			try {
				
				if(mStacks.size() > 0) {
					sendRequest(mStacks.get(0));
				}
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
	

	private void sendRequest(HashMap<String, JSONObject> p_map) {
		
		LogMessage("Send Request");
		
		try {
			
			String request = "";
			
			for ( String key : p_map.keySet() ) {
				request = key;
				break;
			}
			
			LogMessage("Key:" + request);
			
			JSONObject jsonValue = new JSONObject(p_map.get(request).toString()) ;
			
			LogMessage("jsonValue:" + jsonValue);
			
			
			String response = RequestManager.instance().post(request, jsonValue);
			
			JSONObject jsonResponse;
	        jsonResponse = new JSONObject(response);
			
	        if(jsonResponse.has("success")) {
	        	mStacks.remove(0);
	        	SQLiteDatabase db = DatabaseManager.instance((Context) this).getWritableDatabase();
	        	String epoch = jsonValue.getString("time").toString();
	        	db.delete("Stack_Activities", "Epoch = ?", new String[] { epoch });
	        	db.close();
	        	
	        	LogMessage("Request Success");
	        }
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private boolean isInternetConnected() {
        ConnectivityManager connectivityManager =
            ( ConnectivityManager ) this.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private void LogMessage(String p_message) {
		if(mIsLogEnable) {
			Log.d("StackRequest", p_message);
		}	
	}
	

}
