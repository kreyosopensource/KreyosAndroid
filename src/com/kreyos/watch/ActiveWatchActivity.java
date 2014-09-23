package com.kreyos.watch;

import java.lang.ref.WeakReference;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.coboltforge.slidemenu.SlideMenuInterface.OnSlideMenuItemClickListener;
import com.kreyos.watch.R;
import com.kreyos.watch.bluetooth.BluetoothAgent;
import com.kreyos.watch.bluetooth.KreyosService;
import com.kreyos.watch.bluetooth.Protocol;
import com.kreyos.watch.managers.AppHelper;
import com.kreyos.watch.managers.AppHelper.WATCH_STATE_VALUE;

public class ActiveWatchActivity extends KreyosActivity implements OnSlideMenuItemClickListener
{
	
	private BTDataHandler btMsgHandler = null;

	public ActiveWatchActivity() 
	{
		btMsgHandler = new BTDataHandler(this);
		if( AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.CONNECTED ) {
 			BluetoothAgent.getInstance(btMsgHandler);
 		}
	}
	
	private static class BTDataHandler extends Handler 
	{
		private final WeakReference<ActiveWatchActivity> mService;

		public BTDataHandler(ActiveWatchActivity service)
		{
			mService = new WeakReference<ActiveWatchActivity>(service);
		}

		@Override
		public void handleMessage(Message msg)
		{
			ActiveWatchActivity service = mService.get();
			if (service != null) 
			{
				service.handleBTData(msg);
			}
		}
	}
	
	
	private void handleBTData(Message msg)
	{

		switch (msg.what) 
		{
			// + ET 04242014 : Move to Sports Mode	
			case Protocol.MessageID.MSG_ACTIVITY_PREPARE: {
				Intent i3 = new Intent(ActiveWatchActivity.this, SportsActivity.class);
				startActivity(i3);
				finish();
			}
			break;
			
			
			case Protocol.MessageID.MSG_BLUETOOTH_STATUS: {
				if ( msg.obj.toString() == "Running" ) {
					connecBluetoothHeadset();
				} else {
					AppHelper.instance().WATCH_STATE = AppHelper.WATCH_STATE_VALUE.DISCONNECTED;
					setHeaderByConnection();
				}
			}
			break;

			case Protocol.MessageID.MSG_FIRMWARE_VERSION: {
				String version = (String) msg.obj;
				AppHelper.instance().saveFirmwareVersion(this, version);
			}
			break;
		}
	}
	
	
	// + ET 040714 : Slide menu variables
	private SlidingMenu slidemenu;
	private SlidingMenu slidemenu_right;
	private boolean isLeftMenuSelected = false;
	

	private TextView m_txtWatchLabel = null;
	private Button m_btnPairUnPair	 = null;
	
	private boolean mIsSaveConnection = false;
	private boolean mIsNeedToUnlock = false;
	
	@Override
	public void onSlideMenuItemClick(int itemId) {
		// TODO Auto-generated method stub
		AppHelper.instance().onSwitchActivity(isLeftMenuSelected, this, itemId);
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView(R.layout.activity_active_device);
		
		
		KreyosActivity.initPrefs(this);
		// + ET 040714 : Override all fonts 
		KreyosUtility.overrideFonts(this, ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0), KreyosUtility.FONT_NAME.LEAGUE_GOTHIC_REGULAR);
		SharedPreferences prefs = KreyosActivity.getPrefs();
		
		if (prefs.contains("bluetooth.device_name")) { 
			mIsSaveConnection = true;
		} 
		
		onCallonCreate();
		setupSlideMenu();
		setupUITriggers();
		checkConnnectedWatch();
		
	} 
	
	
	private void setupSlideMenu() {
		
	    slidemenu = (SlidingMenu) findViewById(R.id.slideMenu);
		slidemenu.init(this, R.menu.slide, this, 333, true); // left animation
		
		slidemenu_right = (SlidingMenu) findViewById(R.id.slideMenu_right);
		slidemenu_right.init(this, R.menu.right_slide, this, 333, false); //right animation

		// connect the fallback button in case there is no ActionBar
		ImageView b = (ImageView) findViewById(R.id.imageView_menu1);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isLeftMenuSelected = true;
				slidemenu.show();
			}
		});
	
		ImageView imageView_menu2 = (ImageView) findViewById(R.id.imageView_menu2);
		imageView_menu2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isLeftMenuSelected = false;
				slidemenu_right.show();
			}
		});
	}
	
	
	private void setupUITriggers() {
		
		m_txtWatchLabel = (TextView) findViewById(R.id.txt_watch_connected);
		m_btnPairUnPair = (Button) findViewById(R.id.btn_search_again);
		m_btnPairUnPair.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View arg0) 
			{
				// TODO Auto-generated method stub
				arg0.setEnabled(false);
				if (AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.CONNECTED) {
					onUnpairWatch(arg0);
				} else {
					onSearchWatch(arg0);
				}
			}
		});
	}
	
	
	private void checkConnnectedWatch() {
		
		setHeaderByConnection();
		
		if (AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.CONNECTED) {
			
			String deviceName = getPrefs().getString("bluetooth.device_name", "");
			
			if(!deviceName.equals("")) {
				m_txtWatchLabel.setText("WATCH CONNECTED: " + deviceName);
			} else {
				// + ET 05072014 : Error occured during connection of watch
			}
			
			if (!mIsNeedToUnlock) {
			 	return;
			}
				
			// Log.d("Unlock", "Unlocking Watch");
			// unlockKreyosWatch();
			// mIsNeedToUnlock = false;
			
		} else {
			m_btnPairUnPair.setText("CONNECT");
			m_txtWatchLabel.setText("WATCH CONNECTED: NONE");
		}
	}
	
	
	private void onSearchWatch(View p_view) {
		
		p_view.setEnabled(true);
		init();
		getAvailableWatchDevices();
		
	}
	
	
	private void init() {
		
		mIsNeedToUnlock = true;
		
		SharedPreferences prefs = KreyosActivity.getPrefs();
	    
		BluetoothAgent.initBluetoothAgent(this);
		BluetoothAgent.getInstance(null).initialize();
    	
    	Intent intent = new Intent(this, KreyosService.class);
        initCloudSync(prefs, intent);
        initNotifications(prefs, intent);
        initBluetoothAgent(prefs, intent);
        bindService(intent,	mServiceConnection, Context.BIND_AUTO_CREATE);
        btMsgHandler = new BTDataHandler(this);
	}
	
	
	private void setupConnection() {
		
		String activeDeviceName = m_selectedDevice.getName();
		
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putString("bluetooth.device_name", activeDeviceName);
		editor.commit();
		
		BluetoothAgent agent = BluetoothAgent.getInstance(btMsgHandler);
		agent.bindDevice(activeDeviceName);
		agent.restartSession();
		
		getService().initBluetooth(activeDeviceName);
	}
	
	
	@Override
	protected void onBluetoothHeadsetConnected() {
		// TODO Auto-generated method stub
		super.onBluetoothHeadsetConnected();
		
		if (!mIsSaveConnection) {
			setupConnection();
			mIsSaveConnection = true;
			return;
		}
		
		m_btnPairUnPair.setText("DISCONNECT");
		checkConnnectedWatch();
		
		if (!mIsNeedToUnlock) {
			return;
		}
		
		Log.d("Unlock", "Unlocking Watch");
		unlockKreyosWatch();
		
		// Sync watch config
		writeWatchUIConfig();
		
		mIsNeedToUnlock = false;
	}
	

	private void onUnpairWatch(View p_view) {
		
		AppHelper.instance().disconnectWatch(this);
		m_progressDialog = ProgressDialog.show(this, "Please wait",	"Disconnecting on Watch", true);
		
		// Delay on disconnection
		Handler delay = new Handler();
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mIsSaveConnection = false;
				m_btnPairUnPair.setEnabled(true);
				checkConnnectedWatch();
				if (m_progressDialog == null) {
					return;
				} 
				m_progressDialog.dismiss();
			}
		};
		delay.postDelayed(runnable, 2000); // 2 seconds
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

}
