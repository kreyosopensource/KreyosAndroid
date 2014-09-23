package com.kreyos.watch.bluetooth;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.CalendarContract;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.kreyos.watch.HomeActivity;
import com.kreyos.watch.KreyosActivity;
import com.kreyos.watch.KreyosActivity.PrefKeys;
import com.kreyos.watch.utils.LocalWeather;
import com.kreyos.watch.utils.LocationUpdater;


@SuppressLint({ "DefaultLocale", "NewApi" })
@SuppressWarnings("deprecation")
public class KreyosService extends Service {
	
	
	
	private static final String TAG = "KreyosService";
	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

	private LocalWeather.Data   mWeather = null;
	//private LocationSearch.Data mLocation;
	
	private Timer     mNTimerFacebook = null;
	private TimerTask mNTaskFacebook  = null;
	
	private Timer     mNTimerTwitter = null;
	private TimerTask mNTaskTwitter  = null;
	
	private Timer     mNTimerWeather = null;
	private TimerTask mNTaskWeather  = null;
	
	private Timer     mSportsSyncTimer = null;
	private TimerTask mSportsSyncTask  = null;
	
	private Timer     mActivitySyncTimer = null;
	private TimerTask mActivitySyncTask  = null;
	
	private Timer     mClockSyncTimer = null;
	private TimerTask mClockSyncTask  = null;
	
	private LocationUpdater mLocationUpdater = null;	
	
	private SMSReceiver              mSMSReceiver        = null;
	private IncommingCallReceiver    mCallReciever       = null;
	private CalendarReminderReceiver mReminderReceiver   = null;
	private BatteryIndicatorReceiver mLowBatteryReceiver = null;
	

	private int mUserId = -1;
	
	private String mActivityId = "";
	//private CachedSportsDataUploadThread mCachedSportsDataUploader = null;

	private KreyosServiceBinder mBinder = new KreyosServiceBinder();
	private BTDataHandler mBTMsgHandler = null;
	
	//private LinkedList<Location> mActivitySpots = new LinkedList<Location>();
	private Location lastSpot = null;
	
	private TimeSyncThread mSyncThread = null;
	private LinkedList<String> mToReadFiles = new LinkedList<String>();
	private LinkedList<String> mToWriteFiles = new LinkedList<String>();
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override  
    public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
	
	public void initServiceTasks() {
		//initialize bluetoothAgent
		SharedPreferences prefs = KreyosActivity.getPrefs();
		try
		{
			String deviceName = prefs.getString("bluetooth.device_name", "");
			Log.d("KreyosService", "deviceName:" + deviceName );
			initBluetooth(prefs.getString("bluetooth.device_name", ""));
		}
		catch(Exception e)
		{
			prefs.edit().clear().commit();
			Log.e("Reconnection", "Needs to clear the service");
			Intent i = new Intent(this, HomeActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
		}
			
		//initialize all notifications
		
		if (prefs.getBoolean("notification.facebook",       false)) startFacebookNotification();
		if (prefs.getBoolean("notification.weather",        false)) startWeatherNotification();
		if (prefs.getBoolean("notification.twitter",        false)) startTwitterNotification();
		if (prefs.getBoolean("notification.reminder",       false)) // startReminderListener();
		if (prefs.getBoolean("notification.sms",            false)) startSMSReceiver();
		//if (prefs.getBoolean("notification.call",           false)) startCallReceiver();
		if (prefs.getBoolean("notification.low_battery",    false)) startLowBatteryNotification();
		if (prefs.getBoolean("notification.bt_outof_range", false));
		
		//initialize timely sync tasks
		startSportsSyncTask(5 * 60 * 1000);
	//	startCachedSportsDataUploader();
	}

	@Override  
    public void onCreate() {
		super.onCreate();
		// TODO Auto-generated method stub
		Log.v(TAG, "Service Created");
		//initServiceTasks();
		
		
	}
	
	@Override  
    public int onStartCommand(Intent intent, int flags, int startId) {
		
		
		Log.v(TAG, "Service Start");
		//initialize bluetoothAgent
		initBluetooth(intent.getStringExtra("bluetooth.device_name"));
			
		//initialize all notifications
		if (intent.getBooleanExtra("notification.facebook",       false)) startFacebookNotification();
		if (intent.getBooleanExtra("notification.weather",        false)) startWeatherNotification();
		if (intent.getBooleanExtra("notification.twitter",        false)) startTwitterNotification();
		if (intent.getBooleanExtra("notification.reminder",       false)) // startReminderListener();
		if (intent.getBooleanExtra("notification.sms",            false)) startSMSReceiver();
		//if (intent.getBooleanExtra("notification.call",           false)) startCallReceiver();
		if (intent.getBooleanExtra("notification.low_battery",    false)) startLowBatteryNotification();
		if (intent.getBooleanExtra("notification.bt_outof_range", false)) ;
		
		//initialize timely sync tasks
		startSportsSyncTask(5 * 60 * 1000);
	//	startCachedSportsDataUploader();
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override  
    public void onDestroy() 
	{
		Log.v(TAG, "Service Destroy");
		stopAllService();
		//stopSportsSyncSchedular();
		//stopFacebookNotification();
		//Session.getActiveSession().closeAndClearTokenInformation();
		
		
	}
	
	private void stopAllService()
	{
		stopActivitySyncTask();
		stopAutoClockSync();
		stopCallReceiver();
		stopFacebookNotification();
		stopLocationUpdater();
		stopLowBatteryNotification();
		stopReminderListener();
		stopSMSReceiver();
		stopSportsSyncTask();
		stopTwitterNotification();
		stopWeatherNotification();
	}
	
	public void initBluetooth(String deviceName)
	{
		mBTMsgHandler = new BTDataHandler(this);
		BluetoothAgent btAgent = BluetoothAgent.getInstance(null);
		
		btAgent.bindDevice(deviceName);
		btAgent.startActiveSession();
		btAgent.bindServiceHandler(mBTMsgHandler);
		BluetoothDevice btDevice = btAgent.getTargetDevice();
		
		
		if (btDevice == null) {
			// Toast.makeText(this, "No Kreyos Watch Connected", Toast.LENGTH_SHORT).show();
			Log.v(TAG, "No Device binded");
			return;
		}
		
		//try start bluetooth service - since the inner used state machine, 
		// Multiple call to the startActivieSession is ok.
		btAgent.startActiveSession();
	}
	
	private JSONArray parseFacebookNotifications(String rawData) {
		JSONTokener jsonParser = new JSONTokener(rawData);
		
		try {
			JSONObject jsonObj = (JSONObject)jsonParser.nextValue();
			return (JSONArray)jsonObj.getJSONArray("data");
		} catch (JSONException e) {	
			e.printStackTrace();
			return null;
		}
	}
	
	private class FacebookNotificationRequestListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {
			Log.v(TAG, "Facebook Feed Fetched");
			
		Log.d("Tagfacebook", response.toString());
			
		
		try {
	       
	        Log.d("Tests", "got response: " + response);
	        if (response == null || response.equals("")
	                || response.equals("false")) {
	            Log.i("TAG", "Blank Response...");
	        } else {

	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		
			/*JSONArray notifications = parseFacebookNotifications(response);
			if (notifications != null && BluetoothAgent.getInstance(null) != null) {
				Protocol p = new Protocol(BluetoothAgent.getInstance(null), KreyosService.this);
				for (int i = 0; i < notifications.length(); ++i) {
					try {
						JSONObject item = notifications.getJSONObject(i);
						String from  = item.getJSONObject("from").getString("name");
						String title = item.getString("title");
						p.notifyMessage(Protocol.elementTypeMsgFB, from, title);
					} catch (JSONException e) {
						Log.e(TAG, "Parse Facebook Notifcations failed");
					}
				}
			} */
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Log.e(TAG, "Facebook Feed IO failed:" + e.getMessage());
		}

		@Override
		public void onFileNotFoundException(
				FileNotFoundException e, Object state) {
			Log.e(TAG, "Facebook Feed failed:" + e.getMessage());
		}

		@Override
		public void onMalformedURLException(
				MalformedURLException e, Object state) {
			Log.e(TAG, "Facebook Feed failed:" + e.getMessage());
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Log.e(TAG, "Facebook Feed failed:" + e.getMessage());
		}		
	}
	//joe https://github.com/WorldWeatherOnline/AndroidWWO/blob/master/src/com/example/androidwwo/ExampleAppWidgetProvider.java
	private void notifyWeather(Location location) {
		String q = Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude());
		Log.e("Latitude", Double.toString(location.getLatitude()));
		Log.e("Longhitude", Double.toString(location.getLongitude()));
		//get weather
		LocalWeather lw = new LocalWeather(true);
		String query = (lw.new Params(lw.key)).setQ(q).getQueryString(LocalWeather.Params.class);
		LocalWeather.Data curWeather = lw.callAPI(query);
		
		if (mWeather == null ||
			mWeather.current_condition.observation_time != 
					curWeather.current_condition.observation_time) {
			
			if (BluetoothAgent.getInstance(null) == null) {
				return;
			}
			
			Protocol p = new Protocol(BluetoothAgent.getInstance(null), KreyosService.this);
			String weatherMsg = String.format("%s:%s,%s,%s", 
					curWeather.current_condition.weatherDesc,
					curWeather.current_condition.temp_C,
					curWeather.current_condition.humidity,
					curWeather.current_condition.windspeedKmph
					);
			p.notifyMessage("MW", "weather", weatherMsg);
			mWeather = curWeather;
		}
		
		//get location
		//LocationSearch ls = new LocationSearch(true);
		//query = (ls.new Params(ls.key)).setQuery(q).getQueryString(LocationSearch.Params.class);
		//mLocation = ls.callAPI(query);
		
		//updateWidget
		//updateAppWidget(UpdateService.this);
	}
	
	public void stopLocationUpdater() {
		if (mLocationUpdater != null) {
			mLocationUpdater.stopLocationTask();
			mLocationUpdater = null;
		}
	}
	
	public Location getLastLocation() {
		if (mLocationUpdater != null) {
			return mLocationUpdater.getLastLocation();
		}
		else {
			return null;
		}
	}
	
	public void startLocationUpdater() {

		if (mLocationUpdater != null)
			return;
		
		LocationUpdater.LocationResult locationResult = new LocationUpdater.LocationResult(){
    	    @Override
    	    public void gotLocation(Location location){
    	        //Got the location!
    	    	//Prevent multiple execution of the callback gotLocation, as reported at
    	    	//http://stackoverflow.com/questions/3145089/what-is-the-simplest-and-most-robust-way-to-get-the-users-current-location-in-a
    	    	//synchronized (mWeatherUpdated) {
    	    	//	if(!mWeatherUpdated) {
    	    	//		notifyWeather(location);    	    			
    	    	//		mWeatherUpdated = true;
    	    	//	}
    	    	//}
    	    	//Location lastSpot = mActivitySpots.getLast();
    	    	Date now = new Date();
    	    	if (lastActivitySyncTime != null &&
    	    		now.getTime() - lastActivitySyncTime.getTime() < syncTimeout) {
    	    		
    	    		short distance = 0;
    	    		int calories = 0; //TODO: calculate this
    	    		if (lastSpot != null) {
    	    			distance = (short)Math.abs(location.distanceTo(lastSpot) * 10);
        	    	}
	    			    	    		
    	    		Log.v("KreyosService", String.format(
    	    				"Send GPS Info(%d, %d, %d, %d)", 
    	    				distance, calories, (int)location.getSpeed(), (int)location.getAltitude()));
    	    		
    	    		if (BluetoothAgent.getInstance(null) != null) {
    	    			Protocol p = new Protocol(BluetoothAgent.getInstance(null), null);
    	    			p.sendGPSInfo(location, distance, calories);
    	    		}
    	    		
    	    		lastSpot = location;
	    		} else {
	    			stopLocationUpdater();
	    			lastSpot = null;
	    		}
    	    	
    	    	//mActivitySpots.add(location);
    	    }
    	};
    	
    	Log.d(TAG, "buildLocationUpdate");
    	
    	mLocationUpdater = new LocationUpdater();
    	mLocationUpdater.startLocationTask(this, locationResult);
	}
	
	private class SMSReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v(TAG, "SMSReceiver::onReceive()");
			if (SMS_RECEIVED.equals(intent.getAction())) {
				Log.v(TAG, "SMS Message Received");
				
				Bundle bundle = intent.getExtras(); 
	            if (bundle != null && BluetoothAgent.getInstance(null) != null) {
	                Object[] pdus = (Object[]) bundle.get("pdus");
	                Protocol p = new Protocol(BluetoothAgent.getInstance(null), KreyosService.this);
	                final SmsMessage[] messages = new SmsMessage[pdus.length];
	                for (int i = 0; i < pdus.length; i++) {
	                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
	                    Log.v(TAG, "SMS Message From:" + messages[i].getDisplayOriginatingAddress());
	                    Log.v(TAG, "SMS Message Body:" + messages[i].getDisplayMessageBody());
	                    
		                p.notifyMessage(
		                		Protocol.elementTypeMsgSMS, 
		                		messages[i].getDisplayOriginatingAddress(), 
		                		messages[i].getDisplayMessageBody()
		                		);
	                }
	            } 
			}
		}
    	
    }
	
	public class IncommingCallReceiver extends BroadcastReceiver {

	    private final String TAG = IncommingCallReceiver.class.getSimpleName();
	    public static final String ACTION_PHONE_STATE = TelephonyManager.ACTION_PHONE_STATE_CHANGED;

	    @Override 
	    public void onReceive(Context context, Intent intent) { 
	        final String action = intent.getAction(); 
	        final String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

	        Log.e(TAG, "Call State Changed, Action = " + action);

	        if (ACTION_PHONE_STATE.equals(action)) {
	            if (TelephonyManager.EXTRA_STATE_RINGING.equals(state) && BluetoothAgent.getInstance(null) != null) {
	                final String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER); 
	                Log.e(TAG, "Incomming call, Number��" + number);
	                Protocol p = new Protocol(BluetoothAgent.getInstance(null), KreyosService.this);
	                p.notifyMessage(
	                		"MC",
	                		number,
	                		"Calling"
	                		);
	            }
	        }
	    }
	}
	
	public class CalendarReminderReceiver extends BroadcastReceiver {

	    @Override 
	    public void onReceive(Context context, Intent intent) { 
	        final String action = intent.getAction();
	        if (CalendarContract.ACTION_EVENT_REMINDER.equals(action) && BluetoothAgent.getInstance(null) != null) {
	        	Log.v("CalendarReminder", "CalenderReminder:");
	        	Protocol p = new Protocol(BluetoothAgent.getInstance(null), KreyosService.this);
                p.notifyMessage(
                		"MR",
                		"Calendar Reminder",
                		"Unknown"
                		);
	        }
	    }

	}
	
	public class BatteryIndicatorReceiver extends BroadcastReceiver {

	    @Override 
	    public void onReceive(Context context, Intent intent) { 
	        final String action = intent.getAction();
	        if (Intent.ACTION_BATTERY_LOW.equals(action) && BluetoothAgent.getInstance(null) != null) {
	        	Log.v("Battery", "Battery is low");
	        	Protocol p = new Protocol(BluetoothAgent.getInstance(null), KreyosService.this);
                p.notifyMessage(
                		"MB",
                		"Battery",
                		"Battery is low"
                		);
	        }
	    }

	}
	
	/*
	
	private void uploadSportsDataItem(String rawDataLine) {
		String[] data = rawDataLine.split(",");
		user_sports_data item = new user_sports_data();
		
		item.mUserId     = Integer.parseInt(data[0]);
		item.mTimeStamp  = Integer.parseInt(data[1]);
		item.mSportsType = Integer.parseInt(data[2]);			
		item.mAltitude   = Float.parseFloat(data[3]);
		item.mCalories   = Float.parseFloat(data[4]);
		item.mDistance   = Float.parseFloat(data[5]);
		item.mSpeed      = Float.parseFloat(data[6]);
		item.mHeartRate  = Float.parseFloat(data[7]);
		item.mSteps      = Float.parseFloat(data[8]);
		item.mLatitude   = Float.parseFloat(data[9]);
		item.mLongitude  = Float.parseFloat(data[10]);
		
		mUsersDataTable.insert(item, null);
	}
	
	private boolean uploadCachedSportsData (String rawData) {
		String[] dataItem = rawData.split(";");
		for (String item : dataItem) {
			uploadSportsDataItem(item);
		}
		return true;
	}
	
	private class CachedSportsDataUploadThread extends Thread {

		public void run() {
			if (!initAzureClient()) {
				Log.e(TAG, "Cannot connect to Azure");
				return;
			}
			
			SharedPreferences pref = KreyosActivity.getPrefs();	
			while (mUserId == -1) {
				try {
					Thread.sleep(10 * 60 * 1000);
				} catch (InterruptedException e) {
					return;
				}
			}
			
			int counter = 0;
			while (true) {
				String prefKey = String.format("write_cache.user_sports_data.%d", counter);
				if (pref.contains(prefKey)) {
					String data = pref.getString(prefKey, "");
					if (data.length() > 0) {
						uploadCachedSportsData(data);
					}
				}
				counter++;
				int waitTime = 10 * 1000;
				if (counter % 1000 == 0) {
					waitTime = 30 * 60 * 1000;
					pref = KreyosActivity.getPrefs();
					counter = 0;
				}
				
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}; */
	
	private class TimeSyncThread extends Thread {

		private static final int syncStateWaiting = 0;
		private static final int syncStateReading = 1;
		private static final int syncStateWriting = 2;
		private static final int syncStateListing = 3;
		private static final int syncStateListed  = 4;
		
		public void run() {
			int state = syncStateWaiting;
			while (true) {
				int sleepTime = 1000;
				
				switch (state) {
				case syncStateWaiting:
					if (!mToReadFiles.isEmpty()) {
						//means we have something to read from watch
						state = syncStateReading;
					}
					else if (!mToWriteFiles.isEmpty()) {
						//means we have something to upload to cloud
						state = syncStateReading;
					}
					else {
						//try get the to read file list
						state = syncStateListing;
					}
					break;
					
				case syncStateReading:
					String toReadFileName = mToReadFiles.pollLast();
					if (toReadFileName != null && BluetoothAgent.getInstance(null) != null) {
						Protocol p = new Protocol(BluetoothAgent.getInstance(null), KreyosService.this);
						p.readFile(toReadFileName);
						sleepTime = 10 * 1000; // reading command will be sent every 10 seconds until we get result
					}
					else if (!mToWriteFiles.isEmpty()) {
						//reading over and there are files need to be uploaded
						state = syncStateWriting;
					}
					else {
						//nothing to to, we can rest for a while
						state = syncStateWaiting;
						sleepTime = 20 * 60 * 1000;
					}
					break;
					
				case syncStateWriting:
					String toWriteFilename = mToReadFiles.pollLast();
					if (toWriteFilename != null) {
						//TODO: parse binary raw data and send it to cloud
					}
					else {
						//nothing to to, we can rest for a while
						state = syncStateWaiting;
						sleepTime = 20 * 60 * 1000;
					}
					break;
					
				case syncStateListing:
					if (BluetoothAgent.getInstance(null) != null) {
						Protocol p = new Protocol(BluetoothAgent.getInstance(null), KreyosService.this);
						p.listFile("sports/");
						state = syncStateListed;
					}
					break;
					
				case syncStateListed:
					if (!mToReadFiles.isEmpty()) {
						//means we have something to read from watch
						state = syncStateReading;
					}
					else if (!mToWriteFiles.isEmpty()) {
						//means we have something to upload to cloud
						state = syncStateReading;
					}
					else {
						//nothing to to, we can rest for a while
						state = syncStateWaiting;
						sleepTime = 20 * 60 * 1000;
					}
					break;
				}
				
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	};
	
	private void handleFacebokNotification() {
		Facebook facebook = KreyosActivity.getFacebook(this);
		facebook.getSession();
		
		if (facebook.isSessionValid()) {
			AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
	       mAsyncRunner.request("me/notifications", new FacebookNotificationRequestListener());
			/*try{ 
			String response = facebook.request("me");
			    Bundle parameters = new Bundle();
			    //parameters.putString("message", msg);
			    parameters.putString("description", "test test test");
			    response = facebook.request("me/feed", parameters, "GET");
			    Log.e("FB RESPONSE", response.toString());
			    
			}catch(Exception e){
				
			} */
	        Log.e("facebook", "notifications");
		} else{
			Log.e("fACEBOOK", "!facebook.isSessionValid()");
			
		}
		

			 
	}
	
	private void handleTwitterNotification() {
		//TODO: Implement this
		
	}
	
	private void handleWeatherNotification() {
		if (mLocationUpdater == null) {
			//ensure the location updater is working
			//edited joe removed uncomment
			//startLocationUpdater();
		}
		else if (mLocationUpdater.getLastLocation() != null){
			notifyWeather(mLocationUpdater.getLastLocation());
		}
	}
		
	private void handleSportsDataSync() {
		if (mUserId == -1) {
			return;
		}
	}
	
	private void handleAutoClockSync() {
		if (BluetoothAgent.getInstance(null) != null) {
			Protocol p = new Protocol(BluetoothAgent.getInstance(null), KreyosService.this);
			p.syncTime();
		}
	}
	
	public void startFacebookNotification() {
		
	//	stopFacebookNotification();
		
		Log.v(TAG, String.format("NotificationSchedular Start"));
		//mNTimerFacebook = new Timer();
		//mNTaskFacebook = new TimerTask() {
			//@Override
			//public void run() {
				handleFacebokNotification();
			//}
		//};
		//mNTimerFacebook.schedule(mNTaskFacebook, 0, 20 * 60 * 1000);
	}
		
	public void stopFacebookNotification() {		
		if (mNTimerFacebook != null) {
			Log.v(TAG, "FacebookNotification Timer Stopped");
			mNTimerFacebook.cancel();
			mNTimerFacebook = null;
		}
		if (mNTaskFacebook != null) {
			Log.v(TAG, "FacebookNotification Task Stopped");
			mNTaskFacebook.cancel();
			mNTaskFacebook = null;
		}
	}

	public void startTwitterNotification() {
		Log.v(TAG, "NotificationSchedular Start");
		mNTimerTwitter = new Timer();
		mNTaskTwitter = new TimerTask() {
			@Override
			public void run() {
				handleTwitterNotification();
			}
		};
		mNTimerTwitter.schedule(mNTaskTwitter, 0, 20 * 60 * 1000);
	}
		
	public void stopTwitterNotification() {
		if (mNTimerTwitter != null) {
			Log.v(TAG, "Schedule timer Stop");
			mNTimerTwitter.cancel();
			mNTimerTwitter = null;
		}
		if (mNTaskTwitter != null) {
			Log.v(TAG, "Schedule Task Stop");
			mNTaskTwitter.cancel();
			mNTaskTwitter = null;
		}
	}
	
	public void startWeatherNotification() {
		
		stopWeatherNotification();
		
		if (mLocationUpdater == null) {
			//startLocationUpdater();
		}
		
		Log.v(TAG, "WeatherNotification Start");
		mNTimerWeather = new Timer();
		mNTaskWeather = new TimerTask() {
			@Override
			public void run() {
				handleWeatherNotification();
			}
		};
		mNTimerWeather.schedule(mNTaskWeather, 0, 20 * 60 * 1000);
	}
		
	public void stopWeatherNotification() {
		if (mNTimerWeather != null) {
			Log.v(TAG, "WeatherNotification Timer Stop");
			mNTimerWeather.cancel();
			mNTimerWeather = null;
		}
		if (mNTaskWeather != null) {
			Log.v(TAG, "WeatherNotification Task Stop");
			mNTaskWeather.cancel();
			mNTaskWeather = null;
		}
	}
	
	public void startSportsSyncTask(final int interval) {
		
		stopSportsSyncTask();
		
		Log.v(TAG, "SportsSyncSchedular Start");
		mSportsSyncTimer = new Timer();
		mSportsSyncTask = new TimerTask() {
			@Override
			public void run() {
				handleSportsDataSync();
			}
		};
		mSportsSyncTimer.schedule(mSportsSyncTask, 0, interval);
		
		mSyncThread = new TimeSyncThread();
		mSyncThread.start();
	}
	
	public void stopSportsSyncTask() {
		if (mSportsSyncTimer != null) {
			Log.v(TAG, "SportsSync Timer Stopped");
			mSportsSyncTimer.cancel();
			mSportsSyncTimer = null;
		}
		if (mNTaskFacebook != null) {
			Log.v(TAG, "SportsSync Task Stopped");
			mNTaskFacebook.cancel();
			mNTaskFacebook = null;
		}
		if (mSyncThread != null) {
			mSyncThread.interrupt();
			try {
				mSyncThread.join();
			} catch (InterruptedException e) {
			}
			mSyncThread = null;
		}
	}
	
	public void stopActivitySyncTask() {
		if (mLocationUpdater != null) {
			stopLocationUpdater();
			lastSpot = null;
		}
		if (mActivitySyncTimer != null) {
			Log.v(TAG, "Activity Timer Stopped");
			mActivitySyncTimer.cancel();
			mActivitySyncTimer = null;
		}
		if (mActivitySyncTask != null) {
			Log.v(TAG, "Activity Task Stopped");
			mActivitySyncTask.cancel();
			mActivitySyncTask = null;
		}
	}
	
	public void startSMSReceiver() {
    	IntentFilter filter = new IntentFilter(SMS_RECEIVED);
    	if (mSMSReceiver == null) {
    		mSMSReceiver = new SMSReceiver();
    	}
    	registerReceiver(mSMSReceiver, filter);
	}
	
	public void stopSMSReceiver() {
		if (mSMSReceiver != null) {
			unregisterReceiver(mSMSReceiver);
			mSMSReceiver = null;
		}
	}
	
	public void startCallReceiver() {
		IntentFilter filter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		if (mCallReciever == null) {
			mCallReciever = new IncommingCallReceiver();
		}
		registerReceiver(mCallReciever, filter);
	}
	
	public void stopCallReceiver() {
		if (mCallReciever != null) {
			unregisterReceiver(mCallReciever);
			mCallReciever = null;
		}
	}
	
	public void startReminderListener() {
        IntentFilter filter = new IntentFilter(CalendarContract.ACTION_EVENT_REMINDER);
        filter.addDataScheme("content");
        if (mReminderReceiver == null) {
        	mReminderReceiver = new CalendarReminderReceiver();
        }        
        registerReceiver(mReminderReceiver, filter);
	}
	
	public void stopReminderListener() {
		if (mReminderReceiver != null) {
			unregisterReceiver(mReminderReceiver);
			mCallReciever = null;
		}
	}
	
	public void startLowBatteryNotification() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_LOW);
		if (mLowBatteryReceiver == null) {
			mLowBatteryReceiver = new BatteryIndicatorReceiver();
		}
		registerReceiver(mLowBatteryReceiver, filter);
	}
	
	public void stopLowBatteryNotification() {
		if (mLowBatteryReceiver != null) {
			unregisterReceiver(mLowBatteryReceiver);
			mLowBatteryReceiver = null;
		}
	}
	
/*	public void startCachedSportsDataUploader() {
		mCachedSportsDataUploader = new CachedSportsDataUploadThread();
		mCachedSportsDataUploader.start();
	} */
	
	public void startAutoClockSync() {
		stopAutoClockSync();
		
		mClockSyncTimer = new Timer();
		mClockSyncTask = new TimerTask() {
			@Override
			public void run() {
				handleAutoClockSync();
			}
		};
		mClockSyncTimer.schedule(mClockSyncTask, 0, 6 * 60 * 60 * 1000);
	}
	
	public void stopAutoClockSync() {
		if (mClockSyncTimer != null) {
			mClockSyncTimer.cancel();
			mClockSyncTimer = null;
		}
		if (mClockSyncTask != null) {
			mClockSyncTask.cancel();
			mClockSyncTask = null;
		}
	}
	
	public void clearFacebookTokenCache() {
		SharedPreferences.Editor editor = KreyosActivity.getPrefs().edit();
		editor.remove(PrefKeys.FB_ACCESS_TOKEN);
		editor.remove(PrefKeys.FB_ACCESS_EXPIRES);
        editor.commit();
	}
	
	/*Facebook facebook = null;
	public void logoutFacebook(final MoreSettingsActivity activity) {
		facebook = KreyosActivity.getFacebook(this);
		if (facebook.isSessionValid()) {
			AsyncTask<Void, Void, String> t = new AsyncTask<Void, Void, String>(){
                protected String doInBackground(Void... p) {
                	try {
						return facebook.logout(KreyosService.this);
					} catch (MalformedURLException e) {
						e.printStackTrace();
						return "failed";
					} catch (IOException e) {
						e.printStackTrace();
						return "failed";
					} catch (IllegalArgumentException e) {
						return "failed";
					}
                }

                protected void onPostExecute(String resp){
                	clearFacebookTokenCache();
                	activity.adjustLoginStatus();
                }
            };
            t.execute();
		}
	} */

	private byte[] sportsGridBuf = null;
	private byte[] sportsDataBuf = null;
	private Date lastActivitySyncTime = null;
	private long syncTimeout = 15 * 1000;
	
	private void handleBTData(Message msg) {
		
		switch (msg.what) {
		/* remove by Jacky since March 30
		 * case Protocol.MessageID.MSG_ACTIVITY_DATA_GOT:
			syncTimeout = 15 * 1000;
			sportsDataBuf = (byte[])msg.obj;
			startLocationUpdater();
			lastActivitySyncTime = new Date();
			
			//TODO: sync to cloud
			break;
			
		case Protocol.MessageID.MSG_ACTIVITY_DATA_END:
			sportsDataBuf = null;
			stopLocationUpdater();
			lastSpot = null;
			lastActivitySyncTime = null;
			//TODO: sync to cloud
			break;
			
		case Protocol.MessageID.MSG_GRID_GOT:
			sportsGridBuf = (byte[])msg.obj;
			break;
			
		case Protocol.MessageID.MSG_ACTIVITY_PREPARE:
			syncTimeout = 45 * 1000;
			
			lastSpot = null;
    		startLocationUpdater();
    		
    		lastActivitySyncTime = new Date();
			break;
			
		case Protocol.MessageID.MSG_ACTIVITY_SYNC:
			//TODO: save data somewhere and show them in home activity
			//the data will be in format like:
			 byte[] databuffer = (byte[])msg.obj;
			 
			 int timestamp = readDataInt(databuffer, 0);
			 int steps = readDataInt(databuffer, 1);
			 int cals   = readDataInt(databuffer, 2);
			 int distance = readDataInt(databuffer, 3);
			 
			 Log.e("timestamp", Integer.toString(timestamp));
			 Log.e("steps", Integer.toString(steps));
			 Log.e("cals", Integer.toString(cals));
			 Log.e("distance", Integer.toString(distance));
			// byte[0]  ~ byte[3]  : time stamp - seconds from 2000-01-01 00:00:00
			// byte[4]  ~ byte[7]  : steps
			// byte[8]  ~ byte[11] : calories
			// byte[12] ~ byte[15] : distance
			// refer to SportsActivity.displaySportsData() to understand how to show them
			break; */
			
		case Protocol.MessageID.MSG_LAUNCH_GOOGLENOW:
			Intent intent = new Intent();
			intent.setClassName(
					"com.google.android.googlequicksearchbox",
					"com.google.android.googlequicksearchbox.VoiceSearchActivity"
					);
			intent.addCategory(Intent.CATEGORY_DEFAULT);  
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;
			
		case Protocol.MessageID.MSG_BLUETOOTH_STATUS:
			//TODO: show something on UI to tell user that the BTAgent status changed
			// refer to SelfTestActivity.java
			break;
					
		case Protocol.MessageID.MSG_FIRMWARE_VERSION:
			String version = (String) msg.obj;
			SharedPreferences.Editor editor = KreyosActivity.getPrefs().edit();
			editor.putString("watch.version", version);
			editor.commit();
			//TODO: This part of code is for testing only, please compare the version with the
			// file in web service to determine if upgrade is needed
			break;
		}		
		
	}
	
	private int readDataInt(byte[] buffer, int i) 
	{
		   byte b0 = buffer[i * 4 + 0];
		   byte b1 = buffer[i * 4 + 1];
		   byte b2 = buffer[i * 4 + 2];
		   byte b3 = buffer[i * 4 + 3];
		   
		   //TODO: verify whether it is big-endian or little-endian
		   int value = (int) ((int)b0 | (int)b1 << 8 | (int)b2 << 16 | (int)b3 << 24);
		   
		   return value;
		   //return value = (int) ((int)b3 | (int)b2 << 8 | (int)b1 << 16 | (int)b0 << 24);
	}
	
	private static class BTDataHandler extends Handler {
        private final WeakReference<KreyosService> mService;

        public BTDataHandler(KreyosService service) {
        	mService = new WeakReference<KreyosService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
        	KreyosService service = mService.get();
			if (service != null) {
				service.handleBTData(msg);
			}
        }
    }
	
    public class KreyosServiceBinder extends Binder{

    	public KreyosService getService(){
            return KreyosService.this;
        }
    }

}
