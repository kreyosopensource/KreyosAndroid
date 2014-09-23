package com.kreyos.watch;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.service.textservice.SpellCheckerService.Session;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.text.format.Time;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
//import android.widget.Toast;

import com.coboltforge.slidemenu.SlideMenuInterface.OnSlideMenuItemClickListener;
import com.kreyos.watch.R;
import com.kreyos.watch.adapter.ActivityStatsAdapter;
import com.kreyos.watch.bluetooth.BluetoothAgent;
import com.kreyos.watch.bluetooth.KreyosService;
import com.kreyos.watch.bluetooth.Protocol;
import com.kreyos.watch.bluetooth.BluetoothAgent.ErrorCode;
import com.kreyos.watch.dataobjects.ActivityData;
import com.kreyos.watch.managers.AppHelper;
import com.kreyos.watch.managers.AppHelper.ACTIVITY_METRIC;
import com.kreyos.watch.managers.AppHelper.WATCH_STATE_VALUE;
import com.kreyos.watch.objectdata.ActivityDataDoc;
import com.kreyos.watch.objectdata.ActivityDataRow;
import com.kreyos.watch.objectdata.TodayActivity;
import com.kreyos.watch.utils.CircularImageView;
import com.kreyos.watch.utils.CircularProgressbar;
import com.kreyos.watch.utils.Utils;

public class HomeActivity extends KreyosActivity 
implements 
	OnSlideMenuItemClickListener {

	/* create new slideMenu for the right */
	private SlidingMenu slidemenu;
	private SlidingMenu slidemenu_right;
	
	//private final static int MYITEMID = 42;
	private boolean condition = true;
	
	public static int 	gridIndex = 0;
	private static final int NUM_PAGES = 2;
	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;
	
	private BTDataHandler mBTMsgHandler = null;
	
	private String for_calorie;
	private String for_distance;
	private String for_steps;

	private boolean isLeftMenuSelected = false;
	
	private ListView m_listviewDataItems;
	private TextView m_txtTotalSteps;
	private TextView m_txtTotalDistance;
	
	private RelativeLayout m_layoutProgress;
	private RelativeLayout m_layoutSetTarget;
	private RelativeLayout m_layoutActivityData;
	
	private CircularImageView m_avatarImage = null;
	
	private int IMAGE_REQUEST_CODE = 5;
	private int CAMERA_REQUEST_CODE = 6;
	
	private boolean m_isSavedConnection = false;
	
	// +AS:03222014 Please see the EHomeActivity Enum
	public static EHomeActivity targetContentView = EHomeActivity.RWB_;
	
	public static String LOG_TAG = "HomeActivity";
	
	private KreyosActivity m_activity = null;
    private String activeDeviceName = "";
    CircularProgressbar mCProgressBar;
    private Button mBtnDailyTarget;
    private boolean mIsGoingToDialyTarget = true;
    
    Timer mGetDataTimer;
    TimerTask mGetDataTask;
    long mGetDataDelay = 10000;
    boolean mIsAlreadyGettingData = false;
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView(R.layout.activity_main);
		
		AppHelper.instance().IS_TUTORIAL_MODE = false;
		
		KreyosUtility.overrideFonts(this, 
				((ViewGroup)findViewById(android.R.id.content)).getChildAt(0), 
				KreyosUtility.FONT_NAME.LEAGUE_GOTHIC_REGULAR);
		
		KreyosActivity.initPrefs(this);
	    Log.e("activeDeviceName", "sdfdsf:"+activeDeviceName);
	    
	    setupHUDandCallbacks();
	    loadProfileImage(m_avatarImage);
	    /* Test
	    SharedPreferences prefs = getPrefs();
	    
	    if(!prefs.contains(KreyosPrefKeys.USER_FIRST_VIEW)) 
	    {
	    	m_layoutSetTarget.setVisibility(View.VISIBLE);;
	    }
	    else
	    {
	    	m_layoutSetTarget.setVisibility(View.INVISIBLE);;
	    	m_layoutActivityData.setVisibility(View.VISIBLE
	    	);
	    }

	    m_layoutSetTarget.setVisibility(View.VISIBLE);;
    	m_layo
    	utActivityData.setVisibility(View.INVISIBLE);
    	*/
	    onStartApp();
 
	}
	
	private void onStartApp()
	{
	    SharedPreferences prefs = KreyosActivity.getPrefs();
//	    prefs.edit().clear().commit();
	    
	    if(enableBluetooth()) {
	    	
	    	//  Log.d("Log", "WATCH STATE:" + AppHelper.instance().WATCH_STATE.toString());	    	
	    	
	    	/* There case of checking watch connections
	    	 * 1. The default one, no saved connection manually connect to desire watch
	    	 * 2. Open application with saved connection
	    	 * 3. Switching from other activities
	    	 */
	    	
	    	// No watch connected
	    	if (!isThereWatchAvailable() 
	    	|| !prefs.contains("bluetooth.device_name") 
	    	|| prefs.getString("bluetooth.device_name", "") == "") {
	    		if( AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.DISCONNECTED ) { 
	    			// Log.d("Log", "Start Connection");
	    			SharedPreferences.Editor editor = prefs.edit();
		    		editor.remove("bluetooth.device_name");
		    		editor.commit();
		    		
		    		init();
			    	// Why commented? Push by user to removed auto search
		    		// getAvailableWatchDevices();
		    		onCancelPairing();
		    		return;
	    		} 
	    		// User canceled pairing 
    			// Enable user to browse even without no watch paired
    			onCancelPairing();
	    		return;
	    	}
	    	
	    	// Newly open application with saved device
	    	m_isSavedConnection = true;
 	        mBTMsgHandler = new BTDataHandler(this);
 	        if (BluetoothAgent.getInstance(null) == null) { 
 	        	Log.d("Log", "Fresh Connection");
 	        	BluetoothAgent.initBluetoothAgent(this);
 				BluetoothAgent.getInstance(mBTMsgHandler).initialize();
 				onCallonCreate();
 				return;
 	        } 
 	        
 	        
 	        // Switched from another activity
 	        onCallonCreate();  
 	        if (AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.CONNECTED) { // Register message handler
 	        	BluetoothAgent.getInstance(mBTMsgHandler);
 	        }
 	        
 	        getActivityData();
	    }  
	   
	}
	
	private boolean isThereWatchAvailable() {
		Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
		for (BluetoothDevice i : bondedDevices) {
			if (i.getName().startsWith("Meteor") 
			|| i.getName().startsWith("Kreyos")) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) 
	{
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intent);
		
		if (requestCode == BLUETOOTH_REQUEST_CODE_ENABLE) {
			if (resultCode == 0) {
		    	// Cancel
				Log.d("TutorialActivity", "Cancel");
				enableBluetooth();
		    } else {
		    	// Ok 
		    	Log.d("TutorialActivity", "OK");
		    	onStartApp();
		    }
		}
		
		if ( requestCode == IMAGE_REQUEST_CODE 
		&& resultCode == RESULT_OK 
		&& null != intent) 
		{
			try
			{
				Uri selectedImage = intent.getData();
	            String[] filePathColumn = { MediaStore.Images.Media.DATA };
	            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
	            cursor.moveToFirst();
	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	            String picturePath = cursor.getString(columnIndex);
	            cursor.close();
		        
		        Bitmap bitmap = getScaledBitmap(picturePath, 800, 800);
		        if (bitmap == null) {
		        	return;
		        }
		        // m_avatarImage.setImageBitmap(bitmap);
		        m_avatarImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 800, 800, false));
		        
		        // bitmap -> byte array
		        byte[] byteArray = KreyosUtility.convertBitmapToByteArray(bitmap);	
		        String imageString 	= KreyosUtility.convertByteArrayToString(byteArray);
		        SharedPreferences.Editor editor = getPrefs().edit();
		        // editor.putString(KreyosPrefKeys.USER_IMAGE, imageString);
		        // Save picture path
		        editor.putString(KreyosPrefKeys.USER_IMAGE, picturePath);
		        editor.commit();
			}
			catch( Exception ex )
			{
				// Log.e("LOG_TAG", "Picture Error:" + ex);
				ex.printStackTrace();
			}
        }
		
		if(requestCode == CAMERA_REQUEST_CODE
		&& null != intent)
		{
			// Bitmap bitmap = (Bitmap) intent.getExtras().get("data");
			// m_avatarImage.setImageBitmap(bitmap);
			
			Uri selectedImage = intent.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
	        
	        Bitmap bitmap = getScaledBitmap(picturePath, 800, 800);
	        if (bitmap == null) {
	        	return;
	        }
	        //m_avatarImage.setImageBitmap(bitmap);
	        m_avatarImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 800, 800, false));
	        
	        
			byte[] byteArray = KreyosUtility.convertBitmapToByteArray(bitmap);	
	        String imageString 	= KreyosUtility.convertByteArrayToString(byteArray);
	        SharedPreferences.Editor editor = getPrefs().edit();
	        // editor.putString(KreyosPrefKeys.USER_IMAGE, imageString);
	        // Save picture path
	        editor.putString(KreyosPrefKeys.USER_IMAGE, picturePath);
	        editor.commit();
		}
	}
	
	private void init() {
		/*
		 * What init() does?
		 * - setup application preferences
		 * - create and initialize bluetoothAgent
		 * - setup and bind the kreyosService class
		 * - create a instance of message handler
		 */
		
	    SharedPreferences prefs = KreyosActivity.getPrefs();
		
	    BluetoothAgent.initBluetoothAgent(this);
		BluetoothAgent.getInstance(null).initialize();
		
		// These are all setup of kreyosService class
    	Intent intent = new Intent(this, KreyosService.class);
    	initCloudSync(prefs, intent);
        initNotifications(prefs, intent);
        initBluetoothAgent(prefs, intent);
        // Start and bind the service class
        bindService(intent,	mServiceConnection, Context.BIND_AUTO_CREATE);
        
        mBTMsgHandler = new BTDataHandler(this);
	}
	
	@Override
	protected void onBluetoothHeadsetConnected() {
		// TODO Auto-generated method stub
		super.onBluetoothHeadsetConnected();
		Log.d("HomeActivity", "Overrided onBluetoothHeadsetConnected.");

		if (!m_isSavedConnection ) {
			setupConnection();
		}
		
		if (mIsAlreadyGettingData) {
			return;
		}
		onStartGetActivityTimer();
		
	}
	
	private void setupConnection() {
		activeDeviceName = m_selectedDevice.getName();
	 	 //if (activeDeviceName               .length() > 0) {
		//connectDevice();

		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putString("bluetooth.device_name", activeDeviceName);
		editor.commit();
		
		BluetoothAgent agent = BluetoothAgent.getInstance(mBTMsgHandler);
		agent.bindDevice(activeDeviceName);
		agent.restartSession();
		// agent.startActiveSession();

		// Log devicename
		Log.d("Debug", "Device Name" + activeDeviceName);
		
		HomeActivity.getService().initBluetooth(activeDeviceName);
		//HomeActivity.getService().initBluetooth(preferences.getString("bluetooth.device_name", ""));
		Log.e("xzcvbdsfg", "::"+activeDeviceName);
	}
	
	@Override
	protected void onCancelPairing() {
		// TODO Auto-generated method stub
		super.onCancelPairing();
		getActivityData();
	}
	
	private void setupSlideMenu()
	{
	    /*
	     * Overload the init method, added boolean to check if left or right animations
	     */
	    slidemenu = (SlidingMenu) findViewById(R.id.slideMenu);
		slidemenu.init(this, R.menu.slide, this, 333, true); // left animation
		
		slidemenu_right = (SlidingMenu) findViewById(R.id.slideMenu_right);
		slidemenu_right.init(this, R.menu.right_slide, this, 333, false); //right animation

		// connect the fallback button in case there is no ActionBar
		ImageView b = (ImageView) findViewById(R.id.imageView_menu1);
		b.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				isLeftMenuSelected = true;
				slidemenu.show();
			}
		});
		
		
		ImageView imageView_menu2 = (ImageView) findViewById(R.id.imageView_menu2);
		imageView_menu2.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				isLeftMenuSelected = false;
				slidemenu_right.show();
			}
		});
	}
	
	private void setupHUDandCallbacks()
	{
		setupSlideMenu();
		
		/*
		m_layoutActivityData = (RelativeLayout)findViewById(R.id.layout_activity_data);
		m_layoutProgress = (RelativeLayout)findViewById(R.id.layout_loading_progress);
		m_layoutSetTarget = (RelativeLayout)findViewById(R.id.layout_set_target);
		
		m_listviewDataItems = (ListView) findViewById(R.id.listView1);
		m_txtTotalSteps =  (TextView) findViewById(R.id.txt_value_steps);//((TextView)((RelativeLayout)findViewById(R.id.layout_steps)).findViewById(R.id.txt_value));
		m_txtTotalDistance = (TextView) findViewById(R.id.txt_value_distance);//((TextView)((RelativeLayout)findViewById(R.id.layout_distance)).findViewById(R.id.txt_value));
		m_txtTotalCalories = (TextView) findViewById(R.id.txt_value_calories);//((TextView)((RelativeLayout)findViewById(R.id.layout_calories)).findViewById(R.id.txt_value));
		*/
		
		mCProgressBar = (CircularProgressbar)findViewById(R.id.circularProgressbar1);
		m_avatarImage = (CircularImageView) findViewById(R.id.image_avatar);
		m_avatarImage.setActivityCallback(this);
		
		mBtnDailyTarget = (Button)findViewById(R.id.home_activity_btn_set_daily_target);
		mBtnDailyTarget.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				moveToDailyTarget(arg0);
			}
		});

		// + ET 040714 : Setup panel for activity
		//setupActivitySlider();
		
		///*
		// set panel display
		// get data from sqlite
		// setupActivityPanel();
		
		// Instantiate a ViewPager and a PagerAdapter.
//        mPager = (ViewPager) findViewById(R.id.pager);
//        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
//        mPager.setAdapter(mPagerAdapter);
//        mPager.setOnPageChangeListener(new PageListener());
		//*/
		
		/*
		//--- PICK A NEW GOAL BUTTON
		Button btn_newGoal = (Button) findViewById(R.id.button_login);
		btn_newGoal.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				try 
				{
					Intent i = new Intent(HomeActivity.this, SetNewGoalActivity.class);
					startActivity(i);
					
					// +AS:03222014 Debug load of different layout
					//HomeActivity.targetContentView = randomLayout();
					//Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
					//intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					//	startActivity(intent);

				
				}
				catch( Exception e ) {
				
				}
				
			}
		});
		//---
		*/
	}
	
	private void testGet() {
		AppHelper.instance().getActivityStatsonWeb(this, ACTIVITY_METRIC.DAILY);
	}
	
	@Override
	public void onSlideMenuItemClick(int itemId) {
		AppHelper.instance().onSwitchActivity(isLeftMenuSelected, this, itemId);
	}
	
	/***************************************************************************
	 * HELPER ENUM
	 **/
	public enum EHomeActivity 
	{
		E_		( R.layout.activity_home ),		// Empty
		R_		( R.layout.activity_home_r ),	// Running
		W_		( R.layout.activity_home_w ),	// Walking
		B_		( R.layout.activity_home_b ),	// Biking
		RW_		( R.layout.activity_home_rw ),	// Running|Walking
		RB_		( R.layout.activity_home_rb ),	// Running|Biking
		WB_		( R.layout.activity_home_wb ),	// Walking|Biking
		RWB_	( R.layout.activity_home_rwb );	// Running|Walking|Biking
	   
		private int m_homeActValue;
	   
		private EHomeActivity ( int p_activity ) 
		{
			m_homeActValue = p_activity;
		}
	   
		public int getActValue () 
		{
			return m_homeActValue;
		}
	}
	
	/***************************************************************************
	 * Database Queries
	 * 
	 * TODO:
	 * 		Apply the actual queries here! fetch the target activities of the user.
	 * 
	 * Temp:
	 * 		Currently, gives a random set activities
	 * 
	 **/
	public static EHomeActivity getUserActivities ()
	{
		// TODO: Apply queries here
		//	Get the following data
		//		- Activities
		//		- Goal string per activity
		//		- Description/Snippet per activity
		//		- Image per activity
		return HomeActivity.randomLayout();
	}
	
	/***************************************************************************
	 * DEBUG: Random Home Layout
	 **/
	private int[] m_layouts = new int[]
	{
		R.layout.activity_home,
		R.layout.activity_home_r,
		R.layout.activity_home_w,
		R.layout.activity_home_b,
		R.layout.activity_home_rw,
		R.layout.activity_home_rb,
		R.layout.activity_home_wb,
		R.layout.activity_home_rwb,
	};
	
	private static Random m_random = new Random();
	private static int m_min = 0;
	private static int m_max = 8;
	
	public static EHomeActivity randomLayout ()
	{
		return EHomeActivity.values()[( m_random.nextInt( m_max ) + m_min )];
	}
	
	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter
	{
        public ScreenSlidePagerAdapter(FragmentManager fm) 
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) 
        {
            return new ScreenSlidePageFragment();
        }

        @Override
        public int getCount() 
        {
            return NUM_PAGES;
        }
    }
	
	public class PageListener extends SimpleOnPageChangeListener
	{
        public void onPageSelected(int position)
        {
        	
        	setupActivityPanel();
        }
	}
	
	private void setupActivityPanel() 
	{

		//calorie = "5";
		//distance = "10";
		//steps = "3";
		
		TextView txt_calories 	= (TextView)findViewById(R.id.txt_calories);
		TextView txt_distance 	= (TextView)findViewById(R.id.txt_distance);
		TextView txt_steps 		= (TextView)findViewById(R.id.txt_steps);
		
		txt_calories.setText(for_calorie);
		txt_distance.setText(for_distance);
		txt_steps.setText(for_steps);
	}

	private void initCloudSync(SharedPreferences prefs, Intent intent) {
	    	intent.putExtra("cloud_id",             prefs.getString("cloud_id",             ""));
	    	intent.putExtra("access_token",         prefs.getString("access_token",         ""));
	    	intent.putExtra("access_expires",       prefs.getLong("access_expires",      0));
	    	intent.putExtra("twitter_access_token", prefs.getString("twitter_access_token", ""));
	    	intent.putExtra("twitter_secret_token", prefs.getString("twitter_secret_token", ""));
	}
		    
	private void initNotifications(SharedPreferences prefs, Intent intent) {
	    	intent.putExtra("notification.facebook",       prefs.getBoolean("notification.facebook",       false));
	    	intent.putExtra("notification.weather",        prefs.getBoolean("notification.weather",        false));
	    	intent.putExtra("notification.twitter",        prefs.getBoolean("notification.twitter",        false));
	    	intent.putExtra("notification.reminder",       prefs.getBoolean("notification.reminder",       false));
	    	intent.putExtra("notification.sms",            prefs.getBoolean("notification.sms",            false));
	    	intent.putExtra("notification.call",           prefs.getBoolean("notification.call",           false));
	    	intent.putExtra("notification.low_battery",    prefs.getBoolean("notification.low_battery",    false));
	    	intent.putExtra("notification.bt_outof_range", prefs.getBoolean("notification.bt_outof_range", false));
	}
	    
	private void initBluetoothAgent(SharedPreferences prefs, Intent intent) {
	    	intent.putExtra("bluetooth.device_name", prefs.getString("bluetooth.device_name", ""));
	}
	
	//connect to kreyos watch
	private void connectDevice() {

		BluetoothAgent agent = BluetoothAgent.getInstance(mBTMsgHandler);
		agent.bindDevice(activeDeviceName);
		agent.restartSession();
		// agent.startActiveSession();

		HomeActivity.getService().initBluetooth(activeDeviceName);
		//HomeActivity.getService().initBluetooth(preferences.getString("bluetooth.device_name", ""));
		Log.e("xzcvbdsfg", "::"+activeDeviceName);
		
		Protocol p = new Protocol(agent, HomeActivity.this);
		p.getDeviceID();
	}
	
	private void refreshDeviceList() {
		ArrayList<BluetoothDevice> bluetoothDeviceSet = BluetoothAgent.getInstance(mBTMsgHandler).getPairedDevices();
		if (bluetoothDeviceSet == null)
		{
			return;
		}
		
		ArrayList<String> lisfOfWatch = new ArrayList<String>();
		for ( BluetoothDevice btDevice : bluetoothDeviceSet) 
		{
			
			//name.setText(btDevice.getName());
			//status.setText(getDeviceBondStateResId(btDevice.getBondState()));
			Log.d("Device Name", btDevice.getName());
			Log.i("bond state", Integer.toString(getDeviceBondStateResId(btDevice.getBondState())));
			lisfOfWatch.add(btDevice.getName());
		}
		
		final CharSequence[] watchArray = lisfOfWatch.toArray(new CharSequence[lisfOfWatch.size()]);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Connect your Kreyos Watch");
        builder.setItems(watchArray, new DialogInterface.OnClickListener() {

           public void onClick(DialogInterface dialog, int item) {
                //Toast.makeText(getApplicationContext(), watchArray[item], Toast.LENGTH_SHORT).show();
        	   activeDeviceName = (String) watchArray[item];
				//if (activeDeviceName.length() > 0) {
					//connectDevice();

					SharedPreferences.Editor editor = getPrefs().edit();
					editor.putString("bluetooth.device_name", activeDeviceName);
					editor.commit();
					
					BluetoothAgent agent = BluetoothAgent.getInstance(mBTMsgHandler);
					agent.bindDevice(activeDeviceName);
					agent.restartSession();
					// agent.startActiveSession();

					HomeActivity.getService().initBluetooth(activeDeviceName);
					//HomeActivity.getService().initBluetooth(preferences.getString("bluetooth.device_name", ""));
					Log.e("xzcvbdsfg", "::"+activeDeviceName);
					
					Protocol p = new Protocol(agent, HomeActivity.this);
					p.getDeviceID();
					
					p.unlockWatch();
					
					// KreyosApp app = (KreyosApp)getApplicationContext();
					// app.getMainActivity().setActivePage("DeviceInfo",
					// DeviceInfoActivity.class);
				//}
           }

        });

        AlertDialog alert = builder.create();
        alert.show();  
        
	   // }

		//for (BluetoothDevice btDevice : bluetoothDeviceSet) {

			
			//layout.setTag(btDevice.getName());
			

			//TextView name = (TextView) layout.findViewById(R.id.deviceName);
			//TextView status = (TextView) layout.findViewById(R.id.deviceStatus);
			//name.setText(btDevice.getName());
			//status.setText(getDeviceBondStateResId(btDevice.getBondState()));
		
		//}
	}
	
	private static final int getDeviceBondStateResId(final int state) {
		if (state == BluetoothDevice.BOND_BONDED)
		{
			return R.string.paired;
		} else {
			return R.string.unknown;
		}
	}
		
	private void handleBDData(Message msg) {
		Log.d("Log", "Watch message:" + msg.what);
		switch (msg.what) 
		{
		
			case Protocol.MessageID.MSG_DEVICE_ID_GOT: {
				String device_id = new String((byte[]) (msg.obj));
				SharedPreferences.Editor editor = KreyosActivity.getPrefs().edit();
				editor.putString("device_id", device_id);
				editor.commit();
			}
			break;
				
				
			case Protocol.MessageID.MSG_FIRMWARE_VERSION: {
					String version = (String) msg.obj;
					AppHelper.instance().saveFirmwareVersion(this, version);
			}
			break;
			
				
			case Protocol.MessageID.MSG_BLUETOOTH_STATUS: {
				Log.d("HomeActivity", "BluetoothStatus : " + msg.obj );
				
				if (msg.obj.toString() == "Connecting") {
					//m_progressDialog = ProgressDialog.show(this, "Please wait",	"Connecting Watch to App", true);
				}
				
				if (msg.obj.toString() == "Running") {
					//m_progressDialog.dismiss();
					
					connecBluetoothHeadset();
					unlockWatch();
					
				} else {
					
					AppHelper.instance().WATCH_STATE = AppHelper.WATCH_STATE_VALUE.DISCONNECTED;
					setHeaderByConnection();
					
				}
				
			}
			break;
			
				
			case Protocol.MessageID.MSG_FILE_RECEIVED: {
				ActivityDataDoc dataDoc = (ActivityDataDoc) msg.obj;
				displayActivityData(dataDoc);
			}
			break;
				
				
			// + ET 04242014 : Move to Sports Mode	
			case Protocol.MessageID.MSG_ACTIVITY_PREPARE: {
				
				Log.d("HomeActivity", "Preparing to move on SportsActivity");
				Intent i3 = new Intent(HomeActivity.this, SportsActivity.class);
				startActivity(i3);
				finish();
				
			}
			break;
			
			case Protocol.MessageID.MSG_TODAY_ACTIVITY: {
				
				TodayActivity act = (TodayActivity)msg.obj;
				displayTodayActivityData(act);
			
			}
			break;
		
		}
	}
	
	private void unlockWatch() {
		/*
		boolean isWatchUnlock = getPrefs().getBoolean(KreyosPrefKeys.USER_WATCHED_UNLOCK, false);
		
		Log.d("Log", "watch unlock: " + isWatchUnlock);
		if( isWatchUnlock )
		{
			Log.d("Log", "Get Activity Data");
			getActivityData();
			return;
		}
		
		SharedPreferences.Editor editor = getPrefs().edit();
   		editor.putBoolean(KreyosPrefKeys.USER_WATCHED_UNLOCK, true);
   		editor.commit();
   		
		boolean isWatchUnlock = getPrefs().getBoolean(KreyosPrefKeys.USER_WATCHED_UNLOCK, false);
		if(!isWatchUnlock) {
			Protocol p = new Protocol(BluetoothAgent.getInstance(mBTMsgHandler), HomeActivity.this);
			p.unlockWatch();
		}
		*/
		unlockKreyosWatch();
		
		// Get activity stats
		getActivityData();
	}
	
	private void getActivityData() {

		/*
		if(!getPrefs().contains(KreyosPrefKeys.USER_FIRST_VIEW)) {
			return;
		}
		*/
		/*
		if (AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.CONNECTED) {
			Protocol p = new Protocol(BluetoothAgent.getInstance(mBTMsgHandler), HomeActivity.this);
	    	p.getActivityData();
	    	Log.d("HomeActivity", "Getting ActivityData" );
		} else {
			// Get data from local db
			ActivityData stats = AppHelper.instance().getActivityOnLocal(this);
			if (stats != null 
			&& !stats.m_steps.equalsIgnoreCase("0")
			&& !stats.m_distance.equalsIgnoreCase("0")
			&& !stats.m_calories.equalsIgnoreCase("0")) {
				setStatsonAdapter(stats);
			}
		}
		
		*/
		
		// ActivityData stats = AppHelper.instance().getActivityOnLocal(this);
		ActivityData stats = new ActivityData(null, "0.0", "0.0", "0.0");
		setStatsonAdapter(stats);
	}
	
	private void displayActivityData(final ActivityDataDoc p_data) {
		
		// Set activity to be run on different thread
		m_activity = this;
		
		// Show progress dialog that let's the user know
		// m_progressDialog = ProgressDialog.show(this, "Please wait",	"Syncing Activity Data", true);
		
		// Used different thread to avoid freezing when loading data
		AsyncTask<Void, Void, Void> async = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub
				
				// Get data to be displayed
				/*
				final ActivityData stats =  AppHelper.instance().displayActivityData(p_data, m_activity);
				
				runOnUiThread(new Runnable() {
	
					@Override
					public void run() {
						// TODO Auto-generated method stub
						// Display data on the adapter
						try {
							setStatsonAdapter(stats);
						} catch (Exception ex) {
							
						}
					}
				});
				*/
				return null;
			}
		};
		
		// Run the thread
		// async.execute();
		Log.d("ActivityDataTest", "Getting activity with watch new data");
		ActivityData stats =  AppHelper.instance().displayActivityData(p_data, m_activity);
		// setStatsonAdapter(stats);
		
		String[] syncValue = computeWatchSyncData(p_data);
		ActivityData syncData = new ActivityData(null, syncValue[0], syncValue[1], syncValue[2]);
		setStatsonAdapter(syncData);
	}
	
	private void displayTodayActivityData(TodayActivity p_act) {
		Log.d("TodayActivity", "Steps:" + p_act.steps);
		Log.d("TodayActivity", "Distance:" + p_act.distance);
		Log.d("TodayActivity", "Calories:" + p_act.calories);
		
		String steps = KreyosUtility.setDataForDisplay((double)p_act.steps);
		String distance = KreyosUtility.setDataForDisplay((double)(p_act.distance/1000)); 
		String calories = KreyosUtility.setDataForDisplay((double)(p_act.calories /1000));
		ActivityData stats = new ActivityData(null, steps, distance, calories);
		setStatsonAdapter(stats);
	} 
	
	private String[] computeWatchSyncData(ActivityDataDoc p_data) {
			
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

		    //Log.v("periodMode", String.format("periodMode is %f", periodMode));
		    Log.v("periodStartHour", Integer.toString(periodStartHour));
			Log.v("periodMode", Integer.toString(periodMode));
			Log.v("periodSteps", Double.toString(periodSteps));
			Log.v("periodDistance", Double.toString(periodDistance));
			//Log.v("periodSteps", String.format("periodSteps is %f", periodSteps));
			//Log.v("periodDistance", String.format("periodDistance is %f", periodDistance));
        } 
		
		String[] returnValues = new String[] {
				KreyosUtility.setDataForDisplay((double)periodSteps), 
				KreyosUtility.setDataForDisplay((double)(periodDistance/1000)), 
				KreyosUtility.setDataForDisplay((double)(periodCalories /1000))
		};
		
		return returnValues;
		
	}

	// Send data on the adapter
	private void setStatsonAdapter(ActivityData p_stats) {
		
		if (p_stats == null) {
			return;
		}
		/*
		// Hide and unhide layouts
		m_layoutSetTarget.setVisibility(View.INVISIBLE);
		m_layoutActivityData.setVisibility(View.VISIBLE);

		// Set values
		m_listviewDataItems.setAdapter(p_stats.m_adapter);		
		m_txtTotalSteps.setText(p_stats.m_steps);
		m_txtTotalDistance.setText(p_stats.m_distance);
		m_txtTotalCalories.setText(p_stats.m_calories);
		*/
		
		// mBtnDailyTarget.setVisibility(View.GONE);
	
		SharedPreferences prefs = getPrefs();
		float maxSteps = prefs.getInt(KreyosPrefKeys.SPORTS_GOAL_STEPS, 1000);
		float steps = Float.parseFloat(p_stats.m_steps);
		float distance = Float.parseFloat(p_stats.m_distance);
		float calories = Float.parseFloat(p_stats.m_calories);
		
		mCProgressBar.animateProgress(maxSteps, steps, distance, calories);

		if (steps > 0) {
//			mBtnDailyTarget.setText("PICK A DAILY TARGET");
			mIsGoingToDialyTarget = false;
			
			mBtnDailyTarget.setText(R.string.home_btn_overall_activities);
		}
		else {
			mBtnDailyTarget.setText(R.string.home_btn_pick);
		}
		
		// Hide progress dialog
		// m_progressDialog.dismiss();
		
	}

	private static class BTDataHandler extends Handler {
		
		private final WeakReference<HomeActivity> mActivity;

		public BTDataHandler(HomeActivity activity) {
			mActivity = new WeakReference<HomeActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			HomeActivity activity = mActivity.get();
			if (activity != null) {
				activity.handleBDData(msg);
			}
		}
	}
	
	public void moveToDailyTarget( View p_view ) {
		
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putBoolean(KreyosPrefKeys.USER_FIRST_VIEW, true);
		editor.commit();
		Class targetClass = mIsGoingToDialyTarget ? DailyTargetActivity.class : ActivityStatsActivity.class;
		Intent intent = new Intent(HomeActivity.this, targetClass);
		startActivity(intent);
		finish();
//		AppHelper.instance().getActivityStatsonWeb(this);
	}

	// + ET : 04302014 : New Methods
	AlertDialog m_photoDialog = null;
	
	public void showPhotoDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Profile Image");
    	builder.setMessage("Select from:");
    	builder.setPositiveButton("Capture Image", new DialogInterface.OnClickListener() 
    	{
            public void onClick(DialogInterface dialog, int id) 
            {
				Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, CAMERA_REQUEST_CODE);
				m_photoDialog.dismiss();
            }
        });
    	builder.setNegativeButton("Choose From Gallery", new DialogInterface.OnClickListener() 
    	{
            public void onClick(DialogInterface dialog, int id) 
            {
				Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, IMAGE_REQUEST_CODE);
				m_photoDialog.dismiss();
            }
        });
		// + ET 040714 : Create and show dialog
    	m_photoDialog = builder.create();
    	m_photoDialog.show();
	}

	private void onStartGetActivityTimer() {
		mGetDataTimer = new Timer();
		mGetDataTask = new TimerTask() {
			@Override 
			public void run() {
				// TODO Auto-generated method stub
				if (AppHelper.instance().WATCH_STATE != WATCH_STATE_VALUE.CONNECTED) {
					return;
				}
				
				Log.d("ActivityData", "Getting ActivityData");
				Protocol p = new Protocol(BluetoothAgent.getInstance(mBTMsgHandler), HomeActivity.this);
		    	p.sendDailyActivityRequest();
			}
		};
		mGetDataTimer.schedule(mGetDataTask, 0, mGetDataDelay);
		mIsAlreadyGettingData = true;
	}
	
	private void destroyableTask() {
		
		Log.d("GetActivityTask", "Closed Task");
		if (mGetDataTask != null) {
			Log.d("GetActivityTask", "Closed Task");
			mGetDataTask.cancel();
			mGetDataTask = null;
		}
		if (mGetDataTimer != null) {
			Log.d("GetActivityTask", "Closed Timer");
			mGetDataTimer.cancel();
			mGetDataTimer = null;
		}
		
		mCProgressBar.clearBitmaps();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		destroyableTask();
	}
}