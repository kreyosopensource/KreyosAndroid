package com.kreyos.watch.managers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.DropBoxManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.internal.ez;
import com.kreyos.watch.ActiveWatchActivity;
import com.kreyos.watch.ActivityStatsActivity;
import com.kreyos.watch.DailyTargetActivity;
import com.kreyos.watch.DateTimeActivity;
import com.kreyos.watch.HomeActivity;
import com.kreyos.watch.KreyosActivity;
import com.kreyos.watch.KreyosPrefKeys;
import com.kreyos.watch.KreyosUtility;
import com.kreyos.watch.LoginActivity;
import com.kreyos.watch.NotificationsActivity;
import com.kreyos.watch.PersonalInformationActivity;
import com.kreyos.watch.R;
import com.kreyos.watch.SelfTestActivity;
import com.kreyos.watch.SportsActivity;
import com.kreyos.watch.TutorialActivity;
import com.kreyos.watch.UpdateFirmwareActivity;
import com.kreyos.watch.WatchAlarmKreyosActivityGroup;
import com.kreyos.watch.adapter.ActivityStatsAdapter;
import com.kreyos.watch.bluetooth.BluetoothAgent;
import com.kreyos.watch.dataobjects.ActivityData;
import com.kreyos.watch.dataobjects.Profile;
import com.kreyos.watch.db.DBManager;
import com.kreyos.watch.db.DatabaseManager;
import com.kreyos.watch.objectdata.ActivityDataDoc;
import com.kreyos.watch.objectdata.ActivityDataRow;
import com.kreyos.watch.utils.RequestManager;
import com.kreyos.watch.utils.Utils;

public class AppHelper
{
	public static final int HOME_ACTIVITY			= 0;
	public static final int ACTIVITY_STATS			= 1;
	public static final int SPORTS_ACTIVITY			= 2;
	private static AppHelper instance = null;
	
	protected AppHelper() 
	{
      // Exists only to defeat instantiation.
	}
	
	public static AppHelper instance()
	{
		if(instance == null) 
		{
			instance = new AppHelper();
		}
		return instance;
	}
	
	public boolean IS_TUTORIAL_MODE = false;
	
	public enum WATCH_STATE_VALUE
	{
		WAITING,
		DISCONNECTED,
		CONNECTED,
	}
	
	public WATCH_STATE_VALUE WATCH_STATE = WATCH_STATE_VALUE.DISCONNECTED;
	
	public enum ACTIVITY_METRIC {
		INVALID,
		DAILY,
		WEEKLY,
		MONTHLY
	}
	
	public void onSwitchActivity(boolean p_isLeftMenu, KreyosActivity p_activity, int p_index) {
		Class<?> targetClass = null;
		// Stop all the thread running related to db
		DBManager.stopAllTasks();
	
		switch (p_index) {
		case R.id.item_one:
			targetClass = p_isLeftMenu ? HomeActivity.class : ActiveWatchActivity.class;
			break;
		case R.id.item_two:
			targetClass = p_isLeftMenu ? ActivityStatsActivity.class : DateTimeActivity.class;
			break;
		case R.id.item_three:
			targetClass = p_isLeftMenu ? SportsActivity.class : WatchAlarmKreyosActivityGroup.class;
			break;
		case R.id.item_four:
			targetClass = p_isLeftMenu ? DailyTargetActivity.class : UpdateFirmwareActivity.class;
			break;
		case R.id.item_five:
			targetClass = p_isLeftMenu ? NotificationsActivity.class : PersonalInformationActivity.class;
			break;
		case R.id.item_six:
			targetClass = TutorialActivity.class;
		default:
			break;
		}
		
		if (targetClass == null 
		|| targetClass == p_activity.getClass()) {
			Log.d("Log", "Same Class");
			return;	
		}
		
		// Loading bar
		// Creating an error :)
		// p_activity.m_progressDialog = ProgressDialog.show(p_activity, "Please wait", "Switching activity", true);
		p_activity.finish();
		Intent intent = new Intent(p_activity, targetClass);
		p_activity.startActivity(intent);
		
		/*
		if (p_index == R.id.item_seven) {
			if (!Utils.hasConnection(p_activity)) {
				KreyosUtility.showErrorMessage(p_activity, "ERROR:", "No Internet connection");
				return;
			}
			logout(p_activity);
		} else {
			switch (p_index) {
			case R.id.item_one:
				targetClass = p_isLeftMenu ? HomeActivity.class : ActiveWatchActivity.class;
				break;
			case R.id.item_two:
				targetClass = p_isLeftMenu ? ActivityStatsActivity.class : DateTimeActivity.class;
				break;
			case R.id.item_three:
				targetClass = p_isLeftMenu ? SportsActivity.class : WatchAlarmKreyosActivityGroup.class;
				break;
			case R.id.item_four:
				targetClass = p_isLeftMenu ? DailyTargetActivity.class : UpdateFirmwareActivity.class;
				break;
			case R.id.item_five:
				targetClass = p_isLeftMenu ? NotificationsActivity.class : PersonalInformationActivity.class;
				break;
			case R.id.item_six:
				targetClass = TutorialActivity.class;
			default:
				break;
			}
			
			if (targetClass == null 
			|| targetClass == p_activity.getClass()) {
				Log.d("Log", "Same Class");
				return;	
			}
			
			// Loading bar
			// Creating an error :)
			// p_activity.m_progressDialog = ProgressDialog.show(p_activity, "Please wait", "Switching activity", true);
			p_activity.finish();
			Intent intent = new Intent(p_activity, targetClass);
			p_activity.startActivity(intent);
			
		}
		*/
	}
	
	public void logout(KreyosActivity p_activity)
	{
		
		// + ET 05072014 : Disconnect on watch
		disconnectWatch(p_activity);
		
		// + ET 05072014 : Logging out of facebook
		if(com.facebook.Session.getActiveSession() != null) {
			com.facebook.Session.getActiveSession().closeAndClearTokenInformation();
		}
		
		// + ET 05072014 : Logging out of session
		if(true) {
			Profile profile = new Profile();
			profile.loadfromPrefs();
			
			/* Old implementation
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("email", profile.EMAIL));
			params.add(new BasicNameValuePair("auth_token", profile.KREYOS_TOKEN));
			*/
			try
			{
				JSONObject params = new JSONObject();
				params.put("email", profile.EMAIL);
				params.put("auth_token", profile.KREYOS_TOKEN);
				
				String response = RequestManager.instance().post(KreyosPrefKeys.URL_DELETE_SESSION, params);
				Log.d("Response", "" + response);
			
				// + ET 04292014 : Check if your logged in
				JSONObject jsonObject = new JSONObject(response.toString());
				if( jsonObject.getInt("status") == 200 )
				{
					Log.d("Response", "User Logout");
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
		// Delete all settings and informations
		SharedPreferences.Editor editor = p_activity.getPrefs().edit();
		if(editor != null) {
			editor.clear();
			editor.commit();
		}
		
		final KreyosActivity logoutActivity = p_activity;
		AlertDialog.Builder builder = new AlertDialog.Builder(p_activity);
    	builder.setTitle("Logout");
    	builder.setMessage("You've successfully logged out");
    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            	logoutActivity.finish();
            	Intent intent = new Intent(logoutActivity, LoginActivity.class);
        		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        		logoutActivity.startActivity(intent);
        		
            }
        });		
		// + ET 040714 : Create and show dialog
    	AlertDialog dialog = builder.create();
    	dialog.show();
	}
	
	public void sessionError(KreyosActivity p_activity) {
		disconnectWatch(p_activity);
		Intent intent = new Intent(p_activity, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		p_activity.startActivity(intent);
		p_activity.finish();
	}
	
	
	public void saveActivityStats(ActivityDataDoc p_dataDoc, KreyosActivity p_activity)
	{
		for(ActivityDataRow row : p_dataDoc.data)
		{
			// + ET 05062014 : Convert time and date to milliseconds
			Calendar ca = Utils.calendar();
			ca.set(Calendar.YEAR, p_dataDoc.year + 2000);
			ca.set(Calendar.MONTH, p_dataDoc.month - 1);
			ca.set(Calendar.DAY_OF_MONTH, p_dataDoc.day);	
			ca.set(Calendar.HOUR_OF_DAY, row.hour);
			ca.set(Calendar.MINUTE, row.minute);
			ca.set(Calendar.SECOND, row.mode == 0 ? 0 : 1);
			
			String epochTime = Long.toString(Utils.epoch(ca));
			double steps 	= 0;
			double distance = 0;
			double calories = 0;
			double heart	= 0;
			int rowMode		= row.mode;
			
			if(row.data.get(ActivityDataRow.DataType.DATA_COL_STEP) != null) {
				steps = row.data.get(ActivityDataRow.DataType.DATA_COL_STEP);
			}
			if(row.data.get(ActivityDataRow.DataType.DATA_COL_DIST) != null) {
				distance = row.data.get(ActivityDataRow.DataType.DATA_COL_DIST);
			}
			if(row.data.get(ActivityDataRow.DataType.DATA_COL_CALS) != null) {
				calories = row.data.get(ActivityDataRow.DataType.DATA_COL_CALS);
			}
			if(row.data.get(ActivityDataRow.DataType.DATA_COL_HR) != null) {
				heart = row.data.get(ActivityDataRow.DataType.DATA_COL_HR);
			}
			
			JSONObject params = new JSONObject();
			
			try {
				
				// Construct database values
				String tableName		 		= "Kreyos_User_Activities";
				ContentValues values 			= new ContentValues();
				//values.put( "ActivitySpeed", 0.0f );
				values.put( "CreatedTime", epochTime );
				values.put( "Sport_ID", row.mode );
				values.put( "ActivityDistance", distance );
				values.put( "ActivityCalories", calories );
				values.put( "ActivitySteps", steps );
				
				
				if(DatabaseManager.instance( (Context)p_activity ).insert( tableName, values )) {
					Log.d("DatabaseManager", "Saved on local db");
					
					/*
					boolean isActivityWillbeSavedonStack = true;
					
					// Check if there's internet to send request to web
					if( Utils.hasConnection( p_activity ) ) {

						params.put("auth_token",p_activity.getPrefs().getString(KreyosPrefKeys.USER_KREYOS_TOKEN, ""));
						params.put("email", 	p_activity.getPrefs().getString(KreyosPrefKeys.USER_EMAIL, ""));
						params.put("time", 		epochTime );
						params.put("sport_id", 	rowMode);
						params.put("steps", 	steps);
						params.put("distance", 	distance);
						params.put("calories", 	calories);
						params.put("heart", 	heart);
						
						// Process request
						String response = RequestManager.instance().post(KreyosPrefKeys.URL_USER_ACTIVITIES, params);
						
						try {

							JSONObject jsonResponse = new JSONObject(response);
							
							// Check if request is successful else move to Stack table 
							if(jsonResponse.has("success")) {
								
								// Flag that the activity is already send to web
								isActivityWillbeSavedonStack = false;
								Log.d("DatabaseManager", "Activity sent on web");
							} 
							
						} catch(Exception ex) {
							
							ex.printStackTrace(); 
						}
						
					} 
					
					// Save the activity on stack table to be send later
					if(isActivityWillbeSavedonStack) {
						
						tableName	= "Stack_Activities";
						values 		= new ContentValues();
						values.put( "Epoch", epochTime);
						values.put( "RequestKey", KreyosPrefKeys.URL_USER_ACTIVITIES );
						values.put( "JSONData", params.toString() );
						
						// Saving on database
						if(DatabaseManager.instance( (Context)p_activity ).insert( tableName, values )) {
							Log.d("DatabaseManager", "Save activity on stack table");
						} else {
							Log.e("DatabaseManager", "Saving Error:");
						}
					}
					
					*/
					
				} else {
					
					Log.d("DatabaseManager", "Already saved");
				}
				
			} catch ( Exception ex ) {
				
				Log.d("DatabaseManager", "" + ex);
			}
		}
	}
	
	
	public void clearConnectedWatch(KreyosActivity p_activity) {
		
		Log.d("Bluetooth", "Clearing Paired Connection");
		
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> bondDevices = adapter.getBondedDevices();
		ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
		
		for (BluetoothDevice device : bondDevices) {
			if (device.getName().startsWith("Meteor") || device.getName().startsWith("Kreyos")) {
				p_activity.unpairDevice(device);
			} 
		}
	}
	
	public void disconnectWatch(KreyosActivity p_activity) {
		
		// Clear all connected kreyos watch
		clearConnectedWatch(p_activity);
		
		if (AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.CONNECTED) {
			
			String deviceName = p_activity.getPrefs().getString("bluetooth.device_name", "");
			if( p_activity.m_selectedDevice == null ) {
				
				p_activity.m_selectedDevice = null;
				Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
				ArrayList<BluetoothDevice> bondedWatches = new ArrayList<BluetoothDevice>();
				for (BluetoothDevice i : bondedDevices) {
					if (i.getName().startsWith("Meteor") || i.getName().startsWith("Kreyos")){
						bondedWatches.add(i);
					}
				}
				for (BluetoothDevice device : bondedWatches) {
					if (device.getName().equals(deviceName)) {
						p_activity.m_selectedDevice = device;
						break;
					}			
				}
			}
			
			if (p_activity.m_selectedDevice != null) {
				p_activity.unpairDevice(p_activity.m_selectedDevice);
				BluetoothAgent.getInstance(null).forceStopSession();
			}
		}
		
		AppHelper.instance().WATCH_STATE = WATCH_STATE_VALUE.DISCONNECTED;
		SharedPreferences.Editor editor = p_activity.getPrefs().edit();
		editor.remove("bluetooth.device_name");
		editor.remove(KreyosPrefKeys.USER_WATCHED_STATE);
		editor.commit();
	}
	
	
	public ActivityData displayActivityData(ActivityDataDoc p_data, KreyosActivity p_activity)
	{
		return this.displayActivityData( p_data, p_activity, HOME_ACTIVITY );
	}
	
	
	/** Note
	 * 
	 * @param p_actScreen
	 * 
	 * The value of p_actScreen is should always be either of the following.
	 * 
	 * @HOME_ACTIVITY			
	 * @ACTIVITY_STATS			
	 * @SPORTS_ACTIVITY			
	 * 
	 **/
	public ActivityData displayActivityData(ActivityDataDoc p_data, KreyosActivity p_activity, int p_actScreen )
	{
		Log.i( "AppHelper::displayActivityData", "Display Activity Stats" );
		
		int steps 			= 0;
		double distance 	= 0;
		double calories 	= 0;
		double heartRate 	= 0;
		
		Log.i( "AppHelper::displayActivityData", "WATCH date year:" + p_data.year + " month:" + p_data.month + " day:" + p_data.day + "" );
		
		Calendar ca 		= Utils.calendar();
		ca.set(Calendar.YEAR, p_data.year + 2000);
		ca.set(Calendar.MONTH, p_data.month - 1);
		ca.set(Calendar.DAY_OF_MONTH, p_data.day);	
		
		String dateString =  Utils.dateString( ca );
		Log.i( "Bluetooth", "BLE_Fetch.. KreyosA dateString:" + dateString + "" );
		
		JSONArray jArray = new JSONArray();
		
		for (ActivityDataRow row: p_data.data) 
		{
			SparseArray<Double> rowData = row.data;
			
			try
			{
				steps += rowData.get( ActivityDataRow.DataType.DATA_COL_STEP );
			}
			catch(Exception e)
			{
				steps = 0;
			}
			
			try
			{
				distance += rowData.get(ActivityDataRow.DataType.DATA_COL_DIST);	
			}
			catch(Exception e)
			{
				distance = 0;
			}
			
			try
			{
				heartRate += rowData.get(ActivityDataRow.DataType.DATA_COL_HR);
			}
			catch(Exception e)
			{
				heartRate = 0;
			}				
			
			try
			{
				calories += rowData.get(ActivityDataRow.DataType.DATA_COL_CALS);
			}
			catch(Exception e)
			{
				calories = 0;
			}
			
			// recompute epoch here
			ca.set( Calendar.HOUR_OF_DAY, row.hour );
			ca.set( Calendar.MINUTE, row.minute );
			long epochTime = Utils.epoch( ca ); // Time in sec since 01/01/1970
			
			row.epoch = epochTime;
			row.dateString = dateString;
			
			Log.i( "Bluetooth", "BLE_Fetch.. KreyosA dateString:" + dateString + " epoch:" + epochTime + "" );
			
        }  
		
		//Log.v("steps", String.format("steps is %f", steps));
		Log.v("Distance", String.format("distance is %f", distance));
		Log.v("Heart Rate", String.format("Heart Rate is %f", heartRate));
		Log.v("calories", String.format("calories is %f", calories));
		
		ArrayList<ActivityDataRow> arrayHolder = new ArrayList<ActivityDataRow>();
		
		int periodStartHour = 0; 
		int periodStartMin = 0;
		int periodMode = 0;
		
		double periodNormalAndSports = 0;
		
		double periodSteps = 0;
		double periodDistance = 0;
		
		double periodCalories = 0;
		double periodHR = 0;
		double periodCadence = 0;
		
		for ( ActivityDataRow row: p_data.data )  {
			
			periodMode = row.mode;
			
			if ( periodMode == 0 ) {
				
				periodSteps 		+= row.data.get(ActivityDataRow.DataType.DATA_COL_STEP, 0.0);
				periodDistance 		+= row.data.get(ActivityDataRow.DataType.DATA_COL_DIST, 0.0);
				periodCalories 		+= row.data.get(ActivityDataRow.DataType.DATA_COL_CALS, 0.0);
				periodHR 			+= row.data.get(ActivityDataRow.DataType.DATA_COL_HR, 0.0);
				periodCadence 		+= row.data.get(ActivityDataRow.DataType.DATA_COL_CADN, 0.0);
				
			}
			
			periodNormalAndSports += row.data.get(ActivityDataRow.DataType.DATA_COL_STEP, 0.0);
			
		    arrayHolder.add(row);
		    
		    try  {
		    	
		    	JSONObject jObject 	= new JSONObject();
				jObject.put("Steps", row.data.get(ActivityDataRow.DataType.DATA_COL_STEP, 0.0));
				jObject.put("Distance", row.data.get(ActivityDataRow.DataType.DATA_COL_DIST, 0.0));
				jObject.put("Calories", row.data.get(ActivityDataRow.DataType.DATA_COL_CALS, 0.0));
				jArray.put(jObject);
			
		    } 
		    catch ( JSONException e )  {
				// TODO Auto-generated catch block
				e.printStackTrace();//
			}
		    
		    //Log.v("periodMode", String.format("periodMode is %f", periodMode));
		    Log.v("periodStartHour", Integer.toString(periodStartHour));
			Log.v("periodMode", Integer.toString(periodMode));
			Log.v("periodSteps", Double.toString(periodSteps));
			Log.v("periodDistance", Double.toString(periodDistance));
			//Log.v("periodSteps", String.format("periodSteps is %f", periodSteps));
			//Log.v("periodDistance", String.format("periodDistance is %f", periodDistance));
			
			Log.i( "Bluetooth", "BLE_Fetch.. KreyosB pushing.. dateString:" + row.dateString+ " epoch:" + row.epoch + "" );
        } 
		
		Log.d("Total Steps", "0:" + Double.toString(periodSteps));
		Log.d("Total Steps", "0 and 1: " +periodNormalAndSports);
		
		// Debug test on saving activity on web
		//*
		saveActivityStats(p_data, p_activity);
		//*/
		
		/*
        int m_diffDays = 1;
        long headEpoch = Utils.epoch();
        long tail = Utils.epochMinusDay( m_diffDays );
        
        Log.i( "ActivityStatsActivity::loadMultipleScrolls", "loadMultipleScrolls DATE_CHECK Head:" + headEpoch + " Tail:" + tail + " diffDays:" + m_diffDays + "" );
        
        // Create custom adapter(array) and set it to the list view
        Cursor queriedData = DatabaseManager.instance( (Context)p_activity ).queryData("SELECT * FROM Kreyos_User_Activities WHERE CreatedTime <= " + headEpoch + " AND " + "CreatedTime >= " + tail + " ORDER BY CreatedTime ASC" );
        Log.d("Queried Data Count", "" + queriedData.getCount());
        
        ArrayList<ActivityDataRow> testRow = new ArrayList<ActivityDataRow>();
        double totalSteps 		= 0;
        double totalDistance 	= 0;
        double totalCalories 	= 0;
        if(queriedData != null && queriedData.moveToFirst()) {
        	do {
        		
        		// Log.d("Queried Data Value","" + queriedData.getString(queriedData.getColumnIndex("CreatedTime")));
        		ActivityDataRow dataRow = new ActivityDataRow();
        		dataRow.data = new SparseArray<Double>();
        		
        		String epochValue 		= queriedData.getString(queriedData.getColumnIndex("CreatedTime"));
            	Calendar rowCalendar 	= Utils.calendar(Long.parseLong(epochValue));
            	dataRow.hour 			= rowCalendar.get(Calendar.HOUR_OF_DAY);
            	dataRow.minute 			= rowCalendar.get(Calendar.MINUTE);
            	dataRow.mode			= Integer.parseInt(queriedData.getString(queriedData.getColumnIndex("Sport_ID")));
            	
            	dataRow.data.put(ActivityDataRow.DataType.DATA_COL_INVALID, (double) 0);
            	dataRow.data.put(ActivityDataRow.DataType.DATA_COL_STEP, 	Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivitySteps"))));
            	dataRow.data.put(ActivityDataRow.DataType.DATA_COL_DIST, 	Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivityDistance"))));
            	dataRow.data.put(ActivityDataRow.DataType.DATA_COL_CALS, 	Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivityCalories"))));
            	dataRow.data.put(ActivityDataRow.DataType.DATA_COL_CADN, 	(double) 0);
            	dataRow.data.put(ActivityDataRow.DataType.DATA_COL_HR, 		(double) 0);
            	
            	if(dataRow.mode == 0) {
            		totalSteps += Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivitySteps")));
            		totalDistance += Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivityDistance")));
            		totalCalories += Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivityCalories")));
            	}
            	
            	Log.d("Hour", "" + rowCalendar.get(Calendar.HOUR_OF_DAY));
            	Log.d("Steps", "" + queriedData.getString(queriedData.getColumnIndex("ActivitySteps")));
            	
            	testRow.add(dataRow);
            	
        	} while(queriedData.moveToNext());
        }
        
		
		ActivityStatsAdapter adapter = new ActivityStatsAdapter( p_activity, R.layout.activity_stats_item_dup, testRow );
		Log.e("InsertUser", "{Data:"+jArray.toString()+"}");
		
        
		ActivityData activityData = new ActivityData(
			adapter,
			KreyosUtility.setDataForDisplay((double)totalSteps), 
			KreyosUtility.setDataForDisplay((double)(totalDistance/1000)), 
			KreyosUtility.setDataForDisplay((double)(totalCalories /1000)));
		
		return activityData;*/
		return getActivityOnLocal(p_activity);
	}
	
	public ActivityData getActivityOnLocal(KreyosActivity p_activity) {
		
		int m_diffDays = 0;
        long headEpoch = Utils.epoch();
        long tail = Utils.epochMinusDay( m_diffDays );
        long epochMinute = 60;
        
        Log.i( "ActivityStatsActivity::loadMultipleScrolls", "loadMultipleScrolls DATE_CHECK Head:" + headEpoch + " Tail:" + tail + " diffDays:" + m_diffDays + "" );
        
        // Create custom adapter(array) and set it to the list view
        Cursor queriedData = DatabaseManager.instance( (Context)p_activity ).queryData("SELECT * FROM Kreyos_User_Activities WHERE CreatedTime <= " + headEpoch + " AND " + "CreatedTime >= " + (tail + epochMinute) + " ORDER BY CreatedTime ASC" );
        Log.d("Queried Data Count", "" + queriedData.getCount());
        
        ArrayList<ActivityDataRow> testRow = new ArrayList<ActivityDataRow>();
        double totalSteps 		= 0;
        double totalDistance 	= 0;
        double totalCalories 	= 0;
        if(queriedData != null && queriedData.moveToFirst()) {
        	do {
        		
        		// Log.d("Queried Data Value","" + queriedData.getString(queriedData.getColumnIndex("CreatedTime")));
        		ActivityDataRow dataRow = new ActivityDataRow();
        		dataRow.data = new SparseArray<Double>();
        		
        		String epochValue 		= queriedData.getString(queriedData.getColumnIndex("CreatedTime"));
            	Calendar rowCalendar 	= Utils.calendar(Long.parseLong(epochValue));
            	dataRow.hour 			= rowCalendar.get(Calendar.HOUR_OF_DAY);
            	dataRow.minute 			= rowCalendar.get(Calendar.MINUTE);
            	dataRow.mode			= Integer.parseInt(queriedData.getString(queriedData.getColumnIndex("Sport_ID")));
            	
            	dataRow.data.put(ActivityDataRow.DataType.DATA_COL_INVALID, (double) 0);
            	dataRow.data.put(ActivityDataRow.DataType.DATA_COL_STEP, 	Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivitySteps"))));
            	dataRow.data.put(ActivityDataRow.DataType.DATA_COL_DIST, 	Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivityDistance"))));
            	dataRow.data.put(ActivityDataRow.DataType.DATA_COL_CALS, 	Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivityCalories"))));
            	dataRow.data.put(ActivityDataRow.DataType.DATA_COL_CADN, 	(double) 0);
            	dataRow.data.put(ActivityDataRow.DataType.DATA_COL_HR, 		(double) 0);
            	
            	if(dataRow.mode == 0) {
            		totalSteps += Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivitySteps")));
            		totalDistance += Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivityDistance")));
            		totalCalories += Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivityCalories")));
            	}
            	
            	Log.d("Hour", "" + rowCalendar.get(Calendar.HOUR_OF_DAY));
            	Log.d("Steps", "" + queriedData.getString(queriedData.getColumnIndex("ActivitySteps")));
            	
            	testRow.add(dataRow);
            	
        	} while(queriedData.moveToNext());
        }
        
		
		ActivityStatsAdapter adapter = new ActivityStatsAdapter( p_activity, R.layout.activity_stats_item_dup, testRow );
		
		ActivityData activityData = new ActivityData(
			adapter,
			KreyosUtility.setDataForDisplay((double)totalSteps), 
			KreyosUtility.setDataForDisplay((double)(totalDistance/1000)), 
			KreyosUtility.setDataForDisplay((double)(totalCalories /1000)));
		
		return activityData;
	}
	
	public void getActivityStatsonWeb(KreyosActivity p_activity, ACTIVITY_METRIC p_metric) {
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("auth_token", p_activity.getPrefs().getString(KreyosPrefKeys.USER_KREYOS_TOKEN, "")));
		params.add(new BasicNameValuePair("email", p_activity.getPrefs().getString(KreyosPrefKeys.USER_EMAIL, "")));
		
		String metricValue = ACTIVITY_METRIC.DAILY.toString();
		if (p_metric != ACTIVITY_METRIC.INVALID) {
			metricValue = p_metric.toString();
			metricValue = metricValue.toLowerCase();
		}
		params.add(new BasicNameValuePair("metric", metricValue));
		
		// Get response based on API url and parameters
		String response = RequestManager.instance().get(KreyosPrefKeys.URL_USER_ACTIVITIES, params);
		
		// Logging response
		Log.d("Response", "Activites: " + response);
		
		//* Testing for converting response to JSONObject
		try {
			// + ET 04292014 : Check if your logged in
			JSONObject jsonObject = new JSONObject(response.toString());
			if( jsonObject != null && jsonObject.has( "user" ) ) {
				
				Log.d("Respones", "" + response);
				JSONArray activities = (JSONArray)jsonObject.get( "user" );
				if ((activities.length() == 0 || activities == null)
				&& p_metric == AppHelper.ACTIVITY_METRIC.DAILY) {
					// Change metric to months
					Log.d("Log", "Change Metric");
					getActivityStatsonWeb(p_activity, AppHelper.ACTIVITY_METRIC.MONTHLY);
				}
				DatabaseManager.instance( (Context)p_activity ).saveDataToLocal( activities );

			}
		} catch(Exception ex) {
			Log.e( "AppHelter::getActivityStatsonWeb", "Error!" );
			ex.printStackTrace();
		}
		//*/
	}
	
	public void saveFirmwareVersion(KreyosActivity p_activity, String p_version)
	{
		SharedPreferences.Editor editor = p_activity.getPrefs().edit();
		editor.putString(KreyosPrefKeys.FIRMWARE_VERSION, p_version);
		editor.commit();	
	}
	
	public void showSessionErrorPrompt( String p_title, String p_message ) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(KreyosActivity.m_activity);
    	builder.setTitle(p_title);
    	builder.setMessage(p_message);
    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            	AppHelper.instance.sessionError(KreyosActivity.m_activity);
            }
        });		
		// + ET 040714 : Create and show dialog
    	AlertDialog dialog = builder.create();
    	dialog.show();
	}
	
	
}	
